<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form action="<c:url value="/c/sources/${activeSource.id}/delete" />" method="post">
	<ul class="breadcrumb">
		<li><a href="<c:url value="/c/sources" />"><spring:message code="logsniffer.breadcrumb.sources" /></a></li>
		<li class="active">${activeSource.name}</li>
		<li class="dropdown">
			<button data-toggle="dropdown" href="#" class="btn btn-xs"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
			<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
				<li role="presentation">			
					<a role="menuitem" href="#" onclick="if (confirm('Delete really?')) {$(this).parents('form').submit()}">
							<i class="glyphicon glyphicon-trash"></i> Delete log source</a>
				</li>
			</ul>
		</li>
	</ul>
</form>