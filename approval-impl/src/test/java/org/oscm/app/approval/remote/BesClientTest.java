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

import javax.jws.WebService;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BesClient.class, Service.class})
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
    // given
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(appDataService);
    when(appDataService.loadOrgAdminCredentials(anyString())).thenReturn(credentials);
    when(webServiceTask.getResult()).thenReturn(null, null, "taskResult");
    // when
    result[0] =
        Whitebox.invokeMethod(
            besClient, "runWebServiceAsOrganizationAdmin", "administrator", webServiceTask);
    // then
    verify(webServiceTask, times(3)).getResult();
    assertEquals("taskResult", result[0]);
  }

  @Test(expected = Exception.class)
  public void testRunWebServiceAsOrganizationAdminThrowsException() throws Exception {
    // given
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(appDataService);
    when(appDataService.loadOrgAdminCredentials(anyString())).thenReturn(credentials);
    when(webServiceTask.getResult()).thenReturn(new Exception());
    // when
    Whitebox.invokeMethod(
        besClient, "runWebServiceAsOrganizationAdmin", "administrator", webServiceTask);
  }

  @Test
  public void testGetWebservice() throws Exception {
    // given
    Service mockService = mock(Service.class);
    BindingProvider bindingProvider = mock(BindingProvider.class);
    Binding binding = mock(Binding.class);
    PowerMockito.mockStatic(Service.class);
    List<Handler> handlerList = new ArrayList<>();
    String webServiceWsdl = "http://oscm-example:3000/{SERVICE}?wsdl";
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(appDataService);
    when(appDataService.loadBesWebServiceWsdl()).thenReturn(webServiceWsdl);
    when(Service.create(any(URL.class), any())).thenReturn(mockService);
    when(mockService.getPort(any())).thenReturn(bindingProvider);
    when(bindingProvider.getBinding()).thenReturn(binding);
    when(binding.getHandlerChain()).thenReturn(handlerList);
    // when
    BindingProvider result =
        Whitebox.invokeMethod(besClient, "getWebservice", "user", "password", TestClass.class);
    // then
    assertEquals(bindingProvider, result);
  }

  @Test
  public void testGetWebserviceWithCreateNewList() throws Exception {
    // given
    Service mockService = mock(Service.class);
    BindingProvider bindingProvider = mock(BindingProvider.class);
    Binding binding = mock(Binding.class);
    PowerMockito.mockStatic(Service.class);
    List<Handler> handlerList = new ArrayList<>();
    String webServiceWsdl = "http://oscm-example:3000/{SERVICE}?wsdl";
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(appDataService);
    when(appDataService.loadBesWebServiceWsdl()).thenReturn(webServiceWsdl);
    when(Service.create(any(URL.class), any())).thenReturn(mockService);
    when(mockService.getPort(any())).thenReturn(bindingProvider);
    when(bindingProvider.getBinding()).thenReturn(binding);
    when(binding.getHandlerChain()).thenReturn(null);
    // when
    BindingProvider result =
        Whitebox.invokeMethod(besClient, "getWebservice", "user", "password", TestClass.class);
    // then
    assertEquals(bindingProvider, result);
  }

  @WebService(
      portName = "ApprovalPort",
      serviceName = "ApprovalService",
      targetNamespace = "http://oscm-approval.org/wsdl",
      endpointInterface = "org.oscm.approval")
  class TestClass {}
}
