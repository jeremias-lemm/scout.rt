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
package org.eclipse.scout.rt.client.extension.ui.form.fields.clipboardfield;

import java.util.Collection;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.AbstractClipboardField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public abstract class AbstractClipboardFieldExtension<OWNER extends AbstractClipboardField> extends AbstractValueFieldExtension<Collection<BinaryResource>, OWNER> implements IClipboardFieldExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractClipboardFieldExtension(OWNER owner) {
    super(owner);
  }

}
