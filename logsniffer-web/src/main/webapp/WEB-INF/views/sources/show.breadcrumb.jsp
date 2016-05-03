<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<c:url var="showHref" value="/c/sources/${activeSource.id}/show">
	<c:param name="log" value="${activeLog.path }" />
</c:url>
<c:url var="infoHref" value="/c/sources/${activeSource.id}/info">
	<c:param name="log" value="${activeLog.path }" />
</c:url>

<ul class="breadcrumb">
	<li><a href="<c:url value="/c/sources" />"><spring:message code="logsniffer.breadcrumb.sources" /></a></li>
	<li><a href="<c:url value="/c/sources/${activeSource.id}/logs" />">${activeSource.name}</a></li>
	<li class="active">${activeLog.name}<!-- in ${logfn:filePath(activeLog.path)}  --></li>
	<li class="dropdown">
		<button data-toggle="dropdown" href="#" class="btn btn-xs"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
		<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
			<li><a href="${showHref}"><i class="glyphicon glyphicon-search"></i> Viewer</a></li>
			<li><a href="${infoHref}"><i class="glyphicon glyphicon-info-sign"></i> Info</a></li>
		</ul>
	</li>
</ul>