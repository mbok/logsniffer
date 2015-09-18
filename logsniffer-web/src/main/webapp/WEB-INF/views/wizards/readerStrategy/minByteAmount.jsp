<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="row">
	<t:ngFormFieldWrapper cssClass="form-group col-md-6 required" fieldName="minBytesAmount">
		<label for="minBytesAmount" class="control-label">Amount of bytes to process at least:</label>
		<div class="controls controls-row input-group">
			<input type="text" class="form-control minBytesAmount" ng-model="bean.minBytesAmount" name="minBytesAmount" id="minBytesAmount" required>
			<div class="input-group-addon"><div class="text-muted" style="width:6em" id="humanMinBytesAmount">{{bean.minBytesAmount | bytesToSize:1}}</div></div>
		</div>
	</t:ngFormFieldWrapper>
</div>
