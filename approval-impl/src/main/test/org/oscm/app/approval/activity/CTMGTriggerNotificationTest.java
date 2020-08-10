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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.connector.framework.ProcessException;

/** @author worf */
public class CTMGTriggerNotificationTest {

  @Mock BesClient besClient;

  @Spy CTMGTriggerNotification notification;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    doNothing()
        .when(notification)
        .notifyCTMGTrigger(anyString(), anyString(), anyString(), anyBoolean());
  }

  @Test
  public void testTransmitReceiveData() throws ProcessException {
    // given

    Map givenData =
        new HashMap() {
          {
            put("triggerkey", "test");
            put("orgid", "test");
            put("reason", "test");
          }
        };

    notification.setNextActivity(null);
    notification.setTriggerKey("triggerkey");
    notification.setOrgid("orgid");
    notification.setReason("reason");

    // when
    Map resultData = notification.transmitReceiveData(givenData);
    // then

    assertEquals(resultData.get("triggerkey"), "test");
    assertEquals(resultData.get("orgid"), "test");
    assertEquals(resultData.get("reason"), "test");
  }
}
