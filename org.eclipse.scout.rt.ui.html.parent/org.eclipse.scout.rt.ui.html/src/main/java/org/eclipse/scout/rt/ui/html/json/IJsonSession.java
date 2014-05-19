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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.IClientSession;

public interface IJsonSession {

  void init(HttpServletRequest request, JsonRequest jsonReq) throws JsonException;

  IClientSession getClientSession();

  String createUniqueIdFor(IJsonRenderer<?> renderer);

  /**
   * Returns an IJsonRenderer instance for the given renderer ID.
   */
  IJsonRenderer<?> getJsonRenderer(String id);

  /**
   * Returns an IJsonRenderer instance for the given model object.
   */
  IJsonRenderer<?> getJsonRenderer(Object modelObject);

  /**
   * Creates a new IJsonRenderer instance for the given modelObject, or returns an existing instance.
   */
  IJsonRenderer<?> getOrCreateJsonRenderer(Object modelObject);

  /**
   * Creates a new IJsonRenderer instance for the given modelObject.
   */
  IJsonRenderer<?> createJsonRenderer(Object modelObject);

  void unregisterJsonRenderer(String id);

  /**
   * @return the current ui response that is collecting changes for the next
   *         {@link #processRequest(HttpServletRequest, JsonRequest)} cycle
   */
  JsonResponse currentJsonResponse();

  HttpServletRequest currentHttpRequest();

  JsonResponse processRequest(HttpServletRequest httpReq, JsonRequest jsonReq) throws JsonException;

}
