package com.mossle.bpm.handler;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.activiti.engine.parse.BpmnParseHandler;

public class ProxyUserTaskBPMNParserHandler extends UserTaskParseHandler {
	private static Logger logger = LoggerFactory.getLogger(ProxyUserTaskBPMNParserHandler.class);
	private List<BpmnParseHandler> bpmnParseHandlers = new ArrayList<BpmnParseHandler>();

	@Override
	public void parse(BpmnParse bpmnParse, BaseElement element) {
		logger.info("bpmnParseHandlers : {}", bpmnParseHandlers);
		for (BpmnParseHandler bpmnTaskParseHandler : bpmnParseHandlers) {
			logger.info("bpmnTaskParseHandler : {}", bpmnTaskParseHandler);
			bpmnTaskParseHandler.parse(bpmnParse, element);
		}
	}

	public void setBpmnParseHandlers(List<BpmnParseHandler> bpmnParseHandlers) {
		this.bpmnParseHandlers = bpmnParseHandlers;
	}

}
