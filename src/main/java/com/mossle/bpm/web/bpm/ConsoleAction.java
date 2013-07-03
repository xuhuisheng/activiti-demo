package com.mossle.bpm.web.bpm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.mossle.bpm.cmd.JumpCmd;
import com.mossle.bpm.cmd.ListActivityCmd;
import com.mossle.bpm.cmd.ProcessDefinitionDiagramCmd;

import com.mossle.core.struts2.BaseAction;
import com.mossle.core.util.IoUtils;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import org.springframework.jdbc.core.JdbcTemplate;

@Results( { @Result(name = ConsoleAction.RELOAD, location = "console!listProcessDefinitions.do?operationMode=RETRIEVE", type = "redirect") })
public class ConsoleAction extends BaseAction {
    public static final String RELOAD = "reload";
    private ProcessEngine processEngine;
    private List<ProcessDefinition> processDefinitions;
    private String processDefinitionId;
    private String xml;
    private List<HistoricProcessInstance> historicProcessInstances;
    private List<HistoricTaskInstance> historicTasks;
    private List<Task> tasks;
    private JdbcTemplate jdbcTemplate;
    private List<Map<String, Object>> delegateInfos;
    private List<Map<String, Object>> delegateHistories;
    private String executionId;
    private String activityId;
    private CommandExecutor commandExecutor;
    private Map<String, String> activityMap;

    /**
     * 新建流程.
     */
    public String create() {
        return "create";
    }

    public String deploy() throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        ByteArrayInputStream bais = new ByteArrayInputStream(xml
                .getBytes("UTF-8"));
        repositoryService.createDeployment().addInputStream(
                "process.bpmn20.xml", bais).deploy();

        return RELOAD;
    }

    /**
     * 流程定义.
     */
    public String listProcessDefinitions() {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        processDefinitions = repositoryService.createProcessDefinitionQuery()
                .list();

        return "listProcessDefinitions";
    }

    public String removeProcessDefinition() {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        ProcessDefinition processDefinition = repositoryService
                .getProcessDefinition(processDefinitionId);

        repositoryService.deleteDeployment(processDefinition.getDeploymentId(),
                true);

        return RELOAD;
    }

    public String suspendProcessDefinition() {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        repositoryService.suspendProcessDefinitionById(processDefinitionId);

        return RELOAD;
    }

    public String activeProcessDefinition() {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();

        repositoryService.activateProcessDefinitionById(processDefinitionId);

        return RELOAD;
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

    public void viewXml() throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery().processDefinitionId(
                        processDefinitionId).singleResult();
        String resourceName = processDefinition.getResourceName();
        InputStream resourceAsStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(), resourceName);
        ServletActionContext.getResponse().setContentType("text/xml;charset=UTF-8");
        IoUtils.copyStream(resourceAsStream, ServletActionContext.getResponse()
                .getOutputStream());
    }

    /**
     * 流程实例.
     */
    public String listProcessInstances() {
        HistoryService historyService = processEngine.getHistoryService();

        historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery().list();

        return "listProcessInstances";
    }

    /**
     * 任务.
     */
    public String listTasks() {
        TaskService taskService = processEngine.getTaskService();
        tasks = taskService.createTaskQuery().list();

        return "listTasks";
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

    // ~ ======================================================================
    public String prepareJump() {
        Command<Map<String, String>> cmd = new ListActivityCmd(executionId);

        activityMap = commandExecutor.execute(cmd);

        return "prepareJump";
    }

    public String jump() {
        Command<Object> cmd = new JumpCmd(executionId, activityId);

        commandExecutor.execute(cmd);

        return RELOAD;
    }

    // ~ ======================================================================
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public List<ProcessDefinition> getProcessDefinitions() {
        return processDefinitions;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public List<HistoricProcessInstance> getHistoricProcessInstances() {
        return historicProcessInstances;
    }

    public List<HistoricTaskInstance> getHistoricTasks() {
        return historicTasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getDelegateInfos() {
        return delegateInfos;
    }

    public List<Map<String, Object>> getDelegateHistories() {
        return delegateHistories;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public Map<String, String> getActivityMap() {
        return activityMap;
    }
}
