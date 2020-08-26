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
import org.oscm.app.connector.util.SpringBeanSupport;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

public class DatabaseWriter extends Activity {
    private static Logger logger = LogManager.getLogger(DatabaseWriter.class);

    String statement = null;
    String url, driver, username, password, namespace = "";

    public DatabaseWriter() {
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
    public void doConfigure(java.util.Properties props)
            throws ProcessException {
        logger.debug("beanName: " + getBeanName());
        url = SpringBeanSupport.getProperty(props, SpringBeanSupport.URL, null);
        driver = SpringBeanSupport.getProperty(props, SpringBeanSupport.DRIVER,
                null);
        username = SpringBeanSupport.getProperty(props, SpringBeanSupport.USER,
                null);
        password = SpringBeanSupport.getProperty(props,
                SpringBeanSupport.PASSWORD, null);

        if (url == null) {
            logger.error("beanName: " + getBeanName() + " The property \""
                    + SpringBeanSupport.URL + "\" is not set.");
            throw new ProcessException(
                    "beanName: " + getBeanName() + " The property \""
                            + SpringBeanSupport.URL + "\" is not set.",
                    ProcessException.CONFIG_ERROR);
        }

        if (driver == null) {
            logger.error("beanName: " + getBeanName() + " The property \""
                    + SpringBeanSupport.DRIVER + "\" is not set.");
            throw new ProcessException(
                    "beanName: " + getBeanName() + " The property \""
                            + SpringBeanSupport.DRIVER + "\" is not set.",
                    ProcessException.CONFIG_ERROR);
        }

        if (username == null) {
            logger.error("beanName: " + getBeanName() + " The property \""
                    + SpringBeanSupport.USER + "\" is not set.");
            throw new ProcessException(
                    "beanName: " + getBeanName() + " The property \""
                            + SpringBeanSupport.USER + "\" is not set.",
                    ProcessException.CONFIG_ERROR);
        }

        if (password == null) {
            logger.error("beanName: " + getBeanName() + " The property \""
                    + SpringBeanSupport.PASSWORD + "\" is not set.");
            throw new ProcessException(
                    "beanName: " + getBeanName() + " The property \""
                            + SpringBeanSupport.PASSWORD + "\" is not set.",
                    ProcessException.CONFIG_ERROR);
        }
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace + ".";
    }

    /**
     * Overrides the base class method.
     *
     * @see Activity
     */
    @Override
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException {
        logger.debug("beanName: " + getBeanName() + " statement: " + statement);

        if (statement == null) {
            logger.error("beanName: " + getBeanName()
                    + " missing SQL statement in bean configuration");
            throw new ProcessException(
                    "beanName: " + getBeanName()
                            + " missing SQL statement in bean configuration",
                    ProcessException.CONFIG_ERROR);
        }

        while (statement.indexOf("$(") >= 0) {
            int beginIndex = statement.indexOf("$(");
            String first = statement.substring(0, beginIndex);
            String rest = statement.substring(beginIndex + 2);
            String key = rest.substring(0, rest.indexOf(")"));
            if (!transmitData.containsKey(key)) {
                logger.error("beanName: " + getBeanName() + " key " + key
                        + " from SQL statement is not defined as property");
                throw new ProcessException(
                        "beanName: " + getBeanName() + " key " + key
                                + " from SQL statement is not defined as property",
                        ProcessException.CONFIG_ERROR);
            }
            statement = first + transmitData.get(key)
                    + rest.substring(rest.indexOf(")") + 1, rest.length());
        }

        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName(driver);
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            conn = DriverManager.getConnection(url, props);
            conn.setAutoCommit(true);
            stmt = conn.createStatement();
            logger.debug("execute statement " + statement);
            ResultSet resultSet = null;
            if (statement.startsWith("insert")
                    && statement.indexOf("returning") > 0) {
                resultSet = stmt.executeQuery(statement);
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();
                String[] columnNames = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    columnNames[i] = metadata.getColumnName(i + 1);
                }

                while (resultSet.next()) {
                    for (int i = 0; i < columnCount; i++) {
                        String value = resultSet.getString(i + 1);
                        logger.debug(columnNames[i] + ": " + value);
                        transmitData.put(namespace + columnNames[i], value);
                    }
                }
                resultSet.close();
            } else {
                stmt.executeUpdate(statement);
            }

        } catch (ClassNotFoundException e) {
            logger.error(
                    "beanName: " + getBeanName()
                            + " Failed to load database driver class " + driver,
                    e);
            throw new ProcessException(
                    "beanName: " + getBeanName()
                            + " Failed to load database driver class " + driver,
                    ProcessException.CONFIG_ERROR);
        } catch (SQLException e) {
            logger.error("beanName: " + getBeanName()
                    + " Failed to execute statement " + statement, e);
            throw new ProcessException(
                    "beanName: " + getBeanName()
                            + " Failed to execute statement " + statement,
                    ProcessException.ERROR, e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }

        if (getNextActivity() == null) {
            return transmitData;
        } else {
            return getNextActivity().transmitReceiveData(transmitData);
        }
    }

}
