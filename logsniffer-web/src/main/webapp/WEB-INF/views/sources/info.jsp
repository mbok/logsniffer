<%@page import="com.logsniffer.model.RollingLog"%>
<%@page import="com.logsniffer.model.Log"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${logfn:fileName(activeLog.path)} - Info" activeNavbar="sources">
	<ul class="breadcrumb">
		<li><a href="<c:url value="/c/sources" />"><spring:message code="logsniffer.breadcrumb.sources" /></a></li>
		<li><a href="<c:url value="/c/sources/${activeSource.id}/logs" />">${activeSource.name}</a></li>
		<li class="active">${logfn:fileName(activeLog.path)}</li>
	</ul>
	<ul class="nav nav-tabs">
		<c:url var="showHref" value="/c/sources/${activeSource.id}/show">
			<c:param name="log" value="${activeLog.path }" />
		</c:url>
		<li><a href="${showHref}">Browse</a></li>
		<c:url var="infoHref" value="/c/sources/${activeSource.id}/info">
			<c:param name="log" value="${activeLog.path }" />
		</c:url>
		<li class="active"><a href="${infoHref}">Info</a></li>
	</ul>

	<table class="table table-hover">
		<thead>
			<tr><th>Log file</th><th>Last modified</th><th><div class="text-right">Size</div></th></tr>
		</thead>
		<tbody>
			<c:choose>
				<c:when test="${logfn:isRollingLog(activeLog)}">
					<c:forEach items="${activeLog.parts}" var="part">
						<tr>
							<td>${part.path}</td>
							<td><t:outputDate value="${logfn:getTimestampAsDate(part.lastModified)}" /></td>
							<td><div class="text-right">${logfn:bytesToSize(part.size, 2)}</div></td>
						</tr>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<tr>
						<td>${activeLog.path}</td>
						<td><t:outputDate value="${logfn:getTimestampAsDate(activeLog.lastModified)}" /></td>
						<td><div class="text-right">${logfn:bytesToSize(activeLog.size, 2)}</div></td>
					</tr>					
				</c:otherwise>
			</c:choose>
		</tbody>
	</table>
</tpl:bodyFull>