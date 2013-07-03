<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%String url = "/bpm/workspace!listProcessDefinitions.do";%>
<%
	if (request.getParameter("username") != null) {
		com.mossle.core.util.SpringSecurityUtils.username = request.getParameter("username");
	}
%>
<%response.sendRedirect(request.getContextPath() + url);%>
