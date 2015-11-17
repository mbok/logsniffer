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
	.module(
		'EventsModule',
		[ 'googlechart', 'ngRoute'])
	.config(
		[
			'$routeProvider',
			function($routeProvider) {
			    $routeProvider
				    .when(
					    '/',
					    {
						templateUrl : LogSniffer.config.contextPath
							+ '/ng/sniffer/event/eventsList.html',
						controller : "EventsListController"
					    })
				    .when(
					    '/:eventId',
					    {
						templateUrl : LogSniffer.config.contextPath
							+ '/ng/sniffer/event/eventDetail.html',
						controller : "EventsDetailController"
					    })
				    .otherwise({
					redirectTo : '/'
				    });
			} ])
	.controller(
		"EventsListController",
		[
			'$scope',
			'$http',
			'usSpinnerService',
			'$log',
			'$routeParams',
			'$location',
			'$timeout',
			'messageCenterService',
			'lsfAlerts',
			function($scope, $http, usSpinnerService, $log,
				$routeParams, $location, $timeout,
				messageCenterService, lsfAlerts) {
			    $scope.Math = window.Math;
			    $scope.eventsList = null;
			    $scope.alerts = lsfAlerts.create();
			    $scope.state = {
			    	busy: false	
			    };
			    $scope.searchForm = {
				basicSearch: $routeParams._nativeQuery ? false: true,
				_nativeQuery: $routeParams._nativeQuery ? $routeParams._nativeQuery : null,
				_itemsPerPage : 25,
				_offset : $routeParams._offset ? $routeParams._offset * 1 : 0,
				_from : $routeParams._from ? new Date($routeParams._from*1) : null,
				_to : $routeParams._to ? new Date($routeParams._to*1) : null,
				_query: $routeParams._query ? $routeParams._query : null
			    };
			    $scope.pager = {
				currentPage: 1 + Math
				    .floor($scope.searchForm._offset
					    / $scope.searchForm._itemsPerPage)
			    };
			    
			    var getBasicEsQuery = function() {
				var nativeQuery = {
					"query" : {
					    "filtered" : {
						"query" : {
						    // To be determined
						},
						"filter" : {
						    "range" : {
							"lf_occurrence" : {
							    "to" : $scope.searchForm._to ? $scope.searchForm._to.getTime() : null,
							    "from" : $scope.searchForm._from ? $scope.searchForm._from.getTime() : null,
							    "include_lower" : true,
							    "include_upper" : true
							}
						    }
						}
					    }
					},
					"sort" : [ {
					    "lf_occurrence" : {
						"order" : "asc",
						"ignore_unmapped" : true
					    }
					} ]
				    };
            			if ($scope.searchForm._query) {
            			    nativeQuery.query.filtered.query = {
            				    "simple_query_string" : {
            				        "query" : $scope.searchForm._query
            				}
            			    };
            			} else {
            			    nativeQuery.query.filtered.query = {
            				    "match_all" : {}
            			    };
            			}
            			return nativeQuery;
			    };
			    
			    var loadingStatusStart = function() {
			    	$scope.state.busy = true;
			    };
			    var loadingStatusStop = function() {
			    	$scope.state.busy = false;
			    };
				
			    $scope.internalSearch = function() {
				var nativeQuery = null;
				if ($scope.searchForm.basicSearch) {
				    nativeQuery = getBasicEsQuery();
				} else {
				    nativeQuery = $scope.searchForm._nativeQuery;
				}
				$log.info("Load events from offset/page",
					$scope.searchForm._offset,
					$scope.pager.currentPage, nativeQuery);
				loadingStatusStart();
				$http(
					{
					    url : $scope.contextPath
						    + "/c/sniffers/"
						    + $scope.sniffer.id
						    + "/events/nativeSearch?_offset="
						    + $scope.searchForm._offset
						    + "&_size="
						    + $scope.searchForm._itemsPerPage
						    + "&_histogram=true",
					    method : "POST",
					    data : nativeQuery
					})
					.success(
						function(data, status, headers,
							config) {
						    $scope.eventsList = data;
						    $scope.items = $scope.eventsList.items;
						    $log.info("Events loaded",
							    data);
						    $scope.updateTrendChart();
						    loadingStatusStop();
						})
					.error(
					function(data, status, headers,
						config, statusText) {
					    loadingStatusStop();
					    $scope.alerts.httpError("Failed to load events", data, status, headers, config, statusText);
					});
			    };
			    $scope.internalSearch();

			    $scope.search = function() {
				$location.search('_offset', 0);
				$location.search('_reload', Math.random());
				if ($scope.searchForm.basicSearch) {
        				if ($scope.searchForm._from) {
        				    $location.search('_from', $scope.searchForm._from.getTime());
        				} else {
        				    $location.search('_from', null);
        				}
        				if ($scope.searchForm._to) {
        				    $location.search('_to', $scope.searchForm._to.getTime());
        				} else {
        				    $location.search('_to', null);
        				}
        				if ($scope.searchForm._query) {
        				    $location.search('_query', $scope.searchForm._query);
        				} else {
        				    $location.search('_query', null);
        				}
        				 $location.search('_nativeQuery', null);
				} else {
				    $location.search('_query', null);
				    $location.search('_from', null);
				    $location.search('_to', null);
				    $location.search('_nativeQuery', $scope.searchForm._nativeQuery);
				}
			    };
			    
			    $scope.pageChanged = function() {
				$log.info("Page changed",
					$scope.pager.currentPage);
				var newOffset =  ($scope.pager.currentPage - 1) * $scope.searchForm._itemsPerPage;
				$location.search('_offset', newOffset);
			    };

			    $scope.updateTrendChart = function() {
				$scope.eventsTrendChart = {
				    "type" : "ColumnChart",
				    "displayed" : true,
				    "data" : new LogSniffer.DataTableBinder(
					    null, [ {
						path : "time",
						options : {
						    type : "datetime",
						    label : "Event occurrence"
						}
					    }, {
						path : "count",
						options : {
						    type : "number",
						    label : "Events count"
						}
					    } ])
					    .bind()
					    (
						    $scope.eventsList.eventsCountHistogram.entries),
				    "options" : {
					chartArea : {
					    width : "100%",
					    top : 0
					},
					legend : {
					    position : "none"
					},
					vAxis : {
					    textPosition : "none"
					},
					bar : {
					    groupWidth : "90%"
					},
				    },
				    "formatters" : {}
				};
			    };
			    
			    var addInterval = function (tmst, interval, amount) {
				switch(interval) {
				case "SECOND":
					return tmst.addSeconds(amount);
				case "MINUTE":
					return tmst.addMinutes(amount);
				case "HOUR":
					return tmst.addHours(amount);
				case "DAY":
					return tmst.addDays(amount);
				case "WEEK":
					return tmst.addWeeks(amount);
				case "MONTH":
					return tmst.addMonths(amount);
				case "YEAR":
					return tmst.addYears(amount);
				}
				return tmst;
			    };
			    $scope.setOccurrenceRangeFrom = function(interval, amount) {
				$scope.searchForm._from = interval ? addInterval(new Date(), interval, amount) : null;
				if (!interval) {
				    $scope.searchForm._to = null;
				}
			    };
			    
			    $scope.setOccurrenceRangeFromChart = function(selectedItem) {
				if (!$scope.searchForm.basicSearch) {
				    messageCenterService.add(
					    'warning',
					    "Adjusting the query from graph isn't supported in extended search mode. Please adapt your query manually.");
				    return;
				}
			         var histoEntry = $scope.eventsList.eventsCountHistogram.entries[selectedItem.row];
			         $log.info("Set occurence filter by chart selection row/entry", selectedItem.row, histoEntry);
			         $location.search("_from", histoEntry.time);
			         $location.search("_to", addInterval(new Date(histoEntry.time), $scope.eventsList.eventsCountHistogram.interval, 1).getTime());
			         $location.search('_nativeQuery', null);
			         $location.search("_offset", "0"); 
			    };
			    
			    $scope.switchToExtendedSearch = function() {
				$scope.searchForm.basicSearch = false;
				if (!$scope.searchForm._nativeQuery) {
				    $scope.searchForm._nativeQuery = JSON.stringify(getBasicEsQuery(), null, 3);
				}
			    };

			    $scope.switchToBasicSearch = function() {
				$scope.searchForm.basicSearch = true;
			    };
			    
			    $scope.deleteAllEvents = function() {
				if (confirm("Are you sure you want to delete all events?")) {
				    loadingStatusStart();
				    $log.info("Deleting all events");
				    $http(
					{
					    url : $scope.contextPath + "/c/sniffers/" + $scope.sniffer.id + "/events",
					    method : "DELETE"
					})
					.success(
					function(data, status, headers,
						config) {
					    loadingStatusStop();
					    $scope.eventsList = null;
					    $log.info("Deleted all events");
					    messageCenterService.add(
						    'success',
						    'Deleted all events.');
					    
					})
					.error(
					function(data, status, headers,
						config) {
					    loadingStatusStop();
					    messageCenterService.add(
						    'danger',
						    'Failed to delete events: '
							    + status);
					});
				}
			    };


			    $scope.deleteSniffer = function() {
				if (!$scope.sniffer.aspects.scheduleInfo.scheduled && confirm("Are you sure you want to delete the sniffer and all saved events?")) {
				    $("#sniffer-delete-form").attr("action", $scope.contextPath+"/c/sniffers/"+$scope.sniffer.id+"/delete").submit();
				}
			    };
			} ])
			
	.controller(
		"EventsDetailController",
		[
			'$scope',
			'$http',
			'usSpinnerService',
			'$log',
			'$routeParams',
			'$location',
			'$timeout',
			'messageCenterService',
			function($scope, $http, usSpinnerService, $log,
				$routeParams, $location, $timeout,
				messageCenterService) {
			    $scope.Math = window.Math;
			    $scope.event = null;
			    $scope.eventId = $routeParams.eventId;
			    $scope.loadEvent = function() {
				var eventId = $scope.eventId;
				$log.info("Loading event for id", eventId);
				$(".backdrop-overlay").show();
				$timeout(function() {
				    usSpinnerService.spin('eventLoading');
				});
				var always = function() {
				    $(".backdrop-overlay").hide();
				    usSpinnerService.stop('eventLoading');
				};
				$http(
					{
					    url : $scope.contextPath
						    + "/c/sniffers/"
						    + $scope.sniffer.id
						    + "/events/" + eventId,
					    method : "GET"
					})
				.success(
					function(data, status, headers, config) {
					    $scope.event = data;
					    $log.info("Event loaded", data);
					    always();
					})
				.error(
					function(data, status, headers, config) {
					    always();
					    if (status==404) {
						messageCenterService.add('danger', 'Event not found. It doesn\'t longer exist or is deleted.');
					    } else {
						messageCenterService.add('danger', 'Failed to load event: ' + status);
					    }
					}
				);
			    };
			    
			    $scope.deleteEvent = function () {
				var eventId = $scope.eventId;
				if ($scope.event && confirm("Delete really?")) {
					$log.info("Deleting event for id", eventId);
					$(".backdrop-overlay").show();
					$timeout(function() {
					    usSpinnerService.spin('eventLoading');
					});
					var always = function() {
					    $(".backdrop-overlay").hide();
					    usSpinnerService.stop('eventLoading');
					};
					$http(
						{
						    url : $scope.contextPath
							    + "/c/sniffers/"
							    + $scope.sniffer.id
							    + "/events/" + eventId,
						    method : "DELETE"
						})
					.success(
						function(data, status, headers, config) {
						    $scope.event = null;
						    $log.info("Event deleted");
						    messageCenterService.add('success', 'Event deleted');
						    always();
						})
					.error(
						function(data, status, headers, config) {
						    always();
						    if (status==404) {
							messageCenterService.add('danger', 'Event not found. It doesn\'t longer exist or is deleted.');
						    } else {
							messageCenterService.add('danger', 'Failed to delete event: ' + status);
						    }
						}
					);
				}
			    };

			    $scope.loadEvent();
			}
		]
	);