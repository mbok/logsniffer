<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<spring:message code="logsniffer.breadcrumb.sources" var="title"/>
<tpl:bodySidebar title="${title}" activeNavbar="sources">
	<jsp:attribute name="sidebar"><jsp:include page="sidebar.jsp" /></jsp:attribute>
	<jsp:body>
		<ul class="breadcrumb">
			<li class="active">${title}</li>
			<li class="pull-right dropdown"><a href="<c:url value="/c/sources/new" />" class="btn btn-primary btn-xs" role="menuitem"><i class="glyphicon glyphicon-plus"></i> New log source</a></li>
			<!-- 
				<li class="pull-right dropdown">
					<button data-toggle="dropdown" href="#" class="btn btn-xs btn-primary"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
					<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
						<li role="presentation"></li>
					</ul>
				</li>
			 -->
		</ul>

		<t:messages />

		<c:choose>
			<c:when test="${empty logSources}">
				<div class="alert alert-info">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					<i class="glyphicon glyphicon-exclamation-sign"></i> You have not created any log source.
				</div>
			</c:when>
			<c:otherwise>
				<c:forEach var="source" items="${logSources}">
					<c:url value="/c/sources/${source.id}/logs" var="logsLink" />
					<div class="row">
						<div class="col-md-12">
							<div class="row">
								<div class="col-md-12">
									<h4><a href="${logsLink}">${source.name}</a></h4>
								</div>
							</div>
							<div class="row">
								<div class="col-md-12">
									<i class="glyphicon glyphicon-list"></i> Log files: <a href="${logsLink}"><span class="label label-info">${source.logs.size()}</span></a>
									<div class="pull-right">
										<div class="btn-group btn-group-sm" role="group" aria-label="Actions">
											<a href="<c:url value="/c/sources/${source.id}" />" class="btn btn-sm btn-default"><i class="glyphicon glyphicon-edit"></i> Edit</a>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
					<hr class="small">
				</c:forEach>
			</c:otherwise>
		</c:choose>
	</jsp:body>
</tpl:bodySidebar>