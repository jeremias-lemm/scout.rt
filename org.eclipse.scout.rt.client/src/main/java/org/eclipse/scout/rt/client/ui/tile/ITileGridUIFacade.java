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
package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ui.MouseButton;

public interface ITileGridUIFacade<T extends ITile> {

  void setSelectedTilesFromUI(List<T> tiles);

  void handleTileClickFromUI(T tile, MouseButton mouseButton);

  void handleTileActionFromUI(T tile);

}
