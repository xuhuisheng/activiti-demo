package com.mossle.bpm.multiSubProcess.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class MultiInstanceExecutionListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		if(execution.getEventName()!=null&&execution.getEventName().equals(ExecutionListener.EVENTNAME_START)){
			System.out.println("并发子流程开始啦!");
		}else if(execution.getEventName()!=null&&execution.getEventName().equals(ExecutionListener.EVENTNAME_END)){
			System.out.println("并发子流程结束啦!");
		}
	}

}
