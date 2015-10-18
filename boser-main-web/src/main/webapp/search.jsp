<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div>
	<p>Ricerche effettuate per la configurazione con id={{configId}}:</p>
	<table border="1px solid grey">
			<thead>
				<tr>
					<th></th>
					<th>data</th>
					<th>file</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="element in searches">
					<td><span ng-click="toggleIndexDetail(element)">&nbsp;+&nbsp;</span></td>
					<td>{{element.timestamp | date:'dd/MM/yyyy HH:mm.ss'}}</td>
					<td><a href="<c:url value="/searchDownload?searchId={{element.id}}"/>">{{element.zipFilePath}}</a></td>
				</tr>
			</tbody>
		</table>
</div>
<form action="<c:url value='/search'/>">
	<input type="hidden" value="{{configId}}"/>
	<input type="submit" value="nuova ricerca" />
</form>
<hr />
<a href="<c:url value="/index.jsp#/home"/>">home page</a>