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

<h4>Main</h4>
<script type="text/javascript">
	LogSnifferNgApp.controller(
			"SourceBeanWizardControllerWrapper",
			function($scope, $http, $log, $modal) {
				$scope.sourceWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.model.LogSource', pageContext.response.locale))};
				$scope.readerWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.reader.LogEntryReader', pageContext.response.locale))};
				$scope.readerFilterWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.reader.filter.FieldsFilter', pageContext.response.locale))};
				$scope.readerTestSession = {};
				$scope.resolvedTestLogs = null;
				
				$scope.$watch('beanWrapper[0]', function(newValue, oldValue) {
					if (newValue) {
						newValue.name = $scope.statefullName;
					}
			    });
				$scope.$watch('statefullName', function(newValue, oldValue) {
					if ($scope.beanWrapper[0]) {
						$scope.beanWrapper[0].name = newValue;
					}
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
					$modal.open({
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
				    $scope.beanWrapper[0].reader.filters.push({});  
				};

				$scope.deleteReaderFilter = function(index) {
				    $scope.beanWrapper[0].reader.filters.splice(index, 1);
				};

			});
</script>
<div id="source-editor" ng-controller="SourceBeanWizardControllerWrapper" ng-form="form2">
	<div ng-init="statefullName=beanWrapper[0].name" ng-form="form">
		<div class="row">
			<div 
				class="col-md-6 form-group" ng-class="{'has-error': form.name.$invalid && !form.name.$pristine || bindErrors.name && form.name.$pristine}">
				<label class="control-label" for="name">Name*:</label>
				<div class="controls">
			        <input type="text" ng-model="statefullName" name="name" id="name" class="form-control" placeholder="Name" required>
			    </div>
			    <div class="help-block" ng-if="bindErrors.name && form.name.$pristine">{{bindErrors.name}}</div>
		    </div>
		</div>
		<!-- Wizard -->
		<lfs-bean-wizard bean="beanWrapper[0]" bean-type-label="Source type" wizards="sourceWizards"
			shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="">
			
			<button type="button" class="btn btn-default btn-xs" ng-click="testResolvingLogs()" ng-disabled="form.$invalid">
				<i class="glyphicon glyphicon-check"></i> Test resolving logs
			</button> <i class="fa" ng-class="{'fa-refresh fa-spin': resolvingTestLogsInProgress}"></i>
			<div class="alert alert-success animate-show" ng-show="resolvedTestLogs.length>0">
				<h4>Resolved logs:</h4>
				<ol>
					<!-- TODO: Rolling logs -->
					<li ng-repeat="log in resolvedTestLogs">{{log.path}} ({{log.size | bytesToSize}})
						<label ng-if="log['@type']=='rolling'" class="blocked">Log parts:</label>
						<ol ng-if="log['@type']=='rolling'">
							<li ng-repeat="part in log.parts">{{part.path}} ({{part.size | bytesToSize}})</li>
						</ol>
					</li>
				</ol>
			</div>
			<div class="alert alert-warning animate-show" ng-show="resolvedTestLogs.length==0"><h4>No logs resolved!</h4>Please check for misconfiguration.</div>
			<div class="alert alert-danger animate-show" ng-show="resolvingTestLogsError"><h4>Error occurred!</h4>See above errors for more details.</div>
		</lfs-bean-wizard>
	</div>

	<hr>
	
	<!-- TODO if reader is configurable -->
	<div id="log-reader-editor" ng-if="beanWrapper[0]['@type']" ng-form="form">
		<h4>Log entry reader</h4>
		<lfs-bean-wizard bean="beanWrapper[0].reader.targetReader" bean-type-label="Reader type" wizards="readerWizards"
			shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="reader.targetReader.">
		</lfs-bean-wizard>
	</div>

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
	<div id="source-reader-filters" ng-if="beanWrapper[0]['@type']">
		<h4>Filters
			<small>Used to filter log entries e.g. for field transformation, normalization etc.</small></h4>
		<div class="panel panel-default" ng-repeat="filter in beanWrapper[0].reader.filters">
			<div class="panel-heading">
				<button type="button" class="close pull-right" title="Delete" ng-click="deleteReaderFilter($index)"><i class="glyphicon glyphicon-trash"></i></button>
				<h3 class="panel-title">Filter {{$index+1}}</h3>
			</div>
			<div class="panel-body" ng-form="form">
				<div ng-controller="SourceReaderFilterHelpController">
					<lfs-bean-wizard bean="filter" bean-type-label="Filter type" wizards="readerFilterWizards"
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