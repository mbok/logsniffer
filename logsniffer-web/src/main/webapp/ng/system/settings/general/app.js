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
.module('SystemSettingsGeneralModule',[])
.controller(
	"SystemSettingsGeneralController",
	[
	 '$scope',
	 '$http',
	 '$log',
	 'lsfAlerts',
	 function($scope, $http, $log, lsfAlerts) {
	     $scope.alerts = lsfAlerts.create();
	     $scope.busy = false;
	     $scope.bindErrors = {};
	     $scope.state =  {
		     httpProxyEnabled: false
	     };
	     
	     $scope.loadSettings = function() {
		 $scope.state.busy = true;
		 $http({
		     url : $scope.contextPath + "/c/system/settings/general",
		     method : "GET"
		 })
		.success(
			function(data, status, headers, config) {
			    $scope.settings = data;
			    $log.info("Settings loaded", data);
			    if (!$scope.settings.httpProxy || !$scope.settings.httpProxy.host) {
				$scope.settings.httpProxy = null;
				$scope.state.httpProxyEnabled = false;
			    } else {
				$scope.state.httpProxyEnabled = true;
			    }
			    $scope.state.busy = false;
			}
		)
		.error(
			function(data, status, headers, config, statusText) {
			    $scope.state.busy = false;
			    $scope.alerts.httpError("Failed to load settings", data, status, headers, config, statusText);
			}
		);
	     };
	     
	     $scope.save = function() {
		$scope.state.busy = true;
		$scope.alerts.clear();
		if (!$scope.state.httpProxyEnabled) {
		    $scope.settings.httpProxy = null;
		}
		$log.info("Saving settings", $scope.settings);
		 $http({
		     url : $scope.contextPath + "/c/system/settings/general",
		     method : "POST",
		     data: $scope.settings
		 })
		.success(
			function(data, status, headers, config) {
			    $log.info("Settings saved");
			    $scope.state.busy = false;
			    $scope.alerts.success("Changes applied successfully");
			}
		)
		.error(
			function(data, status, headers, config, statusText) {
			    $scope.state.busy = false;
			    $scope.alerts.httpError("Failed to save settings", data, status, headers, config, statusText);
			    if (data && data.bindErrors) {
			    	$scope.bindErrors = data.bindErrors;
			    }
			}
		);
	     };
	     
	     // Init
	     $scope.loadSettings();
	 }
	]
);