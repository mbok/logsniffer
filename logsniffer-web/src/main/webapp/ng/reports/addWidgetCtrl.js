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
var AddWidgetCtrl = function($scope, $modalInstance, $log) {
	$scope.widget = {
		title : "New Widget",
		mode : "CHART",
		data : {
			chartType : null,
			options : null,
			dataBuilderConfig : null
		}
	};

	$scope.templates = {
		CHART : [ {
			label : "Events Histogram",
			data : {
				chartType : "ColumnChart",
				options : {
					"title" : "Events Histogram"
				},
				formatters : {},
				dataBuilderConfig : {
					builders: [
					    {
					    	_type: "ElasticRequestBuilder",
			    	        requestParts: [
					    	  {
								_type : "DateHistogramFacet",
								name : "Events Histogram",
								request : {
									"date_histogram" : {
										"field" : "occurrence",
										"interval" : "day"
									}
								},
								cols : [ {
									path : "time",
									options : {
										label : "Occurrence",
										type : "datetime"
									}
								}, {
									path : "count",
									options : {
										type : "number",
										label : "Events count"
									}
								} ]
					    	  } 
					       ]
					   }
					]
				}
			}
		}, {
			label : "Event Severity Statistic",
			data : {

			}
		} ],
		RAW : [ {
			label : "Hello World",
			rawHtml : 'Hello World Raw HTML Widget'
		} ]
	};

	$scope.template = $scope.templates.CHART[0];

	$scope.ok = function() {
		if ($scope.widget.mode == "CHART" && $scope.template) {
			$scope.widget.data = $scope.template.data;
		} else if ($scope.template) {
			$scope.widget.rawHtml = $scope.template.rawHtml;
		}
		$modalInstance.close($scope.widget);
	};

	$scope.cancel = function() {
		$modalInstance.dismiss('cancel');
	};
};