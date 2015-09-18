<%@tag description="Renders a popover using the Bootstrap styles" pageEncoding="UTF-8"%>
<%@attribute name="cssClass" required="false" type="java.lang.String"%>
<%@attribute name="style" required="false" type="java.lang.String"%>
<%@attribute name="title" required="false" type="java.lang.String"%>
<div style="position:absolute;right:0;top:0">
	<div style="position:relative">
		<div class="popover right fade ${cssClass}" style="top: -12px; left: 0px; display: block;${style}">
		  <div class="arrow"></div>
		
		  <div class="popover-inner">
		      <h3 class="popover-title${empty title?' hidden':''}">${title}</h3>
		      <div class="popover-content"><jsp:doBody /></div>
		  </div>
		</div>
	</div>
</div>