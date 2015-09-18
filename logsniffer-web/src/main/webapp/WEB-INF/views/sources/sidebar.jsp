<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<ul class="nav nav-sidebar">
	<c:forEach var="source" items="${logSources}">
		<li class="${source.id==activeSource.id?'active':''}"><a href="<c:url value="/c/sources/${source.id }/logs" />">${source.name }</a>
			<ul>
				<c:forEach var="log" items="${source.logs}">
					<c:url var="logLink" value="/c/sources/${source.id }/show">
						<c:param name="log" value="${log.path }" />
					</c:url>
					<li class="${log eq activeLog?'active':''}"><a href="${logLink }" title="${log.path }"><t:fileName path="${log.path }" /></a></li>
				</c:forEach>
			</ul>
		</li>
	</c:forEach>
</ul>
