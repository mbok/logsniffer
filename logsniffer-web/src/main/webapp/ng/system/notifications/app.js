/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2016 Scaleborn UG, www.scaleborn.com
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
.module('SystemNotificationsModule',["ngSanitize"])
.controller(
	"SystemNotificationsController",
	[
	 '$scope',
	 '$http',
	 '$log',
	 'lsfAlerts',
	 '$sce',
	 function($scope, $http, $log, lsfAlerts, $sce) {
	     $scope.alerts = lsfAlerts.create();
	     $scope.bindErrors = {};
	     $scope.state =  {
		     busy: false
	     };
	     $scope.notifications = null;
		 $scope.settingsRessource = $scope.contextPath + "/c/system/notifications";	     

		 var initResponse = function (data) {
			$log.info("Update notifications: ", data)
     		$scope.notifications = data;
		 };

		 $scope.acknowledge = function(id) {
			 $http({
			     url : $scope.settingsRessource+"?id="+encodeURIComponent(id),
			     method : "POST"
			 })
			.success(
				function(data, status, headers, config) {
					if (data) {
						$scope.$emit('systemNotificationSummaryChanged', data);
					}
				    $log.info("Acknowledged notification", id);
				    $scope.state.busy = false;
				    $scope.loadSettings($scope).success(initResponse);
				}
			)
			.error(
				function(data, status, headers, config, statusText) {
					$scope.state.busy = false;
					$scope.alerts.httpError("Failed to acknowledge notification", data, status, headers, config, statusText);
				}
			);
		 };
		 
		 $scope.sanitizeMessage = function(n) {
			 return $sce.trustAsHtml(n.message);
		 };
		 
		 // Init
	     $scope.loadSettings($scope).success(initResponse);
	 }
	]
);