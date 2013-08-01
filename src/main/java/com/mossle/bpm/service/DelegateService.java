package com.mossle.bpm.service;

import java.util.Date;
import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DelegateService {
    private JdbcTemplate jdbcTemplate;

	public void addDelegateInfo(String assignee, String attorney, Date startTime, Date endTime, String processDefinitionId) {
		String sql = "insert into bpm_delegate_info(assignee,attorney,start_time,end_time,process_definition_id,status) values(?,?,?,?,?,?)";
        jdbcTemplate.update(sql, assignee, attorney, startTime, endTime, processDefinitionId, 1);
	}

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
