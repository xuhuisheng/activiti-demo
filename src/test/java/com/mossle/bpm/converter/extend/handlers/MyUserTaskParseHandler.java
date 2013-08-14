package com.mossle.bpm.converter.extend.handlers;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;

import com.mossle.bpm.converter.extend.node.MyUserTask;

/** 
 * @author  
 */
public class MyUserTaskParseHandler extends UserTaskParseHandler {
	
	@Override
	public Class<? extends BaseElement> getHandledType() {
		return MyUserTask.class;
	}
	
	
	@Override
	public ActivityImpl createActivityOnScope(BpmnParse bpmnParse, FlowElement flowElement, String xmlLocalName, ScopeImpl scopeElement) {
		ActivityImpl activityImpl =  super.createActivityOnScope(bpmnParse, flowElement, xmlLocalName, scopeElement);
		activityImpl.setProperty("xuhuisheng", ((MyUserTask)flowElement).getXuhuisheng());
		activityImpl.setProperty("tests", ((MyUserTask)flowElement).getTests());
		return activityImpl;
	}
}
