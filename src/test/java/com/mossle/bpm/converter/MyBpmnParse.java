package com.mossle.bpm.converter;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.parse.Problem;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.context.Context;

/**
 * @author 
 * 
 * 自定义解析器，由 @link BpmnParser (bpmn2.0建模分析器) 生成
 */
public class MyBpmnParse extends BpmnParse {

	public MyBpmnParse(BpmnParser parser) {
		super(parser);
	}

	public BpmnParse execute() {

		try {
			//自定义解析器
			MyBpmnXMLConverter converter = new MyBpmnXMLConverter();

			boolean enableSafeBpmnXml = false;
			String encoding = null;
			if (Context.getProcessEngineConfiguration() != null) {
				enableSafeBpmnXml = Context.getProcessEngineConfiguration().isEnableSafeBpmnXml();
				encoding = Context.getProcessEngineConfiguration().getXmlEncoding();
			}

			if (encoding != null) {
				bpmnModel = converter.convertToBpmnModel(streamSource, false, enableSafeBpmnXml, encoding);
			} else {
				bpmnModel = converter.convertToBpmnModel(streamSource, false, enableSafeBpmnXml);
			}

			createImports();
			createItemDefinitions();
			createMessages();
			createOperations();
			transformProcessDefinitions();
		} catch (Exception e) {
			if (e instanceof ActivitiException) {
				throw (ActivitiException) e;
			} else {
				throw new ActivitiException("Error parsing XML", e);
			}
		}

		if (bpmnModel.getProblems().size() > 0) {
			StringBuilder problemBuilder = new StringBuilder();
			for (Problem error : bpmnModel.getProblems()) {
				problemBuilder.append(error.toString());
				problemBuilder.append("\n");
			}
			throw new ActivitiException("Errors while parsing:\n" + problemBuilder.toString());
		}

		return this;
	}
}
