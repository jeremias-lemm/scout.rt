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
package org.eclipse.scout.rt.server.jms;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IConfigIniConstants;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;

/**
 * Configure jndi by simply placing the jndi.properties on the classpath, i.e. place it in WEB-INF/classes or in
 * development in src/main/resources
 * <p>
 * This class is thread save.
 * <p>
 * Some notes about JMS objects:
 * <ul>
 * <li>A {@link Connection} is a is a relatively heavyweight object. It supports concurrent use. Therefore it should be
 * shared.
 * <li> {@link Session}, {@link MessageConsumer} and {@link MessageProducer} are lightweight objects and should not be
 * used by different threads.
 * <li>To receive new messages one should not use the method
 * {@link MessageConsumer#setMessageListener(javax.jms.MessageListener)} as this is a J2EE container private method.
 * Instead one should use the synchronous methods like {@link MessageConsumer#receive(long)}
 * </ul>
 *
 * @param <T>
 *          the type of message that should be sent and received
 */
public abstract class AbstractJmsService<T> extends AbstractService {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJmsService.class);

  private Connection m_connection;

  protected AbstractJmsService() {
  }

  protected abstract ConnectionFactory getConnectionFactory();

  protected abstract Destination getDestination();

  protected boolean isEnabled() {
    return getConnectionFactory() != null && getDestination() != null;
  }

  @SuppressWarnings("unchecked")
  protected Class<T> getMessageType() {
    return TypeCastUtility.getGenericsParameterClass(this.getClass(), AbstractJmsService.class);
  }

  protected IJmsMessageSerializer<T> createMessageSerializer() {
    return new JmsMessageSerializer<>(getMessageType());
  }

  protected synchronized Connection getConnection() {
    return m_connection;
  }

  protected synchronized void setupConnection() throws ProcessingException {
    closeConnection();
    ConnectionFactory connectionFactory = getConnectionFactory();
    Connection con;
    try {
      con = connectionFactory.createConnection();
    }
    catch (JMSException e) {
      throw new ProcessingException("Failed creating JMS connection", e);
    }
    String clientId = null;
    try {
      // try to set clientId; might fail, ignore if happens
      clientId = createClientId();
      con.setClientID(clientId);
    }
    catch (Exception e) {
      LOG.info("Unable to set clientID '{0}' for consumer connection, possibly because of running in J2EE container: {1}", clientId, e.getMessage());
      LOG.trace("Full Exception:", e);
    }
    m_connection = con;
  }

  protected synchronized void closeConnection() throws ProcessingException {
    Connection connection = m_connection;
    if (connection != null) {
      m_connection = null;
      try {
        connection.close();
      }
      catch (JMSException e) {
        throw new ProcessingException("Failed closing JMS connection", e);
      }
    }
  }

  protected String createClientId() {
    String serverVersion = ConfigIniUtility.getProperty(IConfigIniConstants.APPLICATION_VERSION);
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName()).append(" ");
    if (serverVersion != null) {
      sb.append(serverVersion).append(" ");
    }
    sb.append("nodeId=").append(BEANS.get(IClusterSynchronizationService.class).getNodeId()).append(" ");
    sb.append("registered at ");
    sb.append(DateUtility.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS"));
    return sb.toString();
  }
}
