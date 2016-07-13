<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${activeSniffer.name } - Control" activeNavbar="sniffers" ngModules="'LogSnifferStatuswModule'">
	<jsp:body>
		<script type="text/javascript">
		angular.module('LogSnifferStatuswModule', [])
		.controller(
			"LogSnifferStatusController",
			function($scope, $location, $log, $http, $uibModal, lsfAlerts) {
				$scope.alerts = lsfAlerts.create();
				$scope.source = ${logfn:jsonify(source)};
				$scope.status = ${logfn:jsonify(logsStatus)};
				$scope.scheduleInfo = ${logfn:jsonify(scheduleInfo)};
				$scope.snifferPath = $scope.contextPath + "/c/sniffers/${activeSniffer.id}";
				
				for(var i=0;i<$scope.status.length;i++) {
					$scope.status[i].startFromHead = false;
					$scope.status[i].startFromTail = false;
				}

				$scope.resetAllTo = function (head, tail) {
					for(var i=0;i<$scope.status.length;i++) {
						$scope.resetTo(i, head, tail);
					}
				};

				$scope.resetTo = function (index, head, tail) {
					$scope.status[index].startFromHead = head;
					$scope.status[index].startFromTail = tail;
					$scope.status[index].currentPointer = null;
				};

				$scope.reposition = function (statusIndex) {
					var status = $scope.status[statusIndex];
					var modalInstance = $uibModal.open({
				      templateUrl: $scope.contextPath + '/ng/sniffer/statusReposition.html',
				      controller: 'SnifferStatusRepositionCtrl',
				      size: 'lg',
				      scope: $scope,
				      resolve: {
				        source: function () {
							return $scope.source;
				        },
				        status: function () {
			        		return status;
			        	}
				      }
				    });
					modalInstance.result.then(function (pointer) {
						if (pointer) {
							$log.debug("Refreshing pointer status for log:", statusIndex, pointer);
							$scope.status[statusIndex].busy = true;
							$http({
							    url: $scope.snifferPath + "/status/pointerOffset?log="+encodeURIComponent(status.log.path),
							    method: "POST",
							    data: pointer
							}).success(function(data, status, headers, config) {
								$log.debug("Retrieved pointer offset", pointer, data);
								$scope.status[statusIndex] = data;
								$scope.status[statusIndex].startFromTail = false;
								$scope.status[statusIndex].startFromHead = false;
							}).error(function(data, status, headers, config, statusText) {
								$scope.status[statusIndex].busy = false;
								$scope.alerts.httpError("Failed to update log position", data, status, headers, config, statusText);
							});
						}
					});
			    };
			    
				$scope.start = function () {
					var startFrom = [];
					for(var i=0;i<$scope.status.length;i++) {
						var s = $scope.status[i];
						startFrom.push({
							logPath: s.log.path,
							startFromHead: s.startFromHead == true,
							startFromTail: s.startFromTail == true,
							startFromPointer: s.currentPointer
						});
					}
					$log.debug("Starting sniffer from:", startFrom);
					$scope.busy = true;
					$http({
					    url: $scope.snifferPath + "/status/startFrom",
					    method: "POST",
					    data: startFrom
					}).success(function(data, status, headers, config) {
						$scope.busy = false;
						$scope.scheduleInfo.scheduled = true;
						$log.debug("Started sniffer");
					}).error(function(data, status, headers, config, statusText) {
						$scope.busy = false;
						$scope.alerts.httpError("Failed to start sniffer", data, status, headers, config, statusText);
					});
			    };

				$scope.stop = function() {
					$log.info("Stopping sniffer");
					$scope.busy = true;
					$http({
					    url: $scope.snifferPath + "/stop",
					    method: "POST"
					}).success(function(data, status, headers, config) {
						$scope.busy = false;
						$log.info("Stopped sniffer");
						$scope.scheduleInfo.scheduled = false;
						$scope.reload();
					}).error(function(data, status, headers, config, statusText) {
						$scope.busy = false;
						$scope.alerts.httpError("Failed to stop sniffer. Try to reload first.", data, status, headers, config, statusText);
					});
				};
				
				$scope.reload = function() {
					$log.info("Reloading schedule and status info");
					$scope.busy = true;
					$http({
					    url: $scope.snifferPath + "/status/summary",
					    method: "GET"
					}).success(function(data, status, headers, config) {
						$scope.busy = false;
						$log.info("Got schedule and status info", data);
						$scope.scheduleInfo = data.scheduleInfo;
						$scope.status = data.logsStatus;
					}).error(function(data, status, headers, config, statusText) {
						$scope.busy = false;
						$scope.alerts.httpError("Failed to reload sniffer status information. Try to reload the page.", data, status, headers, config, statusText);
					});
				};
			})
		.controller(
			"SnifferStatusRepositionCtrl",
			function($scope, $location, $log, $http, $uibModalInstance, source, status) {
				$scope.scannerWizards = ${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.event.Scanner', pageContext.response.locale))};
				$log.info("Openning log viewer to reposition for", source, status);
				$scope.source = source;
				$scope.log = status.log;
				$scope.pointer = status.currentPointer ? angular.copy(status.currentPointer.json) : {};
				$scope.$on("snifferReposition", function(event, pointer) {
					$log.debug("Setting position for log ", status, pointer);
					$uibModalInstance.close(pointer);
				});
				$scope.close = function() {
					$uibModalInstance.close();
				};
			});
		</script>

		
		<div ng-controller="LogSnifferStatusController">
			<jsp:include page="sniffer.breadcrumb.jsp">
				<jsp:param value="Control" name="context"/>
			</jsp:include>
			<div lsf-busy-container busy="busy">
				<div class="clearfix">
					<div class="pull-right">
						Last run: 
						<span ng-if="scheduleInfo.lastFireTime">{{scheduleInfo.lastFireTime | date:'medium'}}</span>
						<span ng-if="!scheduleInfo.lastFireTime"> - never -</span>
					</div>
					<div class="btn-toolbar">
						<div class="btn-group">
							<button class="btn btn-default dropdown-toggle"
								ng-disabled="scheduleInfo.scheduled" data-toggle="dropdown">Reset all positions
								<span class="caret"></span>
							</button>
							<ul class="dropdown-menu">
								<li><a href ng-click="resetAllTo(true, false)">
									<i class="glyphicon glyphicon-fast-backward"></i> All to start</a></li>
								<li><a href ng-click="resetAllTo(false, true)">
									<i class="glyphicon glyphicon-fast-forward"></i> All to end</a></li>
							</ul>
						</div>
						<button class="btn btn-default" ng-disabled="scheduleInfo.scheduled" type="button" ng-click="start()">
							<i class="glyphicon glyphicon-play"></i> Start</button>						
						<button class="btn btn-default" ng-disabled="!scheduleInfo.scheduled" type="button" ng-click="stop()">
							<i class="glyphicon glyphicon-pause"></i> Pause</button>						
						<button class="btn btn-default" type="button" ng-click="reload()">
							<i class="glyphicon glyphicon-repeat"></i> Reload</button>						
					</div>
	
				</div>
				<c:if test="${started}">
					<div class="alert alert-success">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
						Monitoring started!
					</div>
				</c:if>
				<c:if test="${stopped}">
					<div class="alert alert-success">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
						Monitoring stopped!
					</div>
				</c:if>
	
				<div lsf-alerts alerts="alerts"></div>
				<h3>Positioning
					 <small ng-if="scheduleInfo.scheduled"><spring:message code="logsniffer.sniffers.positioning.show"/></small>
					 <small ng-if="!scheduleInfo.scheduled"><spring:message code="logsniffer.sniffers.positioning.set"/></small>
				</h3>
				<div class="well well-sm" ng-repeat="s in status" lsf-busy-container busy="s.busy">
					<h4>{{s.log.path!='default'?s.log.path:s.log.name}}</h4>
					<div class="row">
						<div class="col-md-2">Current position:</div>
						<div class="col-md-7">
							<span ng-if="source.navigationType=='DATE'">
								<i ng-if="s.currentPointer" class="glyphicon glyphicon-time"></i> {{s.currentPointer.json.d | date: 'medium'}}<br>
							</span>
							<uib-progressbar ng-if="s.startFromHead || s.startFromTail"
								max="1" value="s.startFromHead?0:1" type="success" ng-class="{'progress-striped active':scheduleInfo.scheduled}"
								animate="true" style="margin-bottom:5px"></uib-progressbar>
							<uib-progressbar ng-if="!(s.startFromHead || s.startFromTail)"
								max="s.log.size" value="s.currentOffset" type="success" ng-class="{'progress-striped active':scheduleInfo.scheduled}"
								animate="true" style="margin-bottom:5px"></uib-progressbar>
						</div>
						<div class="col-md-3">
							<span ng-if="source.navigationType=='DATE'"><br></span>
							<span ng-if="(!s.currentPointer && !s.startFromTail) || s.currentPointer.sof || s.startFromHead">Start</span>
							<span ng-if="s.currentPointer.eof || s.startFromTail">End</span>
							<span ng-if="s.currentPointer && !s.currentPointer.sof && !s.currentPointer.eof">{{s.currentOffset | bytesToSize:2}}</span>
							 of {{s.log.size | bytesToSize:2}}
						</div>
					</div>
					<div class="row">
						<div class="col-md-2">Reset position:</div>
						<div class="col-md-10">
							<div class="btn-group btn-group-xs">
								<button class="btn btn-xs btn-default" ng-click="resetTo($index, true, false)" type="button" ng-disabled="scheduleInfo.scheduled">
									<i class="glyphicon glyphicon-fast-backward"></i> To start</a></button>
								<button class="btn btn-xs btn-default" ng-click="reposition($index)" type="button" ng-disabled="scheduleInfo.scheduled">
									<i class="glyphicon glyphicon-screenshot"></i> Select...</button>
								<button class="btn btn-xs btn-default" ng-click="resetTo($index, false, true)" type="button" ng-disabled="scheduleInfo.scheduled">
									<i class="glyphicon glyphicon-fast-forward"></i> To end</a></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</jsp:body>
</tpl:bodyFull>