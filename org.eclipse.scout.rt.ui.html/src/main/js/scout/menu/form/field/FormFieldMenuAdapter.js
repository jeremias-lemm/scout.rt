/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.FormFieldMenuAdapter = function() {
  scout.FormFieldMenuAdapter.parent.call(this);
};
scout.inherits(scout.FormFieldMenuAdapter, scout.MenuAdapter);

/**
 * @override
 */
scout.FormFieldMenuAdapter.prototype._postCreateWidget = function() {
  scout.FormFieldMenuAdapter.parent.prototype._postCreateWidget.call(this);
  // Use grid data from server as hints
  if (this.widget.field) {
    this.widget.field.gridDataHints = new scout.GridData(this.widget.field.gridData);
  }
};
