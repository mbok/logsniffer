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
.module('lsfReaderTestModule',[])
.controller(
	"ReaderTestCtrl",
	['$scope', '$http', '$log', 'usSpinnerService', '$modalInstance', '$timeout', 'lsfAlerts', 'title', 'source', 'testSession',
	function($scope, $http, $log, usSpinnerService, $modalInstance, $timeout, lsfAlerts, title, source, testSession) {
	    $scope.alerts = lsfAlerts.create();
	    $scope.busy = false;
	    $scope.title = title;
	    $scope.source = angular.copy(source);
	    $scope.source.id = null;
	    $scope.logs = [];
	    $scope.settings = testSession;
	    if (typeof $scope.settings.testLog == "undefined") {
		$scope.settings.testLog = null;
	    }
	    if (typeof $scope.settings.logPointer == "undefined") {
		$scope.settings.logPointer = {};
	    }
	    $scope.viwerInitialized = false;
	    
	    $scope.close = function () {
		$modalInstance.close();
	    };
	    
	    $scope.logViewerErrorHandler = function () {
		$scope.alerts.error("Please check the log source and reader configuration to fix below errors!");
	    };
	    
	    $scope.loadLogs = function() {
		$scope.busy = true;
		$log.info("Resolving logs for source: ", $scope.source);
		$http({
        		url : $scope.contextPath + "/c/sources/logs",
        		method : "POST",
        		data: $scope.source
        	})
        	.success(
        		function(data, status, headers, config) {
        		    $scope.busy = false;
        		    $scope.logs = data;
        		    $log.info("Logs loaded: ", $scope.logs);
        		})
        	.error(
        		function(data, status, headers, config, statusText) {
        		    $scope.busy = false;
        		    $scope.alerts.httpError("Failed to resolve logs, please check the log source configuration", data, status, headers, config, statusText);
        		}
        	);
	    };

	    $scope.$watch('settings.testLog', function (newValue, oldValue) {
		$log.info("Selected new log", newValue);
		if ($scope.viwerInitialized) {
		    $scope.settings.logPointer = {};
		}
		if (newValue) {
		    if ($scope.viwerInitialized) {
			$timeout(function() { $scope.$broadcast('resetLogViewer'); });
		    }
		    $scope.viwerInitialized = true;
		}
	    });
	    
	    $scope.loadLogs();
	}]
);
