/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 4 Nov 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.approval.util.JsonResult;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceResult.class})
public class ServiceResultTest {

  private ServiceResult serviceResult;
  private HttpServletResponse response;
  private PrintWriter printWriter;

  @Before
  public void setUp() {
    serviceResult = spy(new ServiceResult());
    response = mock(HttpServletResponse.class);
    printWriter = mock(PrintWriter.class);
  }

  @Test
  public void testSetErrorWithThrowable() {
    // given
    serviceResult.setError(404, new Throwable("Test exception"));
    // when
    int httpCode = Whitebox.getInternalState(serviceResult, "httpStatus");
    // then
    assertEquals(404, httpCode);
  }

  @Test
  public void testSetErrorWithMessage() {
    // given
    serviceResult.setError(404, "Test exception");
    // when
    JsonResult json = Whitebox.getInternalState(serviceResult, "jsonResult");
    // then
    assertEquals("{\"status\":\"Test exception\"}", json.getJson());
  }

  @Test
  public void testSetStatus() {
    // given
    serviceResult.setStatus(HttpServletResponse.SC_CONFLICT);
    // when
    int status = Whitebox.getInternalState(serviceResult, "httpStatus");
    // then
    assertEquals(HttpServletResponse.SC_CONFLICT, status);
  }

  @Test
  public void testSetHTMLOutput() {
    // given
    serviceResult = spy(new ServiceResult());
    serviceResult.setHTMLOutput("Test http output");
    // when
    String output = Whitebox.getInternalState(serviceResult, "htmlOutput");
    // then
    assertEquals("Test http output", output);
  }

  @Test
  public void testSendResultJSON() throws Exception {
    // given
    serviceResult = spy(new ServiceResult());
    serviceResult.setError(404, "Test exception");
    when(response.getWriter()).thenReturn(printWriter);
    serviceResult.sendResult(response);
    // when
    JsonResult json = Whitebox.getInternalState(serviceResult, "jsonResult");
    // then
    assertEquals("{\"status\":\"Test exception\"}", json.getJson());
    verify(response, times(2)).setHeader(anyString(), anyString());
  }

  @Test
  public void testSendResultHTML() throws Exception {
    // given
    serviceResult.setHTMLOutput("Test http output");
    when(response.getWriter()).thenReturn(printWriter);
    serviceResult.sendResult(response);
    // when
    String output = Whitebox.getInternalState(serviceResult, "htmlOutput");
    // then
    assertEquals("Test http output", output);
    verify(response, times(1)).setContentType(anyString());
  }
}
