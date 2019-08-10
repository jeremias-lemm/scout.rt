/**
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableField", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class OrigFormWithTableFieldData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public TableInOrigForm getTableInOrigForm() {
    return getFieldByClass(TableInOrigForm.class);
  }

  public static class TableInOrigForm extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    @Override
    public TableInOrigFormRowData addRow() {
      return (TableInOrigFormRowData) super.addRow();
    }

    @Override
    public TableInOrigFormRowData addRow(int rowState) {
      return (TableInOrigFormRowData) super.addRow(rowState);
    }

    @Override
    public TableInOrigFormRowData createRow() {
      return new TableInOrigFormRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableInOrigFormRowData.class;
    }

    @Override
    public TableInOrigFormRowData[] getRows() {
      return (TableInOrigFormRowData[]) super.getRows();
    }

    @Override
    public TableInOrigFormRowData rowAt(int index) {
      return (TableInOrigFormRowData) super.rowAt(index);
    }

    public void setRows(TableInOrigFormRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableInOrigFormRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;
      public static final String orig = "orig";
      private String m_orig;

      public String getOrig() {
        return m_orig;
      }

      public void setOrig(String newOrig) {
        m_orig = newOrig;
      }
    }
  }
}
