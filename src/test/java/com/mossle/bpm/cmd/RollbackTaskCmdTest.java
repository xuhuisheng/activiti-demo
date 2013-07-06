package com.mossle.bpm.cmd;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import org.junit.After;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class RollbackTaskCmdTest {
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private IdentityService identityService;
    private Deployment deployment;
    private Map<String, Object> variables;

    @Test
    public void testRollBack() throws Exception {
        // 启动
        start();

        ProcessInstance processInstance = runtimeService
                .createProcessInstanceQuery().processInstanceBusinessKey(
                        "businessKey").singleResult();
        assertNotNull(processInstance);

        // assertEquals(leave.getProcessInstanceId(), processInstance.getId());

        // 完成任务
        String userId = "leaderuser";
        Task task = taskService.createTaskQuery().processInstanceId(
                processInstance.getId()).taskAssignee(userId).singleResult();
        variables = new HashMap<String, Object>();
        variables.put("deptLeaderPass", true); // 是否通过
        taskService.complete(task.getId(), variables);

        userId = "hruser";
        task = taskService.createTaskQuery().processInstanceId(
                processInstance.getId()).taskAssignee(userId).singleResult();

        Command<Integer> cmd = new RollbackTaskCmd(task.getId());
        ((ServiceImpl) repositoryService).getCommandExecutor().execute(cmd);

        userId = "leaderuser";
        task = taskService.createTaskQuery().taskAssignee(userId)
                .processInstanceId(processInstance.getId()).singleResult();
        assertNotNull(task);
    }

    private void start() {
        Map leave = new HashMap();
        leave.put("applyTime", new Date());
        leave.put("startTime", new Date());
        leave.put("endTime", new Date());
        leave.put("leaveType", "公休");
        leave.put("userId", "newtec");
        leave.put("reason", "测试数据....");
        variables = new HashMap<String, Object>();

        String businessKey = "businessKey";

        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        identityService.setAuthenticatedUserId((String) leave.get("userId"));

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("leave", businessKey, variables);
        String processInstanceId = processInstance.getId();
        leave.put("processInstanceId", processInstanceId);
    }

    @Before
    public void setUp() {
        //部署
        deployment = repositoryService.createDeployment() //
                .addClasspathResource("diagrams/leave/leave.bpmn") //
                .addClasspathResource("diagrams/leave/leave.png") //
                .deploy();

        //identityService.saveUser(identityService.newUser("leaderuser"));
        //identityService.saveGroup(identityService.newGroup("deptLeader"));
        //identityService.createMembership("leaderuser", "deptLeader");
    }

    @After
    public void tearDown() {
        //identityService.deleteMembership("leaderuser", "deptLeader");
        //identityService.deleteUser("leaderuser");
        //identityService.deleteGroup("deptLeader");
        repositoryService.deleteDeployment(deployment.getId(), true);
    }
}
