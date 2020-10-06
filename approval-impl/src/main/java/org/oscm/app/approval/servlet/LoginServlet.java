package org.oscm.app.approval.servlet;

import org.oscm.app.approval.auth.User;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.intf.IdentityService;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
    name = "loginServlet",
    urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);
  private static final String LOGIN_PAGE = "/loginform.jsp";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String username = request.getParameter("username");
    String password = request.getParameter("password");

    try {
      IdentityService identityService =
          BesClient.getWebservice(username, password, IdentityService.class);

      VOUserDetails userDetails = identityService.getCurrentUserDetails();
      LOGGER.info(
          "USER_DETAILS:"
              + userDetails.getKey()
              + ", "
              + userDetails.getUserId()
              + ", "
              + userDetails.getOrganizationId());

      User user = User.builder().username("approver").build();
      request.getSession().setAttribute("user", user);
      response.sendRedirect(request.getContextPath());
      return;
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
    request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
  }
}
