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
package org.eclipse.scout.rt.server.services.common.security;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.SingleUserFilter;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestAccessControlService;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestClientNotificationQueueListener;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.SERVICES;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractAccessControlService}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class AccessControlServiceTest {
  private AbstractAccessControlService m_accessControlService;
  private TestClientNotificationQueueListener m_listener;
  private List<IBean<?>> m_registerServices;

  @Before
  public void setup() {

    m_accessControlService = BeanInstanceUtil.create(TestAccessControlService.class);

    //Register this IAccessControlService with an higher priority than AllAccessControlService registered in CustomServerTestEnvironment
    m_registerServices = TestingUtility.registerServices(500, m_accessControlService);

    m_listener = new TestClientNotificationQueueListener();
    SERVICES.getService(IClientNotificationService.class).addClientNotificationQueueListener(m_listener);
  }

  @After
  public void after() {
    TestingUtility.unregisterServices(m_registerServices);
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when permissions are loaded
   * {@link AbstractAccessControlService#getPermissions()}
   */
  @Test
  @RunWithSubject("anna")
  public void testClientNotificationSentForGetPermissions() {
    callGetPermissions();

    verifyOneNotificationSent(AccessControlChangedNotification.class, SingleUserFilter.class, "anna");
  }

  /**
   * Tests that a client notification of {@link ResetAccessControlChangedNotification} is sent when the cache is
   * cleared: {@link AbstractAccessControlService#clearCache()}
   */
  @Test
  public void testClientNotificationSentForClearCache() {
    callClearCache();

    verifyOneNotificationSent(ResetAccessControlChangedNotification.class, SingleUserFilter.class, null);
  }

  /**
   * Tests that no client notification at all is sent when the cache is cleared:
   * {@link AbstractAccessControlService#clearCacheNoFire()}
   */
  @Test
  public void testClientNotificationSentForClearCacheNoFire() {
    callClearCacheNoFire();

    verifyNoNotificationsSent();
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cache is cleared:
   * {@link AbstractAccessControlService#clearCacheOfUserIds(Collection)}
   */
  @Test
  public void testClientNotificationSentForClearCacheOfUserIds() {
    final String testUser = "testuser";
    Set<String> testUsers = Collections.singleton(testUser);
    callClearCacheOfUserIds(testUsers);

    verifyOneNotificationSent(AccessControlChangedNotification.class, SingleUserFilter.class, testUser);
  }

  /**
   * Tests that no client notification at all is sent when the cache is cleared:
   * {@link AbstractAccessControlService#clearCacheOfUserIdsNoFire(Collection)}
   */
  @Test
  public void testClientNotificationSentForClearCacheOfUserIdsNoFire() {
    final String testUser = "testuser";
    Set<String> testUsers = Collections.singleton(testUser);
    callClearCacheOfUserIdsNoFire(testUsers);

    verifyNoNotificationsSent();
  }

  /**
   * Tests that no client notification at all is sent when the cache is cleared (no
   * users): {@link AbstractAccessControlService#clearCacheOfUserIds(Collection)}
   */
  @Test
  public void testClientNotificationSentForClearCacheNoUsers() {
    callClearCacheOfUserIds(Collections.<String> emptySet());
    verifyNoNotificationsSent();
  }

  private void callGetPermissions() {
    m_accessControlService.getPermissions();
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void callClearCache() {
    m_accessControlService.clearCache();
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void callClearCacheNoFire() {
    ((TestAccessControlService) m_accessControlService).callClearCacheNoFire();
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void callClearCacheOfUserIds(Collection<String> testUsers) {
    m_accessControlService.clearCacheOfUserIds(testUsers);
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void callClearCacheOfUserIdsNoFire(Collection<String> testUsers) {
    ((TestAccessControlService) m_accessControlService).callClearCacheOfUserIdsNoFire(testUsers);
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void verifyOneNotificationSent(Class<? extends AbstractClientNotification> clientNotificationType, Class<SingleUserFilter> notificationFilterType, String userId) {
    List<ClientNotificationQueueEvent> eventList = m_listener.getEventList();
    assertEquals("Client notification received size", 1, eventList.size());

    ClientNotificationQueueEvent clientNotificationQueueEvent = eventList.get(0);
    assertEquals("ClientNotification Type", clientNotificationType, clientNotificationQueueEvent.getNotification().getClass());
    IClientNotificationFilter filter = clientNotificationQueueEvent.getFilter();
    assertEquals("Notification filter type", notificationFilterType, filter.getClass());
    assertEquals("UserId in client notification filter", userId, ((SingleUserFilter) filter).getUserId());
  }

  private void verifyNoNotificationsSent() {
    assertEquals("Client notification received size", 0, m_listener.getEventList().size());
  }

}
