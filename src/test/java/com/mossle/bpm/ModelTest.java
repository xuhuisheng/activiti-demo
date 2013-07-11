
package com.mossle.bpm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
public class ModelTest {
    @Resource
    private RepositoryService repositoryService;
    @Test
	public void testDefault() {
		List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
		System.out.println("processDefinitions : " + processDefinitions);
		for (ProcessDefinition processDefinition : processDefinitions) {
			System.out.println("processDefinition : " + processDefinition);
			BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
			System.out.println("bpmnModel : " + bpmnModel);
			for (Process process : bpmnModel.getProcesses()) {
				System.out.println("process : " + process);
				for (FlowElement flowElement : process.getFlowElements()) {
					System.out.println("flowElement : " + flowElement);
				}
			}
		}
	}
}
