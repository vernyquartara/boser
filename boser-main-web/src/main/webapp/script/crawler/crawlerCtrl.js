angular.module('Boser')
.controller('CrawlerCtrl', ['$scope','$http',function ($scope, $http) {
	/*
	 * controllo dei pannelli lista/avvia
	 */
	$scope.bntListActive = true;
	$scope.submenu = function(selected) {
		$scope.bntNewActive = (selected == 'new' && !$scope.bntNewActive);
		$scope.bntListActive = (selected == 'list' && !$scope.bntListActive);
	}
	/*
	 * inizializzazione lista
	 */
	$http({
		method: 'GET',
		url: 'rest/crawlRequest'
	}).success(function(result) {
		$scope.requests = result;
	});
	/*
	 * inizializzazione form
	 */
	//$scope.formData = {'indexConfigId': 1, 'sites':['http://www.quattroruote.it','http://www.alvolante.it']};
	$scope.formData = {'indexConfigId': 1};
	$http({
		method: 'GET',
		url: 'rest/indexConfig/1'
	}).success(function(result) {
		console.log(result);
		$scope.formData.sites = result.sites;
		$scope.formData.depth = result.depth;
		$scope.formData.topN = result.topN;
	});
	
	/*
	 * gestione siti
	 */
	//$scope.sites = ['http://www.quattroruote.it','http://www.alvolante.it'];
	$scope.addNewSite = function() {
		$http({
    		method: 'POST',
    		url: 'rest/indexConfig/site',
    		data: {'indexConfigId': $scope.formData.indexConfigId, 'url': $scope.newSite},
    		headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    	    transformRequest: function(obj) {
    	        var str = [];
    	        for(var p in obj)
    	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
    	        return str.join("&");
    	    }
    	}).success(function (response) {
    		/*
    		 * se tutto va bene si aggiorna la lista
    		 */
    		console.log("ok "+response);
    		$scope.formData.sites = response.sites;
    	}).error(function(data, status, headers, config, statusText) {
    		console.log("ko");
    	});
	}
	$scope.removeSite = function(siteId) {
        $http({
    		method: 'DELETE',
    		url: 'rest/indexConfig/'+$scope.formData.indexConfigId+'/site/'+siteId
    	}).success(function (response) {
    		/*
    		 * se tutto va bene si aggiorna la lista
    		 */
    		console.log("ok "+response);
    		$scope.formData.sites = response.sites;
    	}).error(function(data, status, headers, config, statusText) {
    		console.log("ko");
    	});
	}
	
	/*
	 * gestione inserimento crawl request
	 */
    $scope.processForm = function() {
    	console.log("submit!!");
    	$http({
    		method: 'POST',
    		url: 'rest/crawlRequest',
    		data: {'indexConfigId': $scope.formData.indexConfigId,
    			   'depth'	: $scope.formData.depth,
    			   'topN'	: $scope.formData.topN},
    		headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    	    transformRequest: function(obj) {
    	        var str = [];
    	        for(var p in obj)
    	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
    	        return str.join("&");
    	    }
    	}).then(function successCallback(response) {
    		console.log("ok");
    	}, function errorCallback(data, status, headers, config, statusText) {
    		console.log("ko");
    	});
    }
    
    
}]);