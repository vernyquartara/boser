<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="main">
	<div>
		I tuoi indici:<br />
		<table border="1px solid grey">
			<thead>
				<tr>
					<th></th>
					<th>nome</th>
					<th>ultima indicizzazione</th>
					<th>stato</th>
					<th>ID configurazione</th>
					<th>ID crawler</th>
					
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="element in indexes">
					<td><span ng-click="toggleCrawlerDetail(element.config.crawler)">&nbsp;+&nbsp;</span></td>
					<td>{{element.config.crawler.description}}</td>
					<td>{{element.whenStarted | date:'dd/MM/yyyy HH:mm'}}</td>
					<td>{{element.state}}</td>
					<td>{{element.config.id}}</td>
					<td>{{element.config.crawler.id}}</td>
				</tr>
			</tbody>
		</table>
	</div>

	<div ng-show="crawlerDetailVisible">
		<p>dettaglio del crawler: {{selectedCrawler.description}}</p>
		<p>azioni: <a href="#"">avvia il crawler</a></p>
		<table border="1px solid grey">
			<thead>
				<tr>
					<!-- <th></th> -->
					<th>configurazione di ricerca</th>
					<th>ultima ricerca effettuata in data</th>
					<th>risultati</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="element in searches">
					<!-- <td><span ng-click="toggleIndexDetail(element)">&nbsp;+&nbsp;</span></td> -->
					<td>
						<a href="<c:url value='/index.jsp#/search/{{element.config.crawler.id}}'/>">
							{{element.config.crawler.description}}
						</a>
					</td>
					<td>{{element.timestamp | date:'dd/MM/yyyy HH:mm.ss'}}</td>
					<td><a href="<c:url value="/searchDownload?searchId={{element.id}}"/>">{{element.zipFilePath}}</a></td>
				</tr>
			</tbody>
		</table>
	</div>

	<%-- <div ng-show="searchConfigDetailVisible">
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
	</div> --%>

</div>

<hr />
<a href="<c:url value="/index.jsp#/home"/>">home page</a>
</body>
</html>