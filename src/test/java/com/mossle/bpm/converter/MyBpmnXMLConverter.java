package com.mossle.bpm.converter;

import org.activiti.bpmn.converter.BaseBpmnXMLConverter;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BaseElement;

import com.mossle.bpm.converter.extend.ExtendsStartEventXMLConverter;
import com.mossle.bpm.converter.extend.ExtendsUserTaskXMLConverter;

/**
 * @author
 * 替换原始的流程解析器
 * 通过 addConverter 增加自定义扩展的节点解析器， 相关的扩展模块，参看： com.mossle.bpm.converter.extend 下结构
 */
public class MyBpmnXMLConverter extends BpmnXMLConverter {
	static{
		addConverter(ExtendsStartEventXMLConverter.getXMLType(), ExtendsStartEventXMLConverter.getBpmnElementType(), ExtendsStartEventXMLConverter.class);
		addConverter(ExtendsUserTaskXMLConverter.getXMLType(), ExtendsUserTaskXMLConverter.getBpmnElementType(), ExtendsUserTaskXMLConverter.class);
		//...
	}
	
	public static void addConverter(String elementName, Class<? extends BaseElement> elementClass, Class<? extends BaseBpmnXMLConverter> converter) {
		convertersToBpmnMap.put(elementName, converter);
		convertersToXMLMap.put(elementClass, converter);
	}
	
}
