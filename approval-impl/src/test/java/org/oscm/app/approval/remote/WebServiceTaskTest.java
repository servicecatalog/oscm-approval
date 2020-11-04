/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 3 Nov 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.remote;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jws.WebService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BesClient.class, WebServiceTask.class})
public class WebServiceTaskTest {

  private WebServiceTask webServiceTask;
  private Object result;

  @Before
  public void setUp() {
    result = new Object();
    webServiceTask =
        PowerMockito.spy(
            new WebServiceTask(TestClass.class) {
              @Override
              public Object execute(Object service) throws Exception {
                return result;
              }
            });
    PowerMockito.mockStatic(BesClient.class);

    result = new Object[1];
  }

  @Test
  public void testSetWsdlUrl() {
    String webServiceWsdl = "http://oscm-example:3000/{SERVICE}?wsdl";

    webServiceTask.setWsdlUrl(webServiceWsdl);

    String wsdlUrl = Whitebox.getInternalState(webServiceTask, "wsdlUrl");
    assertEquals(webServiceWsdl, wsdlUrl);
  }

  @Test
  public void testSetAuthentication() {
    PasswordAuthentication pa = new PasswordAuthentication("username", "password");

    webServiceTask.setAuthentication(pa);

    PasswordAuthentication auth = Whitebox.getInternalState(webServiceTask, "pa");
    assertEquals("username", auth.getUserName());
  }

  @Test
  public void testRun() throws Exception {
    TestClass testClass = new TestClass();
    String webServiceWsdl = "http://oscm-example:3000/{SERVICE}?wsdl";
    PasswordAuthentication pa = new PasswordAuthentication("username", "password");

    webServiceTask.setAuthentication(pa);
    webServiceTask.setWsdlUrl(webServiceWsdl);
    PowerMockito.when(BesClient.getWebserviceIntern(anyString(), anyString(), anyString(), any()))
        .thenReturn(testClass);

    webServiceTask.run();

    assertEquals(result, webServiceTask.getResult());
  }

  @Test
  public void testRunThrowsException() {
    webServiceTask.run();

    assertNotEquals(result, webServiceTask.getResult());
  }

  @WebService(
      portName = "ApprovalPort",
      serviceName = "ApprovalService",
      targetNamespace = "http://oscm-approval.org/wsdl",
      endpointInterface = "org.oscm.approval")
  class TestClass {}
}
