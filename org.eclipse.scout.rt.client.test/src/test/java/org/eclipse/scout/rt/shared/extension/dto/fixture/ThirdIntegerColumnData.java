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

import java.io.Serializable;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTableData.OrigPageWithTableRowData;

/**
 * <b>NOTE:</b><br>This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Extends(OrigPageWithTableRowData.class)
@Generated(value = "org.eclipse.scout.rt.shared.extension.dto.fixture.ThirdIntegerColumn", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ThirdIntegerColumnData implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String thirdInteger = "thirdInteger";
  private Integer m_thirdInteger;

  public Integer getThirdInteger() {
    return m_thirdInteger;
  }

  public void setThirdInteger(Integer newThirdInteger) {
    m_thirdInteger = newThirdInteger;
  }
}
