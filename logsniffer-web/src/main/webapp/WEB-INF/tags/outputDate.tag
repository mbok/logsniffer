<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@tag description="Outputs a formated date string" pageEncoding="UTF-8"%>
<%@attribute name="value" required="true" type="java.util.Date"%>
<%@attribute name="emptyLabel" required="false" type="java.lang.String"%>
<c:choose>
	<c:when test="${not empty value}"><fmt:formatDate type="both" value="${value}" /></c:when>
	<c:when test="${not empty emptyLabel}">${emptyLabel}</c:when>
</c:choose>