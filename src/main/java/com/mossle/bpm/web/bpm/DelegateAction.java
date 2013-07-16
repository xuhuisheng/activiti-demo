package com.mossle.bpm.web.bpm;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mossle.bpm.service.DelegateService;
import com.mossle.core.struts2.BaseAction;
import com.mossle.core.util.SpringSecurityUtils;

/**
 * 自动委托
 * 
 * @author LuZhao
 * 
 */
@Results({ @Result(name = DelegateAction.RELOAD, location = "delegate!listMyDelegateInfos.do?operationMode=RETRIEVE", type = "redirect") })
public class DelegateAction extends BaseAction {
	public static final String RELOAD = "reload";
	private ProcessEngine processEngine;
	private JdbcTemplate jdbcTemplate;
	private DelegateService delegateService;
	private String attorney;
	private Long id;
	private List<Map<String, Object>> delegateInfos;
	private List<Map<String, Object>> delegateHistories;

	/**
	 * 自动委托列表 
	 * TODO 可以指定多个自动委托人？
	 * 
	 * @return
	 */
	public String listMyDelegateInfos() {
		delegateInfos = jdbcTemplate.queryForList("select * from bpm_delegate_info where assignee=?", SpringSecurityUtils.getCurrentUsername());
		return "listMyDelegateInfos";
	}

	/**
	 * 删除自动委托
	 * 
	 * @return
	 */
	public String removeDelegateInfo() {
		jdbcTemplate.update("delete from bpm_delegate_info where id=?", id);
		return RELOAD;
	}

	// ~ ======================================================================
	/**
	 * 自动委托页面
	 * 
	 * @return
	 */
	public String prepareAutoDelegate() {
		return "prepareAutoDelegate";
	}

	/**
	 * 自动委托
	 * 
	 * @return
	 */
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
		delegateInfos = jdbcTemplate.queryForList("select * from bpm_delegate_info");

		return "listDelegateInfos";
	}
	
	/**
	 * 自动委托历史
	 * 
	 * @return
	 */
	public String listDelegateHistories() {
		delegateHistories = jdbcTemplate.queryForList("select * from bpm_delegate_history");

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
