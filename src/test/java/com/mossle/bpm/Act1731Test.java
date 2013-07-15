package com.mossle.bpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import org.junit.After;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.FlowElement;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class Act1731Test {
    @Resource
    private RepositoryService repositoryService;
	@Resource
	private RuntimeService runtimeService;
	@Resource
	private TaskService taskService;
	@Resource
	private HistoryService historyService;

	private Deployment deployment;

    /**
     * The commit https://github.com/Activiti/Activiti/commit/136859b6e4100d80c34bfc5c43964c8c8e4362de
     * has fixed but only for runtime task...
     * @throws Exception
     */
    @Test
    public void testAct1731() throws Exception {
        Map<String, Object> vars = new HashMap<String, Object>();
        List<Date> objs = new ArrayList<Date>();
        objs.add(new Date());
        vars.put("list", objs);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        // fail
        ProcessInstance processInstanceNew = runtimeService.createProcessInstanceQuery().includeProcessVariables()
                .processInstanceId(processInstance.getProcessInstanceId()).singleResult();
        assertNotNull(processInstanceNew.getProcessVariables());

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId())
                .includeProcessVariables().singleResult();
        // fail on 5.13, ok on 5.14-SNAPSHOT
        assertNotNull(task.getProcessVariables());

        // it's ok
        taskService.setVariableLocal(task.getId(), "localVar", Arrays.asList(new Date(), new Date()));
        task = taskService.createTaskQuery().includeProcessVariables().includeTaskLocalVariables().singleResult();
        assertNotNull(task.getProcessVariables());
        assertNotNull(task.getTaskLocalVariables());

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables().singleResult();
        assertNotNull(historicProcessInstance);
        // fail
        assertNotNull(historicProcessInstance.getProcessVariables());
    }

	@Before
	public void setUp() {
		// 部署
		deployment = repositoryService.createDeployment()
				.addClasspathResource("diagrams/act1731/oneProcess.bpmn20.xml")
				.deploy();
	}

	@After
	public void tearDown() {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}

}
