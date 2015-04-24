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
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.EventListener;
import java.util.List;

public interface PlannerListener extends EventListener {

  void plannerChanged(PlannerEvent e);

  /**
   * batch event for fast processing of batch changes
   */
  void plannerChangedBatch(List<? extends PlannerEvent> batch);
}
