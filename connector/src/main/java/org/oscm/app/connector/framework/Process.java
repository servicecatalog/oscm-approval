/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.Properties;

/**
 * Every real world connector needs to be derived from this class. An instance
 * of this class keeps references to the CommandManager in order to route
 * incoming request from the FCML-Proxy to the appropriate component.
 */
public class Process implements IProcess, BeanNameAware, InitializingBean {
    private static Logger logger = LogManager.getLogger(Process.class);

    // This activity is used to issue command requests to the chain of
    // activities.
    private IActivity activity;

    // this object is kept to be returned in getConfiguration()
    private Properties connectorProperties;

    /**
     * The BeanFactory will call the bean through this interface to inform the
     * bean of the id it was deployed under. The callback will be Invoked after
     * population of normal bean properties but before an init callback like
     * InitializingBean's afterPropertiesSet or a custom init-method. The bean
     * name will be used for logging purposes and to extract configuration
     * parameters.
     *
     * @param beanName The name or id of the bean as defined in the spring bean
     *                 definition file
     */
    @Override
    public void setBeanName(String beanName) {
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>
     * This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an exception
     * in the event of misconfiguration.
     * <p>
     * Configures all chains and the command manager.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * Implements the Connector Interface. How configuration works is described
     * in the bean definition file.
     *
     * @see IProcess
     */
    @Override
    public void setConfiguration(Properties props) {
        connectorProperties = props;
    }

    @Override
    public Properties getConfiguration() {
        return connectorProperties;
    }

    /**
     * Bean property. Sets the start activity that is used to issue command
     * requests to the chain of activities.
     *
     * @param activity
     * @see IActivity
     */
    public void setActivity(IActivity activity) throws ProcessException {
        this.activity = activity;
    }

    /**
     * Implements the IProcess Interface.
     *
     * @see IProcess
     */
    @Override
    public Map<String, String> execute(Map<String, String> transmitData)
            throws ProcessException {
        return activity.transmitReceiveData(transmitData);
    }
}
