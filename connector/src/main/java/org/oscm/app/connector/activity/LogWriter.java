/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class LogWriter extends Activity {
    private static Logger logger = LogManager.getLogger(LogWriter.class);

    Properties parameter = new Properties();

    public LogWriter() {
        super();
    }

    @Override
    public void doConfigure(java.util.Properties props)
            throws ProcessException {
        logger.debug("beanName: " + getBeanName());
    }

    public void setParameter(Properties parameter) {
        this.parameter = parameter;
    }

    /**
     * Overrides the base class method.
     *
     * @see Activity
     */
    @Override
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException {
        logger.debug("beanName: " + getBeanName());

        Iterator iter = parameter.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = parameter.getProperty(key);
            try {
                value = replacePlaceholder(value, transmitData);
            } catch (Exception e) {
                // ignore
            }
            logger.info(key + ": " + value);
        }

        logger.info("Transmit Data Start");
        iter = transmitData.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = transmitData.get(key);
            logger.info("   " + key + ": " + value);
        }
        logger.info("Transmit Data End");

        if (getNextActivity() == null) {
            return transmitData;
        } else {
            return getNextActivity().transmitReceiveData(transmitData);
        }
    }
}
