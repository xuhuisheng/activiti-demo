package com.mossle.bpm.doc;

import java.util.List;

import javax.annotation.Resource;

import junit.framework.AssertionFailedError;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <pre>
 * 对财务报表进行测试
 * taskAssignee	:	
 * 		流程定义中，在Main config的设置人员信息时，如果是填写的Assignee那么就使用taskAssignee
 * taskCandidateUser、taskCandidateGroup	：	
 * 		流程定义中，在Main config的设置人员信息时，如果是填写taskCandidate Groups和taskCandidate Users，
 * 	那么就使用taskCandidateUser和taskCandidate Groups都能够查询到该节点任务（该节点是停留状态）。
 * @author LuZhao
 * 
 * TODO  对财务报表增加功能：
 * 		(1)、增加网关实现决策，经理审批就可以驳回
 * 		(2)、使用变量即表单
 * 		(3)、增加邮件发送功能（对每一位领导发送该报表）
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class FinancialReportProcessTest {

	@Resource
	protected RepositoryService repositoryService;
	@Resource
	protected RuntimeService runtimeService;
	@Resource
	protected TaskService taskService;
	@Resource
	protected HistoryService historyService;
	@Resource
	protected IdentityService identityService;
	protected Deployment deployment;
	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void testFinancialReportProcess() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("financialReport");
		assertNotNull(processInstance);

		// 制作月度财报 newtec start
		Task task = taskService.createTaskQuery()//
				.processInstanceId(processInstance.getId())//
				.taskCandidateUser("newtec")//
				.singleResult();

		Task task01 = taskService.createTaskQuery()//
				.processInstanceId(processInstance.getId())//
				.taskCandidateGroup("accountancy")//
				.singleResult();
		assertNotNull(task);
		assertNotNull(task01);

		logger.info("--------->" + task.toString());
		logger.info("--------->" + task01.toString());

		assertEquals("制作月度财报", task.getName().trim());
		taskService.claim(task.getId(), "newtec");
		taskService.complete(task.getId());
		task = taskService.createTaskQuery()//
				.processInstanceId(processInstance.getId())//
				.taskCandidateUser("newtec")//
				.singleResult();
		assertNull(task);

		// 制作月度财报 newtec end

		// 验证月度财报 zhao start
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskAssignee("zhao").singleResult();
		task01 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskCandidateUser("zhao").singleResult();
		Task task02 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskCandidateGroup("management").singleResult();

		assertNotNull(task);
		assertNull(task01);
		assertNull(task02);

		logger.info("--------->" + task.toString());

		assertEquals("验证月度财报", task.getName().trim());

		// 如果不拾取，那么historicIdentityLink表里面是不会有数据的
		taskService.claim(task.getId(), "zhao");
		taskService.complete(task.getId());
		// 验证月度财报zhao end

		// 使用Candidate Users zhangsan start
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskCandidateUser("zhangsan").singleResult();
		task01 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskCandidateGroup("test").singleResult();

		assertNotNull(task);
		assertNull(task01);

		assertEquals("使用Candidate Users", task.getName().trim());

		// 如果不拾取，那么historicIdentityLink表里面是不会有数据的
		taskService.claim(task.getId(), "zhangsan");
		taskService.complete(task.getId());
		// 使用Candidate Users zhangsan end8

		HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()//
				.processInstanceId(processInstance.getId())//
				.singleResult();

		// TODO 时间格式化工具类
		logger.info("流程实例结束时间 = {}", historicProcessInstance.getEndTime().toLocaleString());

		assertProcessEnded(processInstance.getId());

		List<HistoricIdentityLink> historicIdentityLinks = historyService.getHistoricIdentityLinksForProcessInstance(processInstance.getId());
		for (HistoricIdentityLink historicIdentityLink : historicIdentityLinks) {
			logger.info("任务ID:{},角色:{},用户:{},类型:{}", new Object[] { historicIdentityLink.getTaskId(), historicIdentityLink.getGroupId(), historicIdentityLink.getUserId(),
					historicIdentityLink.getType() });
		}
	}

	// 判断流程实例是否结束
	private void assertProcessEnded(final String processInstanceId) {
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()//
				.processInstanceId(processInstanceId)//
				.singleResult();
		if (processInstance != null) {
			throw new AssertionFailedError("期望完成的流程实例 '" + processInstanceId + "' 但是它依然存在数据库中。");
		}
		logger.info("--------->流程实例：{}	已经结束",processInstanceId);
	}

	@Before
	public void setUp() throws Exception {
		identityService.saveUser(identityService.newUser("newtec"));
		identityService.saveUser(identityService.newUser("zhao"));
		identityService.saveUser(identityService.newUser("zhangsan"));

		identityService.saveGroup(identityService.newGroup("accountancy"));
		identityService.saveGroup(identityService.newGroup("management"));
		identityService.saveGroup(identityService.newGroup("test"));

		identityService.createMembership("newtec", "accountancy");
		identityService.createMembership("zhao", "management");
		identityService.createMembership("zhangsan", "test");

		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/bpmn/FinancialReportProcess.bpmn") //
				.deploy();
	}

	@After
	public void tearDown() throws Exception {
		identityService.deleteUser("newtec");
		identityService.deleteUser("zhao");
		identityService.deleteUser("zhangsan");
		identityService.deleteGroup("accountancy");
		identityService.deleteGroup("management");
		identityService.deleteGroup("test");
		repositoryService.deleteDeployment(deployment.getId(), true);
	}
}
