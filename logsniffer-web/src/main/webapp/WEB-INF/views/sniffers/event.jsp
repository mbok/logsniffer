<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:formatDate type="both" value="${event.occurrence}" var="occurrenceStr"/>
<tpl:bodyFull title="Event from ${occurrenceStr} in ${activeSniffer.name }" activeNavbar="sniffers">
	<jsp:body>
		<style>
		.log .text {
			padding: 9px;
		}
		</style>

		<ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
	       	<li class="active"><a href="#tab-overview" data-toggle="tab">Overview</a></li>
	       	<!-- <li><a href="#tab-index" data-toggle="tab">Indexed data</a></li> -->
		</ul>
    	<div class="tab-content">
        	<div class="tab-pane active" id="tab-overview">
			
        	</div>
        	<div class="tab-pane active" id="tab-index">
				
        	</div>
        </div>

	</jsp:body>
</tpl:bodyFull>