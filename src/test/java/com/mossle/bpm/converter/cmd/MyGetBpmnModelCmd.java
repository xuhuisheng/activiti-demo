package com.mossle.bpm.converter.cmd;

import java.io.InputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.util.io.BytesStreamSource;
import org.activiti.engine.repository.Deployment;

import com.mossle.bpm.converter.MyBpmnXMLConverter;

public class MyGetBpmnModelCmd extends GetBpmnModelCmd{

	public MyGetBpmnModelCmd(String processDefinitionId) {
		super(processDefinitionId);
	}

	public BpmnModel execute(CommandContext commandContext) {
	    if (processDefinitionId == null) {
	      throw new ActivitiIllegalArgumentException("processDefinitionId is null");
	    }

	    // Find the bpmn 2.0 xml resource name which is stored on the process definition
	    ProcessDefinitionEntity processDefinitionEntity = commandContext
	            .getProcessDefinitionEntityManager()
	            .findProcessDefinitionById(processDefinitionId);
	    
	    if (processDefinitionEntity == null) {
	      throw new ActivitiObjectNotFoundException("Process definition does not exist: " + processDefinitionId, ProcessDefinitionEntity.class);
	    }

	    // Fetch the resource
	    String resourceName = processDefinitionEntity.getResourceName();
	    ResourceEntity resource = commandContext.getResourceEntityManager()
	            .findResourceByDeploymentIdAndResourceName(processDefinitionEntity.getDeploymentId(), resourceName);
	    if (resource == null) {
	      if (commandContext.getDeploymentEntityManager().findDeploymentById(processDefinitionEntity.getDeploymentId()) == null) {
	        throw new ActivitiObjectNotFoundException("deployment for process definition does not exist: " 
	      + processDefinitionEntity.getDeploymentId(), Deployment.class);
	      } else {
	        throw new ActivitiObjectNotFoundException("no resource found with name '" + resourceName 
	                + "' in deployment '" + processDefinitionEntity.getDeploymentId() + "'", InputStream.class);
	      }
	    }
	    
	    // Convert the bpmn 2.0 xml to a bpmn model
	    //更换xml解析器
	    MyBpmnXMLConverter bpmnXMLConverter = new MyBpmnXMLConverter();
	    return bpmnXMLConverter.convertToBpmnModel(new BytesStreamSource(resource.getBytes()), false, false); // no need to validate schema, it was already validated on deploy
	  }
}
