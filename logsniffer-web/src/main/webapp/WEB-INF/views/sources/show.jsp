<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${activeLog.name} - View" activeNavbar="sources"
	ngModules="'LogShowModule'">
  <jsp:body>
	<c:url var="entriesJsonURL"
		value="/c/sources/${activeSource.id }/entries">
		<c:param name="log" value="${activeLog.path }" />
		<c:param name="mark" value="-mark-" />
		<c:param name="count" value="-count-" />
	</c:url>
	<c:url var="randomAccessEntriesJsonURL"
		value="/c/sources/${activeSource.id }/randomAccessEntries">
		<c:param name="log" value="${activeLog.path }" />
		<c:param name="mark" value="-mark-" />
	</c:url>
	<c:url var="searchEntriesJsonURL"
		value="/c/sources/${activeSource.id }/search">
		<c:param name="log" value="${activeLog.path }" />
		<c:param name="mark" value="-mark-" />
		<c:param name="count" value="-count-" />
	</c:url>
	<c:url var="moreBaseURL" value="/c/sources/${activeSource.id }/show">
		<c:param name="log" value="${activeLog.path }" />
	</c:url>

	<script type="text/javascript">
		var LogShowModule=angular.module('LogShowModule', ['ui.bootstrap', 'angularSpinner']);
		LogShowModule.controller(
			"LogShowController",
			function($scope, $location, $log, $http, lsfAlerts) {
				$scope.alerts = lsfAlerts.create();
				
				$scope.scannerWizards=${logfn:jsonify(filterScannerWizards)};

				$scope.source=${logfn:jsonify(activeSource)};
				$scope.log = ${logfn:jsonify(activeLog)};
				$scope.sharedScope = {
					source: $scope.source
				};
				$scope.pointerTpl = ${pointerTpl.json};
				$scope.pointer = {};
				$scope.initTail = $location.hash()=="tail";

				$scope.viewerFields = null;
				var userProfileViewerFields = ${logfn:jsonify(userProfileViewerFields)};
				if (userProfileViewerFields) {
					$scope.viewerFields = userProfileViewerFields.fields;
				}
				if (!$scope.viewerFields && $scope.source && $scope.source.uiSettings && $scope.source.uiSettings.viewerFields) {
					$log.info("Viewer fields not configured in profile settings, the default source settings are used");
					$scope.viewerFields = $scope.source.uiSettings.viewerFields;
				}
				$log.info("Using viewer fields config", $scope.viewerFields);
				

				var pointerParam = $location.search().pointer;
				if (pointerParam && typeof pointerParam == "string") {
					try {
					    $scope.pointer = JSON.parse(pointerParam);
					    $log.info("Initiated log view with pointer: ", $scope.pointer);
					} catch(e)
					{
					    $log.warn("Failed to init log viewer with erroneous JSON pointer: ", pointerParam, e);
					}
				}
				$scope.highlightPointer = $location.search().highlight=="true";

				$scope.$on("viewerFieldsChanged", function(event, viewerFields) {
					$log.info("Saving changed viewer fields as profile settings", viewerFields);
					$http(
						{
						    url : $scope.contextPath + "/c/user/profile/settings/logSource/"+$scope.source.id+"/viewerFields",
						    method : "POST",
						    data : {
						    	fields: viewerFields
						    }
						})
						.success(
							function(data, status, headers, config) {
							    $log.info("Viewer fields stored to profile settings");
							})
						.error(
							function(data, status, headers, config, statusText) {
							    $scope.alerts.httpError("Failed to save viewer fields configuration", data, status, headers, config, statusText);
							}
						);
				});


			}
		);
	
	</script>

	<jsp:include page="show.breadcrumb.jsp" />

	<div class="container-fluid well log" ng-controller="LogShowController">

		<div lsf-alerts alerts="alerts"></div>

		<lsf-log-viewer source="source" log="log" pointer="pointer" fix-top-element-selector=".navbar-fixed-top"
			init-tail="initTail" search-wizards="scannerWizards" full-height="true" configured-viewer-fields="viewerFields"
			default-viewer-fields="source.uiSettings.viewerFields" viewer-fields-config-enabled="true" highlight-pointer="highlightPointer"></lsf-log-viewer>

	</div>
  </jsp:body>
</tpl:bodyFull>