<%@ page language="java" pageEncoding="UTF-8" %>
      <!-- start of sidebar -->
      <aside id="m-sidebar" class="accordion span2" data-spy="affix" data-offset-top="100">

        <div class="accordion-group">
          <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#m-sidebar" href="#collapse-bpm">
              <i class="icon-user"></i>
              <span class="title">流程管理</span>
            </a>
          </div>
          <div id="collapse-bpm" class="accordion-body collapse ${currentMenu == 'bpm' ? 'in' : ''}">
            <ul class="accordion-inner nav nav-list">
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listPersonalTasks.do">待办任务</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listGroupTasks.do">待领任务</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listHistoryTasks.do">已办任务</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listDelegatedTasks.do">代理中的任务</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listRunningProcessInstances.do">发起的流程</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listInvolvedProcessInstances.do">参与的流程</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/workspace!listProcessDefinitions.do">流程列表</a></li>
            </ul>
          </div>
        </div>

        <div class="accordion-group">
          <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#m-sidebar" href="#collapse-delegate">
              <i class="icon-user"></i>
              <span class="title">设置自动委托</span>
            </a>
          </div>
          <div id="collapse-delegate" class="accordion-body collapse ${currentMenu == 'delegate' ? 'in' : ''}">
            <ul class="accordion-inner nav nav-list">
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/delegate!prepareAutoDelegate.do">设置自动委托</a></li>
			  <li class="m-icn-add-user"><a href="${scopePrefix}/bpm/delegate!listMyDelegateInfos.do">自动委托规则</a></li>
            </ul>
          </div>
        </div>

		<footer id="m-footer" class="text-center">
		  <hr>
		  &copy;Mossle
		</footer>
      </aside>
      <!-- end of sidebar -->
