package com.mossle.bpm.converter;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ActivitiRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.mossle.bpm.converter.cmd.MyGetBpmnModelCmd;
import com.mossle.bpm.converter.extend.node.MyUserTask;

public class MyTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule("diagrams/converter/activiti-cfg.xml");

	private Deployment deployment;
	
	private RepositoryService repositoryService;
	

	@Before
	public void setUp() {
		repositoryService = activitiRule.getRepositoryService();
		// 部署
		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/converter/multi-instance.bpmn20.xml") //
				.deploy();
		//TODO 通过部署管理器获取返回的ProcessDefinitionEntity 测试新增属性
		List<ProcessDefinitionEntity> pros = ((DeploymentEntity)deployment).getDeployedArtifacts(ProcessDefinitionEntity.class);
		assertNotNull(pros);
		for (ProcessDefinitionEntity processDefinitionEntity : pros) {
			ActivityImpl activity = processDefinitionEntity.findActivity("startnone-1");
			assertNotNull(activity);
			System.out.println(activity.getProperty("attribute1"));
			System.out.println(processDefinitionEntity.findActivity("taskuser-1").getProperty("xuhuisheng"));
			System.out.println(processDefinitionEntity.findActivity("taskuser-1").getProperty("tests"));
		}
		
	}
	
	
	@Test
	public void testMultiInstance() throws Exception {
		
		//TODO 通过缓存获取流程定义实体ProcessDefinitionEntity 测试新增属性
		ProcessDefinitionEntity processDefinition = findProcessDefinitionByDeploymentId();// 查询最新的流程定义
		assertNotNull(processDefinition);
		System.out.println(processDefinition.findActivity("startnone-1"));

		String myAttribute = (String) processDefinition.findActivity("startnone-1").getProperty("attribute1");
		String xuhuisheng = (String) processDefinition.findActivity("taskuser-1").getProperty("xuhuisheng");
		List<String> tests = (List<String>) processDefinition.findActivity("taskuser-1").getProperty("tests");
		assertNotNull(myAttribute);
		assertNotNull(xuhuisheng);
		assertNotNull(tests);
		
		//通过 repositoryService.getBpmnModel(processDefinitionId) 获取的是默认的无扩展的bpmnModel
		//TODO 以下是通过cmd获取自定义扩展的bpmnModel
		BpmnModel bpmnModel = ((RepositoryServiceImpl)repositoryService).getCommandExecutor()
				.execute(new MyGetBpmnModelCmd(processDefinition.getId()));
		
		System.out.println("bpmnModel:"+((MyUserTask)bpmnModel.getFlowElement("taskuser-1")).getXuhuisheng());
		System.out.println("bpmnModel:"+((MyUserTask)bpmnModel.getFlowElement("taskuser-1")).getTests());
	}


	private ProcessDefinitionEntity findProcessDefinitionByDeploymentId() {
		ProcessDefinition pdf = repositoryService.createProcessDefinitionQuery()//
				.processDefinitionKey("converter-multi-instance")//
				.deploymentId(deployment.getId())//
				.singleResult();
		return (ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService).getProcessDefinition(pdf.getId());
	}


	@After
	public void tearDown() {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}
	
	
}
