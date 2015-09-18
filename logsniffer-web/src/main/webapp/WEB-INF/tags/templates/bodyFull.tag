<%@tag description="Full Size Body Template" pageEncoding="UTF-8"%>
<%@attribute name="activeNavbar" required="false" type="java.lang.String"%>
<%@attribute name="title" required="true" type="java.lang.String"%>
<%@attribute name="ngModules" required="false"  type="java.lang.String" %>
<%@attribute name="htmlHead" required="false" fragment="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>

<tpl:main title="${title}" ngModules="${ngModules}">
	<jsp:attribute name="htmlHead"><jsp:invoke fragment="htmlHead"/></jsp:attribute>
	<jsp:body>
		<tpl:navbar active="${activeNavbar}" />
		<div class="container-fluid">
			<div class="row">
				<div class="main">
					<jsp:doBody />
				</div>
			</div>
		</div>
	</jsp:body>
</tpl:main>