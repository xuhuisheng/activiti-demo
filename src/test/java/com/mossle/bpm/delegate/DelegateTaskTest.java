package com.mossle.bpm.delegate;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
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
 * 对委托任务进行测试
 *
 * @author LuZhao
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class DelegateTaskTest {

	@Resource
	private RepositoryService repositoryService;

	@Resource
	private RuntimeService runtimeService;

	@Resource
	private TaskService taskService;

	@Resource
	private IdentityService identityService;

	@Resource
	private HistoryService historyService;

	private Deployment deployment;

	@Test
	public void testDelegateTask01() throws Exception {

		ProcessDefinition processDefinition = findProcessDefinitionByDeploymentId();// 查询最新的流程定义

		assertNotNull(processDefinition);

		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey());
		assertNotNull(processInstance);

		Task task = findPersonTask(processInstance);// 查询办理任务
		String taskId = task.getId();
		assertNotNull(taskId);

		/** 第一次委托 */
		String userId = "leaderuser";

		taskService.delegateTask(taskId, userId);
		taskService.resolveTask(taskId);
		taskService.addUserIdentityLink(taskId, userId, "delegate");


		/** 第二次委托 */
		// task.setDelegationState(DelegationState.PENDING);
		userId = "admin";
		taskService.delegateTask(taskId, userId);
		taskService.resolveTask(taskId);
		taskService.addUserIdentityLink(taskId, userId, "delegate");

		taskService.complete(taskId);
		/**
		 * 该对象无法查询出委托任务的参与人有那些...
		 * 解决无法查询出具体的参与人？
		 * 	已经解决
		 * 		解决方案：每次委托自己手动增加信息	
		 * 		taskService.addUserIdentityLink(taskId, userId, "delegate");

		 */

		List<HistoricIdentityLink> historicIdentityLinks = historyService.getHistoricIdentityLinksForTask(taskId);
		for (HistoricIdentityLink historicIdentityLink : historicIdentityLinks) {
			System.err.println(historicIdentityLink.getUserId() + " " + historicIdentityLink.getType());
		}
		System.out.println("===========邪恶的分割线================");
		List<HistoricIdentityLink>  historicIdentityLinkLists = historyService.getHistoricIdentityLinksForProcessInstance(processInstance.getId());
		for (HistoricIdentityLink historicIdentityLink : historicIdentityLinkLists) {
			System.err.println(historicIdentityLink.getUserId() + " " + historicIdentityLink.getType());
		}

	}

	private Task findPersonTask(ProcessInstance processInstance) {
		return taskService.createTaskQuery()//
				.processInstanceId(processInstance.getId())//
				.taskDefinitionKey("usertask1")//
				.taskAssignee("hruser")//
				.singleResult();
	}

	private ProcessDefinition findProcessDefinitionByDeploymentId() {
		return repositoryService.createProcessDefinitionQuery()//
				.processDefinitionKey("delegateTask")//
				.deploymentId(deployment.getId())//
				.singleResult();
	}

	@Before
	public void setUp() {
		// 部署
		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/delegate/delegateTask.bpmn") //
				.addClasspathResource("diagrams/delegate/delegateTask.png") //
				.deploy();
		// identityService.saveUser(identityService.newUser("hruser"));
		// identityService.saveUser(identityService.newUser("leaderuser"));
		// identityService.saveUser(identityService.newUser("admin"));
		//
		// identityService.saveGroup(identityService.newGroup("hr"));
		// identityService.saveGroup(identityService.newGroup("deptLeader"));
		// identityService.saveGroup(identityService.newGroup("admin"));
		// identityService.createMembership("hruser", "hr");
		// identityService.createMembership("leaderuser", "deptLeader");
		// identityService.createMembership("admin", "admin");
	}

	@After
	public void tearDown() {
		// identityService.deleteMembership("hruser", "hr");
		// identityService.deleteMembership("leaderuser", "deptLeader");
		// identityService.deleteMembership("admin", "admin");
		//
		// identityService.deleteUser("hruser");
		// identityService.deleteUser("leaderuser");
		// identityService.deleteUser("admin");
		//
		// identityService.deleteGroup("hr");
		// identityService.deleteGroup("deptLeader");
		// identityService.deleteGroup("admin");

		repositoryService.deleteDeployment(deployment.getId(), true);
	}
}
