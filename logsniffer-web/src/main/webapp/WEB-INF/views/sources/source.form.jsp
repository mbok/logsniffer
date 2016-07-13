<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="backdrop-overlay" style="display:none"></div>
<t:ngNoteBindErrors ngIf="LogSniffer.objSize(bindErrors)>0"/>

<c:if test="${not empty message}">
	<div class="alert alert-success">
		<button type="button" class="close" data-dismiss="alert">&times;</button>
		${message}
	</div>
</c:if>

<script type="text/javascript">
	LogSnifferNgApp.controller(
			"SourceBeanWizardControllerWrapper",
			function($scope, $http, $log, $uibModal, lsfAlerts) {
				$scope.alerts = lsfAlerts.create();
				$scope.sourceWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.model.LogSource', pageContext.response.locale))};
				$scope.readerWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.reader.LogEntryReader', pageContext.response.locale))};
				$scope.readerFilterWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.fields.filter.FieldsFilter', pageContext.response.locale))};
				$scope.readerTestSession = {};
				$scope.resolvedTestLogs = null;
				$scope.dummy = {
					statefullName: $scope.beanWrapper[0]?$scope.beanWrapper[0].name:null
				};
				$scope.formValidation = {
					main: false,
					reader: false,
					filters: false,
					ui: false,
				};
				
				$scope.$watch('beanWrapper[0]', function(newValue, oldValue) {
					if (newValue) {
						newValue.name = $scope.dummy.statefullName;
					}
					if (oldValue && oldValue.reader && newValue) {
						newValue.reader = oldValue.reader;
					}
			    });
				$scope.nameChanged = function() {
					if ($scope.beanWrapper[0]) {
						$scope.beanWrapper[0].name = $scope.dummy.statefullName;
					}					
				};
				$scope.$watch('beanWrapper[0].name', function(newValue, oldValue) {
					$scope.dummy.statefullName = newValue;
			    });

				$scope.$watch('bindErrors', function(newValue, oldValue) {
					$scope.formValidation.main = !LogSniffer.hasKeysExpectOf(newValue, "reader", true);
					$scope.formValidation.reader = !LogSniffer.containsKey(newValue, ["reader.targetReader"], true);
					$scope.formValidation.filters = !LogSniffer.containsKey(newValue, ["reader.filters"], true);
			    });

				$scope.testResolvingLogs = function () {
				    $scope.resolvingTestLogsError = false;
					$scope.resolvingTestLogsInProgress = true;
				    $scope.resolvedTestLogs = null;
				    var testSource = angular.copy($scope.beanWrapper[0]);
				    testSource.reader = null;
				    $log.info("Resolving logs for source: ", testSource);
					$http({
					    url: $scope.contextPath + "/c/sources/logs",
					    method: "POST",
					    data: testSource
					}).success(function(data, status, headers, config) {
						$log.info("Got resolved logs: ", data);
						$scope.resolvingTestLogsInProgress = false;
						$scope.resolvedTestLogs = data;
					}).error(function(data, status, headers, config, statusText) {
					    $scope.resolvingTestLogsError = true;
						$scope.resolvingTestLogsInProgress = false;
						if (data && data.bindErrors) {
							$scope.bindErrors = data.bindErrors;
						}
					});
				};
				
				$scope.testLogViewing = function () {
					$uibModal.open({
				      templateUrl: $scope.contextPath + '/ng/source/readerTest.html',
				      controller: 'ReaderTestCtrl',
				      size: 'lg',
				      windowClass: 'reader-test-modal',
				      scope: $scope,
				      resolve: {
				        source: function () {
							$log.info("Inject to reader test the source: ", $scope.beanWrapper[0]);
							return $scope.beanWrapper[0];
				        },
				        reader: function () {
							$log.info("Inject to reader test the reader: ", $scope.beanWrapper[0].reader);
							return $scope.beanWrapper[0].reader;
				        },
				        title: function () {
							return "Test log viewing";   
			        	},
			        	testSession: function () {
			        	    return $scope.readerTestSession;
			        	}
				      }
				    });
			    };

				$scope.addReaderFilter = function() {
					if (!angular.isArray($scope.beanWrapper[0].reader.filters)) {
						$scope.beanWrapper[0].reader.filters = [];
					}
				    $scope.beanWrapper[0].reader.filters.push({});  
				};

				$scope.deleteReaderFilter = function(index) {
				    $scope.beanWrapper[0].reader.filters.splice(index, 1);
				};
				
				$scope.mainFormValid = function(form, valid) {
					$scope.formValidation.main = valid;
				};
				$scope.readerFormValid = function(form, valid) {
					$scope.formValidation.reader = valid;
				};
				$scope.filtersFormValid = function(form, valid) {
					$scope.formValidation.filters = valid;
				};
				$scope.uiFormValid = function(form, valid) {
					$scope.formValidation.ui = valid;
				};
				
				
				$scope.enableFieldsVisibility = function() {
					if (!$scope.beanWrapper[0].uiSettings) {
						$scope.beanWrapper[0].uiSettings = {};
					}
					$scope.beanWrapper[0].uiSettings.viewerFields = [];
					$scope.reloadPotentialFields();
				};
				
				$scope.reloadPotentialFields = function() {
					$log.info("Resolving potential fields for source: ", $scope.beanWrapper[0]);
					$scope.loadingPotentialFields = true;
					$http({
			        		url : $scope.contextPath + "/c/sources/potentialFields",
			        		method : "POST",
			        		data: $scope.beanWrapper[0]
			        	})
			        	.success(
			        		function(data, status, headers, config) {
			        		    $scope.loadingPotentialFields = false;
			        		    $scope.potentialFields = data;
			        		    $log.info("Potential fields loaded: ", $scope.potentialFields);
			        		})
			        	.error(
			        		function(data, status, headers, config, statusText) {
			        		    $scope.loadingPotentialFields = false;
			        		    $scope.alerts.httpError("Failed to load known fields, please check the log source configuration", data, status, headers, config, statusText);
			        		}
			        	);
				};

				$scope.disableFieldsVisibility = function() {
					$scope.beanWrapper[0].uiSettings.viewerFields = null;
				};
			});
