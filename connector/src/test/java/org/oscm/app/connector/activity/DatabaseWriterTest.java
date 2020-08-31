/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.connector.util.SpringBeanSupport;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({
  DatabaseWriter.class,
  Properties.class,
  LogManager.class,
  DriverManager.class,
  ResultSet.class
})
public class DatabaseWriterTest {

  private static final Random RANDOM = new Random();

  private Logger logger;
  private Connection connection;
  private Statement statement;
  private Properties props;
  private DatabaseWriter databaseWriter;
  private Activity activity;
  private ResultSetMetaData metadata;

  private Map<String, String> transmitData;
  ResultSet resultSet;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    databaseWriter = PowerMockito.spy(new DatabaseWriter());

    statement = mock(Statement.class);
    connection = mock(Connection.class);
    metadata = mock(ResultSetMetaData.class);
    props = mock(Properties.class);
    logger = mock(Logger.class);
    activity = mock(Activity.class);

    transmitData = new HashMap<>();

    Whitebox.setInternalState(DatabaseWriter.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    when(props.containsKey(any())).thenReturn(true);
    when(props.getProperty(anyString())).thenReturn(anyString());
    when(SpringBeanSupport.getProperty(props, SpringBeanSupport.URL, null)).thenReturn("url");
    when(SpringBeanSupport.getProperty(props, SpringBeanSupport.DRIVER, null))
        .thenReturn("driver");
    when(SpringBeanSupport.getProperty(props, SpringBeanSupport.USER, null))
        .thenReturn("user");
    when(SpringBeanSupport.getProperty(props, SpringBeanSupport.PASSWORD, null))
        .thenReturn("password");

    databaseWriter.doConfigure(props);

    assertEquals("url", databaseWriter.url);
    assertEquals("driver", databaseWriter.driver);
    assertEquals("user", databaseWriter.username);
    assertEquals("password", databaseWriter.password);
    verify(logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testDoConfigureThrowException() throws Exception {

    when(props.containsKey(any())).thenReturn(true);
    when(props.getProperty(anyString())).thenReturn(null);
    when(SpringBeanSupport.getProperty(props, anyString(), null)).thenReturn(null);

    databaseWriter.doConfigure(props);
  }

  @Test
  public void testSetStatement() {

    databaseWriter.setStatement("statement");

    assertEquals("statement", databaseWriter.statement);
  }

  @Test
  public void testSetNamespace() {

    databaseWriter.setNamespace("namespace");

    assertEquals("namespace.", databaseWriter.namespace);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataStatementNull() throws Exception {

    databaseWriter.transmitReceiveData(transmitData);
  }

  @Test
  public void testTransmitReceiveDataReturnTransmitData() throws Exception {
    int noColumns = RANDOM.nextInt(20);
    PowerMockito.mockStatic(DriverManager.class);
    PowerMockito.mockStatic(ResultSet.class);

    transmitData.put("statement", "returning");
    databaseWriter.statement = "insert_$(statement)";
    databaseWriter.driver = "org.junit.Test";
    databaseWriter.username = "user";
    databaseWriter.password = "password";
    databaseWriter.url = "url";
    databaseWriter.namespace = "namespace";

    when(DriverManager.getConnection(anyString(), any())).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.execute(anyString())).thenReturn(true);
    when(resultSet.getMetaData()).thenReturn(metadata);
    when(metadata.getColumnCount()).thenReturn(noColumns);
    when(metadata.getColumnName(anyInt())).thenReturn("column");
    when(resultSet.next()).thenReturn(true, true, true, false);
    when(resultSet.getString(anyInt())).thenReturn("value" + RANDOM.nextInt(1000));

    final Map<String, String> receivedData =
        databaseWriter.transmitReceiveData(transmitData);

    verify(metadata, times(noColumns)).getColumnName(anyInt());
    verify(logger, times(3 * noColumns)).debug(startsWith("column"));
    assertEquals(transmitData, receivedData);
  }

  @Test
  public void testTransmitReceiveDataReturnGetNextActivity() throws Exception {
    PowerMockito.mockStatic(DriverManager.class);
    PowerMockito.mockStatic(ResultSet.class);

    transmitData.put("statement", "subjectValue");
    databaseWriter.statement = "_$(statement)";
    databaseWriter.driver = "org.junit.Test";
    databaseWriter.username = "user";
    databaseWriter.password = "password";
    databaseWriter.url = "url";
    databaseWriter.namespace = "namespace";
    databaseWriter.setNextActivity(activity);

    when(DriverManager.getConnection(anyString(), any())).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.execute(anyString())).thenReturn(true);
    when(resultSet.getMetaData()).thenReturn(metadata);
    when(resultSet.next()).thenReturn(false);

    final Map<String, String> receivedData =
        databaseWriter.transmitReceiveData(transmitData);

    assertNotEquals(transmitData, receivedData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataReturnClassNotFoundException() throws Exception {

    transmitData.put("statement", "subjectValue");
    databaseWriter.statement = "_$(statement)";
    databaseWriter.driver = "Test";

    databaseWriter.transmitReceiveData(transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataReturnSQLException() throws Exception {

    transmitData.put("statement", "subjectValue");
    databaseWriter.statement = "_$(statement)";
    databaseWriter.driver = "org.junit.Test";
    databaseWriter.username = "user";
    databaseWriter.password = "password";
    databaseWriter.url = "url";

    databaseWriter.transmitReceiveData(transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataNotContainsKey() throws Exception {

    transmitData.put("wrongKey", "subjectValue");
    databaseWriter.statement = "_$(statement)";

    databaseWriter.transmitReceiveData(transmitData);
  }
}
