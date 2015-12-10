<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="row">
	<t:ngFormFieldWrapper cssClass="form-group col-md-6 required" fieldName="to">
		<label class="control-label">Recipients:</label>
		<input type="text" name="to" ng-model="bean.to" id="publisher.mail.to" class="form-control" placeholder="Comma separated mail addresses"
			required="required">
	</t:ngFormFieldWrapper>
	<t:ngFormFieldWrapper cssClass="form-group col-md-6 required" fieldName="from">
		<label class="control-label">From:</label>
		<input type="text" name="from" ng-model="bean.from" class="form-control" placeholder="Email from address"
			required="required">
	</t:ngFormFieldWrapper>
</div>

<div class="row">
	<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="subject">
		<lsf-info-label label="Subject:" for="subject">The mail subject is rendered using Velocity template engine and enables to include event data passed
				in attribute <code>event</code>.
				<div ng-include="contextPath + '/ng/help/velocity4events.html?v='+version"></div>
		</lsf-info-label>
		<input type="text" name="subject" id="subject" ng-model="bean.subject" class="form-control" placeholder="Mail subject"
			required="required">
	</t:ngFormFieldWrapper>
</div>

<div class="row">
	<t:ngFormFieldWrapper cssClass="form-group col-md-12" fieldName="textMessage">
		<lsf-info-label label="Text:" for="textMessage">The mail text is rendered using Velocity template engine and enables to include event data passed
				in attribute <code>event</code>.
				<div ng-include="contextPath + '/ng/help/velocity4events.html?v='+version"></div>
		</lsf-info-label>
		<textarea class="form-control" id="textMessage" rows="8" name="textMessage" ng-model="bean.textMessage" placeholder="Mail text"></textarea>
	</t:ngFormFieldWrapper>
</div>