angular.module('Boser')
.controller('SearchCtrl', ['$scope','SearchSrv',function ($scope, SearchSrv) {
	$scope.bntListActive = true;
	$scope.submenu = function(selected) {
		$scope.bntNewActive = (selected == 'new' && !$scope.bntNewActive);
		$scope.bntListActive = (selected == 'list' && !$scope.bntListActive);
	}
	
	$scope.searchConfigId = 1;
	
	/*
	 * inizializzazione lista chiavi
	 */
	SearchSrv.getSearchConfigById(
			$scope.searchConfigId,
			function(response){
				$scope.keys = response.keys;
				for (i = 0; i < $scope.keys.length; i++) {
					var key = $scope.keys[i];
					$scope.editableKeys[key.id] = false;
				}
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
	);
	/*
	 * gestione chiavi
	 */
	$scope.addNewKey = function() {
		SearchSrv.insertKey(
			$scope.searchConfigId,
			$scope.newKey,
			function(response){
				$scope.keys = response.keys;
				$scope.newKey = '';
				$scope.keysForm.newKey.$setValidity("duplicate", true);
			},
			function(data, status, headers, config, statusText){
				console.log(data);
				$scope.keysForm.newKey.$setValidity("duplicate", false);
			}
		);
	}
	$scope.removeKey = function(keyId) {
		SearchSrv.deleteKey(
			$scope.searchConfigId,
			keyId,
			function(response){
				$scope.keys = response.keys;
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
		);
	}
	
	/*
	 * ricerca
	 */
	$scope.startSearch = function() {
		console.log("start search");
		
		SearchSrv.insertRequest(
			$scope.searchConfigId,
			function(response){
				$scope.submenu('list');
				$scope.getList();
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
		);
	}
	/*
	 * lista
	 */
	$scope.getList = function() {
		SearchSrv.getRequests(
			function(response){
				$scope.requests = response;
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
		);
	}
	$scope.getList();
	
	/*
	 * gestione edit singoli elementi
	 */
	$scope.editableKeys = [];
	$scope.editable = function(id) {
		//resituisce l'editabilità del singolo elemento
		return $scope.editableKeys[id];
	};
	/*
	 * al momento il problema è che questo scatta sempre, perché 
	 * l'input è interno a <li> - bisogna trovare il modo di non farlo scattare
	 */
	$scope.editKey = function(id, $event) {
		//modifica l'editabilità del singolo elemento
		console.log("devo modificare: "+id);
		//$scope.editableKeys[id] = !$scope.editableKeys[id];
		$scope.editableKeys[id] = true;
	};
	$scope.doEditKey = function($event, id) {
		var keyCode = $event.which || $event.keyCode;
		if (keyCode === 13) {
			console.log("ora modifico id "+id);
			var inputId = 'key'.concat(id);
			var inputVal = document.getElementById(inputId).value;
			console.log("ora modifico value "+inputVal);
			$scope.editableKeys[id] = false;
		}
	};
	$scope.undoEditKey = function($event, id) {
//		var inputId = 'key'.concat(id);
//		var input = document.getElementById(inputId);
//		input.visibility = hidden;
		$scope.editableKeys[id] = false;
	};
	$scope.dontSubmit = function(event) {
		var keyCode = event.which || event.keyCode;
		if (keyCode === 13) {
			event.preventDefault();
			return false;
		}
	}
}]);