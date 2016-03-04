<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<script type="text/javascript">
	// Requires the "source" attribute set in the parent scope
	LogSnifferNgApp.controllerProvider.register(
		"LevelScannerController", ['$scope', '$http',
	function ($scope, $http) {
		var sourcesUrl = '<c:url value="/c/sources" />';
		var loadSeverities = function(source) {
			$scope.supportedSeverities=[];
			if (source) {
				$scope.severitiesHelpInfo="Loading values...";
				$http({
				    url: sourcesUrl + "/" + source.id+"/reader/supportedSeverities",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					$scope.supportedSeverities = data;
					$scope.severitiesHelpInfo=null;
				}).error(function(data, status, headers, config) {
					$scope.severitiesHelpInfo="Failed to load supported severities";
				});
			} else {
				$scope.severitiesHelpInfo = "Values not accessible due to undefined log source";
			}
		};
		$scope.$parent.$watch('sharedScope.source', function(newValue, oldValue) {
			console.log("Loading supported severities for source " + newValue);
			loadSeverities(newValue);
	    });
	}]);
</script>

<span class="text-muted">Scans the log for entries matching the given severity level</span>
<div class="row" ng-controller="LevelScannerController" ng-form="form">
	<div class="form-group col-md-6 required" ng-class="{'has-error': (form.comparator.$invalid && !form.comparator.$pristine) || (form.severityNumber.$invalid && !form.severityNumber.$pristine)}">
		<label class="control-label">Matching level:</label>
		<div class="row" ng-init="bean.comparator=bean.comparator?bean.comparator:'EQ_OR_GREATER'">
			<t:ngFormFieldWrapper cssClass="col-md-3" fieldName="comparator">
				<select ng-model="bean.comparator" name="comparator" class="form-control" required>
					<option value="LESS">&lt;</option>
					<option value="EQ_OR_LESS">&lt;=</option>
					<option value="EQ">=</option>
					<option value="NEQ">!=</option>
					<option value="EQ_OR_GREATER">&gt;=</option>
					<option value="GREATER">&gt;</option>
				</select>
			</t:ngFormFieldWrapper>
			<t:ngFormFieldWrapper cssClass="col-md-9" fieldName="severityNumber">
				<select ng-model="bean.severityNumber" name="severityNumber" class="form-control" required
					ng-options="s.o as s.n for s in supportedSeverities">
					<option value=""><spring:message code="logsniffer.common.pleaseSelect" /></option>
				</select>
				<div class="help-block" ng-if="severitiesHelpInfo">{{severitiesHelpInfo}}</div>
			</t:ngFormFieldWrapper>
		</div>
	</div>
</div>