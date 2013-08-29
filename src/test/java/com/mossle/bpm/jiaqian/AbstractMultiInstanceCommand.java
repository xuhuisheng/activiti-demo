package com.mossle.bpm.jiaqian;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/** 
 * @author  izerui.com
 * @version createtime：2013年8月29日 下午9:30:32 
 */
public abstract class AbstractMultiInstanceCommand {
	protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
		execution.setVariableLocal(variableName, value);
	}
	
	protected ExecutionEntity getMultiInstanceParent(CommandContext commandContext,String processInstanceId){
		List<ExecutionEntity> executions = commandContext.getExecutionEntityManager().findChildExecutionsByProcessInstanceId(processInstanceId);
		for (ExecutionEntity executionEntity : executions) {
			if(executionEntity.isActive()){
				return executionEntity.getParent();
			}
		}
		return null;
	}
	protected ExecutionEntity getActiveExecutionEntity(CommandContext commandContext,String processInstanceId) {
		List<ExecutionEntity> executions = commandContext.getExecutionEntityManager().findChildExecutionsByProcessInstanceId(processInstanceId);
		for (ExecutionEntity execution : executions) {
			if(execution.isActive()){
				return execution;
			}
		}
		return null;
	}
}
