package com.mossle.bpm.converter.extend;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.activiti.bpmn.converter.StartEventXMLConverter;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BaseElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mossle.bpm.converter.extend.node.MyStartEvent;

public class ExtendsStartEventXMLConverter extends StartEventXMLConverter {
	private Logger log = LoggerFactory.getLogger(ExtendsStartEventXMLConverter.class);
	
	public static Class<? extends BaseElement> getBpmnElementType() {
	    return MyStartEvent.class;
	  }
	
	@Override
	protected BaseElement convertXMLToElement(XMLStreamReader xtr) throws Exception {
		 String formKey = xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_FORM_FORMKEY);
		    MyStartEvent startEvent = null;
//		    if (StringUtils.isNotEmpty(formKey)) {
//		      if (formTypes.contains(formKey)) {
//		        startEvent = new AlfrescoStartEvent();
//		      }
//		    }
		    if (startEvent == null) {
		      startEvent = new MyStartEvent();
		    }
		    BpmnXMLUtil.addXMLLocation(startEvent, xtr);
		    startEvent.setInitiator(xtr.getAttributeValue(ACTIVITI_EXTENSIONS_NAMESPACE, ATTRIBUTE_EVENT_START_INITIATOR));
		    startEvent.setFormKey(formKey);
		    startEvent.setAttribute1(xtr.getAttributeValue(null, "attribute1"));
		    parseChildElements(getXMLElementName(), startEvent, xtr);
		    
		    return startEvent;
	}
	@Override
	protected void writeAdditionalAttributes(BaseElement element, XMLStreamWriter xtw) throws Exception {
		// TODO Auto-generated method stub
		super.writeAdditionalAttributes(element, xtw);
		xtw.writeAttribute("attribute1", ((MyStartEvent)element).getAttribute1());
	}
}
