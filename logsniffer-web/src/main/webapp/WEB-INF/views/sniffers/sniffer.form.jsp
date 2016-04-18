<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:if test="${scheduled}">
	<div class="alert alert-warning" role="alert">
	  	<h4>Editing disabled</h4>
		As long as monitoring is active, you are not able to edit the settings. Stop monitoring to continue.
	</div>
</c:if>

<div class="backdrop-overlay" style="display:none"></div>
<t:ngNoteBindErrors ngIf="LogSniffer.objSize(bindErrors)>0"/>

<c:if test="${not empty message}">
	<div class="alert alert-success">
		<button type="button" class="close" data-dismiss="alert">&times;</button>
		${message}
	</div>
</c:if>
<div mc-messages></div>
<script type="text/javascript">
	LogSnifferNgApp.controller(
		"SnifferBeanWizardControllerWrapper",
		function($scope, $http, $log, $uibModal) {
		    $scope.scannerWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.Scanner', pageContext.response.locale))};
			$scope.readerStrategyWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.LogEntryReaderStrategy', pageContext.response.locale))};
		    $scope.publisherWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.Publisher', pageContext.response.locale))};
			$scope.scannerFilterWizards=${logfn:jsonify(
					logfn:mergeLists(
						logfn:wizardsInfo('com.logsniffer.fields.filter.FieldsFilter', pageContext.response.locale),
						logfn:wizardsInfo('com.logsniffer.event.filter.EventFilter', pageContext.response.locale)
					)
				)};
		    $scope.sharedScope = {};
		    $scope.testSession = {};
			$scope.formValidation = {
				main: false,
				scanner: false,
				publishers: false
			};

			var sourcesUrl = '<c:url value="/c/sources" />';
			$scope.loadSources = function() {
				$scope.availableSources=[];
				$scope.sourcesLoading = true;
				$scope.sourcesLoadingError = false;
				$http({
				    url: sourcesUrl,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					$log.info("Got available sources: length=" + data.length);
					$scope.availableSources = data;
					for(var i=0;i<$scope.availableSources.length;i++) {
						if ($scope.availableSources[i].id==$scope.bean.logSourceId) {
							$scope.source=$scope.availableSources[i];
							$scope.sharedScope.source = $scope.source;
							break;
						}
					};
					$scope.sourceHelpInfo=null;
					$scope.sourcesLoading = false;
				}).error(function(data, status, headers, config) {
					$scope.sourcesLoading = false;
					$scope.sourcesLoadingError = true;
				});
			};
			$scope.loadSources();

			$scope.$watch('bean.logSourceId', function(newValue, oldValue) {
				$scope.source=null;
				$scope.testSession = {};
				$log.info("Exposing new source: " + newValue);
				for(var i=0;i<$scope.availableSources.length;i++) {
					if ($scope.availableSources[i].id==newValue) {
						$scope.source=$scope.availableSources[i];
						$scope.sharedScope.source = $scope.source;
						break;
					}
				};
		    });
			
			$scope.addPublisher = function() {
			    if (!$scope.bean.publishers) {
					$scope.bean.publishers = [];
			    }
				$scope.bean.publishers.push({});  
			};

			$scope.deletePublisher = function(index) {
				$scope.bean.publishers.splice(index, 1);
			};
			
			$scope.testPublisher = function (publisher) {
				$uibModal.open({
			      templateUrl: $scope.contextPath + '/ng/sniffer/snifferTest.html',
			      controller: 'SnifferTestCtrl',
			      size: 'lg',
			      windowClass: 'sniffer-test-modal',
			      scope: $scope,
			      resolve: {
			        source: function () {
						$log.info("Inject to publisher test the source: ", $scope.source);
						return $scope.source;
			        },
			        scanner: function () {
						$log.info("Inject to publisher test the scanner: ", $scope.bean.scanner);
						return $scope.bean.scanner;
			        },
			        publisher: function () {
						$log.info("Inject to publisher for test: ", publisher);
						return publisher;			            
			        },
			        sniffer: function () {
						$log.info("Inject to publisher the sniffer: ", $scope.bean);
			            return $scope.bean;
			        },
			        title: function () {
						return "Publisher test";   
		        	},
		        	testSession: function () {
		        	    return $scope.testSession;
		        	}
			      }
			    });
		    };
		    
			$scope.testScanner = function () {
				$uibModal.open({
			      templateUrl: $scope.contextPath + '/ng/sniffer/snifferTest.html',
			      controller: 'SnifferTestCtrl',
			      size: 'lg',
			      windowClass: 'sniffer-test-modal',
			      scope: $scope,
			      resolve: {
			        source: function () {
						$log.info("Inject to sniffer test the source: ", $scope.source);
						return $scope.source;
			        },
			        scanner: function () {
						$log.info("Inject to sniffer test the scanner: ", $scope.bean.scanner);
						return $scope.bean.scanner;
			        },
			        sniffer: function () {
						$log.info("Inject to sniffer test the sniffer: ", $scope.bean);
			            return $scope.bean;
			        },
			        publisher: function () {
						return null;			            
			        },
			        title: function () {
						return "Scanner test";   
			        },
		        	testSession: function () {
		        	    return $scope.testSession;
		        	}
			      }
			    });
		    };
		    

			$scope.addScannerFilter = function() {
			    $scope.bean.scanner.filters.push({});  
			};

			$scope.deleteScannerFilter = function(index) {
			    $scope.bean.scanner.filters.splice(index, 1);
			};

			$scope.mainFormValid = function(form, valid) {
				$scope.formValidation.main = valid;
			};
			$scope.scannerFormValid = function(form, valid) {
				$scope.formValidation.scanner = valid;
			};
			$scope.publishersFormValid = function(form, valid) {
				$scope.formValidation.publishers = valid;
			};
			$scope.$watch('bindErrors', function(newValue, oldValue) {
				$scope.formValidation.main = !LogSniffer.hasKeysExpectOf(newValue, ["scanner.", "publishers["], true);
				$scope.formValidation.scanner = !LogSniffer.containsKey(newValue, ["scanner."], true);
				$scope.formValidation.publishers = !LogSniffer.containsKey(newValue, ["publishers["], true);
		    });

		});

		SnifferEditorModule.controller(
			"ScannerFilterHelpController",
			function($scope, $http, $log) {
			    $scope.index = $scope.$index;
			    $scope.$watch('filter', function(newValue, oldValue) {
					$scope.bean.scanner.filters[$scope.index] = newValue;
					$log.info("Update filter", $scope.index, newValue);
				}, true);
			}
		);
