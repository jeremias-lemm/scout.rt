/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.server.services.common.file.RemoteFileService;

public final class ServerConfigProperties {

  private ServerConfigProperties() {
  }

  public static class ClusterSyncUserProperty extends AbstractStringConfigProperty {

    public static final String CLUSTER_SYNC_USER_NAME = "system";

    @Override
    public String getDefaultValue() {
      return CLUSTER_SYNC_USER_NAME;
    }

    @Override
    public String getKey() {
      return "scout.clustersync.user";
    }

    @Override
    public String description() {
      return String.format("Technical subject under which received cluster sync notifications are executed. The default value is '%s'.", CLUSTER_SYNC_USER_NAME);
    }
  }

  public static class ServerSessionCacheExpirationProperty extends AbstractPositiveLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.DAYS.toMillis(1);
    }

    @Override
    public String getKey() {
      return "scout.serverSessionTtl";
    }

    @Override
    public String description() {
      return "Server sessions that have not been accessed for the specified number of milliseconds are removed from the cache. The default value is one day.";
    }
  }

  public static class RemoteFilesRootDirProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.remotefileRootPath";
    }

    @Override
    public String description() {
      return String.format("Absolute path to the root directory of the '%s'. The default value is null.", RemoteFileService.class.getSimpleName());
    }
  }
}
