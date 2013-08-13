package com.mossle.bpm.converter.extend.node;

import org.activiti.bpmn.model.StartEvent;

public class MyStartEvent extends StartEvent {
	protected String attribute1;
	public String getAttribute1() {
		return attribute1;
	}
	public void setAttribute1(String attribute1) {
		this.attribute1 = attribute1;
	}
}
