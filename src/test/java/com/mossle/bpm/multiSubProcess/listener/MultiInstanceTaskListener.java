package com.mossle.bpm.multiSubProcess.listener;

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * 获取实例的index值. 对应获取multiAssignees 变量的assignee  .分别给每个子流程的第一个usertask节点赋与审批人
 *
 */
public class MultiInstanceTaskListener implements TaskListener{

	@Override
	public void notify(DelegateTask delegateTask) {
		List<String> assignees = (List<String>) delegateTask.getVariable("multiAssignees");
		Integer index = (Integer) delegateTask.getExecution().getVariable("loopCounter");
		if(assignees!=null){
			delegateTask.setAssignee(assignees.get(index));
		}
	}

}
