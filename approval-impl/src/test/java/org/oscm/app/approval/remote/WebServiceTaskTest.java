/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 26-11-2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import javax.jws.WebService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

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
    // given
    String webServiceWsdl = "http://oscm-example:3000/{SERVICE}?wsdl";
    // when
    webServiceTask.setWsdlUrl(webServiceWsdl);
    // then
    String wsdlUrl = Whitebox.getInternalState(webServiceTask, "wsdlUrl");
    assertEquals(webServiceWsdl, wsdlUrl);
  }

  @Test
  public void testSetAuthentication() {
    // given
    PasswordAuthentication pa = new PasswordAuthentication("username", "password");
    // when
    webServiceTask.setAuthentication(pa);
    // then
    PasswordAuthentication auth = Whitebox.getInternalState(webServiceTask, "pa");
    assertEquals("username", auth.getUserName());
  }

  @Test
  public void testRun() throws Exception {
    // given
    TestClass testClass = new TestClass();
    String webServiceWsdl = "http://oscm-example:3000/{SERVICE}?wsdl";
    PasswordAuthentication pa = new PasswordAuthentication("username", "password");

    webServiceTask.setAuthentication(pa);
    webServiceTask.setWsdlUrl(webServiceWsdl);
    PowerMockito.when(BesClient.getWebserviceIntern(anyString(), anyString(), anyString(), any()))
        .thenReturn(testClass);
    // when
    webServiceTask.run();
    // then
    assertEquals(result, webServiceTask.getResult());
  }

  @Test
  public void testRunThrowsException() {
    // when
    webServiceTask.run();
    // then
    assertNotEquals(result, webServiceTask.getResult());
  }

  @WebService(
      portName = "ApprovalPort",
      serviceName = "ApprovalService",
      targetNamespace = "http://oscm-approval.org/wsdl",
      endpointInterface = "org.oscm.approval")
  class TestClass {}
}
