package org.oscm.app.connector.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.connector.activity.DatabaseWriter;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({SpringBeanSupport.class, Properties.class, LogManager.class})
public class SpringBeanSupportTest {

  private Logger logger;
  private SpringBeanSupport springBeanSupport;
  private Statement statement;
  private Properties props;

  private Map<String, String> transmitData;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    this.springBeanSupport = PowerMockito.spy(new SpringBeanSupport());

    this.logger = mock(Logger.class);
    this.props = mock(Properties.class);

    this.transmitData = new HashMap<>();

    Whitebox.setInternalState(DatabaseWriter.class, "logger", logger);
  }

  @Test
  public void testGetProperty() {

  }
}
