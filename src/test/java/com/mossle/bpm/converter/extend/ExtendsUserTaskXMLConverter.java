package com.mossle.bpm.converter.extend;

import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.UserTaskXMLConverter;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.apache.commons.lang.StringUtils;

import com.mossle.bpm.converter.extend.child.TestElementParser;
import com.mossle.bpm.converter.extend.node.MyUserTask;

public class ExtendsUserTaskXMLConverter extends UserTaskXMLConverter {
	
	public ExtendsUserTaskXMLConverter() {
		
		TestElementParser parser = new TestElementParser();
		childElementParsers.put(parser.getElementName(), parser);
	}
	
	public static Class<? extends BaseElement> getBpmnElementType() {
	    return MyUserTask.class;
	  }
	
	@Override
	protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
		 String formKey = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_FORM_FORMKEY);
		    MyUserTask userTask = null;
//		    if (StringUtils.isNotEmpty(formKey)) {
//		      if (formTypes.contains(formKey)) {
//		        userTask = new AlfrescoUserTask();
//		      }
//		    }
		    if (userTask == null) {
		      userTask = new MyUserTask();
		    }
		    BpmnXMLUtil.addXMLLocation(userTask, xtr);
		    userTask.setDueDate(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_DUEDATE));
		    userTask.setFormKey(formKey);
		    userTask.setAssignee(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_ASSIGNEE)); 
		    userTask.setPriority(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_PRIORITY));
		    
		    userTask.setXuhuisheng(xtr.getAttributeValue(null, "xuhuisheng"));
		    
		    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEUSERS))) {
		      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEUSERS);
		      userTask.getCandidateUsers().addAll(parseDelimitedList(expression));
		    } 
		    
		    if (StringUtils.isNotEmpty(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEGROUPS))) {
		      String expression = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_TASK_USER_CANDIDATEGROUPS);
		      userTask.getCandidateGroups().addAll(parseDelimitedList(expression));
		    }
		    
		    parseChildElements(getXMLElementName(), userTask, xtr);
		    
		    return userTask;
	}
	
	@Override
	protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
		// TODO Auto-generated method stub
		super.writeAdditionalAttributes(element, xtw);
		xtw.writeAttribute("xuhuisheng", ((MyUserTask)element).getXuhuisheng());
	}
	
	
	@Override
	protected void writeExtensionChildElements(BaseElement element, XMLStreamWriter xtw) throws Exception {
		// TODO Auto-generated method stub
		super.writeExtensionChildElements(element, xtw);
		List<String> list = ((MyUserTask)element).getTests();
		xtw.writeStartElement("tests");
		for (String string : list) {
			xtw.writeStartElement("test");
            xtw.writeCharacters(string);
            xtw.writeEndElement();
		}
		xtw.writeEndElement();
	}
}
