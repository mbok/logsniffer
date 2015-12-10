<%@page import="com.logsniffer.model.RollingLog"%>
<%@page import="com.logsniffer.model.Log"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message code="logsniffer.breadcrumb.settings" var="settingsLabel" />
<tpl:bodySidebar title="${activeNode.title} - ${rootNode.title}" activeNavbar="settings" ngModules="'SettingsRootModule'">
	<jsp:attribute name="htmlHead">
		<c:if test="${not empty activeNode.pageContext.jsFiles}">
			<!-- NG page -->
			<c:forEach var="jsFile" items="${activeNode.pageContext.jsFiles}">
			    <script type="text/javascript" src="<c:url value="/${jsFile}" />?v=${logsnifferProps['logsniffer.version']}"></script>
			</c:forEach>
		</c:if>
		<script type="text/javascript">
			angular.module('SettingsRootModule',
				[<c:if test="${not empty activeNode.pageContext.module}">'${activeNode.pageContext.module}'</c:if>]
			)
			.controller(
					"SettingsAbstractController",
					[
					 '$scope',
					 '$http',
					 '$log',
					 'lsfAlerts',
					 function($scope, $http, $log, lsfAlerts) {
						 $scope.loadSettings = function(childScope) {
							 var callbacks = {};
					    	 childScope.state.busy = true;
					    	 $http({
							     url : childScope.settingsRessource,
							     method : "GET"
							 })
							.success(
								function(data, status, headers, config) {
								    $log.info("Settings loaded from", $scope.settingsRessource, data);
								    childScope.state.busy = false;
								    if (callbacks.success) {
								    	callbacks.success(data, status, headers, config);
								    }
								}
							)
							.error(
								function(data, status, headers, config, statusText) {
									childScope.state.busy = false;
									childScope.alerts.httpError("Failed to load settings", data, status, headers, config, statusText);
								    if (callbacks.error) {
								    	callbacks.error(data, status, headers, config, statusText);
								    }
								}
							);
					    	
					    	return {
					    		success: function(callback) {
					    			callbacks.success = callback;
					    		},
					    		error: function(callback) {
					    			callbacks.error = callback;
					    		}
					    	};
					     };
					     
					     $scope.saveSettings = function(childScope, settings) {
							var callbacks = {};
					    	childScope.state.busy = true;
					    	childScope.alerts.clear();
							$log.info("Saving settings", settings);
							 $http({
							     url : childScope.settingsRessource,
							     method : "POST",
							     data: settings
							 })
							.success(
								function(data, status, headers, config) {
								    $log.info("Settings saved", childScope.settingsRessource);
								    childScope.state.busy = false;
								    childScope.alerts.success("Changes applied successfully");
								    if (callbacks.success) {
								    	callbacks.success(data, status, headers, config);
								    }
								}
							)
							.error(
								function(data, status, headers, config, statusText) {
									childScope.state.busy = false;
									childScope.alerts.httpError("Failed to save settings", data, status, headers, config, statusText);
								    if (data && data.bindErrors) {
								    	childScope.bindErrors = data.bindErrors;
								    }
								    if (callbacks.error) {
								    	callbacks.error(data, status, headers, config, statusText);
								    }
								}
							);
					    	return {
					    		success: function(callback) {
					    			callbacks.success = callback;
					    		},
					    		error: function(callback) {
					    			callbacks.error = callback;
					    		}
					    	};

					     };					     
					 }
					]
				);
			
			;
		</script>
    </jsp:attribute>
	<jsp:attribute name="sidebar">
		<ul class="nav nav-sidebar">
			<c:forEach var="node1" items="${rootNode.subNodes}">
				<c:url value="/c/settings" var="url">
					<c:if test="${node1!=rootNode}"><c:param name="path" value="${node1.path}"/></c:if>
				</c:url>
				<li class="${logfn:contains(breadcrumbNodes, node1) || node1 == activeNode ? 'active':''}"><a href="${url}">${node1.title}</a>
				</li>
			</c:forEach>
		</ul>
	</jsp:attribute>
	
	<jsp:body>
		<ul class="breadcrumb">
			<c:forEach var="node" items="${breadcrumbNodes}">
				<c:url value="/c/settings" var="url">
					<c:if test="${node!=rootNode}"><c:param name="path" value="${node.path}"/></c:if>
				</c:url>
				<li><a href="${url}">${node.title}</a></li>
			</c:forEach>
			<li class="active">${activeNode.title}</li>
		</ul>
		
		<div ng-controller="SettingsAbstractController">
			<c:if test="${not empty activeNode.pageContext.module}">
				<div ng-controller="${activeNode.pageContext.controller}" ng-include="'<c:url value="/${activeNode.pageContext.template}" />?v=${logsnifferProps['logsniffer.version']}'"></div>
			</c:if>
		</div>
	</jsp:body>
</tpl:bodySidebar>