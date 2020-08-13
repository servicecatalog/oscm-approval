/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 13 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** @author worf */
public class ServiceBaseTest {

  @Mock HttpServletRequest req;
  @Mock HttpServletResponse res;
  @Mock ServletConfig servletConfig;
  @Mock ServletOutputStream outputStream;
  @Mock ServiceResult service;
  @Spy ServiceBase base;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void TestSplitPath() {
    // given
    String[] expected = {
      "https:", "www.fujitsu.com", "de", "products", "software", "enterprise-catalogmgr"
    };
    String testPah = "https://www.fujitsu.com/de/products/software/enterprise-catalogmgr/";
    // when
    String[] result = base.splitPath(testPah);
    // then
    assertEquals(expected, result);
  }

  @Test
  public void TestGetRequestString_WIthoutPathAndQuery() {
    // give
    when(req.getMethod()).thenReturn("method");

    // when
    String result = base.getRequestString(req);
    // then
    assertEquals("method /", result);
  }

  @Test
  public void TestGetRequestString_WithoutQuery() {
    // give
    when(req.getMethod()).thenReturn("method");
    when(req.getPathInfo()).thenReturn("path");

    // when
    String result = base.getRequestString(req);
    // then
    assertEquals("method path", result);
  }

  @Test
  public void TestGetRequestString() {
    // give
    when(req.getMethod()).thenReturn("method");
    when(req.getPathInfo()).thenReturn("path");
    when(req.getQueryString()).thenReturn("string");

    // when
    String result = base.getRequestString(req);
    // then
    assertEquals("method path Q:string", result);
  }

  @Test
  public void TestHandleServiceCall() throws Exception {
    // given
    doReturn(service).when(base).doService(any(), any(), anyString());
    doNothing().when(service).sendResult(any());
    when(req.getMethod()).thenReturn("method");
    when(req.getPathInfo()).thenReturn("path");
    when(req.getQueryString()).thenReturn("string");
    // when
    base.doPut(req, res);
    // then
    verify(service, times(1)).sendResult(any());
  }
}
