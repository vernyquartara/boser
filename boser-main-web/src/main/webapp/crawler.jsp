<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="main">
	<div>
		I tuoi crawlers:<br /> filtra per valore: <input type="text"
			ng-model="query" />
		<table border="1px solid grey">
			<thead>
				<tr>
					<th></th>
					<th>nome</th>
					<th>creato il</th>
					<th>ultima modifica</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="element in crawlers | filter:query">
					<td><span ng-click="toggleCrawlerDetail(element)">&nbsp;+&nbsp;</span></td>
					<td>{{element.description}}</td>
					<td>{{element.creationDate | date:'dd/MM/yyyy HH:mm'}}</td>
					<td>{{element.lastUpdate | date:'dd/MM/yyyy HH:mm:ss'}}</td>
				</tr>
			</tbody>
		</table>
	</div>

	<div ng-show="crawlerDetailVisible">
		<p>dettaglio del crawler: {{selectedCrawler.description}}</p>
		<p>Configurazioni di ricerca:</p>
		<table border="1px solid grey">
			<thead>
				<tr>
					<th></th>
					<th>nome</th>
					<th>creato il</th>
					<th>ultima modifica</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="element in searchConfigs">
					<td><span ng-click="toggleSearchConfigDetail(element)">&nbsp;+&nbsp;</span></td>
					<td>{{element.description}}</td>
					<td>{{element.creationDate | date:'dd/MM/yyyy HH:mm'}}</td>
					<td>{{element.lastUpdate | date:'dd/MM/yyyy HH:mm:ss'}}</td>
				</tr>
			</tbody>
		</table>
	</div>

	<div ng-show="searchConfigDetailVisible">
		<p>
			configurazione di ricerca: {{selectedSearchConfig.description}} <a
				href="<c:url value="/index.jsp#/search/{{selectedSearchConfig.id}}"/>">vedi risultati di
				ricerca</a>
		</p>
		<p>parole chiave da cercare:</p>
		<table border="1px solid grey">
			<thead>
				<tr>
					<th>testo</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="element in selectedSearchConfig.keys">
					<td>{{element.text}}</td>
					<td><span>&nbsp;X&nbsp;</span></td>
				</tr>
			</tbody>
		</table>
		<p><input type="text" ng-model="newItem"/><button ng-click="addSearchKey(newItem)">Aggiungi</button></p>
	</div>

</div>

<hr />
<a href="<c:url value="/index.jsp#/home"/>">home page</a>
</body>
</html>