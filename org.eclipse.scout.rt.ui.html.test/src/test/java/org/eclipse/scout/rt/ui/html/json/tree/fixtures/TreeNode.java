/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;

public class TreeNode extends AbstractTreeNode {

  public TreeNode() {
    super();
  }

  public TreeNode(String text) {
    super(false);
    getCellForUpdate().setText(text);
    callInitializer();
  }
}
