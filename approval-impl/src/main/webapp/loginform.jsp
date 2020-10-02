<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${pageContext.request.locale}" />
<fmt:setBundle basename="i18n.messages" />

<html>
<head>
<title><fmt:message key='login.title' /></title>
<link rel="stylesheet" type="text/css" href="approval.css">
</head>

<body>
  <div class="wrapper" style="text-align: center;">
    <div id="inner-left" class="block" style="width: 400px; height: 250px;">
      <br>
      <h4><fmt:message key='login.subtitle' /></h4>
      <br>
      <form action="/approval/login" method=post>
        <table style="border-spacing: 5px;">
          <tr>
            <td><fmt:message key='login.label.user' /></td>
            <td><input type="text" name="username" size="30"></td>
          </tr>
          <tr>
            <td><fmt:message key='login.label.password' /></td>
            <td><input type="password" size="30" name="password"></td>
          </tr>
          <tr>
            <td></td>
            <td><input type="submit" value="<fmt:message key='login.label.submit' />"></td>
          </tr>
        </table>
      </form>
    </div>
  </div>
</body>

</html>
