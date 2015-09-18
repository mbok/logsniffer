<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tpl:bodyFull title="${activeSniffer.name } - Control" activeNavbar="sniffers">
	<jsp:body>
		<script type="text/javascript">
			var logPositioners=[];
		</script>
			<jsp:include page="sniffer.breadcrumb.jsp">
				<jsp:param value="Control" name="context"/>
			</jsp:include>
			
			<form action="../${activeSniffer.id}/status" method="post">
				<div class="clearfix">
					<div class="pull-right">
						Last run: <t:outputDate value="${scheduleInfo.lastFireTime}" emptyLabel="- never -" />
					</div>
					<div class="btn-toolbar">
						<div class="btn-group">
							<button class="btn btn-default dropdown-toggle"<c:if test="${scheduled}">disabled="disabled"</c:if> data-toggle="dropdown">Reset position
								<span class="caret"></span>
							</button>
							<ul class="dropdown-menu">
								<li><a href="#" onclick="$.each(logPositioners, function(i,v) {v.resetToStart()}); return false">
									<i class="glyphicon glyphicon-fast-backward"></i> All to start</a></li>
								<li><a href="#" onclick="$.each(logPositioners, function(i,v) {v.resetToEnd()}); return false">
									<i class="glyphicon glyphicon-fast-forward"></i> All to end</a></li>
							</ul>
						</div>
						<button class="btn btn-default"<c:if test="${scheduled}">disabled="disabled"</c:if> type="button" onclick="this.form.action+='/../startForm';this.form.submit()">
							<i class="glyphicon glyphicon-play"></i> Start</button>						
						<button class="btn btn-default"<c:if test="${!scheduled}">disabled="disabled"</c:if> type="button" onclick="this.form.action+='/../stopForm';this.form.submit()">
							<i class="glyphicon glyphicon-pause"></i> Pause</button>						
					</div>

				</div>
				<c:if test="${started}">
					<div class="alert alert-success">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
						Monitoring started!
					</div>
				</c:if>
				<c:if test="${stopped}">
					<div class="alert alert-success">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
						Monitoring stopped!
					</div>
				</c:if>

				<h3>Positioning
					 <small><spring:message code="logsniffer.sniffers.positioning.${scheduled?'show':'set'}"/></small>
				</h3>
				<c:forEach items="${logsStatus}" var="status" varStatus="i">
					<div class="well well-sm">
						<h4>${status.key}</h4>
						<t:logPositioner log="${status.value.log}" pointerTpl="${status.value.pointerTpl}" name="log${i.index}" active="${scheduled}" disabled="${scheduled}" posPath="newPositions['${status.key}']" />
						<script type="text/javascript">
						logPositioners.push(log${i.index});
							$(function() {
								log${i.index}.init(${empty status.value.nextOffset.json ? 'null' : status.value.nextOffset.json}, ${status.value.logSize});
							});
						</script>
					</div>
				</c:forEach>
			</form>
	</jsp:body>
</tpl:bodyFull>