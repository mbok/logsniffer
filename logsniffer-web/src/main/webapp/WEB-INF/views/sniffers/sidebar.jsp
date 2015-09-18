<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>

<ul class="nav nav-sidebar">
	<c:forEach var="sniffer" items="${sniffers}">
		<c:url var="snifferLink" value="/c/sniffers/${sniffer.id }/events" />
		<li class="${sniffer eq activeSniffer?'active':''}">
			<a href="${snifferLink}" title="${sniffer.name}">${sniffer.name} 
				<c:set var="count" value="${logfn:aspect(sniffer,'eventsCount') }" />
				<c:if test="${count>0}"><span class="badge badge-warning pull-right">${count}</span></c:if>
			</a></li>
	</c:forEach>
</ul>