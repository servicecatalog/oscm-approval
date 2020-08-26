/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class SpringBeanSupportTest {

  @RunWith(PowerMockRunner.class)
  @PowerMockIgnore({"javax.management.*", "javax.script.*"})
  @PrepareForTest({SpringBeanSupport.class, Properties.class, LogManager.class})
  public static class SynchronizeTests {

    private Logger logger;
    private Properties props;

    @BeforeClass
    public static void oneTimeSetup() {
      System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
      System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
    }

    @Before
    public void setUp() {
      this.logger = mock(Logger.class);
      this.props = mock(Properties.class);

      Whitebox.setInternalState(SpringBeanSupport.class, "logger", logger);
    }

    @Test
    public void testGetPropertyWithStringDefaultValueWithKey() {
      String key = "key";
      when(this.props.containsKey(anyString())).thenReturn(true);
      when(this.props.getProperty(anyString())).thenReturn(key);

      final String result = SpringBeanSupport.getProperty(this.props, key, anyString());

      assertEquals(key, result);
    }

    @Test
    public void testGetPropertyWithStringDefaultValueWithoutKey() {
      when(this.props.containsKey(anyString())).thenReturn(false);

      final String result = SpringBeanSupport.getProperty(this.props, anyString(), "default");

      verify(this.logger, times(1)).info(startsWith("getProperty() Key not found: "));
      assertEquals("default", result);
    }

    @Test
    public void testGetPropertyWithBooleanDefaultValueWithKey() {
      String key = "true";
      when(this.props.containsKey(anyString())).thenReturn(true);
      when(this.props.getProperty(anyString())).thenReturn(key);

      final boolean result = SpringBeanSupport.getProperty(this.props, key, anyBoolean());

      assertTrue(result);
    }

    @Test
    public void testGetPropertyWithBooleanDefaultValueWithoutKey() {
      when(this.props.containsKey(anyString())).thenReturn(false);

      final boolean result = SpringBeanSupport.getProperty(this.props, anyString(), true);

      assertTrue(result);
    }

    @Test
    public void testLogProperties() {

      Properties properties = new Properties();
      InputStream is = ClassLoader.getSystemResourceAsStream("test.properties");
      try {
        properties.load(is);
      } catch (IOException e) {
        logger.error("Cannot load file with properties");
      }

      SpringBeanSupport.logProperties(properties);

      verify(this.logger, times(21)).debug(anyString());
    }
  }

  @RunWith(Parameterized.class)
  public static class ParametrizedTests {

    private Properties props;

    @Before
    public void setUp() {
      this.props = mock(Properties.class);
    }

    @Parameterized.Parameter(0)
    public int propsDefault;

    @Parameterized.Parameter(1)
    public String propsValue;

    @Parameterized.Parameter(2)
    public int propsResult;

    @Parameterized.Parameter(3)
    public boolean isContain;

    @Parameterized.Parameters(name = "default={0}, value={1}, result={2}, contain={3}")
    public static Collection<Object[]> data() {
      return Arrays.asList(
          new Object[][] {
            {140, "10", 10, true},
            {20, "iterative", 20, false},
            {50, "actual", 50, false},
            {50, "1520", 1520, true},
            {50, "1a2b", 50, false},
            {0, "98321", 98321, true},
            {123, null, 123, false},
            {4523, "", 4523, false}
          });
    }

    @Test
    public void testGetPropertyWithIntegerDefaultValueWithKey() {
      when(this.props.containsKey(anyString())).thenReturn(this.isContain);
      when(this.props.getProperty(anyString())).thenReturn(propsValue);

      final int result = SpringBeanSupport.getProperty(this.props, anyString(), this.propsDefault);

      assertEquals(result, this.propsResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyWhenPropsIsNull() {

      SpringBeanSupport.getProperty(null, this.propsValue, this.propsDefault);
    }

    @Test
    public void testIsNumber() throws Exception {

      final boolean result =
          Whitebox.invokeMethod(SpringBeanSupport.class, "isNumber", this.propsValue);

      assertEquals(result, this.isContain);
    }
  }
}
