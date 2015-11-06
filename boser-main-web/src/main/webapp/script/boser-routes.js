angular.module('Boser')
.config(function($routeProvider) {
    $routeProvider
        .when('/home', {
        	templateUrl: 'mainMenu.jsp'
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