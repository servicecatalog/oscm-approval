/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 01 Oct 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/index.jsp", "/task/*"})
public class AuthenticationFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
  private static final String LOGIN_URL = "/login";

  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    HttpSession session = httpRequest.getSession(false);
    boolean loggedIn = session != null && session.getAttribute("user") != null;

    if (loggedIn) {
      chain.doFilter(request, response);
    } else {
      httpResponse.sendRedirect(httpRequest.getContextPath() + LOGIN_URL);
    }
  }

  @Override
  public void destroy() {}
}
