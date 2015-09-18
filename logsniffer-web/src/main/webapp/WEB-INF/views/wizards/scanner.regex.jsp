<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="scanner-regex-wizard">
	<div class="row">
		<spring:bind path="${param.formPath}.wizard.formData.pattern">
			<div class="form-group col-md-6 ${status.error?'has-error':''}">
				<form:label path="${status.expression}" cssClass="control-label">Regular expression pattern: 
					<i class="glyphicon glyphicon-info-sign help-popup" data-html="true" data-container="body" data-content="This scanner is looking for entries with a sequence (not entire string!) in the log entry text
						matching the given regular expression. The matching groups of the first subsequence are mapped to the event data fields.
						See the documentation of the &lt;a href='http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html' target='_blank'&gt;Pattern&lt;/a&gt; class for 
						correct pattern syntax and for description of below flags." data-title="Regular expression scanner"></i>
				</form:label>
				<form:input path="${status.expression}" cssClass="form-control pattern"/>
				<form:errors path="${status.expression}" cssClass="help-block" />
			</div>
			<div class="form-group col-md-6">
				<label>Pattern flags:</label>
				<div>
					<label class="checkbox-inline">
						<form:checkbox path="${param.formPath}.wizard.formData.caseInsensitive" cssClass="caseInsensitive"/> Case-insensitive matching
					</label>
					<label class="checkbox-inline">
						<form:checkbox path="${param.formPath}.wizard.formData.multiLine" cssClass="multiLine"/> Multiline mode
					</label>
					<label class="checkbox-inline">
						<form:checkbox path="${param.formPath}.wizard.formData.dotAll" cssClass="dotAll"/> Dotall mode
					</label>
				</div>
			</div>
		</spring:bind>
	</div>
	<hr class="spaceless">
	<h4>Pattern test</h4>
	<div class="row">
		<div class="form-group col-md-12">
			<label for="regex-test-input">Example log entry:</label>
			<textarea rows="3" class="form-control" id="regex-test-input" placeholder="Insert a log entry example here to test the pattern matching"></textarea>
			<a href="#" class="btn btn-default btn-xs" onclick="testScannerPattern();return false;"><i class="glyphicon glyphicon-check"></i> Test pattern</a>
		</div>
	</div>
	<div class="row">
		<div class="form-group col-md-12">
			<label for="regex-test-input">Result:</label>
			<div class="regex-test-result"><span class="text-muted">- not yet tested -</span></div>
		</div>
	</div>
</div>
<c:url var="testRestServiceUrl" value="/c/wizards/regexScanner/test" />
<script type="text/javascript">
<!--
function testScannerPattern() {
	var postObj = new Object();
	postObj.pattern=$("#scanner-regex-wizard .pattern").val();
	postObj.text=$("#scanner-regex-wizard #regex-test-input").val();
	postObj.dotAll=$("#scanner-regex-wizard .dotAll:checked").length>0;
	postObj.multiLine=$("#scanner-regex-wizard .multiLine:checked").length>0;
	postObj.caseInsensitive=$("#scanner-regex-wizard .caseInsensitive:checked").length>0;
	var spinner=new Spinner().spin($("#scanner-regex-wizard .regex-test-result")[0]);
	$.ajax({
	    type: "POST",
	    url: "${testRestServiceUrl}",
	    data: postObj,
	}).done(function (data) {
        if (data) {
        	var dl='';
        	for (var i in data.data) {
        		dl+='<dt>'+i+'</dt><dd>'+data.data[i]+'</dd>';
        	}
        	$("#scanner-regex-wizard .regex-test-result").html('<div class="alert alert-success"><h4>It matches!</h4>'+(dl?'<dl class="dl-horizontal">'+dl+'</dl>':'')+'</div>');
        } else {
        	$("#scanner-regex-wizard .regex-test-result").html('<div class="alert alert-error"><h4>No match!</h4></div>');
        }
    }).fail(function(jqXHR, textStatus) {
    	$("#scanner-regex-wizard .regex-test-result").html('<div class="alert alert-error"><h4>Error!</h4>'+jqXHR.statusText+'</div>');
    }).always(function() {
    	spinner.stop();
    });
}
//-->
</script>
