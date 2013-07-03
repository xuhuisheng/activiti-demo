<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentMenu", "bpm");%>
<%pageContext.setAttribute("HEADER_MODEL", "bpm-admin");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.demo.list.title" text="流程列表"/></title>
    <%@include file="/common/s.jsp"%>
  </head>

  <body>
    <%@include file="/header.jsp"%>

    <div class="row-fluid">
	<%@include file="/menu-bpm-admin.jsp"%>

	<!-- start of main -->
    <section id="m-main" class="span10">

      <article class="m-widget">
        <header class="header">
		  <h4 class="title"><spring:message code="demo.demo.list.title" text="列表"/></h4>
		</header>
		<div class="content">

  <table id="demoGrid" class="m-table table table-hover">
    <thead>
      <tr>
        <th width="10" class="m-table-check"><input type="checkbox" name="checkAll" onchange="toggleSelectedItems(this.checked)"></th>
        <th class="sorting" name="id">编号</th>
        <th class="sorting" name="key">代码</th>
        <th class="sorting" name="name">名称</th>
        <th class="sorting" name="category">分类</th>
        <th class="sorting" name="version">版本</th>
        <th class="sorting" name="description">描述</th>
        <th class="sorting" name="suspended">状态</th>
        <th width="150">&nbsp;</th>
      </tr>
    </thead>

    <tbody>
      <s:iterator value="processDefinitions" var="item">
      <tr>
        <td><input type="checkbox" class="selectedItem" name="selectedItem" value="${item.id}"></td>
	    <td>${item.id}</td>
	    <td>${item.key}</td>
	    <td>${item.name}</td>
	    <td>${item.category}</td>
	    <td>${item.version}</td>
	    <td>${item.description}</td>
	    <td>
		  <s:if test="%{suspended}">
		    挂起
            <a href="console!activeProcessDefinition.do?processDefinitionId=${item.id}">(激活)</a>
		  </s:if>
		  <s:else>
		    激活
            <a href="console!suspendProcessDefinition.do?processDefinitionId=${item.id}">(挂起)</a>
		  </s:else>
		</td>
        <td>
          <a href="console!removeProcessDefinition.do?processDefinitionId=${item.id}">删除</a>
          <a href="console!graphProcessDefinition.do?processDefinitionId=${item.id}" target="_blank">流程图</a>
          <a href="console!viewXml.do?processDefinitionId=${item.id}" target="_blank">查看XML</a>
          <a href="${scopePrefix}/diagram-viewer/index.html?processDefinitionId=${item.id}" target="_blank">diagram-viewer</a>
        </td>
      </tr>
      </s:iterator>
    </tbody>
  </table>
        </div>
      </article>

    </section>
	<!-- end of main -->
	</div>

  </body>

</html>
