/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 2 Nov 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthenticationFilter.class})
public class AuthenticationFilterTest {

  private AuthenticationFilter authenticationFilter;
  private HttpSession session;
  private HttpServletRequest httpRequest;
  private HttpServletResponse httpResponse;
  private FilterChain filterChain;

  @Before
  public void setUp() {
    authenticationFilter = PowerMockito.spy(new AuthenticationFilter());
    session = mock(HttpSession.class);
    httpRequest = mock(HttpServletRequest.class);
    httpResponse = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);
  }

  @Test
  public void testDoFilter() throws IOException, ServletException {
    // given
    when(httpRequest.getSession(anyBoolean())).thenReturn(session);
    when(session.getAttribute(anyString())).thenReturn("Jola");
    // when
    authenticationFilter.doFilter(httpRequest, httpResponse, filterChain);
    // then
    verify(filterChain, times(1)).doFilter(any(), any());
  }

  @Test
  public void testDoFilterRedirect() throws IOException, ServletException {
    // when
    authenticationFilter.doFilter(httpRequest, httpResponse, filterChain);
    // then
    verify(httpResponse, times(1)).sendRedirect(anyString());
  }
}
