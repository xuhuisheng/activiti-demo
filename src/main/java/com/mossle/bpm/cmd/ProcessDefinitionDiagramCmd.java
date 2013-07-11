package com.mossle.bpm.cmd;

import java.io.*;

import java.util.*;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.bpmn.diagram.*;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;

public class ProcessDefinitionDiagramCmd implements Command<InputStream> {
    protected String processDefinitionId;

    public ProcessDefinitionDiagramCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public InputStream execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = Context
                .getProcessEngineConfiguration().getProcessDefinitionCache()
                .get(processDefinitionId);

        GetBpmnModelCmd getBpmnModelCmd = new GetBpmnModelCmd(
                processDefinitionId);
        BpmnModel bpmnModel = getBpmnModelCmd.execute(commandContext);

        InputStream is = ProcessDiagramGenerator.generateDiagram(
                bpmnModel, "png", Collections.EMPTY_LIST);

        return is;
    }
}
