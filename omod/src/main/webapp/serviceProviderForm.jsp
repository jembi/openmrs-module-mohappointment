<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Manage Services and Providers" otherwise="/login.htm" redirect="/module/mohappointment/serviceProvider.form"/>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/serviceProviderHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.appointment.service.provider.form"/></h2>
<br/>

<!-- <b class="boxHeader"><spring:message code="@MODULE_ID@.appointment.service.provider.current"/></b> -->
<form  action="serviceProvider.form?save=true" method="get">
	<div id="errorDiv"><input type="hidden" name="save" value="true"></div><br/>
	<table>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.provider"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><openmrs_tag:userField formFieldName="provider"/></td>
			<td valign="top"><span id="providerError"></span></td>
		</tr>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.service"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><select name="service" id="service">
					<option value="">--</option>
					<c:forEach items="${services}" var="service">
						<option value="${service.serviceId}">${service.name}</option>
					</c:forEach>
				</select>
			</td>
			<td valign="top"><span id="serviceError"></span></td>
		</tr>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.startdate"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><input type="text" name="startDate" id="startDate" size="11" onclick="showCalendar(this);"/></td>
			<td valign="top"><span id="startDateError"></span></td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td><input type="submit" value="<spring:message code='@MODULE_ID@.general.save'  />" id="btSave" /></td>
			<td></td>
		</tr>
	</table>
</form>

<script type="text/javascript">
	$(document).ready(function(){
		$("#btSave").click(function(){
			if(validateFormFields()){
				if(confirm("<spring:message code='@MODULE_ID@.general.save.confirm'/>"))
					this.form.submit();
			}
		});
	});

	function validateFormFields(){
		var valid=true;
		if(document.getElementsByName("provider")[0].value==''){
			$("#providerError").html("*");
			$("#providerError").addClass("error");
			valid=false;
		} else {
			$("#providerError").html("");
			$("#providerError").removeClass("error");
		}

		if(document.getElementById("service").value==''){
			$("#serviceError").html("*");
			$("#serviceError").addClass("error");
			valid=false;
		} else {
			$("#serviceError").html("");
			$("#serviceError").removeClass("error");
		}

		if($("#startDate").val()==''){
			$("#startDateError").html("*");
			$("#startDateError").addClass("error");
			valid=false;
		} else {
			$("#startDateError").html("");
			$("#startDateError").removeClass("error");
		}

		if(!valid){
			$("#errorDiv").html("<spring:message code='@MODULE_ID@.general.fillbeforesubmit'/>");
			$("#errorDiv").addClass("error");
		} else {
			$("#errorDiv").html("");
			$("#errorDiv").removeClass("error");
		}
		
		return valid;
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>