<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="row">
	<spring:bind path="${param.formPath}.wizard.formData.to">
		<div class="form-group col-md-6 ${status.error?'has-error':''}">
			<form:label path="${status.expression}" cssClass="control-label">Recipients:</form:label>
			<div class="controls controls-row">
				<form:input path="${status.expression}" cssClass="form-control" placeholder="Comma separated mail addresses" />
				<form:errors path="${status.expression}" cssClass="help-block" />
			</div>
		</div>
	</spring:bind>
	<spring:bind path="${param.formPath}.wizard.formData.from">
		<div class="form-group col-md-6 ${status.error?'has-error':''}">
			<form:label path="${status.expression}" cssClass="control-label">From:</form:label>
			<div class="controls controls-row">
				<form:input path="${status.expression}" cssClass="form-control" placeholder="Email from address" />
				<form:errors path="${status.expression}" cssClass="help-block" />
			</div>
		</div>
	</spring:bind>
</div>

<div class="row">
	<spring:bind path="${param.formPath}.wizard.formData.subject">
		<div class="form-group col-md-12 ${status.error?'has-error':''}">
			<form:label path="${status.expression}" cssClass="control-label">Subject:</form:label>
			<div class="controls controls-row">
				<form:input path="${status.expression}" cssClass="form-control" placeholder="Mail subject" />
				<form:errors path="${status.expression}" cssClass="help-block" />
			</div>
		</div>
	</spring:bind>
</div>
<!-- 
<div class="row">
	<spring:bind path="${param.formPath}.wizard.formData.textMessage">
		<div class="form-group col-md-12 ${status.error?'has-error':''}">
			<form:label path="${status.expression}" cssClass="control-label">Text:</form:label>
			<div class="controls controls-row">
				<form:textarea path="${status.expression}" cssClass="col-md-12" rows="5" placeholder="Mail text" />
				<form:errors path="${status.expression}" cssClass="help-block" />
			</div>
		</div>
	</spring:bind>
</div>
 -->