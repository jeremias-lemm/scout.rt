/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.Test;

public class StrictSimpleDateFormatTest {

  /**
   * Asserts the JavaDoc examples of {@link StrictSimpleDateFormat}
   */
  @Test
  public void testJavaDoc() throws ParseException {
    StringHolder pattern = new StringHolder();
    StringHolder input = new StringHolder();

    pattern.setValue("yyyy-MM-dd hh:mm.ss.SSS");
    input.setValue("2019-01-18");
    ScoutAssert.assertThrows(ParseException.class, () -> new SimpleDateFormat(pattern.getValue()).parse(input.getValue()));
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input.getValue()));

    pattern.setValue("yyyy-MM-dd");
    input.setValue("2019-18");
    ScoutAssert.assertThrows(ParseException.class, () -> new SimpleDateFormat(pattern.getValue()).parse(input.getValue()));
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input.getValue()));

    pattern.setValue("yyyy-MM-dd");
    input.setValue("2019-1-18");
    new SimpleDateFormat(pattern.getValue()).parse(input.getValue());
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input.getValue()));

    pattern.setValue("yyyyMMdd");
    input.setValue("20190118xyz");
    new SimpleDateFormat(pattern.getValue()).parse(input.getValue());
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input.getValue()));

    pattern.setValue("yyyy-MM-dd");
    input.setValue("2019-01-18 23:00:00.000");
    new SimpleDateFormat(pattern.getValue()).parse(input.getValue());
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input.getValue()));

    pattern.setValue("yyyy/yyyy");
    input.setValue("2018/2019");
    new SimpleDateFormat(pattern.getValue()).parse(input.getValue());
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input.getValue()));
  }

  /**
   * Asserts that strings generated by JavaScript when serializing <i>Date</i> objects to JSON are rejected by
   * {@link StrictSimpleDateFormat}.
   *
   * @see <a href=
   *      "https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Date/toJSON">Date.toJSON()</a>
   */
  @Test
  public void testJavaScriptJsonString() throws ParseException {
    final StringHolder pattern = new StringHolder();
    final String input = "2019-01-18T12:42:03.409Z";

    pattern.setValue("yyyy-MM-dd HH:mm:ss.SSS");
    ScoutAssert.assertThrows(ParseException.class, () -> new SimpleDateFormat(pattern.getValue()).parse(input));
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input));

    pattern.setValue("yyyy-MM-dd HH:mm:ss.SSS Z");
    ScoutAssert.assertThrows(ParseException.class, () -> new SimpleDateFormat(pattern.getValue()).parse(input));
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input));

    pattern.setValue("yyyy-MM-dd");
    new SimpleDateFormat(pattern.getValue()).parse(input);
    ScoutAssert.assertThrows(ParseException.class, () -> new StrictSimpleDateFormat(pattern.getValue()).parse(input));
  }
}
