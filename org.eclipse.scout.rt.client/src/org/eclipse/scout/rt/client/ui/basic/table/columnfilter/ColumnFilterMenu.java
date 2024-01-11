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
package org.eclipse.scout.rt.client.ui.basic.table.columnfilter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class ColumnFilterMenu extends AbstractMenu {
  private final ITable m_table;

  public ColumnFilterMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("ColumnFilterMenu");
  }

  @Override
  protected boolean getConfiguredInheritAccessibility() {
    return false;
  }

  @Override
  protected Set<IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet(TableMenuType.Header);
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    m_table.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (ITable.PROP_CONTEXT_COLUMN.equals(evt.getPropertyName())) {
          updateVisibility();
        }
      }

    });
    updateVisibility();
  }

  private void updateVisibility() {
    setVisible(m_table != null && m_table.getColumnFilterManager() != null && m_table.getContextColumn() != null);
  }

  @Override
  protected void execAction() throws ProcessingException {
    if (m_table != null) {
      if (m_table.getColumnFilterManager() != null) {
        IColumn<?> col = m_table.getContextColumn();
        if (col != null) {
          m_table.getColumnFilterManager().showFilterForm(col, true);
        }
      }
    }
  }
}