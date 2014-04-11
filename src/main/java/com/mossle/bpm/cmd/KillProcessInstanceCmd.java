package com.mossle.bpm.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.runtime.Execution;

/**
 * 直接结束流程（非删除）
 * Created by izerui on 14-4-3.
 */
public class KillProcessInstanceCmd implements Command<Execution> {


    private String processInstanceId;

    public KillProcessInstanceCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Execution execute(CommandContext commandContext) {

        ExecutionEntity processInstance = commandContext.getExecutionEntityManager().findExecutionById(processInstanceId);

        processInstance.performOperation(AtomicOperation.DELETE_CASCADE);

        return null;
    }
}
