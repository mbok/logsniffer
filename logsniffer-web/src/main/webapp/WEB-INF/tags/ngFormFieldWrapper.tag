<%@tag description="Wrapper for a NG form field" pageEncoding="UTF-8"%>
<%@attribute name="cssClass" required="false" type="java.lang.String"%>
<%@attribute name="fieldName" required="true" type="java.lang.String"%>
<div class="${cssClass}" ng-class="{'has-error': form.${fieldName}.$invalid && !form.${fieldName}.$pristine || bindErrors.${fieldName} && form.${fieldName}.$pristine}">
	<jsp:doBody />
	<div class="help-block" ng-if="bindErrors.${fieldName} && form.${fieldName}.$pristine">{{bindErrors.${fieldName}}}</div>
</div>