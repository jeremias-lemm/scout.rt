/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui.action.menu;

import org.eclipse.scout.rt.client.extension.ui.action.menu.MenuChains.MenuOwnerValueChangedChain;
import org.eclipse.scout.rt.client.extension.ui.action.tree.IActionNodeExtension;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public interface IMenuExtension<OWNER extends AbstractMenu> extends IActionNodeExtension<IMenu, OWNER> {

  void execOwnerValueChanged(MenuOwnerValueChangedChain chain, Object newOwnerValue);
}
