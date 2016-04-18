<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="ngBasePath" value="/ng" />
<tpl:bodyFull title="${report.name} - Dashboard" activeNavbar="reports">
	<jsp:attribute name="htmlHead">
	    <script type="text/javascript" src="<c:url value="/static/gridster/jquery.gridster.min.js" />"></script>
		<link href="<c:url value="/static/gridster/jquery.gridster.min.css" />" rel="stylesheet" />
		<script src="<c:url value="/static/angular/ng-google-chart.js" />"></script>
		<script type="text/javascript">
			var gridster;
			$(function(){
				var saveWidgetsLayout = function() {
					var scope = angular.element($("#dashboard")).scope();
					scope.$apply(function() {
						scope.saveWidgetsLayout();
					});
				};
				var widgetsGridWidth = $(".widgets-grid").width();
				console.log("Gridster area width: " + widgetsGridWidth);
				gridster = $(".widgets-grid").gridster({
			        widget_margins: [30, 30],
			        widget_base_dimensions: [(widgetsGridWidth - 6 * 30 * 2) / 6, 140],
			        resize: {
			            enabled: true,
			            stop: function(e, ui, $widget) {
			            	saveWidgetsLayout();
			            	$($widget).find("a.redraw").click();
			            }
			        },
			        draggable: {
			            handle: 'div.panel-heading',
			            stop: function() {
			            	saveWidgetsLayout();
			            }
			        },
			        widget_selector: "div.widget",
			        min_cols: 6
			        
			    }).data('gridster');

			});
			
			var dashboardApp=angular.module('dashboard', ['googlechart', 'ui.bootstrap']);
			dashboardApp.controller(
				"DashboardController",
				function($scope, $uibModal, $log, $timeout) {				
					$scope.changed = false;
					$scope.addedWidgets = {};
					
					var strData = atob("${logfn:btoa(logfn:jsonify(report.widgets))}");
					$log.info(strData);
					$scope.widgets = eval(strData);
					$log.info("Decoded " + $scope.widgets.length + " widgets");

					$scope.$watch('widgets', function(newValue, oldValue) {
						if (newValue === oldValue){
							return;
						}
						$scope.changed = true;
					}, true);
					
					$scope.removeWidget = function(index) {
						gridster.remove_widget($("#widget-"+index));
						$("#widget-"+index).hide().remove();
						$scope.widgets[index] = null;
						if ($scope.addedWidgets[index]) {
							$scope.addedWidgets[index] = null;
						}
						$scope.saveWidgetsLayout();
					};
					
					$scope.addWidgetWizard = function () {
						var modalInstance = $uibModal.open({
							templateUrl: '${ngBasePath}/reports/addWidgetWizard.html',
							controller: AddWidgetCtrl
						});
						
						modalInstance.result.then(function (widget) {
							var newIndex = $scope.widgets.length;
							$scope.widgets[newIndex]= widget;
							$scope.addedWidgets[newIndex]= widget;
							$timeout(function () {
								gridster.add_widget("#widget-"+newIndex, 2, 2);
								$scope.saveWidgetsLayout();
                            });
						}, function () {
							$log.info('Add widget wizard dismissed');
						});
					};
					
					$scope.saveWidgetsLayout = function() {
				    	$($scope.widgets).each(function(index, widget) {
				    		if (widget) {
						    	var layout = gridster.serialize($("#widget-"+index))[0];
						    	console.log("Update layout for widget " + index + ": " + JSON.stringify(layout));
						    	widget.layout = layout;				    			
				    		}
				    	});
					};
				}
			);
			dashboardApp.directive('ngUpdateHidden',function() {
			    return function(scope, el, attr) {
			        var model = attr['ngModel'];
			        scope.$watch(model, function(nv) {
			            el.val(nv);
			        }, true);
			    };
			});
			dashboardApp.directive('ngStringify',function() {
			    return function(scope, el, attr) {
			        var model = attr['ngModel'];
			        scope.$watch(model, function(nv) {
			        	if (typeof nv != "undefined" && nv != null) {
			            	el.val(JSON.stringify(nv));
			        	} else {
			        		el.val("null");
			        	}
			        }, true);
			    };
			});
		</script>
		<script type="text/javascript">
			dashboardApp
			.controller(
				"ChartWidgetController",
				function($scope, $uibModal, $log) {																	
					$scope.config = $scope.widget.data;
					
					$scope.chart = {};
									
					var deployChartConfig = function() {
						$scope.chart = {
							chartType: $scope.config.chartType,
							options: $scope.config.options,
							data: function() {
								var builder = new LogSniffer.DataSourceBuilder(
										$scope.config.dataBuilderConfig);
								return builder
										.build({
											ajax: {
												url : "/c/reports/eventSearch"
											}
										});
							}
						};
					};
					
					deployChartConfig();
					
					var chartEditor = null;
					var chartEditorCallback = function() {
						console.log("Update chart");
						var newChartWrapper = chartEditor.getChartWrapper();
						$scope.$apply(function(){
							$scope.config.chartType = newChartWrapper.getChartType();
							$scope.config.options = newChartWrapper.getOptions();
							deployChartConfig();
						});
					};
					
					$scope.openChartEditor = function() {
						var wrapper = new google.visualization.ChartWrapper({
					           chartType: $scope.config.chartType,
					           options: $scope.config.options,
					           dataTable: $scope.chart.data()
					           });
						chartEditor = new google.visualization.ChartEditor();
				        google.visualization.events.addListener(chartEditor, 'ok', chartEditorCallback);
				        chartEditor.openDialog(wrapper, {});
					};
					
					$scope.openDataBuilderEditor = function () {
					  var modalInstance = $uibModal.open({
					    templateUrl: '${ngBasePath}/reports/dataBuilderEditor.html',
					    windowClass: 'ds-editor',
					    controller: DataBuilderEditorCtrl,
					    resolve: {
					      dataBuilderConfig: function () {
					        return $scope.config.dataBuilderConfig;
					      }
					    }
					  });
					
					  modalInstance.result.then(function (dataBuilderConfig) {
						$scope.config.dataBuilderConfig = dataBuilderConfig;
						$log.info('Apply new data source builder config: ' + JSON.stringify(dataBuilderConfig));
						deployChartConfig();
					  }, function () {
					    $log.info('Data source builder dismissed');
					  });
					};
				});
		</script>
		<script type="text/javascript" src="${ngBasePath}/reports/addWidgetCtrl.js"></script>		
		<style>
			/* Boostrap vs. Google Visualization Styling Issue Fixes */
			.google-visualization-charteditor-name-input {
				display: none !important;
			}
			
			.google-visualization-charteditor-dialog.modal-dialog {
				z-index: 1050;
				width: auto;
			}
			
			.modal .modal-dialog {
				position: relative !important;
				padding: 0 !important;
				background: transparent !important;
				border: none !important;
			}
			
			.ds-editor .modal-dialog {
				width: 900px;
			}
			
			.dashboard .panel-body {
				position: absolute;
				margin-top: 45px;
				top: 15px;
				bottom: 15px;
				left: 15px;
				right: 15px;
			}
			
			.dashboard .panel-heading {
				cursor: move;
			}
			
			.full-y {
				height: 100%;
			}
			
			.widgets-grid {
				margin-left: -15px;
				margin-top: -15px;
				margin-bottom: 30px;
			}
		</style>
	</jsp:attribute>
	<jsp:body>
		<ul class="breadcrumb">
			<li><a href="<c:url value="/c/reports" />"><spring:message code="logsniffer.breadcrumb.reports"/></a> </li>
			<li class="active">${report.name}</li>
		</ul>
		<div class="dashboard" id="dashboard" ng-app="dashboard" ng-controller="DashboardController">
			<c:if test="${not empty message}">
				<div class="alert alert-${message.type.name().toLowerCase()}">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					${message.text}
				</div>
			</c:if>

			<form:form id="dashboard-form" method="post" action="/c/reports/${report.id}/reload" commandName="report">
				<div class="alert alert-warning animate-show" ng-show="changed">
					<strong>Dashboard configuration changed. Please click the save button to persist changes!</strong>
					<button class="btn btn-warning" type="submit">Save</button>
					<button class="btn btn-xs" type="button" ng-click="changed=false">Close</button>
				</div>
				<div ng-repeat="widget in widgets track by $index">
					<input type="hidden" name="widgets[{{$index}}].mode" ng-update-hidden ng-model="widget.mode"/>
					<input type="hidden" name="widgets[{{$index}}].title" ng-update-hidden ng-model="widget.title"/>
					<input type="hidden" name="widgets[{{$index}}].rawHtml" ng-update-hidden ng-model="widget.rawHtml" ng-if="widget.mode=='RAW'"/>
					<input type="hidden" name="widgets[{{$index}}].data" ng-stringify ng-model="widget.data" ng-if="widget.mode=='CHART'"/>
					<input type="hidden" name="widgets[{{$index}}].layout" ng-stringify ng-model="widget.layout"/>
				</div>

				<spring:message code="logsniffer.confirms.delete" var="confirmDelMsg"/>
				<div class="row widgets-grid gridster">
					<c:forEach var="widget" items="${report.widgets}" varStatus="status">
						<c:set var="layout" value="${logfn:jsonObject(widget.layout)}"/>
						<div class="panel panel-default widget" id="widget-${status.index}" ng-init="widget=widgets[${status.index}]"
								data-col="${not empty layout.col?layout.col:'1'}" data-row="${not empty layout.row?layout.row:'1'}"
								data-sizex="${not empty layout.size_x?layout.size_x:'2'}" data-sizey="${not empty layout.size_y?layout.size_y:'2'}">
							<c:choose>
								<c:when test="${widget.mode=='RAW'}">
									<div class="panel-heading">
										${widget.title}
										<div class="btn-group pull-right">
											<button class="btn btn-xs"><i class="glyphicon glyphicon-wrench"></i></button>
											<button class="btn btn-xs dropdown-toggle" data-toggle="dropdown">
											  <span class="caret"></span>
											</button>
											<ul class="dropdown-menu">
											  <li><a ng-click="removeWidget(${status.index})" onclick="return confirm('${confirmDelMsg}')"><i class="glyphicon glyphicon-trash"></i> Delete widget</a></li>
											</ul>
										</div>
									</div>
									<div class="panel-body">
										<c:out value="${widget.rawHtml}" escapeXml="false" />
									</div>
								</c:when>
								<c:when test="${widget.mode=='CHART'}">
									<div ng-cloak="" ng-controller="ChartWidgetController">
										<div class="panel-heading">
											${widget.title}
											<div class="btn-group pull-right">
												<button class="btn btn-xs"><i class="glyphicon glyphicon-wrench"></i></button>
												<button class="btn btn-xs dropdown-toggle" data-toggle="dropdown">
												  <span class="caret"></span>
												</button>
												<ul class="dropdown-menu">
												  <li><a ng-click="openDataBuilderEditor()"><i class="glyphicon glyphicon-th"></i> Datasource Editor</a></li>
												  <li><a ng-click="openChartEditor()"><i class="glyphicon glyphicon-picture"></i> Chart Editor</a></li>
												  <li><a ng-click="convertToHtml()"><i class="glyphicon glyphicon-plane"></i> Convert to Native HTML Widget</a></li>
												  <li><a ng-click="removeWidget(${status.index})" onclick="return confirm('${confirmDelMsg}')"><i class="glyphicon glyphicon-trash"></i> Delete widget</a></li>
												</ul>
											</div>
										</div>
										<div class="panel-body">
											<div google-chart chart="chart" class="full-y"></div>
											<a ng-click="$emit('resizeMsg')" class="redraw hidden">Redraw</a>
										</div>
									</div>
								</c:when>
							</c:choose>
	  					</div>
	  				</c:forEach>
	  				
					<div id="widget-{{i}}" class="panel panel-default widget" ng-repeat="(i, widget) in addedWidgets track by i">
						<div class="panel-heading" ng-if="widget.mode=='RAW'">
							{{widget.title}}
							<div class="btn-group pull-right">
								<button class="btn btn-xs"><i class="glyphicon glyphicon-wrench"></i></button>
								<button class="btn btn-xs dropdown-toggle" data-toggle="dropdown">
								  <span class="caret"></span>
								</button>
								<ul class="dropdown-menu">
								  <li><a ng-click="removeWidget(i)" onclick="return confirm('${confirmDelMsg}')"><i class="glyphicon glyphicon-trash"></i> Delete widget</a></li>
								</ul>
							</div>
						</div>
						<div class="panel-body" ng-if="widget.mode=='RAW'">
							...
						</div>
						<div ng-cloak="" ng-controller="ChartWidgetController" ng-if="widget.mode=='CHART'">
							<div class="panel-heading">
								{{widget.title}}
								<div class="btn-group pull-right">
									<button class="btn btn-xs"><i class="glyphicon glyphicon-wrench"></i></button>
									<button class="btn btn-xs dropdown-toggle" data-toggle="dropdown">
									  <span class="caret"></span>
									</button>
									<ul class="dropdown-menu">
									  <li><a ng-click="openDataBuilderEditor()"><i class="glyphicon glyphicon-th"></i> Datasource Editor</a></li>
									  <li><a ng-click="openChartEditor()"><i class="glyphicon glyphicon-picture"></i> Chart Editor</a></li>
									  <li><a ng-click="convertToHtml()"><i class="glyphicon glyphicon-plane"></i> Convert to Native HTML Widget</a></li>
									  <li><a ng-click="removeWidget(i)" onclick="return confirm('${confirmDelMsg}')"><i class="glyphicon glyphicon-trash"></i> Delete widget</a></li>
									</ul>
								</div>
							</div>
							<div class="panel-body">
								<div google-chart chart="chart" class="full-y"></div>
								<a ng-click="$emit('resizeMsg')" class="redraw hidden">Redraw</a>
							</div>
						</div>
	  				</div>
				</div>
				<a ng-click="addWidgetWizard()" class="btn btn-primary"><i class="glyphicon glyphicon-plus icon-white"></i> Add Widget</a>
			</form:form>
		</div>
		
		<script type="text/javascript">
			var DataBuilderEditorCtrl = function ($scope, $uibModalInstance, $log, dataBuilderConfig) {
			  	$scope.templatesPath = "${ngBasePath}/reports/dataBuilders/";
				
				$scope.dataBuilderConfigStr = JSON.stringify(dataBuilderConfig);
				
				$scope.dataBuilderConfig = JSON.parse($scope.dataBuilderConfigStr);
				
				$scope.ok = function () {
					$log.info("Config: " + this.dataBuilderConfigStr);
				   $uibModalInstance.close($scope.dataBuilderConfig);
				};
				
				$scope.cancel = function () {
				  $uibModalInstance.dismiss('cancel');
				};
			};	
		</script>
	</jsp:body>
</tpl:bodyFull>