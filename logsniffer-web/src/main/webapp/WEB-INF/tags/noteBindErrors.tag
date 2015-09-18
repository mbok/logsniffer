<%@tag description="Outputs error message if bind errors exist" pageEncoding="UTF-8"%>
<%@attribute name="commandName" required="true" type="java.lang.String"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:hasBindErrors name="${commandName}" >
	<div class="alert alert-danger">
		<button type="button" class="close" data-dismiss="alert">&times;</button>
		<h4>Erroneous input</h4>
		Please correct below errors to continue!
	</div>
</spring:hasBindErrors>
