
package com.mossle.bpm.listener;

import org.activiti.engine.delegate.*;

public class DeleteCandidateTaskListener implements TaskListener {
	public void notify(DelegateTask delegateTask) {
		System.out.println(delegateTask);
		System.out.println(delegateTask.getCandidates());
		delegateTask.deleteCandidateUser("admin");
		System.out.println(delegateTask.getCandidates());
	}
}
