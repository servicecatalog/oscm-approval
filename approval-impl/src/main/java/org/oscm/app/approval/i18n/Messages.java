/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) FUJITSU LIMITED - ALL RIGHTS RESERVED. 
 *       
 *  Creation Date: 2013-02-06                                                       
 *                                                                              
 *******************************************************************************/
package org.oscm.app.approval.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Handles localized messages for the application.
 */
public class Messages {

    private static final String BUNDLE_NAME = "i18n.messages";

    private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private Messages() {
        // do not allow instantiation
    }

    /**
     * Sets the locale for the used message bag.
     */
    public static void setLocale(String locale) {
        if (locale != null) {
            Locale loc = new Locale(locale);
            RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, loc);
        }
    }

    /**
     * Gets a message by the given key
     */
    public static String get(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Gets a parameterized message by the given key
     */
    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    /**
     * Gets a parameterized message by the given key
     */
    public static String get(String key, long... args) {
        return MessageFormat.format(get(key), args);
    }
}
