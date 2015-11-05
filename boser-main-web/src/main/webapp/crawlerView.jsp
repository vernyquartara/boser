<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<div class="row submenu">
	<div class="col-md-6 col-md-offset-4 btn-group btn-group-justified">
		<!-- <div class="btn-group btn-group-justified"> -->
		<div>
			<button type="button" class="btn btn-default" ng-class="{'active':bntNewActive}" ng-click="submenu('new')">Avvia il crawler</button>
			<button type="button" class="btn btn-default" ng-class="{'active':bntListActive}" ng-click="submenu('list')">Controlla la situazione</button>
		</div>
	</div>
</div>


<!-- AVVIO -->
<div class="row">
	<div class="col-md-12">
		<form class="form-horizontal" role="form">
			<div class="form-group">
				<label class="control-label col-sm-2" for="email">Fonti:</label>
				<div class="col-sm-10">
					<ul class="list-group">
						<li class="list-group-item">www.abc.com<a href="#"><span class="glyphicon glyphicon-minus pull-right"></span></a></li>
						<li class="list-group-item">http://www.abc.com<a href="#"><span class="glyphicon glyphicon-minus pull-right"></span></a></li>
						<li class="list-group-item">www.xyz.com<a href="#"><span class="glyphicon glyphicon-minus pull-right"></span></a></li>
					</ul>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="email">Aggiungi fonte:</label>
				<div class="col-sm-9">
					<input type="url" class="form-control" id="newSite"	placeholder="www.siteToCrawl.com">
				</div>
				<div class="col-sm-1">
					<button type="button" class="btn btn-primary btn-block"><span class="glyphicon glyphicon-plus"></span></button>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="email">Depth:</label>
				<div class="col-sm-10">
					<input type="email" class="form-control" id="email"
						placeholder="Enter email">
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="pwd">TopN:</label>
				<div class="col-sm-10">
					<input type="password" class="form-control" id="pwd"
						placeholder="Enter password">
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="submit" class="btn btn-primary">Avvia</button>
				</div>
			</div>
		</form>
	</div>
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

