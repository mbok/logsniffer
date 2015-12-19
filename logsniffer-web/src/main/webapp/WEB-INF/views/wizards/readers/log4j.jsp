<%@page import="java.nio.charset.Charset"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript">
	$(function() {
		$("#reader-log4j-wizard .help-popup").popover({placement:"top"});
	});
	LogSnifferNgApp.controllerProvider.register(
		"Log4jWizardController", ['$scope', function ($scope) {
		$scope.placeHolders = {
			c: "category",
			t: "thread",
			m: "message",
			p: "priority",
			d: "date"
		};
		$scope.getUsedSpecifiers = function(formatPattern) {
			var specifiers=[];
			var re = /%[\d-\.]*([a-z]+)/gi;
			var m;
			do {
			    m = re.exec(formatPattern);
			    if (m) {
			    	specifiers.push(m[1]);
			    }
			} while (m);
			return specifiers;
		};
	}]);
</script>
<div id="reader-log4j-wizard" class="wizard" ng-controller="Log4jWizardController">
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-10" fieldName="formatPattern">
			<label class="control-label" for="formatPattern">Conversion pattern*: 
				<i class="glyphicon glyphicon-info-sign help-popup" data-container="body" data-html="true" data-content="
				 In your log4j configuration the &lt;i&gt;ConversionPattern&lt;/i&gt; parameter of the desired appender controls the contents of each line of output inside the log file.
				 In order to parse the content attributes correctly please insert the used &lt;i&gt;ConversionPattern&lt;/i&gt; into this field. For example:
				 &lt;code&gt;%d %-5p [%c] (%t) %m%n&lt;/code&gt;.
				 For more detail about the syntax, see the &lt;a href='http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html' target='_blank'&gt;ConversionPattern&lt;/a&gt;
				 API documentation."
				 data-title="Log4j Conversion Pattern"></i>
			</label>
			<div class="controls controls-row">
				<input type="text" class="form-control pattern" ng-model="bean.formatPattern" name="formatPattern" id="formatPattern" required/>
			</div>
		</t:ngFormFieldWrapper>
		<t:ngFormFieldWrapper cssClass="form-group col-md-2" fieldName="charset">
			<label for="charset" class="control-label">Character set*:</label>
			<select name="charset" id="charset" ng-model="bean.charset" class="form-control" required>
				<option value=""><spring:message code="logsniffer.common.pleaseSelect" /></option>
				<c:forEach items="<%=Charset.availableCharsets() %>" var="entry">
					<option value="${entry.key}">${entry.key}</option>
				</c:forEach>
			</select>
		</t:ngFormFieldWrapper>
	</div>
	<div class="row">
		<div class="form-group col-md-12">
			<label class="control-label">Conversion specifier field mapping:
				<i class="glyphicon glyphicon-info-sign help-popup" data-container="body" data-html="true" data-content="
					To assign the extracted log parts to fields with legible names define for each used log4j conversion specifier
					a mapping to the desired field name. For example: &lt;br/&gt;&lt;code&gt;%c=category&lt;/code&gt;&lt;br/&gt;
					&lt;code&gt;%t=thread&lt;/code&gt;&lt;br/&gt;
					&lt;small&gt;Note: The specifier &lt;code&gt;%d&lt;/code&gt; and &lt;code&gt;%p&lt;/code&gt; are parsed and 
					mapped additionally to the internal fields &lt;code&gt;lf_timestamp&lt;/code&gt; and &lt;code&gt;lf_severity&lt;/code&gt;
					by default.&lt;/small&gt;"
					 data-title="Conversion Specifier Mapping"></i></label>
			<div class="row form-group form-horizontal" ng-repeat="c in getUsedSpecifiers(bean.formatPattern)" ng-if="c!='n'">
				<div class="col-md-1 cc"><label class="control-label">%{{c}}:</label></div>				
				<div class="col-md-5">
					<div class="controls controls-row">
						<input type="text" class="form-control" ng-model="bean.specifiersFieldMapping[c]" placeholder="{{placeHolders[c]}}" />
					</div>
				</div>
			</div>
			<div class="help-block" ng-if="getUsedSpecifiers(bean.formatPattern).length==0">Please configure first the proper conversion pattern!</div>
		</div>
	</div>

</div>