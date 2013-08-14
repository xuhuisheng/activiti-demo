package com.mossle.bpm.converter.extend.child;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.child.BaseChildElementParser;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;

import com.mossle.bpm.converter.extend.node.MyUserTask;

/** 
 * @author  izerui.com
 * @version createtime：2013年8月14日 下午3:51:23 
 */
public class TestElementParser extends BaseChildElementParser {

	@Override
	public String getElementName() {
		return  "testElements";
	}

	@Override
	public void parseChildElement(XMLStreamReader xtr, BaseElement parentElement, BpmnModel model) throws Exception {
		 if (parentElement instanceof MyUserTask == false) return;
		 while (xtr.hasNext()) {
			 xtr.next();
			 if (xtr.isStartElement() && "test".equalsIgnoreCase(xtr.getLocalName())) {
				 ((MyUserTask) parentElement).getTests().add(xtr.getElementText());
		      }
		 }
	}

}
