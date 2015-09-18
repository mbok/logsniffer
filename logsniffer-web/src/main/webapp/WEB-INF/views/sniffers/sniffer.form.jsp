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
	SnifferEditorModule.controller(
		"SnifferBeanWizardControllerWrapper",
		function($scope, $http, $log, $modal) {
		    $scope.scannerWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.Scanner', pageContext.response.locale))};
			$scope.readerStrategyWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.LogEntryReaderStrategy', pageContext.response.locale))};
		    $scope.publisherWizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.Publisher', pageContext.response.locale))};
		    $scope.sharedScope = {};
		    $scope.testSession = {};
		    
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
				$scope.bean.publishers.push({});  
			};

			$scope.deletePublisher = function(index) {
				$scope.bean.publishers.splice(index, 1);
			};
			
			$scope.testPublisher = function (publisher) {
				$modal.open({
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
				$modal.open({
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
		});
		
</script>
	
<fieldset id="sniffer-editor" ng-controller="SnifferBeanWizardControllerWrapper" ng-disabled="${scheduled}">
	<h4>Main</h4>
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="name">
			<label for="name" class="control-label">Name:</label>
			<div class="controls">
				<input ng-model="bean.name" name="name" id="name" class="form-control" placeholder="Name" required/>
			</div>
		</t:ngFormFieldWrapper>
	</div>
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-6 required" fieldName="source">
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
		<lfs-bean-wizard bean="bean.readerStrategy" bean-type-label="Strategy type" wizards="readerStrategyWizards"
			shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="readerStrategy."></lfs-bean-wizard>
	</div>

	<!-- Scanner wizard -->
	<div id="sniffer-scanner-editor" ng-form="form">
		<h4>Event scanner configuration
			<small>Configures the scanner sniffing the log consecutively for new events</small></h4>
		<lfs-bean-wizard bean="bean.scanner" bean-type-label="Scanner type" wizards="scannerWizards"
			shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="scanner.">
			<button type="button" class="btn btn-default btn-xs" ng-click="testScanner()" ng-disabled="form.$invalid"><i class="glyphicon glyphicon-check"></i> Test scanning</button>
		</lfs-bean-wizard>
	</div>
	
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
	<div id="sniffer-publishers">
		<h4>Publishers
			<small>Used to publish events additionally to further channels like mail, web etc.</small></h4>
		<div class="panel panel-default" ng-repeat="publisher in bean.publishers">
			<div class="panel-heading">
				<button type="button" class="close pull-right" title="Delete" ng-click="deletePublisher($index)"><i class="glyphicon glyphicon-trash"></i></button>
				<h3 class="panel-title">Publisher {{$index+1}}</h3>
			</div>
			<div class="panel-body" ng-form="form">
				<div ng-controller="SnifferPublisherHelpController">
					<lfs-bean-wizard bean="publisher" bean-type-label="Publisher type" wizards="publisherWizards"
						shared-scope="sharedScope" bind-errors="bindErrors" bind-errors-prefix="publishers[{{$index}}].">
						<button type="button" class="btn btn-default btn-xs" ng-click="testPublisher(publisher)" ng-disabled="form.$invalid"><i class="glyphicon glyphicon-check"></i> Test publishing</button>
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
</fieldset>