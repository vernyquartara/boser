var boserApp = angular.module("Boser", ['ngRoute']);
 
boserApp.config(function($routeProvider) {
    $routeProvider
        .when('/home', {
        	templateUrl: 'mainMenu.jsp',
        	controller: 'BoserCtrl'
        })
        .when('/crawler', {
        	templateUrl: 'crawlerView.jsp',
        	controller: 'CrawlerCtrl'
        })
        .when('/search', {
        	templateUrl: 'searchView.jsp',
        	controller: 'SearchCtrl'
        })
        .otherwise({
            redirectTo: '/home'
        });
});


boserApp.controller('BoserCtrl', ['$scope','$http',function (scope, http) {
}]);
 

boserApp.controller('CrawlerCtrl', ['$scope','$http',function (scope, http) {
	scope.scopevar = true;
	scope.submenu = function(selected) {
		scope.bntNewActive = (selected == 'new' && !scope.bntNewActive);
		scope.bntListActive = (selected == 'list' && !scope.bntListActive);
	}
}]);

boserApp.controller('SearchCtrl', ['$scope','$http','$routeParams',function (scope, http, routeParams) {
	console.log(routeParams);
	var findSearchByConfigUrl = "/boser-main-web/rest/search/searchConfig/"+routeParams.selected;
	http.get(findSearchByConfigUrl).success(function(data) {
		console.log('data received: '+data);
		scope.searches = data;
		scope.searchConfigId = routeParams.selected;
	})
}]);