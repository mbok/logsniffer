<%@page import="java.nio.charset.Charset"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://logsniffer.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<script type="text/javascript">
	$(function() {
		$("#reader-grok-wizard .help-popup").popover({placement:"top"});
	});
</script>

<div id="reader-grok-wizard" class="wizard">
	<div class="modal fade" id="grokReference" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
    		<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title">Grok Pattern Matching</h4>
				</div>
				<div class="modal-body">
					<ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
			        	<li class="active"><a href="#tab-groks" data-toggle="tab">Predefined patterns</a></li>
			        	<li><a href="#tab-info" data-toggle="tab">Info</a></li>
				    </ul>
			    	<div class="tab-content" style="overflow:auto;height:380px;">
			        	<div class="tab-pane active" id="tab-groks">
							<table class="table table-condensed log">
								<c:forEach items="${wizard.grokGroups}" var="group">
									<tr><td colspan="2"><h4>${group.key}</h4></td></tr>
									<c:forEach items="${group.value}" var="entry">
										<tr class="text">
											<td>${entry.key}</td>
											<td><code>${entry.value.grokPattern}</code></td>
										</tr>
									</c:forEach>
								</c:forEach>
							</table>
			        	</div>
			        	<div class="tab-pane" id="tab-info">
							<p>
								Grok is a pattern matching concept based on regular expressions. 
								It lets build or use existing sets of named regular expressions to you use them for string matching.
								The goal is to bring more semantics to regular expressions and allows to express ideas rather than syntax.
							</p>
							<h5>Example</h5>
							<p>
								The Grok pattern <code>%{IP:clientip} %{USER:ident}</code> matches a text line starting with an IP followed by a user name e.g. '127.0.0.1 admin'.
								The Grok reader would assign for the parsed log entry the matching IP to the <code>clientip</code> field and the user name to the 
								<code>ident</code> field.
							</p>
							<p>
								Grok was originally developed by <a href="https://github.com/jordansissel" target="_blank">Jordan Sissel</a>.
								See his <a href="https://code.google.com/p/semicomplete/wiki/Grok" target="_blank">concepts page</a> for more details.
							</p>
			        	</div>
			        </div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Close</button>
				</div>
			</div>
		</div>
	</div>

	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-10" fieldName="grokPattern">
			<label for="grokPattern" class="control-label">Grok pattern*: 
				<a href="#" onclick="$('#grokReference').modal();return false"><i class="glyphicon glyphicon-info-sign"></i></a>
			</label>
			<div class="controls controls-row">
				<input type="text" class="form-control pattern" ng-model="bean.grokPattern" name="grokPattern" id="grokPattern" required/>
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
		<div class="form-group col-md-6">
			<label class="control-label">Pattern flags:</label>
			<div class="controls controls-row">
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.caseInsensitive" class="caseInsensitive"/> Case-insensitive matching
				</label>
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.multiLine" class="multiLine"/> Multiline mode
				</label>
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.dotAll" class="dotAll"/> Dotall mode
				</label>
			</div>
		</div>
		<t:ngFormFieldWrapper cssClass="form-group col-md-6" fieldName="overflowAttribute">
			<label for="overflowAttribute" class="control-label">Overflow field: <i class="glyphicon glyphicon-info-sign help-popup" data-container="body" data-html="true" data-content="
				 In case of lines not matching the Grok pattern these can be attached to a field of last well parsed log entry.
				 The overflow field can reference an existing or a new field. If not set, the not matching lines will be attached (as default)
				 to the raw content of the last well parsed log entry."></i></label>
			<input type="text" class="form-control pattern" ng-model="bean.overflowAttribute" id="overflowAttribute" name="overflowAttribute" />
		</t:ngFormFieldWrapper>		
	</div>

</div>