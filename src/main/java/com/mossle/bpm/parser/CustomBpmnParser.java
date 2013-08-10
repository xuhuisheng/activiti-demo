package com.mossle.bpm.parser;

import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;

public class CustomBpmnParser extends BpmnParser{

	public void setActivityBehaviorFactory(ActivityBehaviorFactory activityBehaviorFactory) {
		
		((DefaultActivityBehaviorFactory)activityBehaviorFactory).setExpressionManager(expressionManager);
		super.setActivityBehaviorFactory(activityBehaviorFactory);
	}
	
}
