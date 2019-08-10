/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.group;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.junit.Test;

/**
 * Test for {@link AbstractGroup}
 *
 * @since 16.1
 */
public class AbstractGroupTest {

  private static final String CUSTOM_DIMENSION = "my-custom-dimension";

  protected static class P_Group extends AbstractGroup {
  }

  @Test
  public void testIsVisible() {
    AbstractGroup group = new P_Group();
    assertVisible(group, true, true, true, true);

    group.setVisible(false);
    assertVisible(group, false, true, false, true);

    group.setVisibleGranted(false);
    assertVisible(group, false, false, false, true);

    group.setVisible(true);
    assertVisible(group, false, false, true, true);

    group.setVisibleGranted(true);
    assertVisible(group, true, true, true, true);

    group.setVisible(false, CUSTOM_DIMENSION);
    assertVisible(group, false, true, true, false);

    group.setVisibleGranted(false);
    assertVisible(group, false, false, true, false);

    group.setVisible(true, CUSTOM_DIMENSION);
    assertVisible(group, false, false, true, true);

    group.setVisibleGranted(true);
    assertVisible(group, true, true, true, true);
  }

  protected void assertVisible(AbstractGroup group, boolean global, boolean granted, boolean visible, boolean custom) {
    assertEquals(global, group.isVisible());
    assertEquals(granted, group.isVisibleGranted());
    assertEquals(visible, group.isVisible(IDimensions.VISIBLE));
    assertEquals(custom, group.isVisible(CUSTOM_DIMENSION));
  }

}
