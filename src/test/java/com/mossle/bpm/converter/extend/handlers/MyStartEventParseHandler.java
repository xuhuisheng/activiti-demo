package com.mossle.bpm.converter.extend.handlers;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.StartEventParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;

import com.mossle.bpm.converter.extend.node.MyStartEvent;

public class MyStartEventParseHandler extends StartEventParseHandler {
	@Override
	public Class<? extends BaseElement> getHandledType() {
		return MyStartEvent.class;
	}
	@Override
	public ActivityImpl createActivityOnScope(BpmnParse bpmnParse, FlowElement flowElement, String xmlLocalName, ScopeImpl scopeElement) {
		ActivityImpl activityImpl = super.createActivityOnScope(bpmnParse, flowElement, xmlLocalName, scopeElement);
		activityImpl.setProperty("attribute1", ((MyStartEvent)flowElement).getAttribute1());
		return activityImpl;
	}
}
