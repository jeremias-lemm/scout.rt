/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.0
 */
@ApplicationScoped
public class ServletFilterHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ServletFilterHelper.class);

  public static final String SESSION_ATTRIBUTE_FOR_PRINCIPAL = ServletFilterHelper.class.getName() + ".PRINCIPAL";
  public static final String HTTP_HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
  public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
  public static final String HTTP_HEADER_AUTHORIZED = "Authorized";
  public static final String HTTP_BASIC_AUTH_NAME = "Basic";
  public static final Charset HTTP_BASIC_AUTH_CHARSET = StandardCharsets.ISO_8859_1;

  // !!! IMPORTANT: This JSON message has to correspond to the response format as generated by JsonResponse.toJson()
  public static final String JSON_SESSION_TIMEOUT_RESPONSE = "{\"error\":{\"code\":10,\"message\":\"The session has expired, please reload the page.\"}}";

  /**
   * get a cached principal from the {@link HttpSession} as {@link #SESSION_ATTRIBUTE_FOR_PRINCIPAL}
   */
  public Principal getPrincipalOnSession(HttpServletRequest req) {
    final HttpSession session = req.getSession(false);
    if (session != null) {
      Principal principal = (Principal) session.getAttribute(SESSION_ATTRIBUTE_FOR_PRINCIPAL);
      if (principal != null) {
        return principal;
      }
    }
    return null;
  }

  /**
   * put a principal to the {@link HttpSession} as {@link #SESSION_ATTRIBUTE_FOR_PRINCIPAL}
   *
   * @param req
   *          The request holding the {@link HttpSession} on which the principal should be stored.
   * @param principal
   *          The principal to put on the session of the given request.
   */
  public void putPrincipalOnSession(HttpServletRequest req, Principal principal) {
    HttpSession session = req.getSession();
    session.setAttribute(SESSION_ATTRIBUTE_FOR_PRINCIPAL, principal);
  }

  /**
   * Returns <code>true</code> if running as a {@link Subject} with a principal corresponding to the authenticated
   * remote user.
   *
   * @see HttpServletRequest#getRemoteUser()
   */
  public boolean isRunningWithValidSubject(HttpServletRequest req) {
    String username = req.getRemoteUser();
    if (username == null || username.isEmpty()) {
      return false;
    }

    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.getPrincipals().isEmpty()) {
      return false;
    }

    for (Principal principal : subject.getPrincipals()) {
      if (username.equalsIgnoreCase(principal.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tries to find the authenticated principal on {@link HttpSession} or {@link HttpServletRequest}, or if not found,
   * and there is a remote user set on {@link HttpServletRequest}, a {@link Principal} is created for that remote user.
   *
   * @param servletRequest
   *          the current request.
   * @param principalProducer
   *          used to create a principal objects.
   * @return authenticated principal, or <code>null</code> if not found.
   */
  public Principal findPrincipal(HttpServletRequest servletRequest, IPrincipalProducer principalProducer) {
    // on session cache
    Principal principal = getPrincipalOnSession(servletRequest);
    if (principal != null) {
      return principal;
    }

    // on request as principal
    principal = servletRequest.getUserPrincipal();
    if (principal != null && StringUtility.hasText(principal.getName())) {
      return principal;
    }

    // on request as remoteUser
    String name = servletRequest.getRemoteUser();
    if (StringUtility.hasText(name)) {
      return principalProducer.produce(name);
    }

    return null;
  }

  /**
   * Adds the given {@link Principal} to the current calling {@link Subject}, or creates a new {@link Subject} if not
   * running as a {@link Subject} yet, or the {@link Subject} is read-only.
   *
   * @return subject with the given principal added.
   */
  public Subject createSubject(Principal principal) {
    // create subject if necessary
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.isReadOnly()) {
      subject = new Subject();
    }
    subject.getPrincipals().add(principal);
    subject.setReadOnly();
    return subject;
  }

  public void continueChainAsSubject(final Principal principal, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
    try {
      Subject.doAs(
          createSubject(principal),
          (PrivilegedExceptionAction<Object>) () -> {
            HttpServletRequest secureReq = new SecureHttpServletRequestWrapper(req, principal);
            chain.doFilter(secureReq, res);
            return null;
          });
    }
    catch (PrivilegedActionException e) { // NOSONAR
      Throwable t = e.getCause();
      if (t instanceof IOException) {
        throw (IOException) t;
      }
      else if (t instanceof ServletException) {
        throw (ServletException) t;
      }
      else {
        throw new ServletException(t);
      }
    }
  }

  public String createBasicAuthRequest(String username, char[] password) {
    StringBuilder cred = new StringBuilder(username).append(':').append(password);
    String encodedCred;
    encodedCred = Base64Utility.encode(cred.toString().getBytes(HTTP_BASIC_AUTH_CHARSET));
    return new StringBuilder(HTTP_BASIC_AUTH_NAME).append(' ').append(encodedCred).toString();
  }

  public String[] parseBasicAuthRequest(HttpServletRequest req) {
    String h = req.getHeader(HTTP_HEADER_AUTHORIZATION);
    if (h == null || !h.matches(HTTP_BASIC_AUTH_NAME + " .*")) {
      return null;
    }
    return new String(Base64Utility.decode(h.substring(6)), HTTP_BASIC_AUTH_CHARSET).split(":", 2);
  }

  /**
   * forward the request to the login.html
   * <p>
   * Detects if the request is a POST. For json send a timeout message, otherwise log a warning
   */
  public void forwardToLoginForm(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    forwardTo(req, resp, "/login.html");
  }

  /**
   * Forwards the request to the logout.html
   * <p>
   * Detects if the request is a POST. For json send a timeout message, otherwise log a warning
   */
  public void forwardToLogoutForm(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    forwardTo(req, resp, "/logout.html");
  }

  public void forwardTo(HttpServletRequest req, HttpServletResponse resp, String targetLocation) throws IOException, ServletException {
    forwardOrRedirectTo(req, resp, targetLocation, false);
  }

  public void redirectTo(HttpServletRequest req, HttpServletResponse resp, String targetLocation) throws IOException, ServletException {
    forwardOrRedirectTo(req, resp, targetLocation, true);
  }

  /**
   * Forwards or redirects the request to the specified location, depending on the value of the argument 'redirect':
   * <ul>
   * <li><b>redirect=true</b>: A HTTP redirect response (302) is sent, using
   * {@link HttpServletResponse#sendRedirect(String)}.
   * <li><b>redirect=false</b>: The request is forwarded to a dispatcher using the new location, using
   * {@link RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)). This has the same
   * effect as if the user had requested the target location from the beginning.
   * </ul>
   * If the client expects JSON as response (accept header contains 'application/json'), no redirection happens, but a
   * JSON timeout message is sent. Also for POST requests no forwarding/redirection will happen but error code 403
   * (forbidden) returned.
   */
  protected void forwardOrRedirectTo(HttpServletRequest req, HttpServletResponse resp, String targetLocation, boolean redirect) throws IOException, ServletException {
    String acceptedMimeTypes = req.getHeader("Accept");
    if (StringUtility.containsString(acceptedMimeTypes, "application/json")) {
      // Since the client expects JSON as response don't forward to the login page, instead send a json based timeout error
      LOG.debug("Returning session timeout error as json for path {}, based on Accept header {}.", req.getPathInfo(), acceptedMimeTypes);
      sendJsonSessionTimeout(resp);
      return;
    }
    if ("POST".equals(req.getMethod())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The request for '{}' is a POST request. " + (redirect ? "Redirecting" : "Forwarding") + " to '{}' will most likely fail. Sending HTTP status '403 Forbidden' instead.", req.getPathInfo(), targetLocation);
      }
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug((redirect ? "Redirecting" : "Forwarding") + " '{}' to '{}'", req.getPathInfo(), targetLocation);
    }
    if (redirect) {
      resp.sendRedirect(targetLocation);
    }
    else {
      req.getRequestDispatcher(targetLocation).forward(req, resp);
    }
  }

  protected void sendJsonSessionTimeout(HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.getWriter().print(JSON_SESSION_TIMEOUT_RESPONSE); // JsonResponse.ERR_SESSION_TIMEOUT
  }

  /**
   * If the request has a HTTP session attached, the session is invalidated.
   */
  public void doLogout(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    if (session != null) {
      LOG.info("Invalidating HTTP session with ID {}", session.getId());
      session.invalidate();
    }
  }
}
