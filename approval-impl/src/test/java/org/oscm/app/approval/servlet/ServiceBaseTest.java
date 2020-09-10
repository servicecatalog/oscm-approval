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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscm.app.approval.servlet.ServiceBase;
import org.oscm.app.approval.servlet.ServiceResult;

/** @author worf */
@RunWith(MockitoJUnitRunner.class)
public class ServiceBaseTest {

  @Mock HttpServletRequest req;
  @Mock HttpServletResponse res;
  @Mock
  ServiceResult service;
  @Spy
  ServiceBase base;

  @SuppressWarnings("deprecation")
  @Test
  public void testSplitPath() {
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
  public void testGetRequestString_WIthoutPathAndQuery() {
    // give
    when(req.getMethod()).thenReturn("method");

    // when
    String result = base.getRequestString(req);
    // then
    assertEquals("method /", result);
  }

  @Test
  public void testGetRequestString_WithoutQuery() {
    // give
    when(req.getMethod()).thenReturn("method");
    when(req.getPathInfo()).thenReturn("path");

    // when
    String result = base.getRequestString(req);
    // then
    assertEquals("method path", result);
  }

  @Test
  public void testGetRequestString() {
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
  public void testHandleServiceCall() throws Exception {
    // given
    doReturn(service).when(base).createServiceResult();
    doNothing().when(service).sendResult(any());
    when(req.getMethod()).thenReturn("method");
    when(req.getParameterMap()).thenReturn(new HashMap<String, String[]>());
    when(req.getPathInfo()).thenReturn("path");
    when(req.getQueryString()).thenReturn("string");
    // when
    base.doPut(req, res);
    // then
    verify(service, times(1)).sendResult(any());
  }
}
