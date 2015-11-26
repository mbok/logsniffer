<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<tpl:bodyFull title="${activeSource.name } - Edit" activeNavbar="sources" ngModules="'SourceEditorModule'">
	<jsp:attribute name="htmlHead">
		<script type="text/javascript" src="<c:url value="/ng/source/readerTest.js" />"></script>
	</jsp:attribute>
	<jsp:body>
		<jsp:include page="source.breadcrumb.jsp" />

 		<div class="tab-pane active">
			<c:if test="${created}">
				<div class="alert alert-success">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					<h4>Successfully created</h4>
				</div>
			</c:if>

 				<script type="text/javascript">
  				var SourceEditorModule = angular.module('SourceEditorModule', ['ui.bootstrap', 'angularSpinner', 'lsfReaderTestModule']);
  				SourceEditorModule.controller(
  					"SourceEditController", ['$scope', '$http', '$location', '$anchorScroll', 'usSpinnerService',
  					function($scope, $http, $location, $anchorScroll, usSpinnerService) {
  						$scope.LogSniffer = LogSniffer;
  						$scope.bindErrors={<spring:hasBindErrors name="sourceForm"><c:forEach items="${errors.allErrors}" var="error">'${error.field }':'<spring:message code="${error.code}" text="${error.defaultMessage}" javaScriptEscape="true" />',</c:forEach></spring:hasBindErrors>};
  						$scope.beanWrapper = [${logfn:jsonify(activeSource)}];

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
							$(".alert-success").hide();
							$http({
							    url: "<c:url value="/c/sources/${activeSource.id}" />",
							    method: "PUT",
							    data: data
							}).success(function(data, status, headers, config) {
								$("form#refresh").submit();
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
			<form ng-controller="SourceEditController" name="form" method="post" action="/c/sources/${activeSource.id}" id="sourceForm"
				role="form" novalidate="novalidate">
	
				<jsp:include page="source.form.jsp">
					<jsp:param value="Save" name="submitLabel"/>
				</jsp:include>
			</form>
		</div>
	</jsp:body>
</tpl:bodyFull>