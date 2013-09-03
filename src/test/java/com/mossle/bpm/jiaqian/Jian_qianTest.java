package com.mossle.bpm.jiaqian;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jian_qianTest {
	
	private static Logger log = LoggerFactory.getLogger(Jian_qianTest.class);

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule("diagrams/multiSubProcess/activiti.cfg.xml");
	
	private String processInstanceId;
	private RuntimeService runtimeService;
	private TaskService taskService;
	private CommandExecutor commandExecutor;
	private Map<String, Object> variableMap;
	
	@Before
	public void initVariables(){
		runtimeService = activitiRule.getRuntimeService();
		taskService = activitiRule.getTaskService();
		commandExecutor = ((ServiceImpl)runtimeService).getCommandExecutor();
		variableMap = new HashMap<String, Object>();
		
		List<String> countersignUsers = new ArrayList<String>();
		countersignUsers.add("kermit");
		countersignUsers.add("duora");
		countersignUsers.add("aobama");
		
		variableMap.put("countersignUsers", countersignUsers);
	}
	
	@Test
	@Deployment(resources="diagrams/jiaqian/parallel.bpmn20.xml")
	public void testParallel() throws Exception { //测试并行多实例
		processInstanceId = getStartProcess("parallel");
		assertNotNull(processInstanceId);
		log.info("processInstanceId : {}",processInstanceId);
		
		logTasks();
		log.info("减签开始 - 移除 aobama");
		commandExecutor.execute(new CountersignCommand("remove", "taskuser-1", "aobama", processInstanceId, "countersignUsers", "countersignUser"));
		logTasks();
		
	}
	
	
	@Test
	@Deployment(resources="diagrams/jiaqian/parallel_subprocess.bpmn20.xml")
	public void testParallel_subprocess() throws Exception {//测试并行子流程多实例
		processInstanceId = getStartProcess("parallel_subprocess");
		assertNotNull(processInstanceId);
		log.info("processInstanceId : {}",processInstanceId);
		
		logTasks();
		log.info("减签开始 - 移除 duora");
		commandExecutor.execute(new CountersignCommand("remove","subprocess1", "duora", processInstanceId,"countersignUsers","countersignUser"));
		logTasks();
		
	}
	
	@Test
	@Deployment(resources="diagrams/jiaqian/sequential.bpmn20.xml")
	public void testSequential(){//测试串行多实例
		processInstanceId = getStartProcess("sequential");
		assertNotNull(processInstanceId);
		log.info("processInstanceId : {}",processInstanceId);
		
		log.info("捡钱开始 - 移除 duora");
		commandExecutor.execute(new CountersignCommand("remove","taskuser-1", "duora", processInstanceId,"countersignUsers","countersignUser"));
	}
	
	
	@Test
	@Deployment(resources="diagrams/jiaqian/sequential_subprocess.bpmn20.xml")
	public void testSequential_subprocess(){//测试串行子流程多实例
		processInstanceId = getStartProcess("sequential_subprocess");
		assertNotNull(processInstanceId);
		log.info("processInstanceId : {}",processInstanceId);
		
		log.info("捡钱开始 - 移除 duora");
		commandExecutor.execute(new CountersignCommand("remove","subprocess1", "duora", processInstanceId,"countersignUsers","countersignUser"));
		
	}
	

	
	private String getStartProcess(String key){
		return runtimeService.startProcessInstanceByKey(key, variableMap).getProcessInstanceId();
	}
	
	private void logTasks(){
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		assertNotNull(tasks);
		log.info("--------------------------------------------------------");
		log.info("{}",tasks);
	}
	
	
	
	protected void completeTask(String assignee){
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee).singleResult();
		taskService.complete(task.getId());
		log.info("完成任务 ID:{} assignee:{} taskDefinitionKey:{}",task.getId(),task.getAssignee(),task.getTaskDefinitionKey());
	}
	
	public static class JiaqianOverListener implements ExecutionListener {
		@Override
		public void notify(DelegateExecution execution) throws Exception {
			log.info("流程执行完毕");
		}
	}
}

