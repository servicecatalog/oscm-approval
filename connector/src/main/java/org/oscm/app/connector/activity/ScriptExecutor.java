/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import java.util.Map;

import org.apache.log4j.Logger;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

/**
 * This class is the last chain item in a chain. It retrieves diagnose-, config-
 * and master data from a HTTP server. It sends HTTP GET or POST requests and
 * returns the retrieved data as plain text. This chain item uses the following
 * configuration parameters:
 * <table>
 * <tr>
 * <td>url</td>
 * <td>the URL where the data can be trieved from</td>
 * </tr>
 * <tr>
 * <td>timeout</td>
 * <td>the timeout for the HTTP request</td>
 * </tr>
 * </table>
 */
public class ScriptExecutor extends Activity {
    private static Logger logger = Logger.getLogger(ScriptExecutor.class);

    String script;
    String[] attributes = new String[] {};

    /**
     * Just calls the base class constructor.
     */
    public ScriptExecutor() {
        super();
    }

    /**
     * Implements the abstract method from the base class. This method will be
     * called from the base class when the configuration is passed down the
     * chain. How configuration works is described in bean definition file. A
     * configuration parameter is described in the javadoc of the class that
     * uses the configuration parameter.
     *
     * @param props
     *            the configuration paramters
     * @see Activity
     */
    @Override
    public void doConfigure(java.util.Properties props)
            throws ProcessException {
        logger.debug("beanName: " + getBeanName());

        // url = SpringBeanSupport.getProperty(props, SpringBeanSupport.URL,
        // null);
        // timeout = SpringBeanSupport.getProperty(props,
        // SpringBeanSupport.TIMEOUT, SpringBeanSupport.DEFAULT_TIMEOUT);
        // String howToSend = SpringBeanSupport.getProperty(props,
        // SpringBeanSupport.HTTP_SEND_METHOD, null);
        //
        // if( url == null )
        // {
        // logger.error("beanName: "+ getBeanName() +" The property \"" +
        // SpringBeanSupport.URL + "\" is not set.");
        // throw new ProcessException("checkConfiguration() "+getBeanName()+ "
        // The property \"" + SpringBeanSupport.URL + "\" is not set.",
        // ProcessException.CONFIG_ERROR);
        // }
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    /**
     * Overwrites the base class method.
     * 
     * @see Activity
     */
    @Override
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException {
        logger.debug("beanName: " + getBeanName());

        try {
            Interpreter interpreter = new Interpreter();
            for (Object key : transmitData.keySet()) {
                interpreter.set((String) key, transmitData.get(key));
            }

            // interpreter.set("students", Arrays.asList(student, student2,
            // student3));
            // in script: for (std : students)

            interpreter.eval(script);

            for (int i = 0; i < attributes.length; i++) {
                String key = attributes[i];
                String value = (String) interpreter.get(key);
                transmitData.put(key, value);
                logger.debug("script returns: " + key + "=" + value);
            }

        } catch (TargetError e) {
            logger.error(
                    "The script or code called by the script threw an exception: ",
                    e);
        } catch (EvalError e) {
            logger.error("There was an error in evaluating the script:", e);
        }

        if (getNextActivity() == null) {
            return transmitData;
        } else {
            return getNextActivity().transmitReceiveData(transmitData);
        }
    }

}
