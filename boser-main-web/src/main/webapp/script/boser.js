var boserApp = angular.module("Boser", ['ngRoute']);
 
boserApp.config(function($routeProvider) {
    $routeProvider
        .when('/home', {
        	templateUrl: 'crawler.jsp',
        	controller: 'CrawlerCtrl'
        })
        .when('/search/:selected', {
        	templateUrl: 'search.jsp',
        	controller: 'SearchCtrl'
        })
        .otherwise({
            redirectTo: '/home'
        });
});
 

boserApp.controller('CrawlerCtrl', ['$scope','$http',function (scope, http) {
	var findCrawlersUrl = "/crawler";
	http.get(findCrawlersUrl).success(function(data) {
		console.log('data received: '+data);
		scope.crawlers = data;
	})
	
	var findSearchConfigByCrawlerUrl = "/searchConfig/crawler/1";
	http.get(findSearchConfigByCrawlerUrl).success(function(data) {
		console.log('data received: '+data);
		scope.searchConfigs = data;
	})
	
	scope.crawlerDetailVisible = false;
	scope.toggleCrawlerDetail = function(element) {
		scope.crawlerDetailVisible = !scope.crawlerDetailVisible;
		if (scope.crawlerDetailVisible) {
			console.log('mostro dettaglio id='+element.id);
			scope.selectedCrawler = element;
		} else {
			console.log('nascondo dettaglio');
			scope.selectedCrawler = null;
		}
	};
	
	scope.searchConfigDetailVisible = false;
	scope.toggleSearchConfigDetail = function(element) {
		scope.searchConfigDetailVisible = !scope.searchConfigDetailVisible;
		if (scope.searchConfigDetailVisible) {
			console.log('mostro dettaglio id='+element.id);
			scope.selectedSearchConfig = element;
		} else {
			console.log('nascondo dettaglio');
			scope.selectedSearchConfig = null;
		}
	};
	
	var addNewSearchKeyUrl = "/searchKey";
	scope.addSearchKey = function(newItem) {
		console.log(newItem);
		http.put(addNewSearchKeyUrl).success(function(data) {
			console.log('data received: '+data);
			scope.searches = data;
		});
	};
}]);

boserApp.controller('SearchCtrl', ['$scope','$http','$routeParams',function (scope, http, routeParams) {
	console.log(routeParams);
	var findSearchByConfigUrl = "/search/searchConfig/"+routeParams.selected;
	http.get(findSearchByConfigUrl).success(function(data) {
		console.log('data received: '+data);
		scope.searches = data;
	})
}]);