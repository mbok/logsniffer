<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@tag description="Renders pagination navigation" pageEncoding="UTF-8"%>
<%@attribute name="pagination" required="true" type="com.logsniffer.web.tags.Pagination"%>
<%@attribute name="assetLabel" required="true" type="java.lang.String"%>
<%@attribute name="urlBuilder" required="false" type="org.springframework.web.util.UriComponentsBuilder"%>
<div class="clearfix">
	<div class="pull-left">
		<strong>${assetLabel} ${pagination.currentOffset+1} - ${logfn:min(pagination.currentOffset+pagination.pageSize,pagination.totalCount)} of ${pagination.totalCount}</strong>
	</div>
	<div class="pull-right"><c:out value="${pagination.getPagerHTML(urlBuilder)}" escapeXml="false" /></div>
</div>