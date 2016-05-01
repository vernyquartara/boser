<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<div class="row submenu">
	<div class="col-md-6 col-md-offset-4 btn-group btn-group-justified">
		<div>
			<button type="button" class="btn btn-default" ng-class="{'active':bntListActive}" ng-click="submenu('list')">Situazione attuale</button>
			<button type="button" class="btn btn-default" ng-class="{'active':bntNewActive}" ng-click="submenu('new')">Avvia il crawler</button>
		</div>
	</div>
</div>


<!-- AVVIO -->
<div class="row" ng-show="bntNewActive">
	<div class="col-md-12">
		<form class="form-horizontal" role="form" ng-submit="addNewSite()">
			<div class="form-group">
				<label class="control-label col-sm-2" for="email">Fonti:</label>
				<div class="col-sm-9">
					<ul class="list-group">
						<li class="list-group-item" ng-repeat="site in formData.sites">{{site.url}}</li>
					</ul>
				</div>
				<div class="col-sm-1" style="padding-top: 5px;padding-bottom: 2px;"  ng-repeat="site in formData.sites">
					<button type="button" class="btn btn-primary btn-block" ng-click="removeSite(site.id)">
						<span class="glyphicon glyphicon-minus"></span>
					</button>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="newSite">Aggiungi fonte:</label>
				<div class="col-sm-9">
					<input type="url" class="form-control" id="newSite"	placeholder="http://site-to-crawl.com" ng-model="newSite">
				</div>
				<div class="col-sm-1">
					<button type="submit" class="btn btn-primary btn-block"><span class="glyphicon glyphicon-plus"></span></button>
				</div>
			</div>
		</form>
		<form class="form-horizontal" role="form" ng-submit="processForm()">
			<input type="hidden" name="indexConfigId" value="1" ng-value="formData.indexConfigId">
			<div class="form-group">
				<label class="control-label col-sm-2" for="email">Depth:</label>
				<div class="col-sm-10">
					<input type="number" class="form-control" id="email" placeholder="Enter depth" ng-model="formData.depth">
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="pwd">TopN:</label>
				<div class="col-sm-10">
					<input type="number" class="form-control" id="pwd" placeholder="Enter topN" ng-model="formData.topN" disabled="disabled">
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="submit" class="btn btn-primary">Avvia</button>
				</div>
			</div>
		</form>
	</div>
	<!-- <pre>{{formData}}</pre> -->
</div>

<!-- LISTA -->
<div class="row" ng-show="bntListActive">
	<caption>Indicizzazioni eseguite</caption>
	<div class="table-responsive">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>id</th>
					<th>stato</th>
					<th>richiesto il</th>
					<th>ultimo aggiornamento</th>
					<th>depth</th>
					<th>topN</th>
					<!-- <th>crawler</th> -->
					<th>fonti</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="req in requests | orderBy:'id':true">
					<td>{{req.id}}</td><!-- index -->
					<td>{{req.state}}</td><!-- index -->
					<td>{{req.creationDate | date:'dd-MM-yy, HH:mm:ss'}}</td><!-- index -->
					<td>{{req.lastUpdate | date:'dd-MM-yy, HH:mm:ss'}}</td><!-- index -->
					<td>{{req.index.depth}}</td><!-- IndexConfig -->
					<td>{{req.index.topN}}</td><!-- IndexConfig -->
					<!-- <td>{{req.index.crawler.description}}</td> --><!-- crawler -->
					<td><button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-plus"></span></button></td><!-- IndexConfig -->
				</tr>
			</tbody>
		</table>
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

