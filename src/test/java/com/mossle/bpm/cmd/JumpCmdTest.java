package com.mossle.bpm.cmd;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class JumpCmdTest {
	@Resource
	private RepositoryService repositoryService;
	@Resource
	private RuntimeService runtimeService;
	@Resource
	private TaskService taskService;
	@Resource
	private IdentityService identityService;
	private Deployment deployment;

	@Test
	public void testJump() throws Exception {
		// 发起任务
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallel");
		assertNotNull(processInstance);

		// 进入第一个任务节点，这时应该只有一个任务
		String userId = "admin";
		Task task = taskService.createTaskQuery()
				.processInstanceId(processInstance.getId())
				.taskAssignee(userId)
				.singleResult();

		// 完成任务后，进入parallel并发节点，而后流程分成两条分支，分别创建两个任务
		taskService.complete(task.getId());
		assertEquals(2, taskService.createTaskQuery().list().size());

		// 获得第一条分支任务，把这个任务对应的分支强制跳转到第一个节点，也就是parallel之前的节点
		Task firstTask = taskService.createTaskQuery().list().get(0);
		JumpCmd jumpCmd = new JumpCmd(firstTask.getExecutionId(), "taskuser-1");
		((ServiceImpl) repositoryService).getCommandExecutor().execute(jumpCmd);

		// 跳转完，删除当前任务，再创建一个任务，总共是两个任务
		assertEquals(2, taskService.createTaskQuery().list().size());

		// 如果是跳转到parallel节点，又会再创建两个分支，总共就是三个任务了
		jumpCmd = new JumpCmd(firstTask.getExecutionId(), "parallel-1");
		((ServiceImpl) repositoryService).getCommandExecutor().execute(jumpCmd);
		assertEquals(3, taskService.createTaskQuery().list().size());

		for (Task childTask : taskService.createTaskQuery().list()) {
			taskService.complete(childTask.getId());
		}
	}

	@Before
	public void setUp() {
		// 部署
		deployment = repositoryService.createDeployment()
				.addClasspathResource("diagrams/parallel/parallel.bpmn")
				.deploy();
	}

	@After
	public void tearDown() {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}
}
