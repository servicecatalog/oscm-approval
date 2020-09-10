/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import bsh.EvalError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({
  Gateway.class,
  LogManager.class,
})
public class GatewayTest {

  private Logger logger;
  private Gateway gateway;
  private Properties props;
  private Activity activity;

  private Map<String, String> transmitData;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    gateway = PowerMockito.spy(new Gateway());

    logger = mock(Logger.class);
    props = mock(Properties.class);
    activity = mock(Activity.class);

    transmitData = new HashMap<>();

    Whitebox.setInternalState(Gateway.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    gateway.doConfigure(props);

    verify(logger, times(1)).debug(contains("beanName: "));
  }

  @Test
  public void testTransmitReceiveData() throws ProcessException {

    final Map<String, String> receivedResult = gateway.transmitReceiveData(transmitData);

    verify(logger, times(1)).debug(contains("beanName: "));
    verify(logger, times(1))
        .warn(
            contains(
                "Process returns because no gateway activity was selected for further execution"));
    assertEquals(transmitData, receivedResult);
  }

  @Test
  public void testTransmitReceiveDataReturnActivity1() throws ProcessException, EvalError {
    PowerMockito.mockStatic(Activity.class);

    transmitData.put("key1", "value1");
    transmitData.put("key2", "value2");
    transmitData.put("key3", "value3");
    gateway.setCondition1("true");
    gateway.setActivity1(activity);

    final Map<String, String> receivedResult = gateway.transmitReceiveData(transmitData);

    verify(logger, times(1)).debug(contains("beanName: "));
    assertEquals(gateway.activity1.transmitReceiveData(transmitData), receivedResult);
  }

  @Test
  public void testTransmitReceiveDataReturnActivity2() throws ProcessException, EvalError {
    PowerMockito.mockStatic(Activity.class);

    gateway.setCondition2("true");
    gateway.setActivity2(activity);

    final Map<String, String> receivedResult = gateway.transmitReceiveData(transmitData);

    verify(logger, times(1)).debug(contains("beanName: "));
    assertEquals(gateway.activity2.transmitReceiveData(transmitData), receivedResult);
  }

  @Test
  public void testTransmitReceiveDataReturnActivity3() throws ProcessException, EvalError {
    PowerMockito.mockStatic(Activity.class);

    gateway.setCondition3("true");
    gateway.setActivity3(activity);

    final Map<String, String> receivedResult = gateway.transmitReceiveData(transmitData);

    verify(logger, times(1)).debug(contains("beanName: "));
    assertEquals(gateway.activity3.transmitReceiveData(transmitData), receivedResult);
  }
}
