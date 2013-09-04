package com.mossle.bpm.jiaqian;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryExecutionTest {

	private static Logger log = LoggerFactory.getLogger(HistoryExecutionTest.class);

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule("diagrams/multiSubProcess/activiti.cfg.xml");

	private String processInstanceId;
	private RuntimeService runtimeService;
	private HistoryService historyService;
	private TaskService taskService;
	private Map<String, Object> variableMap;

	@Before
	public void initVariables() {
		runtimeService = activitiRule.getRuntimeService();
		taskService = activitiRule.getTaskService();
		historyService = activitiRule.getHistoryService();
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
	@Deployment(resources = "diagrams/jiaqian/parallel.bpmn20.xml")
	public void testPercent() throws Exception {

		processInstanceId = getStartProcess("parallel");
		assertNotNull(processInstanceId);

		log.info("processInstanceId : {}", processInstanceId);

		completeTask("kermit", "同意");
		completeTask("maike", "不同意");
		completeTask("buchi", "同意");
		completeTask("duora", "不同意");

		completeTask("aobama", "同意");

		List<HistoricActivityInstance> historyActivityInstances = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).activityId("taskuser-1").list();
		assertNotNull(historyActivityInstances);
		for (HistoricActivityInstance historicActivityInstance : historyActivityInstances) {
			HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
					.taskId(historicActivityInstance.getTaskId()).singleResult();
			assertNotNull(historicVariableInstance);
			log.info("activityInstance: {}  taskVariables:{}", historicActivityInstance, historicVariableInstance);
		}

	}

	private String getStartProcess(String key) {
		return runtimeService.startProcessInstanceByKey(key, variableMap).getProcessInstanceId();
	}

	protected void completeTask(String assignee, String result) {
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee)
				.singleResult();
		taskService.setVariableLocal(task.getId(), "result", result);
		taskService.complete(task.getId());
		log.info("完成任务 ID:{} assignee:{} taskDefinitionKey:{} 意见:{}", task.getId(), task.getAssignee(),
				task.getTaskDefinitionKey(),result);
	}

}
