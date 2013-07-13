package com.mossle.bpm.delegate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegateBPMNParserHandler implements BpmnParseHandler {
    private static Logger logger = LoggerFactory
            .getLogger(DelegateBPMNParserHandler.class);

    public Collection<Class<? extends BaseElement>> getHandledTypes() {
        Set<Class<? extends BaseElement>> types = new HashSet<Class<? extends BaseElement>>();
        types.add(UserTask.class);

        return types;
    }

    public void parse(BpmnParse bpmnParse, BaseElement element) {
        logger.info("bpmnParse : {}, element : {}", bpmnParse, element);

        ActivityImpl activity = bpmnParse.getCurrentScope().findActivity(
                element.getId());
        logger.info("activity : {}", activity);

		ActivityBehavior activityBehavior = activity
                .getActivityBehavior();
		if (activityBehavior instanceof UserTaskActivityBehavior) {
			TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activityBehavior).getTaskDefinition();
			taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT,
					new DelegateTaskListener());
		}
    }
}
