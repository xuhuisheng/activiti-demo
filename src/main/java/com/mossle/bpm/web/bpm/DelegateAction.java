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
import com.mossle.bpm.service.DelegateService;
import com.mossle.core.util.SpringSecurityUtils;

@Results({
	@Result(name = DelegateAction.RELOAD, location = "delegate!listMyDelegateInfos.do?operationMode=RETRIEVE", type = "redirect")
})
public class DelegateAction extends BaseAction {
    public static final String RELOAD = "reload";
    private ProcessEngine processEngine;
    private JdbcTemplate jdbcTemplate;
    private DelegateService delegateService;
    private String attorney;
	private Long id;
    private List<Map<String, Object>> delegateInfos;
    private List<Map<String, Object>> delegateHistories;

	public String listMyDelegateInfos() {
        delegateInfos = jdbcTemplate
                .queryForList("select * from bpm_delegate_info where assignee=?", SpringSecurityUtils.getCurrentUsername());
		return "listMyDelegateInfos";
	}

	public String removeDelegateInfo() {
		jdbcTemplate.update("delete from bpm_delegate_info where id=?", id);
		return RELOAD;
	}

    // ~ ======================================================================
    public String prepareAutoDelegate() {
        return "prepareAutoDelegate";
    }

    public String autoDelegate() {
        String username = SpringSecurityUtils.getCurrentUsername();
        delegateService.addDelegateInfo(username, attorney);

        return RELOAD;
    }

    // ~ ======================================================================
    /**
     * 自动委派
     */
    public String listDelegateInfos() {
        delegateInfos = jdbcTemplate
                .queryForList("select * from bpm_delegate_info");

        return "listDelegateInfos";
    }

    public String listDelegateHistories() {
        delegateHistories = jdbcTemplate
                .queryForList("select * from bpm_delegate_history");

        return "listDelegateHistories";
    }

	// ~ ==================================================
    public void setDelegateService(DelegateService delegateService) {
        this.delegateService = delegateService;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setAttorney(String attorney) {
        this.attorney = attorney;
    }

	public void setId(Long id) {
		this.id = id;
	}

    public List<Map<String, Object>> getDelegateInfos() {
        return delegateInfos;
    }

    public List<Map<String, Object>> getDelegateHistories() {
        return delegateHistories;
    }
}
