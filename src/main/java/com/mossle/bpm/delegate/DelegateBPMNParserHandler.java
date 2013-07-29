package com.mossle.bpm.delegate;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegateBPMNParserHandler extends UserTaskParseHandler {
    private static Logger logger = LoggerFactory
            .getLogger(DelegateBPMNParserHandler.class);

    
    protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
    	 logger.info("bpmnParse : {}, userTask : {}", bpmnParse, userTask);
		super.executeParse(bpmnParse, userTask);
		TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity().getProperty(PROPERTY_TASK_DEFINITION);
		ActivitiListener activitiListener = new ActivitiListener();
		activitiListener.setEvent(TaskListener.EVENTNAME_ASSIGNMENT);
		activitiListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
		activitiListener.setImplementation("#{delegateTaskListener}");
		taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, bpmnParse.getListenerFactory().createDelegateExpressionTaskListener(activitiListener));
	}
    
}
