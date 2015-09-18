<%@tag description="Main HTML Template" pageEncoding="UTF-8"%>
<%@attribute name="title" required="true" type="java.lang.String"%>
<%@attribute name="htmlHead" required="false" fragment="true" %>
<%@attribute name="ngModules" required="false"  type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>${title} | logsniffer</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<link href="<%=request.getContextPath()%>/static/bootstrap/3.1.1/css/bootstrap.min.css" rel="stylesheet" media="screen" />
		<link href="<%=request.getContextPath()%>/static/bootstrap/3.1.1/css/bootstrap-theme.min.css" rel="stylesheet" />
		<script
			src="<%=request.getContextPath()%>/static/jquery/jquery-1.9.1.min.js"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.3.15/angular.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.3.15/angular-route.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/1.3.15/angular-animate.min.js" />"></script>
		<script src="<c:url value="/static/angular/ui-bootstrap-tpls-0.12.0.min.js" />"></script>			
		<script type="text/javascript" src="<c:url value="/static/angular/message-center-master/message-center.js" />"></script>
		<script
			src="<%=request.getContextPath()%>/static/bootstrap/3.1.1/js/bootstrap.min.js"></script>
		<script
			src="<%=request.getContextPath()%>/static/jquery/jquery.endless-scroll.js"></script>
		<script src="<%=request.getContextPath()%>/static/jquery/spin.min.js"></script>
		<script type="text/javascript" src="<c:url value="/static/angular/angular-spinner.min.js" />"></script>
		<link href="<c:url value="/static/slider/css/slider.css" />" rel="stylesheet" />
		<script src="<c:url value="/static/slider/js/bootstrap-slider.js" />"></script>
		<link href="<c:url value="/static/json-formatter/json-formatter.min.css" />" rel="stylesheet" />
		<script src="<c:url value="/static/json-formatter/json-formatter.min.js" />"></script>	
		<link href="<c:url value="/static/fontawesome/css/font-awesome.min.css" />" rel="stylesheet" />
		<script src="<%=request.getContextPath()%>/static/logsniffer.js"></script>
		<script src="<%=request.getContextPath()%>/static/logsniffer.ng-core.js"></script>
		<link href="<c:url value="/static/logsniffer.css" />" rel="stylesheet" />
		<script type="text/javascript">
			Spinner.defaults={ lines: 8, length: 4, width: 3, radius: 5 };
			
			$(function() {
				$(".help-popup").popover({placement:"top"});
			});
		</script>
		<script type="text/javascript">
			LogSniffer.config.contextPath = '${request.contextPath}';
			var LogSnifferNgApp=angular.module('LogSnifferNgApp', ['LogSnifferCore', 'ui.bootstrap', 'angularSpinner', 'MessageCenterModule',${ngModules}]);
			LogSnifferNgApp.config(function($controllerProvider, $compileProvider, $filterProvider, $provide)
		    {
			    LogSnifferNgApp.controllerProvider = $controllerProvider;
			    LogSnifferNgApp.compileProvider    = $compileProvider;
			    LogSnifferNgApp.filterProvider     = $filterProvider;
			    LogSnifferNgApp.provide            = $provide;
			});
			LogSnifferNgApp.controller("BeanWizardController", LogSniffer.ng.BeanWizardController);
			LogSnifferNgApp.controller("LogSnifferRootController", ['$scope', '$modal', function($scope, $modal) {
				$scope.contextPath = LogSniffer.config.contextPath;
				
			    $scope.zoomEntry = function (entry) {
					$modal.open({
				      templateUrl: $scope.contextPath + '/ng/entry/zoomEntry.html',
				      controller: 'ZoomLogEntryCtrl',
				      size: 'lg',
				      windowClass: 'zoom-entry-modal',
				      resolve: {
				        entry: function () {
				          return entry;
				        }
				      }
				    });
			    };

			}]);
			LogSnifferNgApp.filter('escape', function() {
				  return window.encodeURIComponent;
			});
			$.LogSniffer.zoomLogEntry = function(entry) {
				angular.element(document.body).scope().zoomEntry(entry);
			};
		</script>
		<jsp:invoke fragment="htmlHead"/>
	</head>
	<body ng-app="LogSnifferNgApp" ng-controller="LogSnifferRootController" class="ng-cloak">
		<jsp:doBody />
		
		<hr>

			<footer>
				<p>&copy; Company 2013</p>
			</footer>
		
	</body>
</html>