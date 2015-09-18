<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!-- Requires bound "reader" attribute of type com.logsniffer.reader.LogEntryReader -->
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript">
	LogSnifferNgApp.controllerProvider.register(
		"ReaderBeanWizardControllerWrapper", ['$scope', function ($scope) {
			$scope.wizards=${logfn:jsonify(logfn:wizardsInfo('com.logsniffer.reader.LogEntryReader', pageContext.response.locale))};
			if (!$scope.$parent.bean.reader) {
				$scope.$parent.bean.reader = {};
			}
			$scope.beanWrapper = [$scope.$parent.bean.reader];
			$scope.$watch('beanWrapper', function(newValue, oldValue) {
				$scope.$parent.bean.reader = newValue[0];
		    }, true);
			console.log($scope.$parent.bindErrors);
			$scope.$parent.$watch('bindErrors', function(newValue, oldValue) {
				console.log(newValue);
				$scope.bindErrors = LogSniffer.stripPrefix(newValue, "reader.");
		    });
			
		}]);
</script>
<!-- Reader wizard -->
<div id="log-reader-editor" ng-controller="ReaderBeanWizardControllerWrapper" ng-form="form">
	<div ng-controller="BeanWizardController">
		<h4>Log entry reader</h4>
		<div class="row">
			<div class="col-md-6 form-group" ng-class="{'has-error': form.selectedWizard.$invalid && !form.selectedWizard.$pristine}">
				<label class="control-label">Reader type*:</label>
				<div class="controls">
			        <select ng-model="selectedWizard" name="selectedWizard" class="form-control" ng-options="w.label for w in wizards" required>
			        	<option value=""><spring:message code="logsniffer.common.pleaseSelect" /></option>
			        </select>
			    </div>
		    </div>
		</div>
		
		<!-- Wizard -->
		<div>
			<div us-spinner ng-if="templateLoading"></div>
			<div class="slide-animate well well-sm" ng-if="selectedWizard" ng-include="'${request.contextPath}/c/wizards/view?type=' + selectedWizard.beanType"
				onload="templateLoaded()"></div>
		</div>
	</div>
</div>
