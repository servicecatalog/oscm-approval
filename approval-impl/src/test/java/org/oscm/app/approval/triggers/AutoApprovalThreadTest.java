/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 4 Nov 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.triggers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.intf.TriggerService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({BesClient.class, LogManager.class, AutoApprovalThread.class})
public class AutoApprovalThreadTest {

  private AutoApprovalThread autoApprovalThread;
  private Object result;
  private Logger logger;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    autoApprovalThread = spy(new AutoApprovalThread("OrganizationId", 12000));
    logger = mock(Logger.class);

    result = new Object();
    Whitebox.setInternalState(AutoApprovalThread.class, "log", logger);
  }

  @Test
  public void testRunWebServiceAsOrganizationAdmin() throws Exception {
    // given
    PowerMockito.mockStatic(BesClient.class);
    PowerMockito.when(BesClient.runWebServiceAsOrganizationAdmin(anyString(), any()))
        .thenReturn(result);
    // when
    autoApprovalThread.run();
    // then
    verify(logger, times(1)).debug("AutoApprovalThread.run() left");
  }

  @Test
  public void testCreateApproveActionWSCall() throws Exception {
    // given
    TriggerService triggerService = mock(TriggerService.class);
    // when
    String result =
        (String)
            autoApprovalThread
                .createApproveActionWSCall(TriggerService.class, 12000)
                .execute(triggerService);
    // then
    assertEquals("OK", result);
  }

  @Test(expected = Exception.class)
  public void testCreateApproveActionWSCallThrowException() throws Exception {
    // when
    autoApprovalThread.createApproveActionWSCall(TriggerService.class, 12000).execute(null);
  }
}
