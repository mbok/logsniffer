<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<tpl:bodyFull title="New Log Source" activeNavbar="sources" ngModules="'SourceEditorModule'">
	<jsp:attribute name="htmlHead">
		<script type="text/javascript" src="<c:url value="/ng/source/readerTest.js" />"></script>
	</jsp:attribute>
	<jsp:body>
		<ul class="breadcrumb">
			<li><a href="<c:url value="/c/sources" />"><spring:message code="logsniffer.breadcrumb.sources" /></a></li>
			<li class="active">New</li>
		</ul>

		<script type="text/javascript">
			var SourceEditorModule=angular.module('SourceEditorModule', ['ui.bootstrap', 'angularSpinner', 'lsfReaderTestModule']);
			SourceEditorModule.controller(
				"SourceEditController", ['$scope', '$http', '$location', '$anchorScroll', 'usSpinnerService', '$window',
				function($scope, $http, $location, $anchorScroll, usSpinnerService, $window) {
					$scope.LogSniffer = LogSniffer;
					$scope.bindErrors={<spring:hasBindErrors name="sourceForm"><c:forEach items="${errors.allErrors}" var="error">'${error.field }':'<spring:message code="${error.code}" text="${error.defaultMessage}" javaScriptEscape="true" />',</c:forEach></spring:hasBindErrors>};
					$scope.beanWrapper = [];

					$scope.submit = function(form) {
						$(".backdrop-overlay").show();
						usSpinnerService.spin('update');
						form.$setPristine();
						var data=$scope.beanWrapper[0];
						var always = function() {
							$(".backdrop-overlay").hide();
							$location.hash('top');
						    $anchorScroll();
						    usSpinnerService.stop('update');									
						};
						$http({
						    url: "<c:url value="/c/sources" />",
						    method: "POST",
						    data: data
						}).success(function(data, status, headers, config) {
							$window.location = "<c:url value="/c/sources" />/" + data + "?created=true";
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
		<form ng-controller="SourceEditController" name="form" method="post" action="/c/sources/new" id="sourceForm"
			role="form" novalidate="novalidate">
		
			<jsp:include page="source.form.jsp">
				<jsp:param value="Create" name="submitLabel"/>
			</jsp:include>
		</form>
	</jsp:body>
</tpl:bodyFull>