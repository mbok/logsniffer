<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@tag import="com.logsniffer.model.RollingLog"%>
<%@tag description="Input for a log position" pageEncoding="UTF-8"%>
<%@attribute name="log" required="true" type="com.logsniffer.model.Log"%>
<%@attribute name="name" required="true" type="java.lang.String"%>
<%@attribute name="disabled" required="true" type="java.lang.Boolean"%>
<%@attribute name="active" required="true" type="java.lang.Boolean"%>
<%@attribute name="posPath" required="false" type="java.lang.String"%>
<%@attribute name="pointerTpl" required="true" type="com.logsniffer.model.LogPointerTransfer"%>



<%
	if (log instanceof RollingLog) {
%>
	<div class="row">
		<div class="col-md-2">Rolling file:</div>
		<div class="col-md-10">
			<div class="row" id="${name}part">
				<div class="col-md-8">
					<c:choose>
						<c:when test="${!disabled}">
							<input type="text" class="part-slider hidden" data-slider-min="0" data-slider-max="${fn:length(log.parts)-1}"
										data-slider-tooltip="show">
						</c:when>
						<c:otherwise>
							<div class="progress${active?' progress-striped active':''}">
								<div class="progress-bar bar"></div>
							</div>
						</c:otherwise>
					</c:choose>
				</div>
				<div class="col-md-4">
					<div class="controls">
						<select class="col-md-12" ${disabled?'disabled':''} onchange="${name}.partSlider.setValue(this.options.length-1-this.selectedIndex);${name}.currentFile.reset(0,$(this.options[this.selectedIndex]).data('size'));${name}.fireChangeListener();">
							<c:forEach items="${log.parts}" var="part">
								<option value="${part.path}" data-size="${part.size}"><t:fileName path="${part.path}" /></option>
							</c:forEach>
						</select>
					</div>
				</div>		
			</div>
		</div>
	</div>
	<script type="text/javascript">
		${name}=new LogPosition('${name}', ${disabled}, ${active}, true, ${pointerTpl.json}, function(p) {
			var pStr=JSON.stringify(p);
			console.log("New log pos for "+this.name+": "+pStr);
			$("#"+this.name+"pos").val(pStr);
		});
	</script>
<%
	} else {
%>
	<script type="text/javascript">
		${name}=new LogPosition('${name}', ${disabled}, ${active}, false, ${pointerTpl.json}, function(p) {
			var pStr=JSON.stringify(p);
			console.log("New log pos for "+this.name+": "+pStr);
			$("#"+this.name+"pos").val(pStr);			
		});
	</script>
<%
	}
%>


<div class="row">
	<div class="col-md-2">Log position:</div>
	<div class="col-md-10">
		<div class="row" id="${name}current">
			<div class="col-md-8">
				<c:choose>
					<c:when test="${!disabled}">
						<input type="text" class="filepos-slider hidden" data-slider-tooltip="show">
					</c:when>
					<c:otherwise>
						<div class="progress${active?' progress-striped active':''}">
							<div class="progress-bar bar"></div>
						</div>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="col-md-4 info"></div>
		</div>
	</div>
</div>
<c:if test="${not empty posPath}">
	<input type="hidden" name="${posPath}" id="${name}pos"/>
</c:if>