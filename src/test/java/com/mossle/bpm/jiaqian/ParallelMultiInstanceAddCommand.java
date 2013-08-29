package com.mossle.bpm.jiaqian;

import java.util.List;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * @author izerui.com
 * @version createtime：2013年8月29日 下午8:57:47
 */
public class ParallelMultiInstanceAddCommand extends AbstractMultiInstanceCommand implements Command<Object> {
	
	private String activityId;
	private String assignee;
	private String collectionElementVariable;
	private String processInstanceId;
	
	/**
	 * 增加一条并行多实例
	 * 
	 * @param actvityId
	 *            多实例目标节点ID(支持子流程)
	 * @param assignee
	 *            要增加的签收人
	 * @param processInstanceId
	 *            实例ID
	 * @param collectionElementVariable
	 *            设置的elementVariable变量
	 */
	public ParallelMultiInstanceAddCommand(String actvityId, String assignee, String processInstanceId, String collectionElementVariable) {
		this.activityId = actvityId;
		this.assignee = assignee;
		this.processInstanceId = processInstanceId;
		this.collectionElementVariable = collectionElementVariable;
		
	}
	

	@Override
	public Object execute(CommandContext commandContext) {
		ExecutionEntity parentExecutionEntity = getMultiInstanceParent(commandContext, processInstanceId);
		ProcessDefinitionImpl processDefinition = parentExecutionEntity.getProcessDefinition();
		ActivityImpl activity = processDefinition.findActivity(activityId);

		ExecutionEntity execution = parentExecutionEntity.createExecution();
		execution.setActive(true);
		execution.setConcurrent(true);
		execution.setScope(false);
		if (activity.getProperty("type").equals("subProcess")) {
			ExecutionEntity extraScopedExecution = execution.createExecution();
			extraScopedExecution.setActive(true);
			extraScopedExecution.setConcurrent(false);
			extraScopedExecution.setScope(true);
			execution = extraScopedExecution;
		}
		setLoopVariable(parentExecutionEntity, "nrOfInstances", (Integer) parentExecutionEntity.getVariableLocal("nrOfInstances") + 1);
		setLoopVariable(parentExecutionEntity, "nrOfActiveInstances", (Integer) parentExecutionEntity.getVariableLocal("nrOfActiveInstances") + 1);
		setLoopVariable(execution, "loopCounter", parentExecutionEntity.getExecutions().size() + 1);
		setLoopVariable(execution, collectionElementVariable, assignee);

		execution.executeActivity(activity);
		return null;
	}

	
}
