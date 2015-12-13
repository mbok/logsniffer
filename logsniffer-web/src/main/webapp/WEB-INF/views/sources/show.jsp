<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${logfn:fileName(activeLog.path)} - View" activeNavbar="sources"
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
			function($scope, $location, $log) {
				$scope.scannerWizards=${logfn:jsonify(filterScannerWizards)};

				$scope.source=${logfn:jsonify(activeSource)};
				$scope.log = ${logfn:jsonify(activeLog)};
				$scope.sharedScope = {
					source: $scope.source
				};
				$scope.pointerTpl = ${pointerTpl.json};
				$scope.pointer = {};
				$scope.initTail = $location.hash()=="tail";
				
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
				// $scope.$watch('pointer', function(newValue, oldValue) {
				//	$location.search("pointer", JSON.stringify(newValue));
				//}, true);

			}
		);
	
	</script>
	<ul class="breadcrumb">
		<li><a href="<c:url value="/c/sources" />"><spring:message code="logsniffer.breadcrumb.sources" /></a></li>
		<li><a href="<c:url value="/c/sources/${activeSource.id}/logs" />">${activeSource.name}</a></li>
		<li class="active">${logfn:fileName(activeLog.path)}<!-- in ${logfn:filePath(activeLog.path)}  --></li>
	</ul>

	<ul class="nav nav-tabs">
		<c:url var="showHref" value="/c/sources/${activeSource.id}/show">
			<c:param name="log" value="${activeLog.path }" />
		</c:url>
		<li class="active"><a href="${showHref}">Browse</a></li>
		<c:url var="infoHref" value="/c/sources/${activeSource.id}/info">
			<c:param name="log" value="${activeLog.path }" />
		</c:url>
		<li><a href="${infoHref}">Info</a></li>
	</ul>

	<div class="container-fluid well log" ng-controller="LogShowController">


		<lsf-log-viewer source="source" log="log" pointer="pointer" fix-top-element-selector=".navbar-fixed-top"
			init-tail="initTail" search-wizards="scannerWizards" full-height="true" viewer-fields="source.uiSettings.viewerFields"></lsf-log-viewer>

	</div>
  </jsp:body>
</tpl:bodyFull>