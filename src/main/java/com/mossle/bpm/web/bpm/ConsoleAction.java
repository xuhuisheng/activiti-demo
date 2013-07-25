package com.mossle.bpm.web.bpm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mossle.bpm.cmd.JumpCmd;
import com.mossle.bpm.cmd.ListActivityCmd;
import com.mossle.bpm.cmd.ProcessDefinitionDiagramCmd;
import com.mossle.core.struts2.BaseAction;

@Results({ @Result(name = ConsoleAction.RELOAD_PROCESS_DEFINITION, location = "console!listProcessDefinitions.do?operationMode=RETRIEVE", type = "redirect"),
		@Result(name = ConsoleAction.RELOAD_PROCESS_INSTANCE, location = "console!listProcessInstances.do?operationMode=RETRIEVE", type = "redirect"),
		@Result(name = ConsoleAction.RELOAD_TASK, location = "console!listTasks.do?operationMode=RETRIEVE", type = "redirect") })
public class ConsoleAction extends BaseAction {
	public static final String RELOAD_PROCESS_DEFINITION = "reload-process-definition";
	public static final String RELOAD_PROCESS_INSTANCE = "reload-process-instance";
	public static final String RELOAD_TASK = "reload-task";
	private ProcessEngine processEngine;
	private List<ProcessDefinition> processDefinitions;
	private String processDefinitionId;
	private String xml;
	private List<HistoricProcessInstance> historicProcessInstances;
	private List<Task> tasks;
	private JdbcTemplate jdbcTemplate;
	private String executionId;
	private String activityId;
	private CommandExecutor commandExecutor;
	private Map<String, String> activityMap;
	private String processInstanceId;
	private String deleteReason;
	private List<ProcessInstance> processInstances;
	private String taskId;
	private List<HistoricActivityInstance> historicActivityInstances;
	private List<HistoricTaskInstance> historicTaskInstances;

	/**
	 * 新建流程.
	 */
	public String create() {
		return "create";
	}

	public String deploy() throws Exception {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		repositoryService.createDeployment().addInputStream("process.bpmn20.xml", bais).deploy();

		return RELOAD_PROCESS_DEFINITION;
	}

	/**
	 * 流程定义.
	 */
	public String listProcessDefinitions() {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		processDefinitions = repositoryService.createProcessDefinitionQuery().list();

		return "listProcessDefinitions";
	}

	/**
	 * 级联删除流程定义
	 * 
	 * @return
	 */
	public String removeProcessDefinition() {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);

		repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);

		return RELOAD_PROCESS_DEFINITION;
	}

	public String suspendProcessDefinition() {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		repositoryService.suspendProcessDefinitionById(processDefinitionId);

		return RELOAD_PROCESS_DEFINITION;
	}

	public String activeProcessDefinition() {
		RepositoryService repositoryService = processEngine.getRepositoryService();

		repositoryService.activateProcessDefinitionById(processDefinitionId);

		return RELOAD_PROCESS_DEFINITION;
	}

	public void graphProcessDefinition() throws Exception {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Command<InputStream> cmd = null;
		cmd = new ProcessDefinitionDiagramCmd(processDefinitionId);

		InputStream is = ((ServiceImpl) repositoryService).getCommandExecutor().execute(cmd);
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("image/png");

		IOUtils.copy(is, response.getOutputStream());
//		int len = 0;
//		byte[] b = new byte[1024];
//
//		while ((len = is.read(b, 0, 1024)) != -1) {
//			response.getOutputStream().write(b, 0, len);
//		}
	}

	public void viewXml() throws Exception {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
		String resourceName = processDefinition.getResourceName();
		InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
		ServletActionContext.getResponse().setContentType("text/xml;charset=UTF-8");
		IOUtils.copy(resourceAsStream, ServletActionContext.getResponse().getOutputStream());
	}

	/**
	 * 流程实例.
	 */
	public String listProcessInstances() {
		RuntimeService runtimeService = processEngine.getRuntimeService();

		processInstances = runtimeService.createProcessInstanceQuery().list();

		return "listProcessInstances";
	}

	public String removeProcessInstance() {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		runtimeService.deleteProcessInstance(processInstanceId, deleteReason);

		return RELOAD_PROCESS_INSTANCE;
	}

	public String suspendProcessInstance() {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		runtimeService.suspendProcessInstanceById(processInstanceId);

		return RELOAD_PROCESS_INSTANCE;
	}

	public String activeProcessInstance() {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		runtimeService.activateProcessInstanceById(processInstanceId);

		return RELOAD_PROCESS_INSTANCE;
	}

	/**
	 * 任务.
	 */
	public String listTasks() {
		TaskService taskService = processEngine.getTaskService();
		tasks = taskService.createTaskQuery().list();

		return "listTasks";
	}

	// The task cannot be deleted because is part of a running process
	/**
	 * 历史
	 */
	public String listHistoricProcessInstances() {
		HistoryService historyService = processEngine.getHistoryService();

		historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

		return "listHistoricProcessInstances";
	}

	public String listHistoricActivityInstances() {
		HistoryService historyService = processEngine.getHistoryService();

		historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();

		return "listHistoricActivityInstances";
	}

	public String listHistoricTasks() {
		HistoryService historyService = processEngine.getHistoryService();

		historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();

		return "listHistoricTasks";
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

		return RELOAD_TASK;
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

	public List<Task> getTasks() {
		return tasks;
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

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public List<ProcessInstance> getProcessInstances() {
		return processInstances;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public List<HistoricActivityInstance> getHistoricActivityInstances() {
		return historicActivityInstances;
	}

	public List<HistoricTaskInstance> getHistoricTaskInstances() {
		return historicTaskInstances;
	}
}
