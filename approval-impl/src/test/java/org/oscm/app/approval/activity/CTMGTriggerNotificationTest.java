/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 6 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.activity;

import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.approval.remote.WebServiceTask;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.intf.TriggerService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** @author worf */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({CTMGTriggerNotification.class, LogManager.class})
public class CTMGTriggerNotificationTest {

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  private CTMGTriggerNotification notification;
  private Logger logger;
  private Properties props;
  private WebServiceTask webServiceTask;
  private TriggerService triggerService;

  private Activity activity;

  @Before
  public void setUp() {
    notification = PowerMockito.spy(new CTMGTriggerNotification());
    activity = mock(Activity.class);
    logger = mock(Logger.class);
    props = mock(Properties.class);
    webServiceTask = mock(WebServiceTask.class);

    Whitebox.setInternalState(CTMGTriggerNotification.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {
    // given
    notification.doConfigure(props);
    // when
    verify(logger, times(1)).debug(contains("beanName: "));
  }

  @Test
  public void testTransmitReceiveData() throws ProcessException {
    // given

    Map<String, String> givenData =
        Stream.of(
                new String[][] {
                  {"triggerkey", "test"},
                  {"orgid", "test"},
                  {"reason", "test"},
                })
            .collect(Collectors.toMap(data -> data[0], data -> data[1]));

    notification.setNextActivity(null);
    notification.setTriggerKey("triggerkey");
    notification.setOrgid("orgid");
    notification.setReason("reason");

    doNothing()
        .when(notification)
        .notifyCTMGTrigger(anyString(), anyString(), anyString(), anyBoolean());

    // when
    Map<String, String> resultData = notification.transmitReceiveData(givenData);
    // then

    assertEquals(resultData.get("triggerkey"), "test");
    assertEquals(resultData.get("orgid"), "test");
    assertEquals(resultData.get("reason"), "test");
  }

  @Test
  public void testTransmitReceiveDataWithNextActivity() throws ProcessException {
    // given
    Map<String, String> givenData =
        Stream.of(
                new String[][] {
                  {"triggerkey", "test"},
                  {"orgid", "test"},
                  {"reason", "test"},
                })
            .collect(Collectors.toMap(data -> data[0], data -> data[1]));

    notification.setNextActivity(null);
    notification.setTriggerKey("triggerkey");
    notification.setOrgid("orgid");
    notification.setReason("reason");
    notification.setNextActivity(activity);

    doNothing()
        .when(notification)
        .notifyCTMGTrigger(anyString(), anyString(), anyString(), anyBoolean());
    // when
    Map<String, String> resultData = notification.transmitReceiveData(givenData);
    // then
    verify(notification, times(1)).transmitReceiveData(givenData);
    assertEquals(3, givenData.size());
    assertNull(resultData.get("triggerkey"));
  }

  @Test
  public void testCreateTriggerTaskExecuteWithTrue() throws Exception {
    // given
    triggerService = mock(TriggerService.class);
    String key = notification.triggerkey = "12300";
    notification.orgid = "10000";
    // when
    String status =
        (String) notification.createTriggerTask(TriggerService.class, true).execute(triggerService);
    // then
    verify(triggerService, times(1)).approveAction(Long.parseLong(key));
    assertEquals("OK", status);
  }

  @Test
  public void testCreateTriggerTaskExecuteWithFalse() throws Exception {
    // given
    triggerService = mock(TriggerService.class);
    String key = notification.triggerkey = "12300";
    notification.orgid = "10000";
    // when
    String status =
        (String)
            notification.createTriggerTask(TriggerService.class, false).execute(triggerService);
    // then
    verify(triggerService, times(1)).rejectAction(eq(Long.parseLong(key)), anyList());
    assertEquals("OK", status);
  }

  @Test(expected = Exception.class)
  public void testCreateTriggerTaskThrowsException() throws Exception {
    // given
    notification.createTriggerTask(any(), anyBoolean()).execute(triggerService);
  }

  @Test(expected = Exception.class)
  public void testNotifyCTMGTriggerThrowsException()
      throws ProcessException, MalformedURLException, ConfigurationException {
    // given
    when(notification.createTriggerTask(any(), anyBoolean())).thenReturn(webServiceTask);
    // when
    notification.notifyCTMGTrigger("1500", "1000", "Testing notify CTMG trigger", true);
  }
}
