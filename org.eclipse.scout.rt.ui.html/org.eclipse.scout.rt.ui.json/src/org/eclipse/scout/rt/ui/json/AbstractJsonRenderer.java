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
package org.eclipse.scout.rt.ui.json;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonRenderer<T> implements IJsonRenderer<T> {
  private final IJsonSession m_jsonSession;
  private final T m_modelObject;
  private final String m_id;
  private boolean m_initialized;

  public AbstractJsonRenderer(T modelObject, IJsonSession jsonSession) {
    this(modelObject, jsonSession, null);
  }

  public AbstractJsonRenderer(T modelObject, IJsonSession jsonSession, String id) {
    if (modelObject == null) {
      throw new IllegalArgumentException("modelObject must not be null");
    }
    m_modelObject = modelObject;
    m_jsonSession = jsonSession;
    if (id == null) {
      id = jsonSession.createUniqueIdFor(this);
    }
    m_id = id;
  }

  @Override
  public String getId() {
    return m_id;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public T getModelObject() {
    return m_modelObject;
  }

  @Override
  public final void init() {
    getJsonSession().registerJsonRenderer(getId(), this);
    attachModel();
    m_initialized = true;
  }

  protected void attachModel() {
  }

  @Override
  public void dispose() {
    if (!m_initialized) {
      return;
    }
    detachModel();
    getJsonSession().unregisterJsonRenderer(getId());
  }

  protected void detachModel() {
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    putProperty(json, "objectType", getObjectType());
    putProperty(json, "id", getId());
    return json;
  }

  /**
   * Adds a property to the given JSON object and deals with exceptions.
   * 
   * @param json
   * @param key
   * @param value
   * @return
   */
  protected final JSONObject putProperty(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

}
