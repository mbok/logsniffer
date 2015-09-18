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
.module('lsfSnifferTestModule',[])
.controller(
	"SnifferTestCtrl",
	['$scope', '$http', '$log', 'usSpinnerService', '$modalInstance', '$timeout', 'source', 'scanner', 'publisher', 'sniffer', 'title', 'testSession',
	function($scope, $http, $log, usSpinnerService, $modalInstance, $timeout, source, scanner, publisher, sniffer, title, testSession) {
	    $scope.title =  title;
	    $scope.source = source;
	    $scope.scanner = scanner;
	    $scope.publisher = publisher;
	    $scope.settings = testSession;
	    if (typeof $scope.settings.testLog == "undefined") {
		$scope.settings.testLog = null;
	    }
	    if (typeof $scope.settings.logPointer == "undefined") {
		$scope.settings.logPointer = {};
	    }
	    $scope.viwerInitialized = false;
	    $scope.event = null;
	    
	    $scope.close = function () {
		$modalInstance.close();
	    };
	    
	    
	    $scope.loadLogs = function() {
		$("#publisher-test-backdrop").show();
		// usSpinnerService.spin('publisher');
		$http({
        		url : $scope.contextPath + "/c/sources/" + $scope.source.id + "/logs",
        		method : "GET"
        	})
        	.success(
        		function(data, status, headers, config) {
        		    $scope.logs = data;
        		    $log.info("Log loaded: ", $scope.logs);
        		    // always();
        		})
        	.error(
        		function(data, status, headers, config) {
        		    // TODO
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

	    $scope.searchFound = function(searchResult) {
		$log.info("Found event: ", searchResult.event);
		$scope.event = searchResult.event;
	    };
	    
	    $scope.publish = function() {
		$scope.publishing = true;
		$scope.publishingTabActive = true;
		$scope.publishingResult = null;
		if ($scope.event) {
		    $log.info("Testing publisher for event: ", publisher, $scope.event);
		    $http({
	        	url : $scope.contextPath + "/c/publishers/test",
	        	method : "POST",
	        	data : {
	        	    publisher: publisher,
	        	    event: $scope.event,
	        	    snifferId: sniffer && sniffer.id ? sniffer.id : 'not-saved',
	                    logSourceId: source.id,
	                    logPath: $scope.settings.testLog.path
	        	}
		    })
		    .success(
			    function(data, status, headers, config) {
				$scope.publishing = false;
				$log.info("Log loaded: ", $scope.logs);
				$scope.publishingResult = {
					status : 'success'
				};
			    })
		    .error(
			    function(data, status, headers, config) {
				$scope.publishing = false;
				$scope.publishingResult = {
					status : 'error'
				};
	        		if (status == 409) {
	        		    $log.warn("Failed to publish", data);
	        		    $scope.publishingResult.message = data;
	        		} else {
	        		    $scope.publishingResult.message = "Error: " + status;
	        		}
			    }
		    );
		}
	    };

	    $scope.loadLogs();
	}]
);
