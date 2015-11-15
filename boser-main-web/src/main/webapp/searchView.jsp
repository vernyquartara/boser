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
		<form class="form-horizontal" role="form" ng-submit="addNewKey()">
			<input type="hidden" name="searchConfigId" ng-value="searchConfigId">
			<div class="form-group">
				<label class="control-label col-sm-2" for="keys">Chiavi:</label>
				<div class="col-sm-9">
					<ul class="list-group" id="keys">
						<li class="list-group-item" ng-repeat="key in keys">{{key.text}}-{{key.parent.text}}</li>
					</ul>
				</div>
				<div class="col-sm-1" style="padding-top: 5px;"  ng-repeat="key in keys">
					<button type="button" class="btn btn-primary btn-block" ng-click="removeKey(key.id)">
						<span class="glyphicon glyphicon-minus"></span>
					</button>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2"></label>
				<div class="col-sm-10">
					<p>per aggiungere una nuova chiave di ricerca, scrivi nel campo sottostante e premi il pulsante +.
					puoi aggiungere più chiavi correlate fra loro usando il punto e virgola (;) come separatore:
					i risultati per i gruppi di chiavi correlate saranno raggruppati.</p>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label col-sm-2" for="newKey">Aggiungi chiave:</label>
				<div class="col-sm-9">
					<input type="text" class="form-control" name="newKey" placeholder="chiave (o chiavi correlate)" ng-model="newKey">
				</div>
				<div class="col-sm-1">
					<button type="submit" class="btn btn-primary btn-block"><span class="glyphicon glyphicon-plus"></span></button>
				</div>
			</div>
		</form>
		<form class="form-horizontal" role="form" ng-submit="startSearch()">
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="submit" class="btn btn-primary">Esegui ricerca</button>
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
					<th>effettuata il</th>
					<th>crawler</th>
					<th>chiavi</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>1</td>
					<td>2015-11-04</td>
					<td>settimanale</td>
					<td><button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-plus"></span></button></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

