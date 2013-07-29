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
 * 并行网关测试
 * 
 * @author LuZhao
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class ParallelTest {

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
	public void testGateway() throws Exception {
		// ============
		ProcessDefinition processDefinition = findProcessDefinitionByDeploymentId();// 查询最新的流程定义
		assertNotNull(processDefinition);

		String requestUserId = "newtec";
		identityService.setAuthenticatedUserId(requestUserId);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey());
		assertNotNull(processInstance);

		assertEquals(Boolean.FALSE, processInstance.isEnded());// 判断流程是否结束

		// taskuser-1
		Task task = findTaks(processInstance).get(0);
		assertTask(task, "admin");

		List<Task> tasks = findTaks(processInstance);
		assertEquals(2, tasks.size());
		assertEquals("taskuser-3", tasks.get(0).getName());
		assertEquals("taskuser-4", tasks.get(1).getName());

		// taskuser-3
		assertTask(tasks.get(0), "admin");
		assertEquals(1, findTaks(processInstance).size());

		// taskuser-4
		assertTask(tasks.get(1), "admin");
		assertEquals(1, findTaks(processInstance).size());

		// taskuser-5
		assertTask(findTaks(processInstance).get(0), "admin");

		HistoricProcessInstance historyProcessInstance = historyService.createHistoricProcessInstanceQuery()//
				.processInstanceId(processInstance.getId())//
				.finished()//
				.singleResult();
		assertNotNull(historyProcessInstance);

		// TODO 流程结束 为何还是false?
		assertEquals(Boolean.TRUE, processInstance.isEnded());// 判断流程是否结束
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
		taskService.claim(taskId, userId);
		taskService.complete(taskId);
	}

	private ProcessDefinition findProcessDefinitionByDeploymentId() {
		return repositoryService.createProcessDefinitionQuery()//
				.processDefinitionKey("parallel")//
				.deploymentId(deployment.getId())//
				.singleResult();
	}

	@Before
	public void setUp() {
		// 部署
		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/parallel/parallel.bpmn") //
				.deploy();
	}

	@After
	public void tearDown() {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}
}
