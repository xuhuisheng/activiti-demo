package com.mossle.bpm.converter;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.cfg.BpmnParseFactory;

/** 
 * @author 
 * bpmn解析器工厂类，替换默认的BpmnParse
 */
public class MyBpmnParseFactory implements BpmnParseFactory {

	@Override
	public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
		 return new MyBpmnParse(bpmnParser);
	}

}
