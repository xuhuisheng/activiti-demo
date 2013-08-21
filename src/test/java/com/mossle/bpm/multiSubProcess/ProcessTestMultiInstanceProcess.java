package com.mossle.bpm.multiSubProcess;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTestMultiInstanceProcess {
	private static Logger logger = LoggerFactory.getLogger(ProcessTestMultiInstanceProcess.class);

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule("diagrams/multiSubProcess/activiti.cfg.xml");
	
	private RepositoryService repositoryService;
	private RuntimeService runtimeService;
	private TaskService taskService;
	
	@Test
	public void startProcess() throws Exception {
		
		int nrOfMinutes = askForIntegerInput("\n请输入子流程暂停时间(单位:秒 等待时间超过10秒. \n并发子流程会触发边界timer事件, 自动销毁!\n不信你输入11试试?)");
		
		List<String> multiAssignees = new ArrayList<String>();
		multiAssignees.add("subOne");
		multiAssignees.add("subTwo");
		multiAssignees.add("subThere");
		
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("name", "Activiti");
		variableMap.put("multiAssignees", multiAssignees);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess1", variableMap);
		assertNotNull(processInstance.getId());
		logger.info("退学申请流程({})已经启动  processInstanceId : {} ",processInstance.getProcessDefinitionId(),processInstance.getId());
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
		assertNotNull(task);
		logger.info("userTask:{}  taskId:{}",task.getTaskDefinitionKey(),task.getId());
		taskService.complete(task.getId());
		logger.info("生活用品已经上交给生活委员了");
		
		
		logger.info("开始并发子流程了,请各个小组长注意了.");
		
		List<Task> tasks = getTasks(processInstance.getProcessInstanceId());
		logger.info("{}" , tasks);
		for (int i = 0; i < tasks.size(); i++) {
			logger.info("{} 开始审批"+multiAssignees.get(i));
			taskService.complete(tasks.get(i).getId());
		}
		
		tasks = getTasks( processInstance.getProcessInstanceId());
		
//		runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());
		// 测试等待11秒
		logger.info("等..........");
		try { Thread.sleep ( nrOfMinutes * 1000) ; 
		} catch (InterruptedException ie){}
		logger.info("{}",tasks);
		
		if(nrOfMinutes<=10){
			taskService.complete(tasks.get(0).getId());
			taskService.complete(tasks.get(1).getId());
			try {
				taskService.complete(tasks.get(2).getId());
			} catch (ActivitiObjectNotFoundException e) {
				logger.info("好吧. 班长3你不用审批了.同意两个就可以了 {}");
			}
		}else{
			logger.info("自动结束");
		}
		logger.info("..... 班主任,校长同意了你就可以退学了");
		
	}
	
	@Before
	public void init(){
		repositoryService = activitiRule.getRepositoryService();
		runtimeService = activitiRule.getRuntimeService();
		taskService = activitiRule.getTaskService();
		repositoryService.createDeployment().addClasspathResource("diagrams/multiSubProcess/multiInstanceSubProcess.bpmn").deploy();
	}
	
	private List<Task> getTasks(String processInstanceId){
		return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
	}
	
	
	public static int askForIntegerInput(String text) {
	    String input = askForInput(text);
	    return Integer.valueOf(input);
	  }
	
	 public static String askForInput(String text) {
	      logger.info(text);

	      Scanner scanner = new Scanner(System.in);
	      String input = scanner.nextLine();
	      return input;
	  }
}