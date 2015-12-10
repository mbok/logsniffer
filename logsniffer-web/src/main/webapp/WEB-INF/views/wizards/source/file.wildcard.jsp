<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="source-wildcard-file-wizard">
	<span class="text-muted">Source for simple log files matching a file name pattern</span>
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="pattern">
			<lsf-info-label label="File path pattern:" for="pattern">
				<div ng-include="contextPath + '/ng/help/logsByAntPathExpression.html?v='+version"></div>
			</lsf-info-label>
			<input type="text" ng-model="bean.pattern" name="pattern" id="pattern" class="form-control pattern" required>
		</t:ngFormFieldWrapper>
	</div>
</div>