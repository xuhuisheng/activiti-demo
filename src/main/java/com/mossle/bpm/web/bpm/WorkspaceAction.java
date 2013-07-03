package com.mossle.bpm.web.bpm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.bpm.cmd.HistoryProcessInstanceDiagramCmd;
import com.mossle.bpm.cmd.ProcessDefinitionDiagramCmd;
import com.mossle.bpm.cmd.RollbackTaskCmd;
import com.mossle.bpm.cmd.TaskDiagramCmd;
import com.mossle.bpm.cmd.WithdrawTaskCmd;
import com.mossle.bpm.service.DelegateService;

import com.mossle.core.struts2.BaseAction;

import com.mossle.core.util.SpringSecurityUtils;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

/**
 * 我的流程
 * 待办流程
 * 已办未结
 */
@Results( { @Result(name = WorkspaceAction.RELOAD, location = "workspace!listProcessDefinitions.do?operationMode=RETRIEVE", type = "redirect") })
public class WorkspaceAction extends BaseAction {
    public static final String RELOAD = "reload";
    private ProcessEngine processEngine;
    private List<ProcessDefinition> processDefinitions;
    private String processDefinitionId;
    private StartFormData startFormData;
    private List<Task> tasks;
    private String taskId;
    private TaskFormData taskFormData;
    private String processInstanceId;
    private List<HistoricTaskInstance> historicTasks;
    private List<HistoricProcessInstance> historicProcessInstances;
    private List<HistoricVariableInstance> historicVariableInstances;
    private String username;
    private CommandExecutor commandExecutor;
    private String attorney;
    private DelegateService delegateService;

    public String listProcessDefinitions() {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        processDefinitions = repositoryService.createProcessDefinitionQuery()
                .list();

        return "listProcessDefinitions";
    }

    public String listRunningProcessInstances() {
        HistoryService historyService = processEngine.getHistoryService();

        // TODO: finished(), unfinished()
        String username = SpringSecurityUtils.getCurrentUsername();
        historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery().startedBy(username)
                .list();

        return "listRunningProcessInstances";
    }

    public String listInvolvedProcessInstances() {
        HistoryService historyService = processEngine.getHistoryService();

        // TODO: finished(), unfinished()
        String username = SpringSecurityUtils.getCurrentUsername();
        historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery().involvedUser(username)
                .list();

        return "listInvolvedProcessInstances";
    }

    public void graphProcessDefinition() throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Command<InputStream> cmd = null;
        cmd = new ProcessDefinitionDiagramCmd(processDefinitionId);

        InputStream is = ((ServiceImpl) repositoryService).getCommandExecutor()
                .execute(cmd);
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("image/png");

        int len = 0;
        byte[] b = new byte[1024];