</script>
	
<fieldset id="sniffer-editor" ng-controller="SnifferBeanWizardControllerWrapper" ng-disabled="${scheduled}" ng-form="rootForm">
	<lsf-model-editor model="bean" name="Overall sniffer configuration" exclude="['aspects','fieldTypes','supportedSeverities']"></lsf-model-editor>
	<uib-tabset>
    	<uib-tab heading="Main">
	   		<uib-tab-heading>
				Main <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.main, 'glyphicon-remove-circle': !formValidation.main}"></i>
			</uib-tab-heading>
	    	<div ng-form="mainForm">
	    		<lsf-form-valid-observer form="mainForm" on-valid-change="mainFormValid" />
	    		<div ng-form="form">
					<div class="row">
						<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="name">
							<label for="name" class="control-label">Name:</label>
							<div class="controls">
								<input ng-model="bean.name" name="name" id="name" class="form-control" placeholder="Name" required/>
							</div>
						</t:ngFormFieldWrapper>
					</div>
					<div class="row">
						<t:ngFormFieldWrapper cssClass="form-group col-md-6 required" fieldName="logSourceId">
							<label for="logSourceId" class="control-label">Log source:</label>
							<div class="input-group">
								<div class="input-group-addon">
									<a href="#" ng-click="loadSources()" title="Reload log sources"><i class="glyphicon glyphicon-refresh" ng-class="{'spin':sourcesLoading}"></i></a>
								</div>
								<select ng-model="bean.logSourceId" name="logSourceId" id="logSourceId" class="form-control" required
									ng-options="s.id as s.name for s in availableSources">
									<option value=""><spring:message code="logsniffer.common.pleaseSelect" /></option>
								</select>
							</div>
							<div class="has-error" ng-if="sourcesLoadingError"><div class="help-block"><i class="glyphicon glyphicon-warning-sign"></i> Failed to load log sources</div></div>
						</t:ngFormFieldWrapper>
						<t:ngFormFieldWrapper cssClass="form-group col-md-6 required" fieldName="scheduleCronExpression">
							<label for="scheduleCronExpression" class="control-label">Execute every:</label>
							<div class="controls">
								<select ng-model="bean.scheduleCronExpression" name="scheduleCronExpression" id="scheduleCronExpression" class="form-control" required>
									<option value=""><spring:message code="logsniffer.common.pleaseSelect" /></option>
									<optgroup label="Second cycle">
										<option value="*/5 * * ? * * *">5 Seconds</option>
										<option value="*/10 * * ? * * *">10 Seconds</option>
										<option value="*/15 * * ? * * *">15 Seconds</option>
										<option value="*/30 * * ? * * *">30 Seconds</option>
									</optgroup>
									<optgroup label="Minute cycle">
										<option value="0 */1 * ? * * *">1 Minute</option>
										<option value="0 */2 * ? * * *">2 Minutes</option>
										<option value="0 */3 * ? * * *">3 Minutes</option>
										<option value="0 */4 * ? * * *">4 Minutes</option>
										<option value="0 */5 * ? * * *">5 Minutes</option>
										<option value="0 */10 * ? * * *">10 Minutes</option>
										<option value="0 */15 * ? * * *">15 Minutes</option>
										<option value="0 */20 * ? * * *">20 Minutes</option>
										<option value="0 */30 * ? * * *">30 Minutes</option>
										<option value="0 */40 * ? * * *">40 Minutes</option>
										<option value="0 */50 * ? * * *">50 Minutes</option>
									</optgroup>
									<optgroup label="Hour cycle">
										<option value="0 0 */1 ? * * *">1 Hour</option>
										<option value="0 0 */2 ? * * *">2 Hours</option>
										<option value="0 0 */3 ? * * *">3 Hours</option>
										<option value="0 0 */4 ? * * *">4 Hours</option>
										<option value="0 0 */5 ? * * *">5 Hours</option>
										<option value="0 0 */6 ? * * *">6 Hours</option>
									</optgroup>
									<optgroup label="Day cycle">
										<option value="0 0 0 * * ? *">Daily at 00:00</option>
										<option value="0 0 1 * * ? *">Daily at 01:00</option>
										<option value="0 0 2 * * ? *">Daily at 02:00</option>
										<option value="0 0 3 * * ? *">Daily at 03:00</option>
										<option value="0 0 4 * * ? *">Daily at 04:00</option>
										<option value="0 0 5 * * ? *">Daily at 05:00</option>
										<option value="0 0 6 * * ? *">Daily at 06:00</option>
										<option value="0 0 7 * * ? *">Daily at 07:00</option>
										<option value="0 0 8 * * ? *">Daily at 08:00</option>
										<option value="0 0 9 * * ? *">Daily at 09:00</option>
										<option value="0 0 10 * * ? *">Daily at 10:00</option>
										<option value="0 0 11 * * ? *">Daily at 11:00</option>
										<option value="0 0 12 * * ? *">Daily at 12:00</option>
										<option value="0 0 13 * * ? *">Daily at 13:00</option>
										<option value="0 0 14 * * ? *">Daily at 14:00</option>
										<option value="0 0 15 * * ? *">Daily at 15:00</option>
										<option value="0 0 16 * * ? *">Daily at 16:00</option>
										<option value="0 0 17 * * ? *">Daily at 17:00</option>
										<option value="0 0 18 * * ? *">Daily at 18:00</option>
										<option value="0 0 19 * * ? *">Daily at 19:00</option>
										<option value="0 0 20 * * ? *">Daily at 20:00</option>
										<option value="0 0 21 * * ? *">Daily at 21:00</option>
										<option value="0 0 22 * * ? *">Daily at 22:00</option>
										<option value="0 0 23 * * ? *">Daily at 23:00</option>
									</optgroup>
								</select>
							</div>
						</t:ngFormFieldWrapper>		
					</div>
				
				
					<!-- Reader strategy wizard -->
					<div id="sniffer-reader-strategy-editor">
						<h4>Log reader strategy <small>Defines how much of the log should be scanned consecutively per iteration</small></h4>
						<lfs-bean-wizard bean="bean.readerStrategy" bean-type-label="Log reader strategy" wizards="readerStrategyWizards"
							shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="readerStrategy."></lfs-bean-wizard>
					</div>
				</div>
			</div>
	</uib-tab>

	<!-- Scanner wizard -->
	<uib-tab>
   		<uib-tab-heading>
			Scanner <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.scanner, 'glyphicon-remove-circle': !formValidation.scanner}"></i>
		</uib-tab-heading>
		<div id="sniffer-scanner-editor" ng-form="scannerForm">
			<lsf-form-valid-observer form="scannerForm" on-valid-change="scannerFormValid" />
			<div ng-form="form">
				<h4>Event scanner configuration
					<small>Configures the scanner sniffing the log consecutively for new events</small></h4>
				<lfs-bean-wizard bean="bean.scanner.targetScanner" bean-type-label="Scanner" wizards="scannerWizards"
					shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="scanner.targetScanner."
					model-exclude="['fieldTypes']">
				</lfs-bean-wizard>
		
				<!-- Filters -->
				<div id="scanner-filters">
					<h4>Filters
						<small>Used to filter events e.g. for field transformation, normalization etc.</small></h4>
					<div class="panel panel-default" ng-repeat="filter in bean.scanner.filters">
						<div class="panel-heading">
							<button type="button" class="close pull-right" title="Delete" ng-click="deleteScannerFilter($index)"><i class="glyphicon glyphicon-trash"></i></button>
							<h3 class="panel-title">Filter {{$index+1}}</h3>
						</div>
						<div class="panel-body" ng-form="form">
							<div ng-controller="ScannerFilterHelpController">
								<lfs-bean-wizard bean="filter" bean-type-label="Filter" wizards="scannerFilterWizards"
									shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="scanner.filters[{{$index}}].">
								</lfs-bean-wizard>
							</div>
						</div>
					</div>
					<div class="row post-desc">
						<div class="col-md-12">
							<a class="btn btn-link" ng-click="addScannerFilter()">
								<i class="glyphicon glyphicon-plus"></i> Add new filter</a>
						</div>
					</div>
				</div>
		
				<div class="row">
					<div class="col-md-12">
						<button type="button" class="btn btn-default btn-sm" ng-click="testScanner()" ng-disabled="scannerForm.$invalid || !bean.logSourceId"><i class="glyphicon glyphicon-check"></i> Test scanning</button>
					</div>
				</div>
			</div>
		</div>
	</uib-tab>
	
	<!-- Publishers -->
	<script type="text/javascript">
	SnifferEditorModule.controller(
		"SnifferPublisherHelpController",
		function($scope, $http, $log) {
		    $scope.index = $scope.$index;
		    $scope.$watch('publisher', function(newValue, oldValue) {
				$scope.$parent.bean.publishers[$scope.index] = newValue;
				$log.info("Update publisher", $scope.index, newValue);
			}, true);
		}
	);
	</script>
	<uib-tab>
   		<uib-tab-heading>
			Publishers <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.publishers, 'glyphicon-remove-circle': !formValidation.publishers}"></i>
		</uib-tab-heading>
		<div id="sniffer-publishers" ng-form="publishersForm">
			<lsf-form-valid-observer form="publishersForm" on-valid-change="publishersFormValid" />
			<div ng-form="form">
				<h4>Publishers
					<small>Used to publish events additionally to further channels like mail, web etc.</small></h4>
				<div class="panel panel-default" ng-repeat="publisher in bean.publishers">
					<div class="panel-heading">
						<button type="button" class="close pull-right" title="Delete" ng-click="deletePublisher($index)"><i class="glyphicon glyphicon-trash"></i></button>
						<h3 class="panel-title">Publisher {{$index+1}}</h3>
					</div>
					<div class="panel-body" ng-form="form">
						<div ng-controller="SnifferPublisherHelpController">
							<lfs-bean-wizard bean="publisher" bean-type-label="Publisher" wizards="publisherWizards"
								shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="publishers[{{$index}}].">{{$parent.bean.logSourceId}}
								<button type="button" class="btn btn-default btn-xs" ng-click="testPublisher(publisher)" ng-disabled="form.$invalid || scannerForm.$invalid || !sharedScope.source"><i class="glyphicon glyphicon-check"></i> Test publishing</button>
							</lfs-bean-wizard>
						</div>
					</div>
				</div>
				<div class="row post-desc">
					<div class="col-md-12">
						<a class="btn btn-link" ng-click="addPublisher()">
							<i class="glyphicon glyphicon-plus"></i> Add new publisher</a>
					</div>
				</div>
			</div>
		</div>
	</uib-tab>
</fieldset>