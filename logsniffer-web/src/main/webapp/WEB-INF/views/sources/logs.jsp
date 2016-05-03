<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${activeSource.name} - Logs" activeNavbar="sources">
	<jsp:body>
		<jsp:include page="source.breadcrumb.jsp" />
	
		<c:if test="${not empty message}">
			<div class="alert alert-${message.type.name().toLowerCase()}">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
				${message.text}
			</div>
		</c:if>

		<c:choose>
			<c:when test="${empty logs}">
				<div class="alert alert-warning">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					<i class="glyphicon glyphicon-exclamation-sign"></i> No logs resolvable. Configuration error?
				</div>
			</c:when>
			<c:otherwise>
				<c:forEach var="log" items="${logs}">
					<c:url var="tailUrl" value="/c/sources/${activeSource.id}/show">
						<c:param name="log" value="${log.path}" />
						<c:param name="count" value="-${defaultCount}" />
					</c:url>
					<c:url var="showUrl" value="/c/sources/${activeSource.id}/show">
						<c:param name="log" value="${log.path}" />
					</c:url>
					<div class="row">
						<div class="col-md-12">
							<div class="row">
								<div class="col-md-12">
									<h4><a href="${showUrl}">${log.name}</a><br> <small>${logfn:filePath(log.path)}</small></h4>
								</div>
							</div>
							<div class="row">
								<div class="col-md-12">
									<a href="${tailUrl}##tail"><i class="glyphicon glyphicon-hand-down"></i> Tail</a>
									|
									Last modified: <t:outputDate value="${logfn:getTimestampAsDate(log.lastModified)}" />
									|
									Size: ${logfn:bytesToSize(log.size, 2)}
								</div>
							</div>
						</div>
					</div>
					<hr class="small">
				</c:forEach>
			</c:otherwise>
		</c:choose>
	</jsp:body>
</tpl:bodyFull>