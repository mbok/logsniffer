<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:message code="logsniffer.breadcrumb.reports" var="title"/>
<tpl:bodyFull title="${title}" activeNavbar="reports">
	<jsp:body>
		<ul class="breadcrumb">
			<li class="active">${title}</li>
			<li class="pull-right dropdown">
				<button data-toggle="dropdown" href="#" class="btn btn-xs btn-primary"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
				<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
					<li role="presentation"><a href="<c:url value="/c/reports/new" />" role="menuitem"><i class="glyphicon glyphicon-plus"></i> Create New Dashboard</a></li>
				</ul>
			</li>
		</ul>
	
		<c:if test="${not empty message}">
			<div class="alert alert-${message.type.name().toLowerCase()}">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
				${message.text}
			</div>
		</c:if>

		<c:forEach var="report" items="${reports}">
			<c:url var="reportLink" value="/c/reports/${report.id}" />
			
			<div class="row">
				<div class="col-md-12">
					<div class="row">
						<div class="col-md-12">
							<h4>
								<a href="${reportLink}">${report.name}</a>
							</h4>
						</div>
					</div>
					<div class="row">
						<div class="col-md-12">
							Last update: <t:outputDate value="${report.updatedAt}" emptyLabel="-" />
							|
							Created: <t:outputDate value="${report.createdAt}" />
							|
							<a href="<c:url value="/c/reports/${report.id}" />"><i class="glyphicon glyphicon-edit"></i> Edit</a>
						</div>
					</div>
				</div>
			</div>
			<hr class="small">
		</c:forEach>
	</jsp:body>
</tpl:bodyFull>