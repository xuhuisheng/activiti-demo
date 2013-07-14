package com.mossle.bpm.web.bpm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mossle.core.struts2.BaseAction;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import org.springframework.jdbc.core.JdbcTemplate;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.Group;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.Job;

@Results({
	@Result(name = JobAction.RELOAD, location = "job!list.do?operationMode=RETRIEVE", type = "redirect")
})
public class JobAction extends BaseAction {
    public static final String RELOAD = "reload";
    private ProcessEngine processEngine;
	private List<Job> jobs;

	public String list() {
		jobs = processEngine.getManagementService().createJobQuery().list();
		return "list";
	}

	public String executeJob() {
		processEngine.getManagementService().executeJob(id);
		return RELOAD;
	}

	public String removeJob() {
		processEngine.getManagementService().deleteJob(id);
		return RELOAD;
	}

	// ~ ==================================================
	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	public List<Job> getJobs() {
		return jobs;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
