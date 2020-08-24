/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import bsh.EvalError;
import bsh.Interpreter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;

import java.util.Map;
import java.util.Properties;

public class Gateway extends Activity {
    private static Logger logger = LogManager.getLogger(Gateway.class);

    Activity activity1 = null;
    Activity activity2 = null;
    Activity activity3 = null;

    String condition1 = null;
    String condition2 = null;
    String condition3 = null;

    /**
     * Just calls the base class constructor.
     */
    public Gateway() {
        super();
    }

    /**
     * Implements the abstract method from the base class. This method will be
     * called from the base class when the configuration is passed down the
     * chain. How configuration works is described in bean definition file. A
     * configuration parameter is described in the javadoc of the class that
     * uses the configuration parameter.
     *
     * @param props the configuration paramters
     * @see Activity
     */
    @Override
    public void doConfigure(Properties props) throws ProcessException {
        logger.debug("beanName: " + getBeanName());
    }

    public void setActivity1(Activity activity) {
        activity1 = activity;
    }

    public void setCondition1(String condition) {
        condition1 = condition;
    }

    public void setActivity2(Activity activity) {
        activity2 = activity;
    }

    public void setCondition2(String condition) {
        condition2 = condition;
    }

    public void setActivity3(Activity activity) {
        activity3 = activity;
    }

    public void setCondition3(String condition) {
        condition3 = condition;
    }

    /**
     * Overrides the base class method.
     *
     * @see Activity
     */
    @Override
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException {
        logger.debug(String.format("beanName: %s", getBeanName()));

        condition1 = (condition1 == null ? "false" : condition1);
        condition2 = (condition2 == null ? "false" : condition2);
        condition3 = (condition3 == null ? "false" : condition3);

        boolean execActivity1 = false;
        boolean execActivity2 = false;
        boolean execActivity3 = false;

        Interpreter bsh = new Interpreter();

        try {
            for (Object key : transmitData.keySet()) {
                bsh.set((String) key, transmitData.get(key));
            }
            execActivity1 = (Boolean) bsh.eval(condition1);
            logger.debug("condition1: " + condition1 + " evaluates to: "
                    + execActivity1);
        } catch (EvalError e) {
            logger.info(
                    "Failed to evaluate first gateway condition " + condition1,
                    e);
        }

        try {
            execActivity2 = (Boolean) bsh.eval(condition2);
            logger.debug("condition2: " + condition2 + " evaluates to: "
                    + execActivity2);
        } catch (EvalError e) {
            logger.info(
                    "Failed to evaluate second gateway condition " + condition2,
                    e);
        }

        try {
            execActivity3 = (Boolean) bsh.eval(condition3);
            logger.debug("condition3: " + condition3 + " evaluates to: "
                    + execActivity3);

        } catch (EvalError e) {
            logger.info(
                    "Failed to evaluate third gateway condition " + condition3,
                    e);
        }

        if (execActivity1) {
            logger.debug("execute activity1");
            return activity1.transmitReceiveData(transmitData);
        } else if (execActivity2) {
            return activity2.transmitReceiveData(transmitData);
        } else if (execActivity3) {
            return activity3.transmitReceiveData(transmitData);
        } else {
            logger.warn(
                    "Process returns because no gateway activity was selected for further execution.");
            return transmitData;
        }
    }

}
