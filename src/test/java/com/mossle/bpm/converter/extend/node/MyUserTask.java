package com.mossle.bpm.converter.extend.node;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.UserTask;

public class MyUserTask extends UserTask {
	private String xuhuisheng;
	
	private List<String> tests = new ArrayList<String>();



	public List<String> getTests() {
		return tests;
	}

	public void setTests(List<String> tests) {
		this.tests = tests;
	}

	public String getXuhuisheng() {
		return xuhuisheng;
	}

	public void setXuhuisheng(String xuhuisheng) {
		this.xuhuisheng = xuhuisheng;
	}
	
}
