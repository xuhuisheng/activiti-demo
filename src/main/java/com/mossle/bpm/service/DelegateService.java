package com.mossle.bpm.service;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DelegateService {
    private JdbcTemplate jdbcTemplate;

    public void addDelegateInfo(String assignee, String attorney) {
        jdbcTemplate.update(
                        "insert into bpm_delegate_info(assignee,attorney,status) values(?,?,?)",
                        assignee, attorney, 1);
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
