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
            if (!max) return value;
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
	       excludeFields: '='
	   },
	   controller: function($scope, $log) {
		    $scope.rows = null;

		    $scope.getFieldType = function (fieldName) {
			var fields = $scope.fields;
			if (fields && fields["@types"] && fields["@types"][fieldName]) {
			    return fields["@types"][fieldName];
			} else {
			    $log.warn("Unknown field type for", fieldName);
			    return "UNKNOWN";
			}
		    };
		    var ignoreFields = {
		    	"lf_startOffset": true,
		    	"lf_endOffset": true,
		    	"@types": true
		    };
		    if ($scope.excludeFields) {
		    	for (var i=0; i<$scope.excludeFields.length; i++) {
		    		ignoreFields[$scope.excludeFields[i]] = true;
		    	}
		    }
		    angular.forEach($scope.fields, function(value, key) {
			if (!$scope.rows) {
			    $scope.rows = [];
			}
		        if (!($scope.excludeRaw && key=="lf_raw") && !ignoreFields[key]) {
		            $scope.rows.push({
		        	name: key,
		        	value: value,
		        	type: $scope.getFieldType(key)
		            });
		        }
		    });
	   },
	   template: 
	      '<div><div class="panel panel-default" ng-if="rows">'+
	      	'<table class="attributes table table-condensed table-striped table-bordered entries">'+
	      		'<tr ng-repeat="row in rows | orderBy: \'name\'">'+
	      			'<th class="text">{{row.name}}</th>'+
	      			'<td ng-class="row.type" ng-switch="row.type">'+
	      				'<div ng-switch-when="SEVERITY" class="text"><span class="label label-default severity sc-{{row.value.c}}">{{row.value.n}}</span></div>'+
	      				'<div ng-switch-when="DATE" class="text">{{row.value | date:\'medium\'}}</div>'+
	      				'<div ng-switch-when="OBJECT" class="text"><json-formatter json="row.value" open="1"></json-formatter></div>'+
	      				'<div ng-switch-default class="text">{{row.value}}</div>'+
	      			'</td>'+
	      		'</tr>'+
	      	 '</table>'+
	      	'</div>'+
	      	'<p ng-if="!rows" class="text-muted"> - no fields -</p></div>'
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
			return LogSniffer.config.contextPath + selectedWizard.view;
		    } else {			
			return LogSniffer.config.contextPath + '/c/wizards/view?type=' + selectedWizard.beanType;
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
   .directive('lsfLogViewer', ['$timeout', '$location', '$anchorScroll', '$log', '$http', 'lsfAlerts', function($timeout, $location, $anchorScroll, $log, $http, lsfAlerts) {
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
	       fixTopElementSelector: '@',
	       pointerTpl: '&',
	       initTail: '&',
	       searchWizards: '&',
	       searchScanner: '&',
	       searchFound: '&',
	       followDisabled: '&',
	       searchExpanded: '&',
	       onError: '&'
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
	   },
	   link: function(scope, element, attrs, $timeout) {
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
		
		function emptyViewerEntries() {
		    $.LogSniffer._viewerEntries[viewerEntriesId] = {};
		    logEntries = $.LogSniffer._viewerEntries[viewerEntriesId];
		};
		function renderTableHead(fieldTypes) {
		    scope.fieldTypes = fieldTypes;
		    var entriesTableHead=$.LogSniffer.entriesHead(fieldTypes, function() {return '<th style="width:18px"></th>';});
		    $(element).find(".log-entries thead").html(entriesTableHead);
		};

		scope.getLoadLogEntriesUrl = function(mark, count, urlSuffix) {
			return LogSniffer.config.contextPath + '/c/sources/'+ scope.source.id +'/'+(urlSuffix?urlSuffix:'entries')+'?log=' + encodeURIComponent(scope.log.path) +'&mark='+ mark +'&count=' + count;
		};
		
		scope.handleHttpError = function(context, data, status, headers, config, statusText) {
		    if (angular.isFunction(scope.onError)) {
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
			var y = Math.max(scope.fixTopElementSelector ? $(scope.fixTopElementSelector).height() + 1 : 0, entriesOffset.top + entriesPadding / 2 - $(window).scrollTop());
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
				    url: LogSniffer.config.contextPath + '/c/sources/'+ scope.source.id +'/randomAccessEntries?log=' + encodeURIComponent(scope.log.path) +'&mark=' + encodeURIComponent(pStr),
				    method: "GET"
				});
			} else {
			    entriesCall = $http({
				    url: LogSniffer.config.contextPath + '/c/sources/randomAccessEntries?log=' + encodeURIComponent(scope.log.path) +'&mark=' + encodeURIComponent(pStr),
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
			    	$(element).find('.log-entries tbody').empty().append($.LogSniffer.entriesRows(data.fieldTypes, data.entries, renderPrefixCells));
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
					$(element).find('.log-entries tbody').append($.LogSniffer.entriesRows(scope.fieldTypes, entries, renderPrefixCells));
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
						$(element).find('.log-entries tbody').append($.LogSniffer.entriesRows(scope.fieldTypes, data.entries, renderPrefixCells));
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
					$(element).find('.log-entries tbody').prepend($.LogSniffer.entriesRows(scope.fieldTypes, data.entries, renderPrefixCells));
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
					$(element).find('.log-entries tbody').append($.LogSniffer.entriesRows(scope.fieldTypes, data.entries, renderPrefixCells));
					console.log("Jump to log tail: "+scrollOnBottom);
					if (scrollOnBottom || trigger) {
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
	   },
	   templateUrl: LogSniffer.config.contextPath + '/ng/entry/logViewer.html'
       };
   }])
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
	       bindErrors: '='
	   },
	   template: 
	      '<div ng-class="{\'has-error\': $parent.form[fieldName].$invalid || bindErrors[fieldPath]}">' +
	      '	<div ng-transclude></div><div class="help-block" ng-if="bindErrors[fieldPath]">{{bindErrors[fieldPath]}}</div>' +
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
	       $scope.remove = function (index) {
		   $scope.alerts.remove(index);
	       };
	   },
	   template: 
	      '<div ng-repeat="alert in alerts.alerts" class="alert alert-{{alert.type}}">'+
	      // '<a href class="close" ng-click="mother.remove($index)">&times;</a>'+
	      '{{alert.message}}'+
	       '<div ng-if="alert.detail"><div class="log" ng-show="expanded" style="margin:1em 0"><div class="text nowrap" style="overflow-x: auto">{{alert.detail}}</div></div><a href="#" ng-click="expanded=!expanded" onclick="return false"><i class="glyphicon" ng-class="{\'glyphicon-chevron-down\':!expanded,\'glyphicon-chevron-up\':expanded}"></i> <span ng-if="!expanded">show details</span><span ng-if="expanded">hide details</span></a></div>'+
	      '</div>'
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
   });