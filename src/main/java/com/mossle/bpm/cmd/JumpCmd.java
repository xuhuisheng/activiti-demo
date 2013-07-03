package com.mossle.bpm.cmd;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

public class JumpCmd implements Command<Object> {
    private String executionId;
    private String activityId;

    public JumpCmd(String executionId, String activityId) {
        this.executionId = executionId;
        this.activityId = activityId;
    }

    public Object execute(CommandContext commandContext) {
        for (TaskEntity taskEntity : Context.getCommandContext()
                .getTaskEntityManager().findTasksByExecutionId(executionId)) {
            Context.getCommandContext().getTaskEntityManager().deleteTask(
                    taskEntity, "jump", false);
        }

        ExecutionEntity executionEntity = Context.getCommandContext()
                .getExecutionEntityManager().findExecutionById(executionId);
        ProcessDefinitionImpl processDefinition = executionEntity
                .getProcessDefinition();
        ActivityImpl activity = processDefinition.findActivity(activityId);
        executionEntity.executeActivity(activity);

        return null;
    }
}
