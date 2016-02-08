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
		<!-- form gestione chiavi -->
		<form class="form-horizontal" role="form" ng-submit="addNewKey()" name="keysForm">
			<input type="hidden" name="searchConfigId" ng-value="searchConfigId">
			<!-- elenco chiavi -->
			<div class="form-group">
				<label class="control-label col-sm-2" for="keys">Chiavi di ricerca:</label>
				<div class="col-sm-9">
					<span ng-if="keys.length == 0">Nessuna. Aggiungi almeno una chiave per effettuare le ricerche.</span>
					<ul class="list-group" id="keys" ng-if="keys.length > 0">
						<li class="list-group-item" ng-repeat="key in keys" ng-click="editKey(key.id, $event)">
						<!-- un elemento li per ogni chiave. modificabile. -->
						<ng-form name="updateKeyForm">
							<span ng-if="!editable(key.id)">{{key.terms.join(', ')}}</span>
							<div ng-if="editable(key.id)" ng-class="{'alert alert-danger':!updateKeyValid()}">
								<input type="text" id="key{{key.id}}" name="key" ng-keypress="dontSubmit($event)" value="{{key.terms.join(', ')}}" size="50">
								<button type="button" class="btn btn-primary btn-xs" ng-click="updateKey(key.id)">
									<span class="glyphicon glyphicon-ok"></span>
								</button>
								<button type="button" class="btn btn-primary btn-xs" ng-click="undoEditKey($event, key.id)">
									<span class="glyphicon glyphicon-remove"></span>
								</button>
								<span ng-if="!updateKeyValid()">Chiave non valida o gi&agrave; esistente.</span>
							</div>
						</ng-form>
						</li>
					</ul>
				</div>
				<div class="col-sm-1" style="padding-top: 5px;padding-bottom: 2px;" ng-repeat="key in keys">
					<button type="button" class="btn btn-primary btn-block" ng-click="removeKey(key.id)">
						<span class="glyphicon glyphicon-minus"></span>
					</button>
				</div>
			</div>
			<!-- testo istruzioni -->
			<div class="form-group">
				<label class="control-label col-sm-2"></label>
				<div class="col-sm-10">
					<p>Per aggiungere una nuova chiave di ricerca, scrivi nel campo sottostante e premi il pulsante +.</p>
					<p>Puoi aggiungere pi&ugrave; chiavi correlate fra loro usando la virgola (,) come separatore:
					i risultati per i gruppi di chiavi correlate saranno raggruppati.</p>
				</div>
			</div>
			<!-- campo di inserimento nuova chiave -->
			<div class="form-group" ng-class="{ 'has-error': keysForm.newKey.$touched && keysForm.newKey.$invalid }">
				<label class="control-label col-sm-2" for="newKey">Aggiungi chiave di ricerca:</label>
				<div class="col-sm-9">
					<input type="text" class="form-control" name="newKey" placeholder="chiave (o chiavi correlate)" ng-model="newKey">
				</div>
				<div class="col-sm-1">
					<button type="submit" class="btn btn-primary btn-block">
						<span class="glyphicon glyphicon-plus"></span>
					</button>
				</div>
				<div class="help-block col-sm-10" ng-messages="keysForm.newKey.$error" ng-if="keysForm.newKey.$invalid">
        			<p ng-message="duplicate">Hai gi&agrave; inserito questa chiave.</p>
      			</div>
			</div>
		</form>
		<!-- form di avvio ricerca -->
		<form class="form-horizontal" role="form" ng-submit="startSearch()">
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10" ng-if="keys.length > 0">
					<button type="submit" class="btn btn-primary">Esegui ricerca</button>
				</div>
			</div>
		</form>
	</div>
</div>

<!-- LISTA -->
<div class="row" ng-show="bntListActive">
	<caption>Ricerche effettuate</caption>
	<div class="table-responsive">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>id</th>
					<th>stato</th>
					<th>avviata il</th>
					<th>terminata il</th>
					<th>file</th>
					<th>crawler</th>
					<th>chiavi</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="req in requests | orderBy:search.timestamp:desc">
					<td>{{req.id}}</td><!-- request -->
					<td>{{req.state}}</td><!-- request -->
					<td>{{req.creationDate | date:'dd-MM-yy, HH:mm:ss'}}</td><!-- request -->
					<td>{{req.lastUpdate | date:'dd-MM-yy, HH:mm:ss'}}</td><!-- request -->
					<td><a href="<c:url value="/searchDownload"/>?searchId={{req.search.id}}">{{req.search.zipLabel}}</a></td><!-- search -->
					<td>{{req.searchConfig.crawler.description}}</td><!-- crawler -->
					<td><button type="button" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-plus"></span></button></td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

