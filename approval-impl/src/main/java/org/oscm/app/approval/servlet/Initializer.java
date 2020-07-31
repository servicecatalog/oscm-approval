/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.approval.servlet;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializer for logging facade.
 * 
 * @author goebel
 */
@WebListener
public class Initializer implements javax.servlet.ServletContextListener {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    ScheduledFuture<?> refreshHandle;

    void startRefreshTimer() {
        final Runnable refresh = new Runnable() {
            public void run() {
                handleOnChange(logFile);
            }
        };

        refreshHandle = scheduler.scheduleAtFixedRate(refresh, TIMER_DELAY_VALUE, TIMER_DELAY_VALUE, TimeUnit.MILLISECONDS);

    }

    private static final Logger LOG = LoggerFactory.getLogger(Initializer.class);

    private String LOG4J_TEMPLATE = "log4j.properties.template";

    private long TIMER_DELAY_VALUE = 60000;

    
    private File logFile;

    private long logFileLastModified = 0;

    private boolean logFileWarning = false;

    public boolean isLogFileWarning() {
        return logFileWarning;
    }

    public long getLogFileLastModified() {
        return logFileLastModified;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String instanceRoot = System.getProperty("catalina.home");
            if (instanceRoot != null) {
                File root = new File(instanceRoot);
                if (root.isDirectory()) {
                    String filePath = "/conf/log4j.approval.properties";
                    logFile = new File(root, filePath);
                    if (!logFile.exists()) {
                        publishTemplateFile();
                    }
                    handleOnChange(logFile);
                    LOG.debug("Enable timer service for monitoring modification of "
                            + logFile.getPath());
                    startRefreshTimer();
                } else {
                    LOG.error(
                            "Failed to initialize log file: invalid instanceRoot " + instanceRoot);
                    logFile = null;
                }
            } else {
                LOG.error(
                        "Failed to initialize log file: missing system property 'com.sun.aas.instanceRoot'");
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize log file", e);
            logFile = null;
        }
    }

    
    private void publishTemplateFile() {
        try (InputStream is = this.getClass().getClassLoader()
                .getResourceAsStream(LOG4J_TEMPLATE)) {
            if (is == null) {
                LOG.warn("Template file not found: " + LOG4J_TEMPLATE);
            } else if (logFile.getParentFile().exists()) {
                FileUtils.writeByteArrayToFile(logFile, IOUtils.toByteArray(is));
            }
        } catch (Exception e) {
            // ignore
            LOG.error("Failed to publish template file from " + LOG4J_TEMPLATE + " to "
                    + logFile.getAbsolutePath(), e);
        }
    }

    void handleOnChange(File logFile) {
        try {
            long lastModif = logFile.lastModified();
            if (lastModif > logFileLastModified) {
                logFileLastModified = lastModif;
                LOG.debug("Reload log4j configuration from " + logFile.getAbsolutePath());
                configurePropertyConfigurator(logFile);
                logFileWarning = false;
            }
        } catch (Exception e) {
            if (!logFileWarning) {
                logFileWarning = true;
                LOG.error(logFile.getAbsolutePath(), e);
            }
        }
    }

    void configurePropertyConfigurator(File logFile) {
        new PropertyConfigurator().doConfigure(logFile.getAbsolutePath(),
                LogManager.getLoggerRepository());
    }

   
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (refreshHandle != null) {
            refreshHandle.cancel(true);
        }

    }
}
