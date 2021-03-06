/**
 * Copyright (c) 2009 - 2016 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.CloseListener;
import org.candlepin.audit.ActiveMQStatus;
import org.candlepin.common.config.Configuration;
import org.candlepin.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Monitors the status of the ActiveMQ connection and notifies listeners when
 * there are issues with the connection.
 */
@Singleton
public class ActiveMQStatusMonitor implements Runnable, CloseListener {

    private static Logger log = LoggerFactory.getLogger(ActiveMQStatusMonitor.class);

    private Configuration config;
    private long monitorInterval;
    private List<ActiveMQStatusListener> registeredListeners;
    private ActiveMQStatus lastReported;
    private ScheduledExecutorService executorService;
    private Future<?> future = null;
    private final Object lock = new Object();

    @Inject
    public ActiveMQStatusMonitor(Configuration config) {
        this.config = config;
        this.monitorInterval = config.getLong(ConfigProperties.ACTIVEMQ_CONNECTION_MONITOR_INTERVAL);
        this.registeredListeners = new LinkedList<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
        this.lastReported = ActiveMQStatus.UNKNOWN;
    }

    /**
     * Checks the status of the connection to the broker and starts the monitor
     * if it is down.
     */
    public void initialize() {
        ActiveMQStatus current = testConnection() ? ActiveMQStatus.CONNECTED : ActiveMQStatus.DOWN;
        notifyListeners(current);

        // If the connection is currently down, start monitoring the connection
        // so that candlepin will be notified when it comes back up.
        if (ActiveMQStatus.DOWN.equals(current)) {
            monitorConnection();
        }
    }

    public void registerListener(ActiveMQStatusListener listener) {
        this.registeredListeners.add(listener);
    }

    @Override
    public void connectionClosed() {
        notifyListeners(ActiveMQStatus.DOWN);
        monitorConnection();
    }

    private void monitorConnection() {
        // Since there can be multiple connections reporting that they have closed,
        // ensure that we only start one monitoring task.
        synchronized (lock) {
            if (future == null || future.isDone() || future.isCancelled()) {
                log.info("Scheduling connection retries.");
                future = executorService.scheduleAtFixedRate(this, 1, monitorInterval, TimeUnit.MILLISECONDS);
            }
            else {
                log.info("Monitor already running.");
            }
        }
    }

    @Override
    public void run() {
        log.debug("Checking status of the ActiveMQ broker.");
        if (testConnection()) {
            notifyListeners(ActiveMQStatus.CONNECTED);
            future.cancel(false);
        }
    }

    protected boolean testConnection() {
        ServerLocator locator = null;
        ClientSessionFactory factory = null;
        try {
            String serverUrl = config.getProperty(ConfigProperties.ACTIVEMQ_BROKER_URL);
            locator = ActiveMQClient.createServerLocator(serverUrl);
            locator.createSessionFactory();
            log.info("Connection to ActiveMQ is available.");
            return true;
        }
        catch (Exception e) {
            log.debug("Connection to ActiveMQ is unavailable.", e);
            return false;
        }
        finally {
            if (factory != null) {
                factory.close();
            }
            if (locator != null) {
                locator.close();
            }
        }
    }

    private void notifyListeners(ActiveMQStatus newStatus) {
        log.debug("Notifying listeners of new status: {}", newStatus);
        this.registeredListeners.forEach(listener -> {
            try {
                listener.onStatusUpdate(lastReported, newStatus);
            }
            catch (Exception e) {
                // If the listener throws an exception, log it and move on to the next.
                log.error("Unable to notify listener about new status: {}", listener.getClass(), e);
            }
        });
        lastReported = newStatus;
    }
}
