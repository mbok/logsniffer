<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<tpl:bodyFull title="New sniffer" activeNavbar="sniffers" ngModules="'SnifferEditorModule'">
	<jsp:attribute name="htmlHead">
		<script type="text/javascript" src="<c:url value="/ng/sniffer/snifferTest.js" />"></script>
	</jsp:attribute>

	<jsp:body>
		<ul class="breadcrumb">
			<li><a href="<c:url value="/c/sniffers" />">Event Sniffers</a> </li>
				<li class="active">New</li>
			</ul>


		<script type="text/javascript">
			var SnifferEditorModule = angular.module('SnifferEditorModule', ['ui.bootstrap', 'angularSpinner', 'lsfSnifferTestModule']);
			SnifferEditorModule.controller(
				"SnifferEditController", ['$scope', '$http', '$location', '$anchorScroll', 'usSpinnerService', '$window',
				function($scope, $http, $location, $anchorScroll, usSpinnerService, $window) {
					$scope.LogSniffer = LogSniffer;
					$scope.bindErrors={<spring:hasBindErrors name="snifferForm"><c:forEach items="${errors.allErrors}" var="error">'${error.field }':'<spring:message code="${error.code}" text="${error.defaultMessage}" javaScriptEscape="true" />',</c:forEach></spring:hasBindErrors>};
					$scope.bean = {
						"@type": "sniffer",
						"scanner": {
							"@type": "FilteredScanner", 
							"filters": []
						}
					};

					$scope.submit = function(form) {
						$(".backdrop-overlay").show();
						usSpinnerService.spin('update');
						form.$setPristine();
						var data=$scope.bean;
						var always = function() {
							$(".backdrop-overlay").hide();
							$location.hash('top');
						    $anchorScroll();
						    usSpinnerService.stop('update');									
						};
						$http({
						    url: "<c:url value="/c/sniffers" />",
						    method: "POST",
						    data: data
						}).success(function(data, status, headers, config) {
							$window.location = "<c:url value="/c/sniffers" />/" + data + "?created=true";
						}).error(function(data, status, headers, config) {
							always();
						    if (data && data.bindErrors) {
						    	$scope.bindErrors = data.bindErrors;
						    }
						});
					};					
				}
			]);
		</script>
		<form id="refresh" method="post"></form>
		<form ng-controller="SnifferEditController" name="form" method="post" action="/c/sniffers/new" id="snifferForm"
			role="form" novalidate="novalidate">
		
			<jsp:include page="sniffer.form.jsp" />
			
			<hr>
			<div class="row">
				<div class="col-md-12" us-spinner spinner-key="update">
					<button type="button" class="btn btn-primary" ng-disabled="form.$invalid" ng-click="submit(form)">Create</button>
				</div>
			</div>
		</form>
	</jsp:body>
</tpl:bodyFull>