<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentMenu", "delegate");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="demo.demo.input.title" text="编辑"/></title>
    <%@include file="/common/s.jsp"%>
  </head>

  <body>
    <%@include file="/header.jsp"%>

    <div class="row-fluid">
	<%@include file="/menu-bpm.jsp"%>

	<!-- start of main -->
    <section id="m-main" class="span10" style="float:right">

      <article class="m-widget">
        <header class="header">
		  <h4 class="title"><spring:message code="demo.demo.input.title" text="编辑"/></h4>
		</header>

		<div class="content content-inner">

<form id="demoForm" method="post" action="delegate!autoDelegate.do?operationMode=STORE" class="form-horizontal">
  <input id="demo_id" type="hidden" name="taskId" value="${taskId}">
  <div class="control-group">
    <label class="control-label">代理人</label>
	<div class="controls">
	  <input type="text" name="attorney" value="">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">开始时间</label>
	<div class="controls">
	  <input type="text" name="startTime" value="">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">结束时间</label>
	<div class="controls">
	  <input type="text" name="endTime" value="">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label">流程定义</label>
	<div class="controls">
	  <input type="text" name="endTime" value="">
    </div>
  </div>
  <div class="control-group">
    <div class="controls">
      <button id="submitButton" type="submit" class="btn"><spring:message code='core.input.save' text='保存'/></button>
	  &nbsp;
      <button type="button" onclick="history.back();" class="btn"><spring:message code='core.input.back' text='返回'/></button>
    </div>
  </div>
</form>
        </div>
      </article>

    </section>
	<!-- end of main -->
	</div>

  </body>

</html>
