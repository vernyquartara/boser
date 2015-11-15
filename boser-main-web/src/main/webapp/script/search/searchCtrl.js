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
			},
			function(data, status, headers, config, statusText){
				console.log(data);
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
	}
}]);