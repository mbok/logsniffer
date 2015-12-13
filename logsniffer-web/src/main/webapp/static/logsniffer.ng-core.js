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
angular.module('LogSnifferCore', ['jsonFormatter'])
	// From http://stackoverflow.com/questions/18095727/limit-the-length-of-a-string-with-angularjs
	.filter('cut', function () {
        return function (value, wordwise, max, tail, disabled) {
            if (disabled) {
        	return value;
            }
            if (!value) return '';
            max = parseInt(max, 10);
            if (!max || max < 0) return value;
            if (value.length <= max) return value;

            value = value.substr(0, max);
            if (wordwise) {
                var lastspace = value.lastIndexOf(' ');
                if (lastspace != -1) {
                    value = value.substr(0, lastspace);
                }
            }

            return value + (tail || '...');
        };
    })
   .filter('bytesToSize', function () {
       return function(value, precision) {
	   return bytesToSize(value, precision);
       };
   })
   .filter('fileName', function () {
       return function(fullPath) {
	   return fullPath.replace(/^.*[\\\/]/, '');
       };
   })
   .filter('obj2Array', function () {
	   return function(obj) {
	       var a = [];
	       for (var i in obj) {
	    	   a.push({ key: i, value: obj[i]});
	       }
	       return a;
	   };
   })
   .directive('lfsFieldsTable', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       fields: '=',
	       excludeRaw: '=',
	       excludeFields: '=',
	       include: '&'
	   },
	   controller: function($scope, $log) {
		    $scope.rows = null;

		    var includeMap = {};
		    if ($scope.include) {
		    	var include = $scope.include();
		    	if (include) {
		    		for (var i=0;i<include.length;i++) {
		    			includeMap[include[i]] = true;
		    		}
		    	}
		    }
		    
		    var ignoreFields = {
		    	"lf_startOffset": !includeMap["lf_startOffset"],
		    	"lf_endOffset": !includeMap["lf_endOffset"],
		    	"@types": true
		    };
		    if ($scope.excludeFields) {
		    	for (var i=0; i<$scope.excludeFields.length; i++) {
		    		var key = $scope.excludeFields[i];
		    		ignoreFields[key] = !includeMap[key];
		    	}
		    }
		    var internKeys = [];
		    var customKeys = [];
		    angular.forEach($scope.fields, function(value, key) {
		        if (!($scope.excludeRaw && key=="lf_raw") && !ignoreFields[key]) {
		        	if (key.indexOf("lf_")==0 || key.indexOf("_")==0) {
		        		internKeys.push(key);
		        	} else {
		        		customKeys.push(key);
		        	}
		        }
		    });
		    internKeys.sort();
		    customKeys.sort();
		    var keys = internKeys.concat(customKeys);
			if (!$scope.rows) {
			    $scope.rows = [];
			}
		    for (var i = 0; i < keys.length; i++) {
		    	var key = keys[i];
			    $scope.rows.push({
	            	name: key,
	            	value: $scope.fields[key],
	            	type: LogSniffer.getFieldType($scope.fields, key),
	            	internal: key.indexOf("lf_")==0 || key.indexOf("_")==0
	            });
		    }

	   },
	   template: 
	      '<div><div class="panel panel-default" ng-if="rows">'+
	      	'<table class="attributes table table-condensed table-striped table-bordered entries">'+
	      		'<tr ng-repeat="row in rows">'+
	      			'<th class="text">{{row.name}}</th>'+
	      			'<td><lsf-print-field type="row.type" value="row.value"></lsf-print-field></td>'+
	      		'</tr>'+
	      	 '</table>'+
	      	'</div>'+
	      	'<p ng-if="!rows" class="text-muted"> - no fields -</p></div>'
       };
   })
   .directive('lsfPrintField', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       type: '&',
	       value: '&',
	       limit: '&'
	   },
	   controller: function($scope, $log) {
	       $scope.t = $scope.type();
	       $scope.v = $scope.value();
	       $scope.l = $scope.limit ? $scope.limit() : -1;
	       if (!$scope.l) {
	    	   $scope.l = -1;
	       }
	       $scope.openLogPointer = function(pointer) {
	    	   $log.info("Sending event to open log pointer", pointer);
	    	   $scope.$emit('openLogPointer', pointer);
	       };
	   },
	   template: 
	      '<span ng-switch="t" ng-class="t">' +
		  	'<span ng-switch-when="SEVERITY" class="label label-default severity sc-{{v.c}}">{{v.n}}</span>' +
		  	'<span ng-switch-when="DATE">{{v | date:\'medium\'}}</span>' +
		  	'<span ng-switch-when="OBJECT"><json-formatter json="v" open="1"></json-formatter></span>' +
		  	'<span ng-switch-when="STRING">{{v | cut:false:l:\'...\'}}</span>' +
		  	'<span ng-switch-when="LPOINTER"><a href ng-click="openLogPointer(v)"><i class="glyphicon glyphicon-list"></i> Open log entry reference</a></span>' +
		  	'<span ng-switch-default>{{v}}</span>' +
		  '</span>'
       };
   })
   .directive('lsfBusyContainer', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       busy: '=',
	       backdrop: '&'
	   },
	   controller: function($scope) {
	       $scope.loading = $scope.busy;
	       $scope.$watch('busy', function(newValue, oldValue) {
		   $scope.loading = newValue;
	       });
	   },
	   template: 
	      '<div class="busy-container"><div class="backdrop" ng-show="loading"></div><div class="spinner-area" ng-show="loading"><div us-spinner class="spinner"></div></div><div ng-transclude></div></div>'
       };
   })
   .directive('lsfFormValidObserver', function() {
       return {
	   restrict: 'AE',
	   replace: false,
	   transclude: false,
	   scope: {
	       form: '=',
	       onValidChange: '='
	   },
	   controller: function($scope, $log) {
	       $scope.loading = $scope.busy;
	       $scope.$watch('form.$valid', function(newValue, oldValue) {
	    	   $scope.onValidChange($scope.form, newValue);
	       });
	   	}
       };
   })
   .directive('lsfInfoLabel', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       label: '@'
	   },
	   controller: function($scope) {
	       $scope.infoExpanded = false;
	       $scope.expand = function(expand) {
		   $scope.infoExpanded = expand;
	       };
	   },
	   template: 
	      '<div><div class="well well-sm" ng-show="infoExpanded"><button type="button" class="close" ng-click="expand(false)"><span>&times;</span></button><i class="glyphicon glyphicon-info-sign help-icon pull-left" style="margin-right:0.5em"></i> <div ng-transclude></div></div><label class="control-label">{{label}} <a href ng-click="expand(!infoExpanded)"><i class="glyphicon glyphicon-info-sign help-icon"></i></a></label></div>'
       };
   })
   .directive('lfsBeanWizard', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: true,
	   scope: {
	       wizards: '=',
	       bean: '=',
	       sharedScope: '=',
	       parentBindErrors: '=bindErrors',
	       bindErrorsPrefix: '@',
	       beanTypeLabel: '@',
	       styleClass: '@'
	   },
	   controller: function($scope) {
	       	$scope.contextPath = LogSniffer.config.contextPath;
	       	$scope.version = LogSniffer.config.version;
	       	$scope.beanWrapper = [$scope.bean];
		$scope.selectedWizard = null;
		$scope.templateLoading = false;
		$scope.bindErrors = {};

		var unwrapAndSetBean = function(beanWrapper, setType) {
			$scope.bean = beanWrapper.length > 0 ? beanWrapper[0]: {};
			var beanType = $scope.bean ? $scope.bean["@type"]: null;
			if (setType && beanType) {
				for(var z=0;z<$scope.wizards.length;z++) {
					var wizard=$scope.wizards[z];
					if (wizard.beanType == beanType) {
						$scope.selectedWizard = wizard;
						break;
					};
				};
			}
		};
		unwrapAndSetBean($scope.beanWrapper, true);

		$scope.$watch('beanWrapper', function(newValue, oldValue) {
			$scope.bean = newValue[0];
		}, true);
		console.log($scope.bindErrors);
		$scope.$watch('parentBindErrors', function(newValue, oldValue) {
			$scope.bindErrors = LogSniffer.stripPrefix(newValue, $scope.bindErrorsPrefix);
			console.log("Updating bindErrors", newValue, $scope.bindErrorsPrefix, $scope.bindErrors);
		});

		$scope.$watch('bean', function(newValue, oldValue) {
			if ($scope.selectedWizard) {
				$scope.beanWrapper[0] = newValue;
			}
		});
		$scope.$watch('selectedWizard', function(newValue, oldValue) {
			if (newValue != oldValue && newValue) {
				unwrapAndSetBean(angular.copy([newValue.template]));
			}
			if (!newValue) {
				unwrapAndSetBean({});
			}
			if (newValue) {
				$scope.bean["@type"]=newValue.beanType;
				$scope.beanWrapper[0] = $scope.bean;
				$scope.templateLoading = true;
			}
		});
		
		$scope.getWizardView = function(selectedWizard) {
		    if (selectedWizard.view.indexOf("/ng/") == 0) {
			return LogSniffer.config.contextPath + selectedWizard.view + '?v=' + LogSniffer.config.version;
		    } else {			
			return LogSniffer.config.contextPath + '/c/wizards/view?type=' + selectedWizard.beanType + '&v=' + LogSniffer.config.version;
		    }
		};

		$scope.templateLoaded = function() {
			$scope.templateLoading = false;
		};
	   },
	   template: 
	      '<div ng-form="form" class="bean-wizard" ng-class="{\'well well-sm\' : !styleClass, styleClass: styleClass}"><div class="row">' +
	      	'<div class="col-md-6 form-group required" ng-class="{\'has-error\': form.selectedWizard.$invalid && !form.selectedWizard.$pristine}">' +
			'<label class="control-label">{{beanTypeLabel}}</label>' +
			'<div class="controls">' +
				'<select ng-model="selectedWizard" name="selectedWizard" class="form-control" ng-options="w.label for w in wizards" required>' +
					'<option value="">- Please select -</option>' +
				'</select>' +
			'</div>' +
		 '</div>' +
		'</div>' +
		'<!-- Wizard -->' +
		'<div>' +
			'<div us-spinner ng-if="templateLoading"></div>' +
			'<div class="slide-animate" ng-if="selectedWizard" ng-include="getWizardView(selectedWizard)"' +
			   ' onload="templateLoaded()"></div>' +
		'</div><div ng-transclude></div></div>'
       };
   })
   .directive('lsfLogViewer', ['$timeout', '$location', '$anchorScroll', '$log', '$http', 'lsfAlerts', '$modal', function($timeout, $location, $anchorScroll, $log, $http, lsfAlerts, $modal) {
	var defaultLoadCount = 100;
	var tailMaxFollowInterval=1500;
	var tailMinFollowInterval=50;
	var slidingWindowEntriesCount = 250;
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: false,
	   scope: {
	       source: '=',
	       log: '=',
	       mark: '=pointer',
	       viewerFields: '=',
	       fixTopElementSelector: '@',
	       pointerTpl: '&',
	       initTail: '&',
	       searchWizards: '&',
	       searchScanner: '&',
	       searchFound: '&',
	       followDisabled: '&',
	       searchExpanded: '&',
	       onError: '&',
	       fullHeight: '@',
	   },
	   controller: function($scope) {
		$scope.searchSettings= {
			dir: 1,
			expanded: $scope.searchExpanded() === true
		};
		$scope.isFollowEnabled = $scope.followDisabled() === true ? false: true;
		$scope.wizardScannerEnabled = angular.isArray($scope.searchWizards()) && $scope.searchWizards().length > 0;
		$scope.wizardScanner = { bean: {}};
		$scope.searchStatus = "none"; // none, searching, hit, miss, cancelled
		$scope.searchStatusText = null;
		$scope.searchSpeed = 0;
		$scope.searchLogPositionerOriginPointer = null;
		$scope.searchBindErrors = [];
		$scope.searchEnabled = $scope.wizardScannerEnabled || angular.isObject($scope.searchScanner());
		$scope.sharedScope = $scope;
		
		var entriesUpdCallback = null;
		var searchLogPositionerOriginPointer;
		$scope.cancel=function() {
			$scope.searchStatus="cancelled";
			console.log("Cancelled search");
			if (searchLogPositionerOriginPointer) {
			    $scope.setPointer(searchLogPositionerOriginPointer);
			}
			entriesUpdCallback(null);
			$scope.backdropOverlay.hide();
		};
		$scope.search=function(searchDir) {
		    	$scope.disableTailFollow();
		    	$scope.backdropOverlay.show();
			$scope.cancelled=false;
			$scope.searchStatus="searching";
			$scope.searchStatusText = null;
			entriesUpdCallback = $scope.getEntriesUpdateCallback();
			var scanner = $scope.wizardScannerEnabled ? $scope.wizardScanner.bean : $scope.searchScanner();
			var pointer = $scope.getTopLogPointer($scope.searchSettings.dir > 0 ? 'end' : 'start');
			searchLogPositionerOriginPointer = $scope.mark;
			var incrementalSearch = function(pointer) {
				$log.info("Search for entries from "+JSON.stringify(pointer)+" using scanner: " + JSON.stringify(scanner));
				$http({
				    url: $scope.getLoadLogEntriesUrl(pointer?JSON.stringify(pointer):'', $scope.searchSettings.dir * defaultLoadCount, 'search'),
				    method: "POST",
				    data: scanner,
				}).success(function(data, status, headers, config) {
				    	$log.info("Search finished with lastPointer="+JSON.stringify(data.lastPointer));
					if($scope.searchStatus=="cancelled") {
					    	$log.info("Dismiss serach result due to cancellation");
						return;
					}
					if (data.scannedTime>0) {
						$scope.searchSpeed=bytesToSize(Math.round(data.scannedSize/data.scannedTime*1000),2);
					}
					$scope.setPointer(data.lastPointer.json);
					if (data.entries) {
					    	$scope.backdropOverlay.hide();
						$scope.searchStatus="hit";
						$('#search-progress-modal').modal("hide");
						entriesUpdCallback(data.entries.entries);
						$("#log-entries-frame tbody tr:eq(0)").addClass("info");
						if (document.location.hash!="search-control") {
							document.location.hash="search-control";
						}
						if (angular.isFunction($scope.searchFound)) {
						    $scope.searchFound({ searchResult:data });
						}
					} else {
						entriesUpdCallback(null);
						if (data.lastPointer.sof || data.lastPointer.eof) {
						    	$scope.backdropOverlay.hide();
							$scope.searchStatus="miss";
							// End of search
							$log.info("End of log reached without matching");
							$scope.setPointer(searchLogPositionerOriginPointer);
						} else {
							console.log("Continue search from new pointer: "+JSON.stringify(data.lastPointer));
							incrementalSearch(data.lastPointer.json);
						}
					}
				}).error(function(data, status, headers, config, statusText) {
				    	$scope.backdropOverlay.hide();
					$scope.searchStatus="error";
					$scope.searchStatusText = "An error occurred during search: " + status;
					$log.error("Error occurred during searching", status);
					$scope.setPointer(searchLogPositionerOriginPointer);
					$scope.handleHttpError($scope.searchStatusText, data, status, headers, config, statusText);
				});
			}; // incrementalSearch
			incrementalSearch(pointer);
		};
		
		$scope.configureFields = function() {
			$modal.open({
			      templateUrl: LogSniffer.config.contextPath + '/ng/entry/viewerFieldsConfig.html',
			      controller: 'ViewerFieldsConfigCtrl',
			      size: 'lg',
			      scope: $scope,
			      resolve: {
			        viewerFields: function () {
						$log.info("Inject fields to configure visibility: ", $scope.viewerFields);
						return $scope.viewerFields;
			        },
			        fieldTypes: function () {
						return $scope.source.reader.fieldTypes;
			        }
			      }
			    });

		};
		$scope.$on("viewerFieldsChanged", function(event, viewerFields) {
			$log.info("Changed viewer fields, reloading viewer content from current position", viewerFields);
			var topPointer = $scope.getTopLogPointer();
			$scope.viewerFields = viewerFields;
			$scope.loadRandomAccessEntries(topPointer);
		});
	   },
	   link: function(scope, element, attrs) {
		var loadingEntries=false;
		if (!$.LogSniffer._viewerEntries) {
		    $.LogSniffer._viewerEntries = [];
		};
		var viewerEntriesId = $.LogSniffer._viewerEntries.length;
		$.LogSniffer._viewerEntries[viewerEntriesId] = {};
		var logEntries = $.LogSniffer._viewerEntries[viewerEntriesId];
		var entriesStaticCounter = 0;
		scope.backdropOverlay = $(element).find(".backdrop-overlay");
		scope.fieldTypes = {};
		scope.alerts = lsfAlerts.create();
		scope.fullscreen = false;
		scope.frameHeightBeforeFullscreen = null;
		
		
		scope.resizeViewerToFullHeight = function (windowRef, count) {
			$timeout(function() {
				var searchPanelHeight = $(element).find(".viewer-search .panel-body:visible").outerHeight(true);
				if (searchPanelHeight == null) {
					searchPanelHeight = 0;
				}
				var viewerScreenHeight = $(element).find(".lsf-viewer").height();
				var windowHeight = $(windowRef).height();
				if (windowHeight == null) {
					if (count > 3) {
						$log.error("Failed to resize viewer, because window reference not found");
						return;
					} else {
						scope.resizeViewerToFullHeight(windowRef, count + 1);
						return;
					}
				}
				// Reduce total window height by static header etc.
				if (!scope.fullscreen && scope.fixTopElementSelector) {
					windowHeight -= $(scope.fixTopElementSelector).height() + 1;
				}
				// Reduce total window height by upper elements height
				var viewerOffset=$(element).find(".lsf-viewer").offset();
				windowHeight -= viewerOffset.top;
				
				var currentEntriesFrameHeight = $(element).find("#log-entries-frame").height();
				$log.debug("Fullscreen metrics: window, viewer, searchPanel, entriesFrameHeight height: ", windowHeight, viewerScreenHeight, searchPanelHeight, currentEntriesFrameHeight);
				if (windowHeight < (viewerScreenHeight - searchPanelHeight)) {
					var reduceTo = Math.max(450, currentEntriesFrameHeight - (viewerScreenHeight - searchPanelHeight - windowHeight)) + (scope.fullscreen ? 20 : 0);
					$log.debug("Reduce entries frame after resizing to:", reduceTo);
					$(element).find("#log-entries-frame").height(reduceTo);
				} else {
					var incTo = Math.max(450, currentEntriesFrameHeight + (windowHeight - viewerScreenHeight + searchPanelHeight)) - (scope.fullscreen ? 20 : 0);
					$log.debug("Increase entries frame after resizing to:", incTo);
					$(element).find("#log-entries-frame").height(incTo);
				}
			}, 100);
		};

		scope.$on("fullscreenEnabled", function() {
			scope.forceScrollToBottom = scope.isTailFollowOnHead();
			scope.fullscreen = true;
			$log.debug("Viewer switched to fullscreen");
			scope.frameHeightBeforeFullscreen = $(element).find("#log-entries-frame").height();
			scope.resizeViewerToFullHeight(".viewer-fullscreen.fullscreen", 1);
		});
		scope.$on("fullscreenDisabled", function() {
			scope.fullscreen = false;
			scope.forceScrollToBottom = scope.isTailFollowOnHead();
			$log.debug("Viewer switched to normal screen");
			if (scope.frameHeightBeforeFullscreen) {
				$log.debug("Set entries frame height back to:", scope.frameHeightBeforeFullscreen);
				$(element).find("#log-entries-frame").height(scope.frameHeightBeforeFullscreen);
				scope.frameHeightBeforeFullscreen = null;
			}
		});
		
		
		function emptyViewerEntries() {
		    $.LogSniffer._viewerEntries[viewerEntriesId] = {};
		    logEntries = $.LogSniffer._viewerEntries[viewerEntriesId];
		};
		function renderTableHead(fieldTypes) {
		    scope.fieldTypes = fieldTypes;
		    var entriesTableHead=$.LogSniffer.entriesHead(fieldTypes, scope.viewerFields, function() {return '<th style="width:18px"></th>';});
		    $(element).find(".log-entries thead").html(entriesTableHead);
		};

		scope.getLoadLogEntriesUrl = function(mark, count, urlSuffix) {
			return LogSniffer.config.contextPath + '/c/sources/'+ scope.source.id +'/'+(urlSuffix?urlSuffix:'entries')+'?log=' + encodeURIComponent(scope.log.path) +'&mark='+ mark +'&count=' + count;
		};
		
		scope.handleHttpError = function(context, data, status, headers, config, statusText) {
		    if (angular.isFunction(scope.onError())) {
		    	scope.onError()(context);
		    };
		    scope.alerts.httpError(context, data, status, headers, config, statusText);
		};
		
		scope.getLoadLogEntriesHttpCall = function(mark, count) {
		    $log.debug('Loading entries for mark/count/source: ', mark, count, scope.source);
		    if (scope.source.id) {
			return $http({
			    url: scope.getLoadLogEntriesUrl(mark, count),
			    method: "GET"
			});
		    } else {
			return $http({
			    url: LogSniffer.config.contextPath + '/c/sources/entries?log=' + encodeURIComponent(scope.log.path) +'&mark='+ mark +'&count=' + count,
			    method: "POST",
			    data: scope.source
			});
		    }
		};

		function updateLogControls(forwardMove) {
			if ($(element).find('.log-entries tbody tr:first').attr('sof')=='true') {
				$(element).find("a.start").addClass("disabled");
			} else {
				$(element).find("a.start").removeClass("disabled");
			}
		}
		function isEofReached() {
			return $(element).find('.log-entries tbody tr:last').attr('eof')=='true';
		}
		scope.getTopLogPointer = function(pointerType) {
			var entriesPadding=$(element).find("#log-entries-frame").outerHeight(true) - $(element).find("#log-entries-frame").innerHeight();
			var entriesOffset=$(element).find("#log-entries-frame").offset();
			var x=entriesOffset.left + 50;
			var y = 3 + Math.max(!scope.fullscreen && scope.fixTopElementSelector ? $(scope.fixTopElementSelector).height() + 1 : 0, entriesOffset.top + entriesPadding / 2 - (scope.fullscreen ? 0 : $(window).scrollTop()));
			var elem=document.elementFromPoint(x, y);
			if (true && (!elem || $(elem).parents("table").length==0)) {
				// Select first row
				elem=$(element).find("#log-entries-frame tbody td:eq(0)");
			} else if ($(elem).parents("thead").length>0) {
				// Ignore thead and select the first table body cell instead
				elem=$($(elem).parent("table")[0]).find("tbody td:eq(0)");
			}
			if (elem && $(elem).parents("tr").length>0) {
				var mark=$(elem).parents("tr").attr(pointerType?pointerType:'start');
				console.log("Top entry pointer: "+mark);
				if (mark) {
					return $.parseJSON(mark);
				}
			}
			return null;
		};
		scope.setPointer = function(newPointer) {
	    		scope.mark = newPointer;
	    		scope.$broadcast('updateCurrentPosition', { newPointer: scope.mark });
		};
		function updateLogPositioner(forwardMove, inprog) {
		    	console.log("Setting new pointer position after forwardMove: ", forwardMove, scope.mark);
		    	scope.skipPositioning = true;
		    	var setter = function(){
		    	    var scrolledMark = scope.getTopLogPointer();
		    	    if (scrolledMark) {
		    		scope.setPointer(scrolledMark);
		    	    }
		    	};
		    	if (inprog) {
		    	    setter();
		    	} else {
		    	    scope.$apply(setter);		    	    
		    	}
		    	scope.skipPositioning = false;		    		
		}
		function bottomSpinner() {
			return new Spinner().spin($(element).find("#log-entries-frame .spinner.bottom")[0]);
		}
		function topSpinner() {
			return new Spinner().spin($(element).find("#log-entries-frame .spinner.top")[0]);
		}

		function renderPrefixCells(fieldsTypes, e) {
		    	entriesStaticCounter++;
		    	var id = 'entry-' + entriesStaticCounter;
		    	logEntries[id] = e;
			return '<td class="zoom"><a href="#" title="Open full entry" onclick="$.LogSniffer.zoomLogEntry($.LogSniffer._viewerEntries['+viewerEntriesId+'][this.id])" id="'+id+'"><i class="glyphicon glyphicon-zoom-in"/></a></td>';
		}

		function adaptSlidingEntriesWindow(cutHead, adaptScroll) {
		    var oldScroll = $(element).find('#log-entries-frame').scrollTop();
		    var table = $(element).find('.log-entries tbody')[0];
		    var overflow = table.rows.length - slidingWindowEntriesCount;
		    if (overflow > 0) {
			    var now = new Date();
        		    $log.debug("Truncating overflow entries from sliding window", overflow);
        		    var cutHeight = $(table).height();
        		    for (var i=0; i < overflow; i++) {
	        			var rowIndex = cutHead ? 0 : table.rows.length - 1;
	        			var row = $(table.rows[rowIndex]);
	        			var idCell = row.find("td.zoom a");
	        			if (idCell.length > 0) {
	        			    var entryId = idCell.attr("id");
	        			    delete logEntries[entryId];			    
	        			}
	        			table.deleteRow(rowIndex);
        		    }
        		    cutHeight = cutHeight - $(table).height();
        		    if (adaptScroll && cutHead && cutHeight > 0) {
	        			var newScroll = Math.max(10, oldScroll - cutHeight);
	        			$log.debug("Scrolling after truncation of sliding window from/to", oldScroll, newScroll);
	        			$(element).find('#log-entries-frame').scrollTop(newScroll);
        		    }
        		    $log.debug("Truncated overflow entries from sliding window in ms", overflow, new Date().getTime() - now.getTime());
		    }
		};
		
		function loadRandomAccessEntries(jsonMark) {
		    	scope.disableTailFollow();
			var entriesUpdCallback = scope.getEntriesUpdateCallback();
			var pStr = "";
			scope.setPointer(jsonMark);
			if (jsonMark) {
				pStr=JSON.stringify(jsonMark);
			}
			$log.debug('Loading random entries from mark for source: ', pStr, scope.source);
			var entriesCall = null;
			if (scope.source.id) {
			    entriesCall = $http({
				    url: LogSniffer.config.contextPath + '/c/sources/'+ scope.source.id +'/randomAccessEntries?log=' + encodeURIComponent(scope.log.path) +'&mark=' + encodeURIComponent(pStr) + '&count=' + defaultLoadCount,
				    method: "GET"
				});
			} else {
			    entriesCall = $http({
				    url: LogSniffer.config.contextPath + '/c/sources/randomAccessEntries?log=' + encodeURIComponent(scope.log.path) +'&mark=' + encodeURIComponent(pStr) + '&count=' + defaultLoadCount,
				    method: "POST",
				    data: scope.source
				});
			}
			entriesCall.success(function(data, status, headers, config) {
			      emptyViewerEntries();
			      renderTableHead(data.fieldTypes);
			      entriesUpdCallback(data.entries);
			}).error(function(data, status, headers, config, statusText) {
			    entriesUpdCallback(null);
			    scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
			});
		};
		scope.loadRandomAccessEntries = loadRandomAccessEntries;

		function loadEntries(jsonMark, fromTail, skipTailFollowCleanup) {
		    	if (!skipTailFollowCleanup) {
		    	    scope.disableTailFollow();
		    	}
			var spinner=bottomSpinner();
			var pStr = "";
			if (!fromTail && jsonMark) {
				pStr=JSON.stringify(jsonMark);
			}
			console.log('Loading entries from ' + (fromTail?'tail':'mark: ' + pStr));
			var successCallback = null;
			var h = {
				success: function(callback) {
				    successCallback = callback;
				}
			};
			scope.getLoadLogEntriesHttpCall(pStr, (fromTail?-1:1) * defaultLoadCount)
			  .success(function(data) {
			    	emptyViewerEntries();
			    	renderTableHead(data.fieldTypes);
			    	$(element).find('.log-entries tbody').empty().append($.LogSniffer.entriesRows(data.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
				if (fromTail) {
				    $(element).find('#log-entries-frame').scrollTop($(element).find('#log-entries-frame')[0].scrollHeight);
				    // $location.hash("entry-"+(logEntries.length-1));
				    // $anchorScroll();
				} else {
				    $(element).find('#log-entries-frame').scrollTop(10);
				}
				updateLogControls();
				spinner.stop();
				updateLogPositioner(true, true);
				if (successCallback) {
				    successCallback();
				}
			}).error(function(data, status, headers, config, statusText) {
			    spinner.stop();
			    scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
			});
			return h;
		}

		scope.getEntriesUpdateCallback = function() {
			loadingEntries=true;
			var spinner=new Spinner().spin($(element).find("#log-entries-frame")[0]);
			return function(entries) {
				if (entries != null) {
				    	emptyViewerEntries();
					$(element).find('.log-entries tbody').empty();
					$(element).find('.log-entries tbody').append($.LogSniffer.entriesRows(scope.fieldTypes, scope.viewerFields, entries, renderPrefixCells));
					$(element).find('#log-entries-frame').scrollTop(10);
			    		if (entries.length>0) {
			    		    scope.setPointer(entries[0].lf_startOffset.json);
			    		}
				}
				updateLogControls(true);
				spinner.stop();
				var scrollArea=$(element).find("#log-entries-frame");
				var table=scrollArea.find("table");
				console.log("Table height " + table.height()+" vs. scroll area height "+scrollArea.innerHeight());
				if (entries != null && table.height()<scrollArea.innerHeight()) {
					// Not enough loaded
					loadingEntries=false;
					$(element).find('#log-entries-frame').scrollTop(0);
				} else {
					setTimeout(function(){loadingEntries=false;},500);
				};
			};
		};
		
		scope.disableTailFollow = function() {
		    scope.tailFollowEnabled = false;
		    followTail(false, true);
		};

		
		// Init 1
		scope.reset = function() {
        		if (angular.isObject(scope.mark) && LogSniffer.objSize(scope.mark)>0) {
        			loadRandomAccessEntries(scope.mark);
        		} else {
        		    	loadEntries(scope.mark, scope.initTail());
        		}
		};
        	
		scope.$on('resetLogViewer', function(event, args) {
		    $log.info("Reseting viewer");
		    scope.reset();
		});

		// Init 1
		scope.reset();

		// Init 2
		{			
			var lastEntriesScrollTop=-1;
			$(element).find('#log-entries-frame').scroll(function() {
				var scrollTop=$(this).scrollTop();
				updateLogPositioner(scrollTop>=lastEntriesScrollTop);
				lastEntriesScrollTop=scrollTop;
			});

			$(element).find('#log-entries-frame').endlessScroll({
			  fireOnce:false,
			  fireDelay:false,
			  loader:'',
			  ceaseFireOnEmpty: false,
			  inflowPixels: 300,
			  callback: function(fireSequence, pageSequence, scrollDirection) {
				if (loadingEntries) {
					return true;
				}
			    if (scrollDirection == 'next' && $(element).find('.log-entries tbody tr:last').attr('eof')!='true' && !scope.tailFollowEnabled) {
			    	loadingEntries=true;
			    	var spinner=bottomSpinner();
			    	var mark=$(element).find('.log-entries tbody tr:last').attr('end');
			    	console.log('Tailing forward ' + defaultLoadCount + ' entries from mark: ' + mark);
			    	var always = function() {
					updateLogControls(true);
					spinner.stop();
					loadingEntries=false;
					if (!scope.tailFollowEnabled) {
					    adaptSlidingEntriesWindow(true, true);
					}
				};
			    	scope.getLoadLogEntriesHttpCall(mark, defaultLoadCount)
			    		.success(function(data) {
						$(element).find('.log-entries tbody').append($.LogSniffer.entriesRows(scope.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
						always();
			    		}).error(function(data, status, headers, config, statusText) {
					    always();
					    scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
			    		});
				return true;
			    } else if (scrollDirection != 'next' && $(element).find('.log-entries tbody tr:first').attr('sof')!='true') {
			    	loadingEntries=true;
			    	var spinner=topSpinner();
			    	var scrollArea=this;
			    	var mark=$(element).find('.log-entries tbody tr:first').attr('start');
			    	console.log('Loading backward ' + defaultLoadCount + ' entries from mark: ' + mark);
			    	var oldScroll = $(scrollArea).scrollTop();
			    	var oldHeight = $(scrollArea)[0].scrollHeight;
			    	var always = function() {
					updateLogControls(true);
					spinner.stop();
					loadingEntries=false;
					if (!scope.tailFollowEnabled) {
					    adaptSlidingEntriesWindow(false, true);
					}
			    	};
			    	scope.getLoadLogEntriesHttpCall(mark, -defaultLoadCount)
		    		.success(function(data) {
					$(element).find('.log-entries tbody').prepend($.LogSniffer.entriesRows(scope.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
				        var h = $(scrollArea)[0].scrollHeight - oldHeight;
				        $(scrollArea).scrollTop(oldScroll + h);
					always();
		    		})
		    		.error(function(data, status, headers, config, statusText) {
				    always();
				    scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
		    		});
			    }
			    return true;
			  },
			  ceaseFire: function(fireSequence, pageSequence, scrollDirection) {
				return false;
			  }
			});
		};

		var followTailTimeout;
		var followSpinner;
		scope.forceScrollToBottom = false;
		function followTail(enabled, trigger) {
			if (enabled && (trigger || scope.tailFollowEnabled)) {
				if (!followSpinner) {
					followSpinner=bottomSpinner();
				}
				var mark=$(element).find('.log-entries tbody tr:last').attr('end');
				console.log('Following tail from mark: ' + mark);
				scope.getLoadLogEntriesHttpCall(mark, defaultLoadCount)
				.success(function(data) {
					var scrollArea=$(element).find("#log-entries-frame");
					var scrollTop=scrollArea.scrollTop();
					var scrollHeight=scrollArea[0].scrollHeight;
					var scrollOnBottom=scrollTop+scrollArea.outerHeight() >= scrollHeight;
					$(element).find('.log-entries tbody').append($.LogSniffer.entriesRows(scope.fieldTypes, scope.viewerFields, data.entries, renderPrefixCells));
					console.log("Jump to log tail: "+scrollOnBottom);
					if (scrollOnBottom || trigger || scope.forceScrollToBottom) {
						scope.forceScrollToBottom = false;
						adaptSlidingEntriesWindow(true, false);
						scrollArea.scrollTop(scrollArea[0].scrollHeight);
						updateLogPositioner(true, true);
					}
					updateLogControls(true);
					var nextTimeout;
					if ($(element).find('.log-entries tbody tr:last').attr('eof')!='true') {
					    if ($(element).find('#slow-following-popup').next('div.popover:visible').length == 0){
						    $(element).find('#slow-following-popup').popover('show');
					    }
					    nextTimeout=tailMinFollowInterval;
					} else if (data.length>0) {
					    $(element).find('#slow-following-popup').popover('hide');
					    nextTimeout=Math.max(tailMinFollowInterval,Math.min(tailMaxFollowInterval, tailMaxFollowInterval/(data.length/defaultLoadCount)));
					} else {
					    $(element).find('#slow-following-popup').popover('hide');
					    nextTimeout=tailMaxFollowInterval;
					}
					console.log("Going to follow tail the next time in [ms]: "+nextTimeout);
					followTailTimeout=setTimeout(function() {followTail(true);},nextTimeout);
				}).error(function(data, status, headers, config, statusText) {
				    scope.handleHttpError("Failed to load entries", data, status, headers, config, statusText);
				    scope.disableTailFollow();
		    		});

			} else if (followTailTimeout) {
				if (followSpinner) {
					followSpinner.stop();
					followSpinner=null;
				}
				$(element).find('#slow-following-popup').popover('hide');
				clearTimeout(followTailTimeout);
				console.log("Disable following tail");
				followTailTimeout = null;
			}
		}
		scope.isTailFollowOnHead = function() {
			if (scope.tailFollowEnabled) {
				var scrollArea=$(element).find("#log-entries-frame");
				var scrollTop=scrollArea.scrollTop();
				var scrollHeight=scrollArea[0].scrollHeight;
				var scrollOnBottom=scrollTop+scrollArea.outerHeight() >= scrollHeight;
				return scrollOnBottom;
			}
			return false;
		};

		// Clean code
		scope.$on('controlPositionChanged', function(event, args) {
		   scope.mark = args.newPointer;
 		   console.log("Changed log pointer, has to load entries from: ", scope.mark);
 		   loadRandomAccessEntries(scope.mark);
 		});
		
		scope.fromStart = function() {
		    loadEntries(null, false);
		};
		
		scope.fromTail = function() {
		    loadEntries(null, true);
		};
		
		scope.tailFollowEnabled = false;
		scope.toggleTailFollow = function() {
		    scope.tailFollowEnabled = !scope.tailFollowEnabled;
		    $log.info("Follow tail ", scope.tailFollowEnabled?"enabled":"disabled");
		    if (scope.tailFollowEnabled && !isEofReached()) {
			$log.info("Jump first to tail before following");
			loadEntries(null, true, true).success(function() {
			    followTail(true, true);			    
			});
		    } else {
			followTail(scope.tailFollowEnabled, true);
		    }
		};
		
		// Init
		if (scope.fullHeight=="true") {
			scope.resizeViewerToFullHeight(window, 1);
		}
		
	   },
	   templateUrl: LogSniffer.config.contextPath + '/ng/entry/logViewer.html'
       };
   }])
   .controller('ViewerFieldsConfigCtrl', function($scope, $modalInstance) {
	   $scope.enableConfig = function() {
		   $scope.viewerFields = [];
	   };
	   $scope.disableConfig = function() {
		   $scope.viewerFields = null;
	   };
	   $scope.cancel = function() {
		   $modalInstance.close();
	   };
	   $scope.apply = function() {
		   $scope.$emit('viewerFieldsChanged', $scope.viewerFields);
		   $modalInstance.close();
	   };
   })
   .directive('lsfLogPosition', function($timeout) {
       return {
	   restrict: 'AE',
	   replace: true,
	   transclude: false,
	   scope: {
	       pointer: '=',
	       log: '=',
	       name: '@',
	       disabled: '&',
	       active: '&',
	       pointerTpl: '&'
	   },
	   controller: function($scope) {
	       $scope.$on('updateCurrentPosition', function(event, args) {
			   $scope.pointer = args.newPointer;
			   console.log("Updating control position: ", $scope.pointer);
			   if ($scope.initialized) {
				   $scope.logPosition.changePosition($scope.pointer);
			   }
	       });
		   $scope.changedRollingLog = function() {
			   console.log("Changed rolling log");
			   $timeout(function () {
				   $scope.logPosition.updateAfterRollingPartChange();
			   });
		   };
	   },
	   link: function(scope, element, attrs) {
		   scope.initialized = false;
	       scope.logPosition = new LogPosition(scope.name, scope.disabled(), scope.active(), scope.log['@type']=='rolling', scope.pointerTpl(), function(p) {
			console.log("Changing log pos from control for " + scope.name + ": ", p);
			scope.$apply(function(){
			    scope.pointer = p;
			    scope.$emit('controlPositionChanged', { newPointer: p });
		    	}); 
	       });
	       $timeout(function () {
	    	   scope.logPosition.init(scope.pointer);
	    	   scope.initialized = true;
	       });
	   },
	   templateUrl: LogSniffer.config.contextPath + '/ng/entry/logPosition.html'
       };
   })
   .controller("ZoomLogEntryCtrl", ['$scope', '$modalInstance', 'entry', function($scope, $modalInstance, entry) {
       $scope.entry = entry;
       $scope.close = function () {
	   $modalInstance.close();
       };
   }])
   .directive('lsfFormGroup', function() {
       return {
	   restrict: 'AE',
	   transclude: true,
	   replace: true,
	   scope: {
	       fieldName: '@',
	       fieldPath: '@',
	       bindErrorsPath: '@',
	       bindErrors: '='
	   },
	   template: 
	      '<div ng-class="{\'has-error\': $parent.form[fieldName].$invalid || bindErrors[bindErrorsPath?bindErrorsPath:fieldPath]}">' +
	      '	<div ng-transclude></div><div class="help-block" ng-if="bindErrors[bindErrorsPath?bindErrorsPath:fieldPath]">{{bindErrors[bindErrorsPath?bindErrorsPath:fieldPath]}}</div>' +
	      '</div>'
       };
   })
   .directive('lsfAlerts', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       alerts: '=',
	   },
	   controller: function($scope) {
	       $scope.removeAlert = function (index) {
	    	   $scope.alerts.remove(index);
	       };
	   },
	   template: 
	      '<div><div ng-repeat="alert in alerts.alerts" class="alert alert-{{alert.type}}">'+
	      '<a href class="close" ng-click="removeAlert($index)">&times;</a>'+
	      '{{alert.message}}'+
	       '<div ng-if="alert.detail"><div class="log" ng-show="expanded" style="margin:1em 0"><div class="text nowrap" style="overflow-x: auto">{{alert.detail}}</div></div><a href="#" ng-click="expanded=!expanded" onclick="return false"><i class="glyphicon" ng-class="{\'glyphicon-chevron-down\':!expanded,\'glyphicon-chevron-up\':expanded}"></i> <span ng-if="!expanded">show details</span><span ng-if="expanded">hide details</span></a></div>'+
	      '</div></div>'
       };
   })
   .factory('lsfAlerts', function() {
       return {
	   create: function () {
	       	return {	       	    
        	   alerts: [],
        	   clear: function() {
        	       this.alerts = [];
        	   },
        	   add: function(type, msg, detail) {
        	       this.alerts.push( {
        		type: type,
        		message: msg,
        		detail: detail
        	       });
        	   },
        	   remove: function (index) {
        	       this.alerts.splice(index, 1);
        	   },
        	   info: function (msg, detail) {
        	       this.add('info', msg, detail);
        	   },
        	   error: function (msg, detail) {
        	       this.add('danger', msg, detail);
        	   },
        	   httpError: function (msg, data, status, headers, config, statusText) {
        	       if (data && data.bindErrors) {
        		   this.add('danger', "Erroneous input! Please correct below errors to continue.");
        	       } else {        		   
                	   var detail = "HTTP " + status +" status";
                	   if (statusText) {
                	       detail += "\n" + statusText;
                	   }
                	   if (data && typeof data.exceptionMessage != "undefined") {
                	       detail += "\n" + data.exceptionMessage;
                	   }
                	   this.add('danger', msg, detail);
        	       }
        	   },
        	   success: function (msg, detail) {
        	       this.add('success', msg, detail);	       
        	   },
        	   warn: function (msg, detail) {
        	       this.add('warn', msg, detail);	       
        	   },
        	   buildFromMessages: function (messages) {
        		   if (messages) {
        			   for (var i=0; i < messages.length; i++) {
        				   switch(messages[i].type) {
        				   	case 'ERROR':
        					   this.error(messages[i].message);
        					break;
        					default:
        						this.add(messages[i].type.toLowerCase(), messages[i].message);
        				   }
        			   }
        		   }
        	   }
	       	};
	   }
       };
   })
   .directive('lsfFieldsTeaser', function() {
       return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       fields: '&',
	       limit: '&',
	       exclude: '&'
	   },
	   controller: function($scope) {
	       $scope.teaserParts = [];
	       var fields = $scope.fields();
	       var limit = $scope.limit ? $scope.limit() : 10000;
	       var exclude = $scope.exclude ? $scope.exclude() : [];
	       var excludeMap = {};
	       if (exclude) {
		       for (var i=0; i < exclude.length; i++) {
		    	   excludeMap[exclude[i]] = true;
		       }
	       }
	       if (fields) {
			    angular.forEach(fields, function(value, key) {
			    	if (!value) {
			    		return;
			    	}
			    	var type = LogSniffer.getFieldType(fields, key);
			    	if (excludeMap[key] || limit < 0 || !type || type=="OBJECT" || type=="LPOINTER") {
			    		return;
			    	}
			    	$scope.teaserParts.push({
			    		key: key,
			    		type: type,
			    		value: value,
			    		limit: limit
			    	});
			    	limit -= (value+"").length;
			    });
	       }
	   },
	   template: 
	      '<span class="fields-teaser">' +
		   	'<span ng-repeat="p in teaserParts" class="part">' +
	      		'<span class="label label-default">{{p.key}}</span> <lsf-print-field type="p.type" value="p.value" limit="p.limit"></lsf-print-field>' +
	      		'<span ng-if="!$last"> | </span>' +
	      	'</span>' +
		  '</span>'
       };
   })
