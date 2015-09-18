<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:message code="logsniffer.breadcrumb.sniffers" var="title"/>
<tpl:bodySidebar title="${title}" activeNavbar="sniffers" ngModules="'SnifferListModule'">
	<jsp:attribute name="sidebar"><jsp:include page="sidebar.jsp" /></jsp:attribute>
	<jsp:body>
		<ul class="breadcrumb">
			<li class="active"><spring:message code="logsniffer.breadcrumb.sniffers"/></li>
			<li class="pull-right dropdown"><a href="<c:url value="/c/sniffers/new" />" class="btn btn-primary btn-xs" role="menuitem"><i class="glyphicon glyphicon-plus"></i> New sniffer</a></li>
			<!-- 
				<li class="pull-right dropdown">
					<button data-toggle="dropdown" href="#" class="btn btn-xs btn-primary"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
					<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
						<li role="presentation"><a href="<c:url value="/c/sniffers/new" />" role="menuitem"><i class="glyphicon glyphicon-plus"></i> Create New Sniffer</a></li>
					</ul>
				</li>
			 -->
		</ul>
	
		<t:messages />

		<script type="text/javascript">
		$(function() {
			$("ul.bordered-list li").bind("mouseenter", function () {
				$(this).find('.hidden2').fadeIn("fast");
			});
			$("ul.bordered-list li").bind("mouseleave", function () {
				$(this).find('.hidden2').fadeOut("fast");
			});
		});
		
		var SnifferListModule = angular.module('SnifferListModule', ['ui.bootstrap', 'angularSpinner', 'MessageCenterModule']);
		SnifferListModule.controller(
			"SnifferListController", ['$scope', '$http', '$location', '$anchorScroll', 'usSpinnerService',
			function($scope, $http, $location, $anchorScroll, usSpinnerService) {
				$scope.sniffers = ${logfn:jsonify(sniffers)};
				$scope.nls = {
						on:'<spring:message code="logsniffer.sniffers.scheduled.true" />',
						off:'<spring:message code="logsniffer.sniffers.scheduled.false" />'
				};
			}
			]
		);
		SnifferListModule.controller(
				"SnifferController", ['$scope', '$http', '$log', 'messageCenterService',
				function($scope, $http, $log, messageCenterService) {
					$scope.snifferPath = $scope.contextPath + "/c/sniffers/" + $scope.sniffer.id;
					$scope.start = function() {
						$log.info("Starting sniffer", $scope.sniffer);
						$http({
						    url: $scope.snifferPath + "/start",
						    method: "POST"
						}).success(function(data, status, headers, config) {
							$log.info("Started sniffer", $scope.sniffer);
							$scope.sniffer.aspects.scheduleInfo.scheduled=true;
						}).error(function() {
							messageCenterService.add('danger', 'Failed to start');
						});
					};
					$scope.stop = function() {
						$log.info("Stopping sniffer", $scope.sniffer);
						$http({
						    url: $scope.snifferPath + "/stop",
						    method: "POST"
						}).success(function(data, status, headers, config) {
							$log.info("Stopped sniffer", $scope.sniffer);
							$scope.sniffer.aspects.scheduleInfo.scheduled = false;
						}).error(function() {
							messageCenterService.add('danger', 'Failed to stop');
						});
					};
				}
				]
			);
		 
		
		</script>
		<c:choose>
			<c:when test="${empty sniffers}">
				<div class="alert alert-info">
					<i class="glyphicon glyphicon-exclamation-sign"></i> You have not created any sniffers.
				</div>
			</c:when>
			<c:otherwise>
				<div>
					<div mc-messages></div> 
					<ul class="bordered-list sniffers" ng-controller="SnifferListController">
						<li ng-repeat="sniffer in sniffers">
							<div ng-controller="SnifferController">
								<h4>
									<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}/events">{{sniffer.name}}</a>
									<div class="pull-right hidden2">
										<div class="btn-group btn-group-xs" role="group" aria-label="Actions">
											<a href="#" class="btn btn-xs btn-default" ng-if="!sniffer.aspects.scheduleInfo.scheduled"
												ng-click="start()"><i class="glyphicon glyphicon-play"></i>Start</a>
											<a href="#" class="btn btn-xs btn-default" ng-if="sniffer.aspects.scheduleInfo.scheduled"
												ng-click="stop()"><i class="glyphicon glyphicon-pause"></i>Pause</a>
											<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}" class="btn btn-xs btn-default"><i class="glyphicon glyphicon-edit"></i> Edit</a>
											<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}/status" class="btn btn-xs btn-default"><i class="glyphicon glyphicon-play-circle"></i> Control</a>
										</div>
									</div>
								</h4>
								<div class="row">
									<div class="col-md-12">
										<span class="label label-success" ng-if="sniffer.aspects.scheduleInfo.scheduled">{{nls.on}}</span>
										<span class="label label-default" ng-if="!sniffer.aspects.scheduleInfo.scheduled">{{nls.off}}</span>
										|
										Last run: 
											<span ng-if="sniffer.aspects.scheduleInfo.lastFireTime">{{sniffer.aspects.scheduleInfo.lastFireTime | date:'medium'}}</span>
											<span ng-if="!sniffer.aspects.scheduleInfo.lastFireTime"> - never -</span>
										|
										<i class="glyphicon glyphicon-bullhorn"></i> Events: 
											<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}/events">
												<span class="badge" ng-class="{'badge-warning': sniffer.aspects.eventsCount!=0}">{{sniffer.aspects.eventsCount}}</span>
											</a>
									</div>
								</div>
							</div>
						</li>
					</ul>
				</div>
			</c:otherwise>
		</c:choose>
	</jsp:body>
</tpl:bodySidebar>