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

import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <b>NOTE:</b><br>This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Extends(TreeBoxToTemplateFieldData.class)
@Generated(value = "org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxPropertyExtension", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class TreeBoxPropertyExtensionData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  public LongValueProperty getLongValueProperty() {
    return getPropertyByClass(LongValueProperty.class);
  }

  /**
   * access method for property LongValue.
   */
  public Long getLongValue() {
    return getLongValueProperty().getValue();
  }

  /**
   * access method for property LongValue.
   */
  public void setLongValue(Long longValue) {
    getLongValueProperty().setValue(longValue);
  }

  public static class LongValueProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;
  }
}
