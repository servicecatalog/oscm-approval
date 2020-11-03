/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 2 Nov 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.remote;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.dataaccess.Credentials;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BesClient.class})
public class BesClientTest {

  private BesClient besClient;
  private AppDataService appDataService;
  private Credentials credentials;
  private WebServiceTask webServiceTask;
  private Object[] result;

  @Before
  public void setUp() {
    besClient = spy(new BesClient());
    appDataService = mock(AppDataService.class);
    credentials = mock(Credentials.class);
    webServiceTask = mock(WebServiceTask.class);

    result = new Object[1];
  }

  @Test
  public void testRunWebServiceAsOrganizationAdmin() throws Exception {
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(appDataService);
    when(appDataService.loadOrgAdminCredentials(anyString())).thenReturn(credentials);
    when(webServiceTask.getResult()).thenReturn(null, null, "taskResult");

    result[0] =
        Whitebox.invokeMethod(
            besClient, "runWebServiceAsOrganizationAdmin", "administrator", webServiceTask);

    verify(webServiceTask, times(3)).getResult();
    assertEquals("taskResult", result[0]);
  }

  @Test(expected = Exception.class)
  public void testRunWebServiceAsOrganizationAdminThrowsException() throws Exception {
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(appDataService);
    when(appDataService.loadOrgAdminCredentials(anyString())).thenReturn(credentials);
    when(webServiceTask.getResult()).thenReturn(new Exception());

    Whitebox.invokeMethod(
        besClient, "runWebServiceAsOrganizationAdmin", "administrator", webServiceTask);
  }
}
