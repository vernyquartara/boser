<!DOCTYPE html>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <head lang="en">
      <meta charset="utf-8">
      <title>BOSER</title>
 
      <script type="text/javascript" src="<c:url value="/script/angular.js"/>"></script>
      <script type="text/javascript" src="<c:url value="/script/angular-route.js"/>"></script>
      <script type="text/javascript" src="<c:url value="/script/boser.js"/>"></script>
    </head>
    <body>
 
      <div ng-app="Boser">
        <ng-view></ng-view>
      </div>
 
    </body>
</html>