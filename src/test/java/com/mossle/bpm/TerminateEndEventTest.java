package com.mossle.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 终结结束事件测试.
 *
 * @author Lingo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class TerminateEndEventTest {

	@Resource
	private RepositoryService repositoryService;
	@Resource
	private RuntimeService runtimeService;
	@Resource
	private IdentityService identityService;
	@Resource
	private TaskService taskService;
	@Resource
	private HistoryService historyService;

	private Deployment deployment;

	@Test
	public void testTerminateEndEvent() throws Exception {
		// ============
		ProcessDefinition processDefinition = findProcessDefinitionByDeploymentId();// 查询最新的流程定义
		assertNotNull(processDefinition);

		String requestUserId = "user";
		identityService.setAuthenticatedUserId(requestUserId);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey());
		assertNotNull(processInstance);

		assertEquals(Boolean.FALSE, processInstance.isEnded());// 判断流程是否结束

		// taskuser-1
		Task task = findTaks(processInstance).get(0);
		assertTask(task, "admin");

		List<Task> tasks = findTaks(processInstance);
		assertEquals(2, tasks.size());
		assertEquals("User Task 2", tasks.get(0).getName());
		assertEquals("User Task 3", tasks.get(1).getName());

		// taskuser-2
		assertTask(tasks.get(0), "admin");
		assertEquals(0, findTaks(processInstance).size());

		HistoricProcessInstance historyProcessInstance = historyService.createHistoricProcessInstanceQuery()//
				.processInstanceId(processInstance.getId())//
				.finished()//
				.singleResult();
		assertNotNull(historyProcessInstance.getEndTime());
	}

	private List<Task> findTaks(ProcessInstance processInstance) {
		return taskService.createTaskQuery()//
				.processInstanceId(processInstance.getId())//
				.orderByTaskId()//
				.asc()//
				.list();
	}

	private void assertTask(Task task, String userId) {
		String taskId = task.getId();
		String assignee = task.getAssignee();
		assertEquals(userId, assignee);
		taskService.complete(taskId);
	}

	private ProcessDefinition findProcessDefinitionByDeploymentId() {
		return repositoryService.createProcessDefinitionQuery()//
				.processDefinitionKey("terminate")//
				.deploymentId(deployment.getId())//
				.singleResult();
	}

	@Before
	public void setUp() {
		// 部署
		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/end/terminate.bpmn20.xml") //
				.deploy();
	}

	@After
	public void tearDown() {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}
}
