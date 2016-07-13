<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form action="<c:url value="/c/sniffers/${activeSniffer.id}/delete" />" method="post" id="delete-sniffer"></form>
<spring:message code="logsniffer.sniffers.scheduled.true" var="nlsOn" javaScriptEscape="true" />
<spring:message code="logsniffer.sniffers.scheduled.true" var="nlsOff" javaScriptEscape="true" />
<ul class="breadcrumb" ng-init="nlsOn='${nlsOn}';nlsOff='${nlsOff}'">
	<li><a href="<c:url value="/c/sniffers" />"><spring:message code="logsniffer.breadcrumb.sniffers"/></a></li>
	<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}/events" />">${activeSniffer.name}</a></li>
	<li class="dropdown">
		<button data-toggle="dropdown" href="#" class="btn btn-xs"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
		
		<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
			<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}/events" />"><i class="glyphicon glyphicon-bullhorn"></i> Events</a></li>
			<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}" />"><i class="glyphicon glyphicon-edit"></i> Edit</a></li>
			<li><a href="<c:url value="/c/sniffers/${activeSniffer.id}/status" />"><i class="glyphicon glyphicon-play-circle"></i> Control</a></li>
			<li class="divider"></li>
			<li role="presentation" ng-class="{'disabled':scheduleInfo.scheduled}">			
				<a role="menuitem" href="#" ng-if="!scheduleInfo.scheduled" onclick="if (confirm('Delete really?')) {$('form#delete-sniffer').submit()}">
						<i class="glyphicon glyphicon-trash"></i> Delete sniffer</a>
				<a href="#" ng-if="scheduleInfo.scheduled"><i class="glyphicon glyphicon-trash"></i> Delete sniffer</a>
			</li>
		</ul>
		<sup>
			<span class="label label-success" ng-if="scheduleInfo.scheduled"><spring:message code="logsniffer.sniffers.scheduled.true" /></span>
			<span class="label label-default" ng-if="!scheduleInfo.scheduled"><spring:message code="logsniffer.sniffers.scheduled.false" /></span>
		</sup>
	</li>
	<li class="active">${param.context}</li>
</ul>
