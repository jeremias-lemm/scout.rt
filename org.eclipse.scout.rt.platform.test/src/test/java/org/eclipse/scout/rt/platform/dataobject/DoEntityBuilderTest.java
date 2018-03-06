/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.dataobject.DataObjectTestHelper;
import org.junit.Test;

public class DoEntityBuilderTest {

  @Test
  public void testBuild() {
    DoEntity expected = BEANS.get(DoEntity.class);
    expected.put("attribute1", "foo");
    expected.put("attribute2", 42);
    expected.putList("listAttribute", CollectionUtility.arrayList(1, 2, 3));

    DoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("attribute1", "foo")
        .put("attribute2", 42)
        .putList("listAttribute", 1, 2, 3)
        .build();
    BEANS.get(DataObjectTestHelper.class).assertDoEntityEquals(expected, actual);

    // ensure lists are mutable
    expected.getList("listAttribute").remove(1);
    actual.getList("listAttribute").remove(1);
    BEANS.get(DataObjectTestHelper.class).assertDoEntityEquals(expected, actual);
  }
}
