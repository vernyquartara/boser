<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<div class="row submenu">
	<div class="col-md-6 col-md-offset-4 btn-group btn-group-justified">
		<div>
			<button type="button" class="btn btn-default" ng-class="{'active':bntListActive}" ng-click="submenu('list')">Situazione attuale</button>
			<button type="button" class="btn btn-default" ng-class="{'active':bntNewActive}" ng-click="submenu('new')">Esegui una ricerca</button>
		</div>
	</div>
</div>


<!-- AVVIO -->
<div class="row" ng-show="bntNewActive">
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
					<input type="url" class="form-control" id="newSite"	placeholder="http://site-to-crawl.com">
				</div>
				<div class="col-sm-1">
					<button type="button" class="btn btn-primary btn-block"><span class="glyphicon glyphicon-plus"></span></button>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="email">Depth:</label>
				<div class="col-sm-10">
					<input type="email" class="form-control" id="email"
						placeholder="Enter depth">
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="pwd">TopN:</label>
				<div class="col-sm-10">
					<input type="password" class="form-control" id="pwd"
						placeholder="Enter topN">
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

<!-- LISTA -->
<div class="row" ng-show="bntListActive">
	<div class="table-responsive">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>id</th>
					<th>stato</th>
					<th>avviato il</th>
					<th>terminato il</th>
					<th>depth</th>
					<th>topN</th>
					<th>crawler</th>
					<th>fonti</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>1</td>
					<td>SUCCESS</td>
					<td>2015-11-04</td>
					<td>2015-11-05</td>
					<td>10</td>
					<td>5000</td>
					<td>settimanale</td>
					<td><button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-plus"></span></button></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

