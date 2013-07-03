package com.mossle.bpm.delegate;

import java.util.*;

import org.activiti.engine.*;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.task.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;

public class DelegateTaskListener implements TaskListener {
    private static Logger logger = LoggerFactory
            .getLogger(DelegateTaskListener.class);
    public static final String SQL_GET_DELEGATE_INFO = "select * from bpm_delegate_info where status=1";
    public static final String SQL_SET_DELEGATE_INFO = "insert into bpm_delegate_history"
            + "(assignee,attorney,delegate_time,task_id,status) values(?,?,now(),?,1)";

    public void notify(DelegateTask delegateTask) {
        try {
            String eventName = delegateTask.getEventName();
            logger.info("eventName : {}", eventName);

            if ("assignment".equals(eventName)) {
                JdbcTemplate jdbcTemplate = (JdbcTemplate) ApplicationContextHolder
                        .getBean("jdbcTemplate");
                List<Map<String, Object>> list = jdbcTemplate
                        .queryForList(SQL_GET_DELEGATE_INFO);

                for (Map<String, Object> map : list) {
                    logger.info("map : {}", map);

                    String assignee = (String) map.get("assignee");
                    String attorney = (String) map.get("attorney");
                    String processDefinitionId = (String) map
                            .get("process_definition_dd");
                    Date startTime = (Date) map.get("start_time");
                    Date endTime = (Date) map.get("end_time");

                    if (timeNotBetweenNow(startTime, endTime)) {
                        logger.info("timeNotBetweenNow");

                        continue;
                    }

                    if (!assignee.equals(delegateTask.getAssignee())) {
                        logger.info("assignee : " + assignee
                                + ", delegateTask.getAssignee() : "
                                + delegateTask.getAssignee());

                        continue;
                    }

                    if ((processDefinitionId == null)
                            || processDefinitionId.equals(delegateTask
                                    .getProcessDefinitionId())) {
                        logger
                                .info("delegate {} to {}", delegateTask,
                                        attorney);
                        delegateTask.setAssignee(attorney);
                        jdbcTemplate.update(SQL_SET_DELEGATE_INFO, assignee,
                                attorney, delegateTask.getId());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    private boolean timeNotBetweenNow(Date startTime, Date endTime) {
        if ((startTime == null) && (endTime == null)) {
            return false;
        }

        Date now = new Date();

        return now.before(startTime) || now.after(endTime);
    }
}
