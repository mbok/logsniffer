<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form action="<c:url value="/c/sniffers/${activeSniffer.id}/delete" />" method="post">
	<ul class="breadcrumb">
		<li><a href="<c:url value="/c/sniffers" />"><spring:message code="logsniffer.breadcrumb.sniffers"/></a></li>
		<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}/events" />">${activeSniffer.name}</a></li>
		<li class="dropdown">
			<button data-toggle="dropdown" href="#" class="btn btn-xs"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
			
			<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
				<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}/events" />"><i class="glyphicon glyphicon-bullhorn"></i> Events</a></li>
				<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}" />"><i class="glyphicon glyphicon-edit"></i> Edit</a></li>
				<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}/status" />"><i class="glyphicon glyphicon-play-circle"></i> Control</a></li>
				<li class="divider"></li>
				<li role="presentation" class="${scheduled?'disabled':''}">			
					<a role="menuitem" href="#" onclick="if (${!scheduled} && confirm('Delete really?')) {$(this).parents('form').submit()}">
							<i class="glyphicon glyphicon-trash"></i> Delete sniffer</a>
				</li>
			</ul>
			<sup><span class="label${scheduled?' label-success':' label-default'}"><spring:message code="logsniffer.sniffers.scheduled.${scheduled}"/></span></sup>
		</li>
		<li class="active">${param.context}</li>
	</ul>
</form>