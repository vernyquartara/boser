angular.module('Boser')
.controller('CrawlerCtrl', ['$scope','$http',function ($scope, $http) {
	$scope.bntListActive = true;
	$scope.submenu = function(selected) {
		$scope.bntNewActive = (selected == 'new' && !$scope.bntNewActive);
		$scope.bntListActive = (selected == 'list' && !$scope.bntListActive);
	}
	
	// create a blank object to hold our form information
    // $scope will allow this to pass between controller and view
    $scope.formData = {'indexConfigId': 1};

    // process the form
    $scope.processForm = function() {
    	console.log("submit!!");
    	$http({
    		method: 'POST',
    		url: 'rest/crawlRequest',
    		data: $scope.formData,
    		headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    	    transformRequest: function(obj) {
    	        var str = [];
    	        for(var p in obj)
    	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
    	        return str.join("&");
    	    }
    	}).then(function successCallback(response) {
    		// this callback will be called asynchronously
    		// when the response is available
    		console.log("ok");
    	}, function errorCallback(data, status, headers, config, statusText) {
    		// called asynchronously if an error occurs
    		// or server returns response with an error status.
    		console.log("ko");
    	});
    }
    
    
    /*
    $scope.processForm = function() {
    	console.log("submit!!");
    	$http({
    		method  : 'POST',
    		url     : '/crawlRequest',
    		data    : $.param($scope.formData),  // pass in data as strings
    		headers : { 'Content-Type': 'application/x-www-form-urlencoded' }  // set the headers so angular passing info as form data (not request payload)
    	})
		.success(function(data) {
			console.log(data);

		    if (!data.success) {
		    	// if not successful, bind errors to error variables
		    	$scope.depth = data.errors.name;
		    } else {
		    	// if successful, bind success message to message
		    	$scope.message = data.message;
		    }
		});
    };
    */
}]);