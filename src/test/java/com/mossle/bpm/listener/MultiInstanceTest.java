package com.mossle.bpm.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.*;
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
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

/**
 * 测试为multi-instance设置ExecutionListener.
 *
 * @author Lingo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class MultiInstanceTest {

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
	public void testMultiInstance() throws Exception {
		// ============
		ProcessDefinition processDefinition = findProcessDefinitionByDeploymentId();// 查询最新的流程定义
		assertNotNull(processDefinition);

		String requestUserId = "admin";
		identityService.setAuthenticatedUserId(requestUserId);
		List countersignUsers = new ArrayList();
		countersignUsers.add("admin");
		countersignUsers.add("user");
		Map map = new HashMap();
		map.put("rate", "100");
		map.put("countersignUsers", countersignUsers);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(), map);
		assertNotNull(processInstance);

		assertEquals(Boolean.FALSE, processInstance.isEnded());// 判断流程是否结束

		List<Task> tasks = findTaks(processInstance);
		assertEquals(2, tasks.size());
		assertEquals("会签", tasks.get(0).getName());
		assertEquals("会签", tasks.get(1).getName());
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
				.processDefinitionKey("multi-instance")//
				.deploymentId(deployment.getId())//
				.singleResult();
	}

	@Before
	public void setUp() {
		// 部署
		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/multi-instance/multi-instance.bpmn20.xml") //
				.deploy();
	}

	@After
	public void tearDown() {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}

	public static class MultiInstanceExecutionListener implements ExecutionListener {
		public void notify(DelegateExecution delegateExecution) {
			ExecutionEntity executionEntity = (ExecutionEntity) delegateExecution;
			TransitionImpl transition = executionEntity.getTransition();

			String activityId = delegateExecution.getCurrentActivityId();
			// System.out.println("activityId : " + activityId);
			if (transition == null) {
				ActivityImpl activityImpl = executionEntity.getActivity();
				// System.out.println("activityImpl : " + activityImpl);
				// System.out.println("activityImpl : " + activityImpl.getParent());
				// System.out.println("activityImpl : " + activityImpl.getProperties());
			} else {
				ActivityImpl targetActivity = transition.getDestination();
				System.out.println("targetActivity : " + targetActivity);
				if (targetActivity.getProperty("multiInstance") != null) {
					List countersignUsers = new ArrayList();
					countersignUsers.add("admin");
					countersignUsers.add("user");
					countersignUsers.add("lingo");
					countersignUsers.add("vivian");
					delegateExecution.setVariable("countersignUsers", countersignUsers);
				}
			}

		}
	}

	public static class MultiInstanceTaskListener implements TaskListener {
		public void notify(DelegateTask delegateTask) {
			System.out.println(delegateTask);
		}
	}
}
