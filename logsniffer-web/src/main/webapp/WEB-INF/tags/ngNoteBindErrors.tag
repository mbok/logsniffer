<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@tag description="Outputs error message if bind errors exist" pageEncoding="UTF-8"%>
<%@attribute name="ngIf" required="true" type="java.lang.String"%>
<div class="alert alert-danger" ng-if="${ngIf}">
	<button type="button" class="close" data-dismiss="alert">&times;</button>
	<h4>Erroneous input</h4>
	Please correct below errors to continue!
</div>
