angular.module('Boser')
.controller('CrawlerCtrl', ['$scope','$http',function (scope, http) {
	scope.bntListActive = true;
	scope.submenu = function(selected) {
		scope.bntNewActive = (selected == 'new' && !scope.bntNewActive);
		scope.bntListActive = (selected == 'list' && !scope.bntListActive);
	}
}]);