/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.framework;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.app.connector.util.SpringBeanSupport;
import org.springframework.beans.factory.BeanNameAware;

/**
 * A chain item is part of a chain. A chain is used to request data from a
 * device or controller and to transform this data before passing it to the
 * instance that is using the chain.
 * <p>
 * All chain items are linked together to pass the incoming call down the chain
 * until the last chain item is reached that will finally retrieve the data from
 * the device or controller.
 */
abstract public class Activity implements BeanNameAware, IActivity {
    private static Logger logger = LogManager.getLogger(Activity.class);

    // the reference to the next activity in the chain of activities
    // for the last activity in the chain this value is null
    private Activity nextActivity = null;

    /*
     * Every instance of this class is generated by the Spring XMLBeanFactory
     * from a bean definition file. The bean id is a unique identifier in the
     * scope of the bean definition file. The bean id is set by the
     * XMLBeanFactory during startup when you implement the BeanNameAware
     * interface. The bean id is used for logging purposes and to extract
     * configuration parameters.
     */
    private String beanName;

    /**
     * Implements the BeanNameAware interface. The BeanFactory will call the
     * bean through this interface to inform the bean of the id it was deployed
     * under. The callback will be Invoked after population of normal bean
     * properties but before an init callback like InitializingBean's
     * afterPropertiesSet or a custom init-method. The bean name will be used
     * for logging purposes and to extract configuration parameters.
     *
     * @param beanName
     *            The name or id of the bean as defined in the spring bean
     *            definition file
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    protected String getBeanName() {
        return beanName;
    }

    /**
     * Implements the IActivity interface.
     * 
     * @see IActivity
     */
    @Override
    public void setConfiguration(java.util.Properties props)
            throws ProcessException {
        SpringBeanSupport.logProperties(props);
        doConfigure(props);
        if (getNextActivity() != null) {
            getNextActivity().setConfiguration(props);
        }
    }

    /**
     * Any real world chain item needs to overwrite this method in order to
     * configure himself.
     *
     * @param props
     *            the configuration parameters
     */
    abstract public void doConfigure(java.util.Properties props)
            throws ProcessException;

    /**
     * A synchronous call down the chain. All chain items override this method
     * in order to control the data retrieval process and to process the
     * retrieved data.
     *
     * @param transmitData
     *            this is outgoing data that will be send before data is
     *            retrieved (if null, no data is send)
     * @return the data that was received and processed
     * @throws ProcessException
     *             All errors in the chain are passed up the chain with this
     *             exception
     */
    @Override
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException {
        if (getNextActivity() != null) {
            return getNextActivity().transmitReceiveData(transmitData);
        } else {
            return new HashMap<>(); // end of chain
        }
    }

    /**
     * Bean property. The next activity is used to pass data retrieval requests
     * down the chain and the result up the chain.
     *
     * @param activity
     */
    public void setNextActivity(Activity activity) {
        this.nextActivity = activity;
    }

    /**
     * Bean property. The next activity is used to pass data retrieval requests
     * down the chain and the result up the chain.
     *
     * @return The next activity.
     */
    protected Activity getNextActivity() {
        return nextActivity;
    }

    /**
     * 
     * @param value
     * @param transmitData
     * @throws ProcessException
     */
    protected String replacePlaceholder(String value,
            Map<String, String> transmitData) throws ProcessException {

        while (value.indexOf("$(") >= 0) {
            int beginIndex = value.indexOf("$(");
            String first = value.substring(0, beginIndex);
            String rest = value.substring(beginIndex + 2);
            String key = rest.substring(0, rest.indexOf(")"));
            if (!transmitData.containsKey(key)) {
                logger.error(String.format("Failed to replace placeholder %s. beanName: %s key %s is not defined as property", value, getBeanName(), key));
                throw new ProcessException(
                        "Failed to replace placeholder " + value
                                + ". beanName: " + getBeanName() + " key " + key
                                + " is not defined as property",
                        ProcessException.CONFIG_ERROR);
            }
            value = first + transmitData.get(key)
                    + rest.substring(rest.indexOf(")") + 1, rest.length());
            logger.debug(String.format("replaced %s with %s - new value is %s", key, transmitData.get(key), value));
        }
        return value;

    }

}
