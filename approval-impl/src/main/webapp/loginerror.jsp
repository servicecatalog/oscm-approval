<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${pageContext.request.locale}" />
<fmt:setBundle basename="i18n.messages" />
<html>
<head>
<title>Login Error</title>
<link rel="stylesheet" type="text/css" href="approval.css">
</head>
<body>
  <div class="wrapper" style="text-align: center;">
    <div id="inner-left" class="block" style="width: 450px; height: 250px;">
      <br>
      <h4>
        <fmt:message key='error.page.title' />
      </h4>
      <br>
      <p>
        <fmt:message key='error.page.text' />
        <a href="loginform.jsp"><fmt:message key='error.page.link' /></a>
      </p>
    </div>
  </div>
</body>
</html>
