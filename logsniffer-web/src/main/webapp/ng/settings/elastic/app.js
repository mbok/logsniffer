/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
angular
.module('SettingsElasticModule',[])
.controller(
	"SettingsElasticController",
	[
	 '$scope',
	 '$http',
	 '$log',
	 'lsfAlerts',
	 function($scope, $http, $log, lsfAlerts) {
	     $scope.alerts = lsfAlerts.create();
	     $scope.bindErrors = {};
	     $scope.state =  {
		     busy: false
	     };
	     $scope.settings = null;
	     $scope.status = null;
		 $scope.settingsRessource = $scope.contextPath + "/c/settings/elastic";	     

		 var initResponse = function (data) {
			$log.info("Update ES settings and status: ", data)
     		$scope.settings = data.settings;
     		$scope.status = data;
     		if (!$scope.settings.remoteAddresses) {
     			$scope.settings.remoteAddresses = [
     			   {
     				   host: "localhost",
     				   port: 9300
     			   }
     			];
     		}
		 };
		 
		 $scope.saveEsSettings = function() {
			 $scope.$parent.saveSettings($scope, $scope.settings).
			 success(function (data) {
				 initResponse(data);
			 });
		 };
		 
		 $scope.addNode = function() {
			 if (!$scope.settings.remoteAddresses) {
				 $scope.settings.remoteAddresses = [];
			 }
			 if ($scope.settings.remoteAddresses.length==0) {
				 $scope.settings.remoteAddresses.push({
 				   host: "localhost",
 				   port: 9300
 			   });
			 } else {
				 $scope.settings.remoteAddresses.push({});
			 }
		 };
		 $scope.deleteNode = function(i) {
			 $scope.settings.remoteAddresses.splice(i, 1);
		 }
		 
		 // Init
	     $scope.loadSettings($scope)
	     	.success(function(data) {
	     		initResponse(data);
	     	});
	 }
	]
);