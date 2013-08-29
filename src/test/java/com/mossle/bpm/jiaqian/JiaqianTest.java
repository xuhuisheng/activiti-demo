package com.mossle.bpm.jiaqian;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class JiaqianTest {
	
	private static Logger log = LoggerFactory.getLogger(JiaqianTest.class);

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule("diagrams/multiSubProcess/activiti.cfg.xml");
	
	private String processInstanceId;
	private RuntimeService runtimeService;
	private TaskService taskService;
	private Map<String, Object> variableMap;
	
	@Before
	public void startProcess(){
		runtimeService = activitiRule.getRuntimeService();
		taskService = activitiRule.getTaskService();
		variableMap = new HashMap<String, Object>();
		
		List<String> countersignUsers = new ArrayList<String>();
		countersignUsers.add("kermit");
		countersignUsers.add("maike");
		countersignUsers.add("buchi");
		countersignUsers.add("duora");
		countersignUsers.add("aobama");
		
		variableMap.put("countersignUsers", countersignUsers);
	}

	
	@Test
	@Deployment(resources="diagrams/jiaqian/jiaqian.bpmn")
	public void testBingxing() throws Exception {

		processInstanceId = runtimeService.startProcessInstanceByKey("jiaqian", variableMap).getProcessInstanceId();
		assertNotNull(processInstanceId);
		
		log.info("processInstanceId : {}",processInstanceId);
		
		List<Execution> executions = runtimeService.createExecutionQuery().activityId("taskuser-1").list();
		assertNotNull(executions);
		String parentExecutionId =null;
		for (Execution execution : executions) {
			log.info("executionId:{} parentId:{}",execution.getId(),execution.getParentId());
			parentExecutionId = execution.getParentId();
		}
		
		
		logTasks();
		
		
		completeTask("kermit");
		completeTask("maike");
		completeTask("buchi");
		completeTask("duora");
		
		//开始加签
		addParallelMultiInstance("taskuser-1", "xuhuisheng", parentExecutionId,"countersignUser");
		logTasks();
		
		completeTask("aobama");
		
		completeTask("xuhuisheng");
		
		
		
	}
	
	@Test
	@Deployment(resources="diagrams/jiaqian/jiaqian_chuanxing.bpmn")
	public void testChuanxing(){

		processInstanceId = runtimeService.startProcessInstanceByKey("jiaqian_chuanxing", variableMap).getProcessInstanceId();
		assertNotNull(processInstanceId);
		
		log.info("processInstanceId : {}",processInstanceId);
		
		List<Execution> executions = runtimeService.createExecutionQuery().activityId("taskuser-1").list();
		assertNotNull(executions);
		String parentExecutionId =null;
		if(executions.size()==1){//串行多实例
			parentExecutionId = executions.get(0).getId();
		}
		
		
		logTasks();
		
		
		completeTask("kermit");
		completeTask("maike");
		completeTask("buchi");
		completeTask("duora");
		
		//开始加签
		addSequentialMultiInstance("taskuser-1", "xuhuisheng", parentExecutionId, "countersignUsers");
//		addSequentialMultiInstance("taskuser-1", "xuhuisheng1", parentExecutionId, "countersignUsers");
		logTasks();
		
		completeTask("aobama");
		
		completeTask("xuhuisheng");
//		completeTask("xuhuisheng1");
	}
	private void logTasks(){
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		assertNotNull(tasks);
		log.info("--------------------------------------------------------");
		for (Task task : tasks) {
			log.info("taskID:{} assignee:{}",task.getId(),task.getAssignee());
		}
	}
	
	private void addParallelMultiInstance(final String actvityId,final String assignee,final String parentExecutionId,final String collectionElementVariable){
		((ServiceImpl)runtimeService).getCommandExecutor().execute(new Command<Object>() {
			public Object execute(CommandContext commandContext) {
				ExecutionEntity parentExecutionEntity = commandContext.getExecutionEntityManager().findExecutionById(parentExecutionId);
				ExecutionEntity execution = parentExecutionEntity.createExecution();
				execution.setActive(true);
				execution.setConcurrent(true);
				execution.setScope(false);
				
				setLoopVariable(parentExecutionEntity, "nrOfInstances", (Integer)parentExecutionEntity.getVariableLocal("nrOfInstances")+1);
				setLoopVariable(parentExecutionEntity, "nrOfActiveInstances", (Integer)parentExecutionEntity.getVariableLocal("nrOfActiveInstances")+1);
				setLoopVariable(execution, "loopCounter", parentExecutionEntity.getExecutions().size()+1);
				setLoopVariable(execution, collectionElementVariable, assignee);
				
				ProcessDefinitionImpl processDefinition = parentExecutionEntity.getProcessDefinition();
				ActivityImpl activity = processDefinition.findActivity(actvityId);
				
				
				execution.executeActivity(activity);
				return null;
			}
		});
	}
	
	//串行请注意complete 顺序. 因为实例按照定义的collectionVariable变量的顺序生成.同一时刻只能有一个实例
	private void addSequentialMultiInstance(final String actvityId,final String assignee,final String executionId,final String collectionVariable){
		((ServiceImpl)runtimeService).getCommandExecutor().execute(new Command<Object>() {
			public Object execute(CommandContext commandContext) {
				ExecutionEntity execution = commandContext.getExecutionEntityManager().findExecutionById(executionId);
				Collection<String> col = (Collection<String>)execution.getVariable(collectionVariable);
				col.add(assignee);
				execution.setVariable(collectionVariable, col);
				setLoopVariable(execution, "nrOfInstances", (Integer)execution.getVariableLocal("nrOfInstances")+1);
				return null;
			}
		});
	}
	
	
	 protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
		    execution.setVariableLocal(variableName, value);
		  }
	
	protected void completeTask(String assignee){
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee).singleResult();
		taskService.complete(task.getId());
		log.info("完成任务 ID:{} assignee:{}",task.getId(),task.getAssignee());
	}
	
	public static class JiaqianOverListener implements ExecutionListener {
		@Override
		public void notify(DelegateExecution execution) throws Exception {
			log.info("流程执行完毕");
		}
	}
}

