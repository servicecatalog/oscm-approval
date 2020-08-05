/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.util;

import java.util.NoSuchElementException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * When accessed the first time the class loads a property file from the
 * WEB-INF/classes folder.
 */
public class ApprovalProperties {

    private final static Logger log = LoggerFactory
            .getLogger(ApprovalProperties.class);

    private static PropertiesConfiguration props = null;

    static {

        try {
            // Check property file
            props = new PropertiesConfiguration("bss-app-approval.properties");

            // Auto reload
            FileChangedReloadingStrategy reloadStrategy = new FileChangedReloadingStrategy();
            reloadStrategy.setRefreshDelay(10000);
            props.setReloadingStrategy(reloadStrategy);

        } catch (Exception e) {
            log.error("Failed to load properties.", e);
        }
    }

    public static String getValue(String key) {
        String val = null;
        try {
            val = props.getString(key);
        } catch (NoSuchElementException e) {
            // ignore
        }
        return val;
    }
}
