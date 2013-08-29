package com.mossle.bpm.jiaqian;

import java.util.Collection;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author izerui.com
 * @version createtime：2013年8月29日 下午8:57:47
 */
public class SequentialMultiInstanceAddCommand extends AbstractMultiInstanceCommand implements Command<Object> {
	private String activityId;
	private String assignee;
	private String processInstanceId;
	private String collectionVariable;
	

	/**
	 * 增加一条串行多实例(支持子流程) <li>串行请注意complete 顺序.
	 * 因为实例按照定义的collectionVariable变量的顺序生成.同一时刻只能有一个实例
	 * 
	 * @param activityId
	 *            多实例目标节点ID(支持子流程)
	 * @param assignee
	 *            要增加的签收人
	 * @param processInstanceId
	 *            流程实例ID
	 * @param collectionVariable
	 *            设置的elementVariable变量
	 */
	public SequentialMultiInstanceAddCommand(String activityId, String assignee, String processInstanceId, String collectionVariable) {
		this.activityId = activityId;
		this.assignee = assignee;
		this.processInstanceId = processInstanceId;
		this.collectionVariable = collectionVariable;
	}
	

	@Override
	public Object execute(CommandContext commandContext) {
		ExecutionEntity execution = getActiveExecutionEntity(commandContext, processInstanceId);
		if (execution.getProcessDefinition().findActivity(activityId).getProperty("type").equals("subProcess")) {
			if (!execution.isActive() && execution.isEnded() && (execution.getExecutions() == null || execution.getExecutions().size() == 0)) {
				execution.setActive(true);
			}
		}
		Collection<String> col = (Collection<String>) execution.getVariable(collectionVariable);
		col.add(assignee);
		execution.setVariable(collectionVariable, col);
		setLoopVariable(execution, "nrOfInstances", (Integer) execution.getVariableLocal("nrOfInstances") + 1);
		return null;
	}
	

}