        while ((len = is.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    public void graphTask() throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Command<InputStream> cmd = null;
        cmd = new TaskDiagramCmd(taskId);

        InputStream is = ((ServiceImpl) repositoryService).getCommandExecutor()
                .execute(cmd);
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("image/png");

        int len = 0;
        byte[] b = new byte[1024];

        while ((len = is.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    public void graphHistoryProcessInstance() throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Command<InputStream> cmd = null;
        cmd = new HistoryProcessInstanceDiagramCmd(processInstanceId);

        InputStream is = ((ServiceImpl) repositoryService).getCommandExecutor()
                .execute(cmd);
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("image/png");

        int len = 0;
        byte[] b = new byte[1024];

        while ((len = is.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    public String listPersonalTasks() {
        TaskService taskService = processEngine.getTaskService();
        String username = SpringSecurityUtils.getCurrentUsername();
        tasks = taskService.createTaskQuery().taskAssignee(username).list();

        return "listPersonalTasks";
    }

    public String listGroupTasks() {
        TaskService taskService = processEngine.getTaskService();
        String username = SpringSecurityUtils.getCurrentUsername();
        tasks = taskService.createTaskQuery().taskCandidateUser(username)
                .list();

        return "listGroupTasks";
    }

    public String listDelegatedTasks() {
        TaskService taskService = processEngine.getTaskService();
        String username = SpringSecurityUtils.getCurrentUsername();
        tasks = taskService.createTaskQuery().taskOwner(username)
                .taskDelegationState(DelegationState.PENDING).list();

        return "listDelegatedTasks";
    }

    public String listHistoryTasks() {
        HistoryService historyService = processEngine.getHistoryService();
        String username = SpringSecurityUtils.getCurrentUsername();
        historicTasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(username).finished().list();

        return "listHistoryTasks";
    }

    // ~ ======================================================================
    public String prepareStartProcessInstance() {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        FormService formService = processEngine.getFormService();
        startFormData = formService.getStartFormData(processDefinitionId);

        return "prepareStartProcessInstance";
    }

    public String startProcessInstance() {
        IdentityService identityService = processEngine.getIdentityService();
        identityService.setAuthenticatedUserId(SpringSecurityUtils
                .getCurrentUsername());

        FormService formService = processEngine.getFormService();
        StartFormData startFormData = formService
                .getStartFormData(processDefinitionId);
        HttpServletRequest request = ServletActionContext.getRequest();
        Map<String, String> map = new HashMap<String, String>();

        for (FormProperty formProperty : startFormData.getFormProperties()) {
            String name = formProperty.getId();
            map.put(name, request.getParameter(name));
        }

        formService.submitStartFormData(processDefinitionId, map);

        return RELOAD;
    }

    // ~ ======================================================================
    public String prepareCompleteTask() {
        TaskService taskService = processEngine.getTaskService();

        FormService formService = processEngine.getFormService();

        taskFormData = formService.getTaskFormData(taskId);

        return "prepareCompleteTask";
    }

    public String completeTask() {
        HttpServletRequest request = ServletActionContext.getRequest();
        IdentityService identityService = processEngine.getIdentityService();
        identityService.setAuthenticatedUserId(SpringSecurityUtils
                .getCurrentUsername());

        FormService formService = processEngine.getFormService();
        TaskFormData taskFormData = formService.getTaskFormData(taskId);

        Map<String, String> map = new HashMap<String, String>();

        for (FormProperty formProperty : taskFormData.getFormProperties()) {
            if (formProperty.isWritable()) {
                String name = formProperty.getId();
                map.put(name, request.getParameter(name));
            }
        }

        formService.submitTaskFormData(taskId, map);

        return RELOAD;
    }

    public String claimTask() {
        String username = SpringSecurityUtils.getCurrentUsername();

        TaskService taskService = processEngine.getTaskService();
        taskService.claim(taskId, username);

        return RELOAD;
    }

    public String prepareDelegateTask() {
        return "prepareDelegateTask";
    }

    public String delegateTask() {
        TaskService taskService = processEngine.getTaskService();
        taskService.delegateTask(taskId, username);

        return RELOAD;
    }

    public String resolveTask() {
        TaskService taskService = processEngine.getTaskService();
        taskService.resolveTask(taskId);

        return RELOAD;
    }

    public String viewHistory() {
        HistoryService historyService = processEngine.getHistoryService();
        historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).list();
        historicVariableInstances = historyService
                .createHistoricVariableInstanceQuery().processInstanceId(
                        processInstanceId).list();

        return "viewHistory";
    }

    // ~ ======================================================================
    public String rollback() {
        Command<Integer> cmd = new RollbackTaskCmd(taskId);

        commandExecutor.execute(cmd);

        return RELOAD;
    }

    public String withdraw() {
        Command<Integer> cmd = new WithdrawTaskCmd(taskId);

        commandExecutor.execute(cmd);

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
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public List<ProcessDefinition> getProcessDefinitions() {
        return processDefinitions;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public StartFormData getStartFormData() {
        return startFormData;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskFormData getTaskFormData() {
        return taskFormData;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public List<HistoricTaskInstance> getHistoricTasks() {
        return historicTasks;
    }

    public List<HistoricProcessInstance> getHistoricProcessInstances() {
        return historicProcessInstances;
    }

    public List<HistoricVariableInstance> getHistoricVariableInstances() {
        return historicVariableInstances;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public void setAttorney(String attorney) {
        this.attorney = attorney;
    }

    public void setDelegateService(DelegateService delegateService) {
        this.delegateService = delegateService;
    }
}
