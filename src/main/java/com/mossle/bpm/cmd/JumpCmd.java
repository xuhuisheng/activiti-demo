package com.mossle.bpm.cmd;


import java.util.List;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

public class JumpCmd implements Command<Object> {
	private String activityId;
	private String executionId;
	private String jumpOrigin;

	public JumpCmd(String executionId, String activityId) {
		this(executionId,activityId,"jump");
	}
	public JumpCmd(String executionId, String activityId , String jumpOrigin) {
		this.activityId = activityId;
		this.executionId = executionId;
		this.jumpOrigin = jumpOrigin;
	}

	public Object execute(CommandContext commandContext) {
		for (TaskEntity taskEntity : commandContext.getTaskEntityManager().findTasksByExecutionId(executionId)) {
			taskEntity.setVariableLocal("跳转原因", jumpOrigin);
			commandContext.getTaskEntityManager().deleteTask(taskEntity, jumpOrigin, false);
		}
		ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(executionId);
		ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
		ActivityImpl activity = processDefinition.findActivity(activityId);
		if(!executeParallelGateWayActivitys(activity,executionEntity)){//如果节点不是并发节点,则直接执行跳转
			executionEntity.executeActivity(activity);
		}

		return null;
	}
	
	protected boolean executeParallelGateWayActivitys(ActivityImpl activity,ExecutionEntity executionEntity){
		boolean isExecuteParallelGateWay = false;
		List<PvmTransition> pvms = activity.getIncomingTransitions();
		if(pvms.size()==1){
			TransitionImpl transitionImpl = (TransitionImpl) pvms.get(0);
			//如果是并发,则同时激活下面多个任务节点
			ActivityImpl gatewayActivity = transitionImpl.getSource();
			if("parallelGateway".equals(transitionImpl.getSource().getProperty("type"))){
				List<PvmTransition>  gatewayTransitions =  gatewayActivity.getOutgoingTransitions();
				executionEntity.takeAll(gatewayTransitions, null);
				isExecuteParallelGateWay = true;
			}
		}
		return isExecuteParallelGateWay;
	}
}
