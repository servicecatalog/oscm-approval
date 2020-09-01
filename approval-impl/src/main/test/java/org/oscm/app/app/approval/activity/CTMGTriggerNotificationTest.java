/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 6 Aug 2020
 *
 * <p>*****************************************************************************
 */
package java.org.oscm.app.app.approval.activity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscm.app.approval.activity.CTMGTriggerNotification;
import org.oscm.app.connector.framework.ProcessException;

/** @author worf */
@RunWith(MockitoJUnitRunner.class)
public class CTMGTriggerNotificationTest {

  @Spy
  CTMGTriggerNotification notification;

  @Before
  public void setUp() throws Exception {
    doNothing()
        .when(notification)
        .notifyCTMGTrigger(anyString(), anyString(), anyString(), anyBoolean());
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

    // when
    Map<String, String> resultData = notification.transmitReceiveData(givenData);
    // then

    assertEquals(resultData.get("triggerkey"), "test");
    assertEquals(resultData.get("orgid"), "test");
    assertEquals(resultData.get("reason"), "test");
  }
}
