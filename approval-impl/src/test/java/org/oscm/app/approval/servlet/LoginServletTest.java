package org.oscm.app.approval.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.approval.auth.User;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.intf.IdentityService;
import org.oscm.vo.VOUserDetails;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoginServlet.class, BesClient.class, User.class})
public class LoginServletTest {

  private LoginServlet loginServlet;
  private HttpServletRequest servletRequest;
  private HttpServletResponse servletResponse;
  private IdentityService identityService;
  private VOUserDetails userDetails;

  @Before
  public void setUp() {
    loginServlet = PowerMockito.spy(new LoginServlet());
    servletRequest = mock(HttpServletRequest.class);
    servletResponse = mock(HttpServletResponse.class);
    identityService = mock(IdentityService.class);
    userDetails = mock(VOUserDetails.class);
    PowerMockito.mockStatic(BesClient.class);
    PowerMockito.mockStatic(User.class);
  }

  @Test
  public void testDoGet() throws Exception {
    // given
    RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
    when(servletRequest.getRequestDispatcher("/loginform.jsp")).thenReturn(requestDispatcher);
    // when
    loginServlet.doGet(servletRequest, servletResponse);
    // then
    verify(requestDispatcher, times(1)).forward(servletRequest, servletResponse);
  }

  @Test
  public void testDoPost() throws Exception {
    // given
    User.UserBuilder userBuilder = mock(User.UserBuilder.class);
    HttpSession session = mock(HttpSession.class);
    when(servletRequest.getParameter("username")).thenReturn("username");
    when(servletRequest.getParameter("password")).thenReturn("password");
    PowerMockito.when(BesClient.getWebservice("username", "password", IdentityService.class))
        .thenReturn(identityService);
    when(identityService.getCurrentUserDetails()).thenReturn(userDetails);
    when(userDetails.getUserId()).thenReturn("User");
    when(userDetails.getKey()).thenReturn(1100L);
    when(userDetails.getOrganizationId()).thenReturn("User organization");
    PowerMockito.when(User.builder()).thenReturn(userBuilder);
    PowerMockito.when(userBuilder.userId(anyString())).thenReturn(userBuilder);
    PowerMockito.when(userBuilder.orgId(anyString())).thenReturn(userBuilder);
    PowerMockito.when(userBuilder.key(anyLong())).thenReturn(userBuilder);
    User user = mock(User.class);
    when(userBuilder.build()).thenReturn(user);
    when(servletRequest.getSession()).thenReturn(session);
    // when
    loginServlet.doPost(servletRequest, servletResponse);
    // then
    verify(session, times(1)).setAttribute("user", user);
    verify(servletResponse, times(1)).sendRedirect(null);
  }

  @Test(expected = Exception.class)
  public void testDoPostThrowsException() throws Exception {
    // given
    when(servletRequest.getParameter("username")).thenReturn("username");
    when(servletRequest.getParameter("password")).thenReturn("password");
    when(identityService.getCurrentUserDetails()).thenReturn(userDetails);
    // when
    loginServlet.doPost(servletRequest, servletResponse);
  }
}
