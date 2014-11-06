/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.IUiLayer;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UiLayer2;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.json.JSONObject;

public abstract class AbstractJsonSession implements IJsonSession, HttpSessionBindingListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  private JsonClientSession m_jsonClientSession;

  // TODO AWE: JsonAdapterFactory überschreibbar machen, via Scout-service
  // FIXME BSH Allgemein Thema Erweiterbarkeit: es fehlen protected Getter/Setter fuer private Felder, finale Objekte in Konstruktor koennen nicht customized werden
  private final JsonAdapterFactory m_jsonAdapterFactory;
  private final JsonAdapterRegistry m_jsonAdapterRegistry;

  /**
   * Contains IDs to adapters which must be removed <i>after</i> a request has been processed.
   * This concept was introduced, because an adapter can be created, attached and disposed in
   * a single request. When we'd remove the adapters immediately we sometimes have the situation
   * where an event in the response, references an adapter that has already been disposed. With
   * this solution this situation is avoided.
   */
  private final Set<String> m_unregisterAdapterSet;

  private String m_jsonSessionId;
  private long m_jsonAdapterSeq;
  private JsonResponse m_currentJsonResponse;
  private JsonRequest m_currentJsonRequest;
  private HttpServletRequest m_currentHttpRequest;
  private JsonEventProcessor m_jsonEventProcessor;

  public AbstractJsonSession() {
    m_currentJsonResponse = new JsonResponse();
    m_jsonAdapterFactory = createJsonAdapterFactory();
    m_jsonAdapterRegistry = new JsonAdapterRegistry();
    m_unregisterAdapterSet = new HashSet<String>();
  }

  protected JsonAdapterFactory createJsonAdapterFactory() {
    return new JsonAdapterFactory();
  }

  @Override
  public void init(HttpServletRequest request, JsonRequest jsonReq) {
    m_currentHttpRequest = request;
    m_currentJsonRequest = jsonReq;
    m_jsonSessionId = jsonReq.getJsonSessionId();
    UserAgent userAgent = createUserAgent();
    Subject subject = initSubject();
    if (subject == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }

    IClientSession clientSession;
    synchronized (this) {
      HttpSession httpSession = m_currentHttpRequest.getSession();
      // Lookup the requested client session
      String clientSessionId = m_currentJsonRequest.getClientSessionId();
      if (clientSessionId == null) {
        throw new IllegalStateException("Missing clientSessionId in JSON request");
      }
      String clientSessionAttributeName = "scout.htmlui.session.client." + clientSessionId;
      clientSession = (IClientSession) httpSession.getAttribute(clientSessionAttributeName);
      if (clientSession != null) {
        // Found existing client session
        LOG.info("Using cached client session [clientSessionId=" + clientSessionId + "]");
      }
      else {
        // No client session for the requested ID was found, so create one and store it in the map
        LOG.info("Creating new client session [clientSessionId=" + clientSessionId + "]");
        //FIXME CGU session must be started later, see JsonClientSession
        //return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(clientSessionClass(), subject, UUID.randomUUID().toString(), userAgent);
        try {
          LocaleThreadLocal.set(request.getLocale());
          //
          clientSession = createUninitializedClientSession();
          clientSession.setUserAgent(userAgent);
          clientSession.setSubject(subject);
          clientSession.setVirtualSessionId(UUID.randomUUID().toString());
        }
        finally {
          LocaleThreadLocal.set(null);
        }
        httpSession.setAttribute(clientSessionAttributeName, clientSession);
        httpSession.setAttribute(clientSessionAttributeName + ".cleanup", new P_ClientSessionCleanupHandler(clientSessionId, clientSession));
      }
    }
    m_jsonClientSession = (JsonClientSession) getOrCreateJsonAdapter(clientSession);
    m_jsonEventProcessor = new JsonEventProcessor(m_jsonClientSession);
    startUpClientSession(clientSession);

    JSONObject jsonEvent = new JSONObject();
    JsonObjectUtility.putProperty(jsonEvent, "clientSession", m_jsonClientSession.getId());
    m_currentJsonResponse.addActionEvent(m_jsonSessionId, "initialized", jsonEvent);
    LOG.info("JsonSession with ID " + m_jsonSessionId + " initialized");
  }

  /**
   * @return a new {@link IClientSession} that is not yet initialized, so
   *         {@link IClientSession#startSession(org.osgi.framework.Bundle)} was not yet called
   */
  protected abstract IClientSession createUninitializedClientSession();

  protected UserAgent createUserAgent() {
    IUiLayer uiLayer = UiLayer2.HTML;
    IUiDeviceType uiDeviceType = UiDeviceType.DESKTOP;
    String browserId = m_currentHttpRequest.getHeader("User-Agent");
    JSONObject userAgent = m_currentJsonRequest.getUserAgent();
    if (userAgent != null) {
      // FIXME CGU it would be great if UserAgent could be changed dynamically, to switch from mobile to tablet mode on the fly, should be done as event in JsonClientSession
      String uiDeviceTypeStr = userAgent.optString("deviceType", null);
      if (uiDeviceTypeStr != null) {
        uiDeviceType = UiDeviceType.createByIdentifier(uiDeviceTypeStr);
      }
      String uiLayerStr = userAgent.optString("uiLayer", null);
      if (uiLayerStr != null) {
        uiLayer = UiLayer.createByIdentifier(uiLayerStr);
      }
    }
    return UserAgent.create(uiLayer, uiDeviceType, browserId);
  }

  protected Subject initSubject() {
    return Subject.getSubject(AccessController.getContext());
  }

  protected void startUpClientSession(IClientSession clientSession) {
    ClientSyncJob job = new ClientSyncJob("AbstractJsonSession#startClientSession", clientSession) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        m_jsonClientSession.startUp();
      }
    };
    job.runNow(new NullProgressMonitor());
    try {
      job.throwOnError();
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
    if (!clientSession.isActive()) {
      throw new JsonException("ClientSession is not active, there must have been a problem with loading or starting");
    }
  }

  @Override
  public void dispose() {
    m_jsonAdapterRegistry.dispose();
    m_currentJsonResponse = null;
  }

  @Override
  public String getJsonSessionId() {
    return m_jsonSessionId;
  }

  @Override
  public IClientSession getClientSession() {
    return m_jsonClientSession.getModel();
  }

  @Override
  public String createUniqueIdFor(IJsonAdapter jsonAdapter) {
    //FIXME CGU create id based on scout object for automatic gui testing, use @classId? or CustomWidgetIdGenerator from scout.ui.rwt bundle?
    return "" + (++m_jsonAdapterSeq);
  }

  /**
   * Creates an adapter instance for the given model and calls the <code>attach()</code> method on the created instance.
   */
  protected IJsonAdapter<?> createJsonAdapter(Object model) {
    String id = createUniqueIdFor(null); // FIXME CGU
    IJsonAdapter<?> adapter = m_jsonAdapterFactory.createJsonAdapter(model, this, id);
    adapter.attach();
    return adapter;
  }

  @Override
  public IJsonAdapter<?> getJsonAdapter(String id) {
    return m_jsonAdapterRegistry.getJsonAdapter(id);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model) {
    return m_jsonAdapterRegistry.getJsonAdapter(model);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model) {
    A jsonAdapter = getJsonAdapter(model);
    if (jsonAdapter != null) {
      return jsonAdapter;
    }
    jsonAdapter = (A) createJsonAdapter(model);
    // because it's a new adapter we must add it to the response
    m_currentJsonResponse.addAdapter(jsonAdapter); // TODO AWE: (json) in registerJsonAdapter verschieben? analog unregisterJsonAdapter
    return jsonAdapter;
  }

  @Override
  public void registerJsonAdapter(IJsonAdapter<?> adapter) {
    m_jsonAdapterRegistry.addJsonAdapter(adapter);
  }

  @Override
  public void unregisterJsonAdapter(String id) {
    m_unregisterAdapterSet.add(id);
  }

  @Override
  public void flush() {
    LOG.debug("Flush. Remove these adapter IDs from registry: " + m_unregisterAdapterSet);
    for (String id : m_unregisterAdapterSet) {
      m_jsonAdapterRegistry.removeJsonAdapter(id);
    }
    m_unregisterAdapterSet.clear();
  }

  @Override
  public JsonResponse currentJsonResponse() {
    return m_currentJsonResponse;
  }

  @Override
  public HttpServletRequest currentHttpRequest() {
    return m_currentHttpRequest;
  }

  @Override
  public JSONObject processRequest(HttpServletRequest httpRequest, JsonRequest jsonRequest) {
    try {
      m_currentHttpRequest = httpRequest;
      m_currentJsonRequest = jsonRequest;
      // FIXME CGU should only be done after pressing reload, maybe on get request? first we need to fix reload bug, see FIXME in AbstractJsonServlet
      m_jsonClientSession.processRequestLocale(httpRequest.getLocale());
      JsonResponse jsonResponse = currentJsonResponse();
      m_jsonEventProcessor.processEvents(m_currentJsonRequest, jsonResponse);
      return jsonResponse.toJson();
    }
    finally {
      // FIXME CGU really finally? what if exception occurs and some events are already delegated to the model?
      // reset event map (aka jsonResponse) when response has been sent to client
      m_currentJsonResponse = new JsonResponse();
      flush();
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent event) {
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
    dispose();
    LOG.info("JSON session with ID " + m_jsonSessionId + " unbound from HTTP session.");
  }

  /**
   * An instance of this class should be added to the HTTP session for each
   * client session. If the HTTP session is invalidated, this listener is
   * called and can shutdown the client session model.
   */
  protected static class P_ClientSessionCleanupHandler implements HttpSessionBindingListener {

    private final String m_clientSessionId;
    private final IClientSession m_clientSession;

    public P_ClientSessionCleanupHandler(String clientSessionId, IClientSession clientSession) {
      m_clientSessionId = clientSessionId;
      m_clientSession = clientSession;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
      LOG.info("Shutting down client session with ID " + m_clientSessionId + " due to invalidation of HTTP session...");

      // Dispose model (if session was not already stopped earlier by itself)
      if (m_clientSession.isActive()) {
        ClientJob job = new ClientSyncJob("Disposing client session", m_clientSession) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            m_clientSession.getDesktop().getUIFacade().fireDesktopClosingFromUI(true);
          }
        };
        job.runNow(new NullProgressMonitor());
        try {
          job.throwOnError();
        }
        catch (ProcessingException e) {
          throw new JsonException(e);
        }
      }

      LOG.info("Client session with ID " + m_clientSessionId + " terminated.");
    }
  }
}
