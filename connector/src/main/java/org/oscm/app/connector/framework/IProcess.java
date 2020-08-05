/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.framework;

import java.util.Map;
import java.util.Properties;

/**
 * This is the interface the FCML-Proxy will use to retrieve diagnose-, config-
 * and masterdata from a connector and to issue commands on a connector. An
 * instance of a connector is used to communicate with a device or controller.
 *
 * @see com.skytecag.xsight.connector.impl.eeml.EEMLConnector
 * @see com.skytecag.xsight.connector.impl.pc.PCConnector
 *
 * @author opetrovski
 */
public interface IProcess {

    /**
     * Issues a synchronous command request.
     */
    public Map<String, String> execute(Map<String, String> transmitData)
            throws ProcessException;

    /**
     * The connector and all his chains are configured that way. For a
     * description on how configuration works see the bean definition file.
     *
     * @param configuration
     *            configuration parameters
     */
    public void setConfiguration(Properties configuration)
            throws ProcessException;

    /**
     * Get the current connector configuration.
     * 
     * @return connector configuration object that was set previously with
     *         setConfiguration.
     */
    Properties getConfiguration();

}
