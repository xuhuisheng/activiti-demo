package com.mossle.bpm.auto;

import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

import com.mossle.bpm.support.DefaultTaskListener;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.identity.Authentication;

@Component("autoCompleteTaskListener")
public class AutoCompleteTaskListener extends DefaultTaskListener {
    private static Logger logger = LoggerFactory
            .getLogger(AutoCompleteTaskListener.class);

	@Override
	public void onCreate(DelegateTask delegateTask) throws Exception {
		String username = Authentication.getAuthenticatedUserId();
		String assignee = delegateTask.getAssignee();
		if (username != null && username.equals(assignee)) {
			((TaskEntity) delegateTask).complete();
		}
	}
}
