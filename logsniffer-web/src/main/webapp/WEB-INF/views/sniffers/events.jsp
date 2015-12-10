<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${activeSniffer.name } - Events" activeNavbar="sniffers" ngModules="'EventsRootModule','EventsModule'">
	<jsp:attribute name="htmlHead">
	    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
	    <script type="text/javascript">
	      google.load('visualization', '1', {packages: ['corechart']});
	    </script>
	    <script type="text/javascript" src="<c:url value="/static/date.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/ng-google-chart.js" />"></script>

	    <script type="text/javascript" src="<c:url value="/ng/sniffer/event/events.js?v=${logsnifferProps['logsniffer.version']}" />"></script>
	
		<script type="text/javascript">
			angular.module('EventsRootModule',[])
			.controller(
				"EventsRootController", ['$scope', function($scope) {
				    $scope.sniffer = ${logfn:jsonify(activeSniffer)};
				    $scope.sniffer.aspects.scheduleInfo = {
					    scheduled: ${scheduled}  
				    };
					$scope.nls = {
						on:'<spring:message code="logsniffer.sniffers.scheduled.true" />',
						off:'<spring:message code="logsniffer.sniffers.scheduled.false" />'
					};
				}]
			);
		</script>
		<style>
			.log .text {
				padding: 9px;
				overflow-x: auto;
			}
		</style>
	</jsp:attribute>


	<jsp:body>
		<div ng-controller="EventsRootController">
			<div ng-view></div>
		</div>
	</jsp:body>
</tpl:bodyFull>