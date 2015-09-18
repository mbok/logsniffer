<%@tag description="Outputs messages" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<c:if test="${not empty message}">
	<div class="alert alert-${message.type.name().toLowerCase()}">
		<button type="button" class="close" data-dismiss="alert">&times;</button>
		${message.text}
	</div>
</c:if>