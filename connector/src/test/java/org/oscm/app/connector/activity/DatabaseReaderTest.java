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
  DatabaseReader.class,
  Properties.class,
  LogManager.class,
  DriverManager.class,
  ResultSet.class
})
public class DatabaseReaderTest {

  private static final Random RANDOM = new Random();

  private Logger logger;
  private Connection connection;
  private Statement statement;
  private Properties props;
  private DatabaseReader databaseReader;
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
    this.databaseReader = PowerMockito.spy(new DatabaseReader());

    this.statement = mock(Statement.class);
    this.connection = mock(Connection.class);
    this.metadata = mock(ResultSetMetaData.class);
    this.props = mock(Properties.class);
    this.logger = mock(Logger.class);
    this.activity = mock(Activity.class);

    this.transmitData = new HashMap<>();

    Whitebox.setInternalState(DatabaseReader.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    when(this.props.containsKey(any())).thenReturn(true);
    when(this.props.getProperty(anyString())).thenReturn(anyString());
    when(SpringBeanSupport.getProperty(this.props, SpringBeanSupport.URL, null)).thenReturn("url");
    when(SpringBeanSupport.getProperty(this.props, SpringBeanSupport.DRIVER, null))
        .thenReturn("driver");
    when(SpringBeanSupport.getProperty(this.props, SpringBeanSupport.USER, null))
        .thenReturn("user");
    when(SpringBeanSupport.getProperty(this.props, SpringBeanSupport.PASSWORD, null))
        .thenReturn("password");

    this.databaseReader.doConfigure(this.props);

    assertEquals("url", this.databaseReader.url);
    assertEquals("driver", this.databaseReader.driver);
    assertEquals("user", this.databaseReader.username);
    assertEquals("password", this.databaseReader.password);
    verify(this.logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testDoConfigureThrowException() throws Exception {

    when(this.props.containsKey(any())).thenReturn(true);
    when(this.props.getProperty(anyString())).thenReturn(null);
    when(SpringBeanSupport.getProperty(this.props, anyString(), null)).thenReturn(null);

    this.databaseReader.doConfigure(this.props);
  }

  @Test
  public void testSetStatement() {

    this.databaseReader.setStatement("statement");

    assertEquals("statement", this.databaseReader.statement);
  }

  @Test
  public void testSetNamespace() {

    this.databaseReader.setNamespace("namespace");

    assertEquals("namespace.", this.databaseReader.namespace);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataStatementNull() throws Exception {

    this.databaseReader.transmitReceiveData(this.transmitData);
  }

  @Test
  public void testTransmitReceiveDataReturnTransmitData() throws Exception {
    int noColumns = RANDOM.nextInt(20);
    PowerMockito.mockStatic(DriverManager.class);
    PowerMockito.mockStatic(ResultSet.class);

    this.transmitData.put("statement", "subjectValue");
    this.databaseReader.statement = "_$(statement)";
    this.databaseReader.driver = "org.junit.Test";
    this.databaseReader.username = "user";
    this.databaseReader.password = "password";
    this.databaseReader.url = "url";
    this.databaseReader.namespace = "namespace";

    when(DriverManager.getConnection(anyString(), any())).thenReturn(this.connection);
    when(this.connection.createStatement()).thenReturn(this.statement);
    when(this.statement.execute(anyString())).thenReturn(true);
    when(this.resultSet.getMetaData()).thenReturn(this.metadata);
    when(this.metadata.getColumnCount()).thenReturn(noColumns);
    when(this.metadata.getColumnName(anyInt())).thenReturn("column");
    when(this.resultSet.next()).thenReturn(true, true, true, false);
    when(this.resultSet.getString(anyInt())).thenReturn("value" + RANDOM.nextInt(1000));

    final Map<String, String> receivedData =
        this.databaseReader.transmitReceiveData(this.transmitData);

    verify(this.metadata, times(noColumns)).getColumnName(anyInt());
    verify(this.logger, times(3 * noColumns)).debug(startsWith("column"));
    assertEquals(this.transmitData, receivedData);
  }

  @Test
  public void testTransmitReceiveDataReturnGetNextActivity() throws Exception {
    PowerMockito.mockStatic(DriverManager.class);
    PowerMockito.mockStatic(ResultSet.class);

    this.transmitData.put("statement", "subjectValue");
    this.databaseReader.statement = "_$(statement)";
    this.databaseReader.driver = "org.junit.Test";
    this.databaseReader.username = "user";
    this.databaseReader.password = "password";
    this.databaseReader.url = "url";
    this.databaseReader.namespace = "namespace";
    this.databaseReader.setNextActivity(this.activity);

    when(DriverManager.getConnection(anyString(), any())).thenReturn(this.connection);
    when(this.connection.createStatement()).thenReturn(this.statement);
    when(this.statement.execute(anyString())).thenReturn(true);
    when(this.resultSet.getMetaData()).thenReturn(this.metadata);
    when(this.resultSet.next()).thenReturn(false);

    final Map<String, String> receivedData =
        this.databaseReader.transmitReceiveData(this.transmitData);

    assertNotEquals(this.transmitData, receivedData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataReturnClassNotFoundException() throws Exception {

    this.transmitData.put("statement", "subjectValue");
    this.databaseReader.statement = "_$(statement)";
    this.databaseReader.driver = "Test";

    this.databaseReader.transmitReceiveData(this.transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataReturnSQLException() throws Exception {

    this.transmitData.put("statement", "subjectValue");
    this.databaseReader.statement = "_$(statement)";
    this.databaseReader.driver = "org.junit.Test";
    this.databaseReader.username = "user";
    this.databaseReader.password = "password";
    this.databaseReader.url = "url";

    this.databaseReader.transmitReceiveData(this.transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataNotContainsKey() throws Exception {

    this.transmitData.put("wrongKey", "subjectValue");
    this.databaseReader.statement = "_$(statement)";

    this.databaseReader.transmitReceiveData(this.transmitData);
  }
}
