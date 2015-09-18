<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message var="typeLabel" code="logsniffer.type.${ex.resourceType.name}" text="Resource"/>
<spring:message var="errorShort" code="logsniffer.exception.404.short" arguments="${typeLabel}" />
<spring:message var="errorDetail" code="logsniffer.exception.404.detail" arguments="${typeLabel},${ex.id}" />

<tpl:main title="${errorShort}">
	<tpl:navbar />
	<div class="container-fluid">
		<div class="alert alert-danger">
			<h1>${errorShort}</h1>
			<p>${errorDetail}</p>
		</div>
	</div>
</tpl:main>