</script>
<div id="source-editor" ng-controller="SourceBeanWizardControllerWrapper" ng-form="form2">
	<div lsf-alerts alerts="alerts"></div>
	<lsf-model-editor model="beanWrapper[0]" name="Overall log source configuration" exclude="['fieldTypes','supportedSeverities','readerConfigurable','navigationType']"></lsf-model-editor>
	<uib-tabset>
    	<uib-tab>
    		<uib-tab-heading>
				Main <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.main, 'glyphicon-remove-circle': !formValidation.main}"></i>
			</uib-tab-heading>
			<div ng-form="mainForm">
				<div ng-form="form">
					<lsf-form-valid-observer form="mainForm" on-valid-change="mainFormValid" />
					<div class="row">
						<div 
							class="col-md-6 form-group" ng-class="{'has-error': form.name.$invalid && !form.name.$pristine || bindErrors.name && form.name.$pristine}">
							<label class="control-label" for="name">Name*:</label>
							<div class="controls">
						        <input type="text" ng-model="dummy.statefullName" ng-change="nameChanged()" name="name" id="name" class="form-control" placeholder="Name" required>
						    </div>
						    <div class="help-block" ng-if="bindErrors.name && form.name.$pristine">{{bindErrors.name}}</div>
					    </div>
					</div>
					<!-- Wizard -->
					<lfs-bean-wizard bean="beanWrapper[0]" bean-type-label="Source" wizards="sourceWizards"
						shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="" model-exclude="['reader','uiSettings','id','readerConfigurable','navigationType']">
						<button type="button" class="btn btn-default btn-xs" ng-click="testResolvingLogs()" ng-disabled="form.$invalid">
							<i class="glyphicon glyphicon-check"></i> Test resolving logs
						</button> <i class="fa" ng-class="{'fa-refresh fa-spin': resolvingTestLogsInProgress}"></i>
						<div class="alert alert-success animate-show" ng-show="resolvedTestLogs.length>0">
							<h4>Resolved logs:</h4>
							<ol>
								<li ng-repeat="log in resolvedTestLogs">{{log.path!='default'?log.path:log.name}} ({{log.size | bytesToSize}})
									<label ng-if="log['@type']=='rolling'" class="blocked">Log parts:</label>
									<ol ng-if="log['@type']=='rolling'">
										<li ng-repeat="part in log.parts">{{part.path!='default'?part.path:part.name}} ({{part.size | bytesToSize}})</li>
									</ol>
								</li>
							</ol>
						</div>
						<div class="alert alert-warning animate-show" ng-show="resolvedTestLogs.length==0"><h4>No logs resolved!</h4>Please check for misconfiguration.</div>
						<div class="alert alert-danger animate-show" ng-show="resolvingTestLogsError"><h4>Error occurred!</h4>See above errors for more details.</div>
					</lfs-bean-wizard>
				</div>
			</div>
		</uib-tab>

		<uib-tab ng-if="beanWrapper[0]['@type'] && beanWrapper[0].readerConfigurable">
    		<uib-tab-heading>
				Reader <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.reader, 'glyphicon-remove-circle': !formValidation.reader}"></i>
			</uib-tab-heading>
			<!-- TODO if reader is configurable -->
			<div ng-form="readerForm">
				<div id="log-reader-editor" ng-if="beanWrapper[0]['@type']" ng-form="form">
					<lsf-form-valid-observer form="readerForm" on-valid-change="readerFormValid" />
					<h4>Log entry reader</h4>
					<lfs-bean-wizard bean="beanWrapper[0].reader.targetReader" bean-type-label="Reader" wizards="readerWizards"
						shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="reader.targetReader." model-exclude="['fieldTypes', 'supportedSeverities']">
					</lfs-bean-wizard>
				</div>
			</div>
		</uib-tab>

	<!-- Filters -->
	<script type="text/javascript">
	LogSnifferNgApp.controller(
		"SourceReaderFilterHelpController",
		function($scope, $http, $log) {
		    $scope.index = $scope.$index;
		    $scope.$watch('filter', function(newValue, oldValue) {
				$scope.beanWrapper[0].reader.filters[$scope.index] = newValue;
				$log.info("Update filter", $scope.index, newValue);
			}, true);
		}
	);
	</script>
		<uib-tab ng-if="beanWrapper[0]['@type']">
    		<uib-tab-heading>
				Filters <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.filters, 'glyphicon-remove-circle': !formValidation.filters}"></i>
			</uib-tab-heading>
			<div ng-form="filtersForm">
				<div id="source-reader-filters" ng-if="beanWrapper[0]['@type']" ng-form="form">
					<lsf-form-valid-observer form="filtersForm" on-valid-change="filtersFormValid" />
					<h4>Filters
						<small>Used to filter log entries e.g. for field transformation, normalization etc.</small></h4>
					<div class="panel panel-default" ng-repeat="filter in beanWrapper[0].reader.filters">
						<div class="panel-heading">
							<button type="button" class="close pull-right" title="Delete" ng-click="deleteReaderFilter($index)"><i class="glyphicon glyphicon-trash"></i></button>
							<h3 class="panel-title">Filter {{$index+1}}</h3>
						</div>
						<div class="panel-body" ng-form="form">
							<div ng-controller="SourceReaderFilterHelpController">
								<lfs-bean-wizard bean="filter" bean-type-label="Filter" wizards="readerFilterWizards"
									shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="reader.filters[{{$index}}].">
								</lfs-bean-wizard>
							</div>
						</div>
					</div>
					<div class="row post-desc">
						<div class="col-md-12">
							<a class="btn btn-link" ng-click="addReaderFilter()">
								<i class="glyphicon glyphicon-plus"></i> Add new filter</a>
						</div>
					</div>
				</div>
			</div>
		</uib-tab>
		<uib-tab ng-if="beanWrapper[0]['@type']">
    		<uib-tab-heading>
				UI settings <i class="glyphicon muted" ng-class="{'glyphicon-ok-circle': formValidation.ui, 'glyphicon-remove-circle': !formValidation.ui}"></i>
			</uib-tab-heading>
			<div ng-form="uiForm">
				<div id="source-ui" ng-form="form">
					<lsf-form-valid-observer form="uiForm" on-valid-change="uiFormValid" />
					<lsf-model-editor model="beanWrapper[0].uiSettings" name="Viewer fields"></lsf-model-editor>
					<h4>Viewer fields <small>Configures fields which should be visible by default in the viewer</small></h4>
					<div ng-if="beanWrapper[0].uiSettings.viewerFields">
						<lsf-busy-container busy="loadingPotentialFields">
							<lsf-log-viewer-fields-selection 
								field-types="potentialFields" 
								configured-fields="beanWrapper[0].uiSettings.viewerFields"></lsf-log-viewer-fields-selection>					
							<div class="row">
								<div class="col-md-12">
									<button type="button" class="btn btn-default btn-xs" ng-click="reloadPotentialFields()" ng-disabled="form2.$invalid">
										<i class="glyphicon glyphicon-repeat"></i> Refresh known fields
									</button>
									<button type="button" class="btn btn-default btn-xs" ng-click="disableFieldsVisibility()">
										<i class="glyphicon glyphicon-off"></i> Disable field visibility settings
									</button>
							 	</div>
							 </div>
						</lsf-busy-container>
					</div>
					<div ng-if="!beanWrapper[0].uiSettings.viewerFields">
						<p class="alert alert-info">No fields visibility configured, thus all fields will be rendered in the viewer.</p>
						
						<div class="row">
							<div class="col-md-12">
								<button type="button" class="btn btn-default btn-xs" ng-click="enableFieldsVisibility()" ng-disabled="form2.$invalid">
									<i class="glyphicon glyphicon-wrench"></i> Enable field visibility settings
								</button>
						 	</div>
						 </div>
					</div>
				</div>
			</div>

		</uib-tab>
	</uib-tabset>
	<hr>

	<div class="row">
		<div class="col-md-12" us-spinner spinner-key="update">
			<button type="button" class="btn btn-primary" ng-disabled="form2.$invalid" ng-click="submit(form2)">${param.submitLabel}</button>
			<button type="button" class="btn btn-default btn-sm" ng-click="testLogViewing()" ng-disabled="form2.$invalid">
				<i class="glyphicon glyphicon-check"></i> Test log viewing
			</button>
		</div>
	</div>
</div>