.directive('lsfLogViewerFieldsSelection', function() {
    return {
	   restrict: 'AE',
	   replace: true,
	   scope: {
	       fieldTypes: '=',
	       configuredFields: '=',
	   },
	   controller: function($scope, $log) {
		   if (!$scope.configuredFields) {
			   $scope.configuredFields = [];
		   }
			$scope.$watch('fieldTypes', function(newValue, oldValue) {
				if (newValue) {
					var hasEnabled = false;
					for (var i=0;i<$scope.configuredFields.length;i++) {
						if ($scope.configuredFields[i].enabled) {
							hasEnabled = true;
							break;
						}
					}
					for (var f in newValue) {
						var insert = true;
						for (var i=0;i<$scope.configuredFields.length;i++) {
							if ($scope.configuredFields[i].key == f) {
								insert = false;
								break;
							}
						}
						if (insert) {
							$scope.configuredFields.push({
								key: f,
								type: newValue[f],
								enabled: !hasEnabled && !(!hasEnabled && f=="lf_raw")
							});
						}
					}
				}
			});
			
			$scope.deleteField = function(index) {
				$scope.configuredFields.splice(index, 1);
			};
			$scope.addNewField = function(field) {
				for (var i=0;i<$scope.configuredFields.length;i++) {
					if ($scope.configuredFields[i].key == $scope.newField) {
						$log.warn("Duplicate field name to add", $scope.newField);
						return;
					}
				}
				$scope.configuredFields.push({
					key: $scope.newField,
					type: "UNKNOWN",
					enabled: true
				});
				$scope.newField = "";
			};
			$scope.moveUpField = function(index) {
				if (index > 0) {
					var tmp = $scope.configuredFields[index-1];
					$scope.configuredFields[index-1] = $scope.configuredFields[index];
					$scope.configuredFields[index] = tmp;
				}
			};
			$scope.moveDownField = function(index) {
				if (index < $scope.configuredFields.length-1) {
					var tmp = $scope.configuredFields[index+1];
					$scope.configuredFields[index+1] = $scope.configuredFields[index];
					$scope.configuredFields[index] = tmp;
				}
			};
	   },
	   template: 
		      '<div><div class="panel panel-default">'+
		      	'<table class="attributes table table-condensed table-striped table-bordered entries">'+
		      		'<tr><th>Visible</th><th>Name</th><th>Type</th><th colspan="3">Actions</th></tr>'+
		      		'<tr ng-repeat="f in configuredFields">'+
	      				'<th><input type="checkbox" ng-model="f.enabled"></th>'+
		      			'<th class="text">{{f.key}}</th>'+
		      			'<td class="text">{{f.type}}</td>'+
		      				'<td style="width:1em;border-right:none"><button ng-if="!$first" class="btn btn-default" type="button" ng-click="moveUpField($index)"><i class="glyphicon glyphicon-chevron-up"></i></button></td>' +
		      				'<td style="width:1em;border-left:none;border-right:none"><button ng-if="!$last" class="btn btn-default" type="button" ng-click="moveDownField($index)"><i class="glyphicon glyphicon-chevron-down"></i></button></td>' +
		      				'<td style="width:1em;border-left:none;"><button class="btn btn-default" type="button" ng-click="deleteField($index)"><i class="glyphicon glyphicon-trash"></i></button></td>' +
		      		'</tr>'+
		      		'<tr><td></td><td><div class="input-group"><input type="text" ng-model="newField" placeholder="Add a new field" class="form-control"><div class="input-group-btn"><button class="btn btn-default" type="button" ng-click="addNewField(newField)"><i class="glyphicon glyphicon-plus"></i></button></div></td></tr>'+
		      	 '</table>'+
		      	'</div>'
    };
});