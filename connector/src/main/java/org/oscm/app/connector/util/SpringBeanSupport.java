/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to define all available configuration properties and to
 * provide a few methods for convenience.
 */
public class SpringBeanSupport {
    private static Logger logger = LogManager.getLogger(SpringBeanSupport.class);

    // global settings
    public static final String TIMEOUT = "timeout";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String NUMBER_OF_RETRIES_ON_ERROR = "numberOfRetriesOnError";
    public static final String RETRIEVAL_INTERVAL = "retrievalIntervall";

    public static final String URL = "url";
    public static final String REFERRAL = "referral";
    public static final String DRIVER = "driver";
    public static final String USER = "username";
    public static final String PASSWORD = "password";
    public static final String AUTHENTICATION = "authentication";

    // email activity
    public static final String MAIL_SESSION = "mailsession";

    // a default timeout value if the timeout is not configured
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Returns the property value in case of a valid key, otherwise the default
     * value is returned. A valid key is registered as existing key in this
     * class and is part of the properties.
     *
     * @param props        the value is extracted from these properties
     * @param key          the value for this key is returned from the properties
     * @param defaultValue this value is returned if the key is not valid
     * @return the property value if the key is valid otherwise the default
     * value
     */
    public static synchronized String getProperty(java.util.Properties props,
                                                  String key, String defaultValue) {
        if (props.containsKey(key)) {
            return props.getProperty(key);
        } else {
            logger.info("getProperty() Key not found: " + key);
            return defaultValue;
        }
    }

    /**
     * Returns the property value in case of a valid key, otherwise the default
     * value is returned. A valid key is registered as existing key in this
     * class and is part of the properties.
     *
     * @param props        the value is extracted from these properties
     * @param key          the value for this key is returned from the properties
     * @param defaultValue this value is returned if the key is not valid
     * @return the property value if the key is valid otherwise the default
     * value
     */
    public static synchronized boolean getProperty(java.util.Properties props,
                                                   String key, boolean defaultValue) {
        if (props.containsKey(key)) {
            return props.getProperty(key).equals("true");
        } else {
            logger.info("getProperty() Key not found: " + key);
            return defaultValue;
        }
    }

    /**
     * Returns the property value in case of a valid key, otherwise the default
     * value is returned. A valid key is registered as existing key in this
     * class and is part of the properties.
     *
     * @param props        the value is extracted from these properties
     * @param key          the value for this key is returned from the properties
     * @param defaultValue this value is returned if the key is not valid
     * @return the property value if the key is valid otherwise the default
     * value
     */
    public static synchronized int getProperty(java.util.Properties props,
                                               String key, int defaultValue) {
        if (props == null) {
            throw new IllegalArgumentException(
                    "SpringBeanSupport.getProperty() No properties set.");
        }

        if (props.containsKey(key)) {
            String value = props.getProperty(key);
            if (isNumber(value)) {
                return Integer.parseInt(value);
            } else {
                return defaultValue;
            }
        } else {
            logger.info("getProperty() Key not found: " + key);
            return defaultValue;
        }
    }

    private static boolean isNumber(String value) {
        if (value == null || value.trim().length() == 0) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static synchronized void logProperties(java.util.Properties props) {
        for (Object key : props.keySet()) {
            logger.debug(key + ": " + props.getProperty((String) key));
        }
    }
}
