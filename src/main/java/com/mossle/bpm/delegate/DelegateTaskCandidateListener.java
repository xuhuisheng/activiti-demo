package com.mossle.bpm.delegate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("delegateTaskCreateListener")
public class DelegateTaskCandidateListener implements TaskListener {
	private static Logger logger = LoggerFactory.getLogger(DelegateTaskCandidateListener.class);
	public static final String SQL_GET_DELEGATE_INFO = "select * from bpm_delegate_info where status=1";
	public static final String SQL_SET_DELEGATE_INFO = "insert into bpm_delegate_history" + "(assignee,attorney,delegate_time,task_id,status) values(?,?,now(),?,1)";

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private IdentityService identityService;

	public void notify(DelegateTask delegateTask) {
		try {
			if (TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
				List<Map<String, Object>> list = jdbcTemplate.queryForList(SQL_GET_DELEGATE_INFO);
				for (Map<String, Object> map : list) {
					String assignee = (String) map.get("assignee");
					String attorney = (String) map.get("attorney");
					String processDefinitionId = (String) map.get("process_definition_id");
					Date startTime = (Date) map.get("start_time");
					Date endTime = (Date) map.get("end_time");
					if (timeNotBetweenNow(startTime, endTime)) {// 是否活动状态
						continue;
					}
					if ((processDefinitionId == null) || processDefinitionId.equals(delegateTask.getProcessDefinitionId())) {
						Set<IdentityLink> ids = delegateTask.getCandidates();
						for (IdentityLink identityLink : ids) {
							if (identityLink.getUserId()!=null&&identityLink.getUserId().equals(assignee)) {// 包含被代理人
								addCandidateUser(delegateTask, assignee, attorney);
							}else if(identityLink.getGroupId()!=null&&identityService.createGroupQuery().groupMember(assignee).groupId(identityLink.getGroupId()).count()>0l){
								addCandidateUser(delegateTask, assignee, attorney);
							}
						}

					}
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	private void addCandidateUser(DelegateTask delegateTask,String assignee,String attorney){
		logger.info("自动委托任务,设置候选人: {} to {}", delegateTask, attorney);
		delegateTask.addCandidateUser(attorney);
		jdbcTemplate.update(SQL_SET_DELEGATE_INFO, assignee, attorney, delegateTask.getId());
	}

	private boolean timeNotBetweenNow(Date startTime, Date endTime) {
		if ((startTime == null) && (endTime == null)) {
			return false;
		}
		Date now = new Date();
		return now.before(startTime) || now.after(endTime);
	}
}
