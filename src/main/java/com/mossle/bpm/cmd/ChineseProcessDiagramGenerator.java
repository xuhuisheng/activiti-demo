package com.mossle.bpm.cmd;

import java.io.InputStream;

import java.util.Collections;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;

import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

public class ChineseProcessDiagramGenerator extends ProcessDiagramGenerator {
    public static InputStream generateDiagram(BpmnModel bpmnModel,
            String imageType, List<String> highLightedActivities) {
        return generateDiagram(bpmnModel, highLightedActivities,
                Collections.EMPTY_LIST).generateImage(imageType);
    }

    protected static ProcessDiagramCanvas generateDiagram(BpmnModel bpmnModel,
            List<String> highLightedActivities, List<String> highLightedFlows) {
        ProcessDiagramCanvas processDiagramCanvas = initProcessDiagramCanvas(bpmnModel);

        //    // Draw pool shape, if process is participant in collaboration
        for (Pool pool : bpmnModel.getPools()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            processDiagramCanvas
                    .drawPoolOrLane(pool.getName(), (int) graphicInfo.getX(),
                            (int) graphicInfo.getY(), (int) graphicInfo
                                    .getWidth(), (int) graphicInfo.getHeight());
        }

        // Draw lanes
        for (Process process : bpmnModel.getProcesses()) {
            for (Lane lane : process.getLanes()) {
                GraphicInfo graphicInfo = bpmnModel
                        .getGraphicInfo(lane.getId());
                processDiagramCanvas.drawPoolOrLane(lane.getName(),
                        (int) graphicInfo.getX(), (int) graphicInfo.getY(),
                        (int) graphicInfo.getWidth(), (int) graphicInfo
                                .getHeight());
            }
        }

        // Draw activities and their sequence-flows
        for (FlowNode flowNode : bpmnModel.getProcesses().get(0)
                .findFlowElementsOfType(FlowNode.class)) {
            drawActivity(processDiagramCanvas, bpmnModel, flowNode,
                    highLightedActivities, highLightedFlows);
        }

        return processDiagramCanvas;
    }

    protected static ProcessDiagramCanvas initProcessDiagramCanvas(
            BpmnModel bpmnModel) {
        // We need to calculate maximum values to know how big the image will be in its entirety
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;

        for (Pool pool : bpmnModel.getPools()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            minX = graphicInfo.getX();
            maxX = graphicInfo.getX() + graphicInfo.getWidth();
            minY = graphicInfo.getY();
            maxY = graphicInfo.getY() + graphicInfo.getHeight();
        }

        List<FlowNode> flowNodes = gatherAllFlowNodes(bpmnModel);

        for (FlowNode flowNode : flowNodes) {
            GraphicInfo flowNodeGraphicInfo = bpmnModel.getGraphicInfo(flowNode
                    .getId());

            // width
            if ((flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth()) > maxX) {
                maxX = flowNodeGraphicInfo.getX()
                        + flowNodeGraphicInfo.getWidth();
            }

            if (flowNodeGraphicInfo.getX() < minX) {
                minX = flowNodeGraphicInfo.getX();
            }

            // height
            if ((flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight()) > maxY) {
                maxY = flowNodeGraphicInfo.getY()
                        + flowNodeGraphicInfo.getHeight();
            }

            if (flowNodeGraphicInfo.getY() < minY) {
                minY = flowNodeGraphicInfo.getY();
            }

            for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                List<GraphicInfo> graphicInfoList = bpmnModel
                        .getFlowLocationGraphicInfo(sequenceFlow.getId());

                for (GraphicInfo graphicInfo : graphicInfoList) {
                    // width
                    if (graphicInfo.getX() > maxX) {
                        maxX = graphicInfo.getX();
                    }

                    if (graphicInfo.getX() < minX) {
                        minX = graphicInfo.getX();
                    }

                    // height
                    if (graphicInfo.getY() > maxY) {
                        maxY = graphicInfo.getY();
                    }

                    if (graphicInfo.getY() < minY) {
                        minY = graphicInfo.getY();
                    }
                }
            }
        }

        int nrOfLanes = 0;

        for (Process process : bpmnModel.getProcesses()) {
            for (Lane l : process.getLanes()) {
                nrOfLanes++;

                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(l.getId());

                // // width
                if ((graphicInfo.getX() + graphicInfo.getWidth()) > maxX) {
                    maxX = graphicInfo.getX() + graphicInfo.getWidth();
                }

                if (graphicInfo.getX() < minX) {
                    minX = graphicInfo.getX();
                }

                // height
                if ((graphicInfo.getY() + graphicInfo.getHeight()) > maxY) {
                    maxY = graphicInfo.getY() + graphicInfo.getHeight();
                }

                if (graphicInfo.getY() < minY) {
                    minY = graphicInfo.getY();
                }
            }
        }

        // Special case, see http://jira.codehaus.org/browse/ACT-1431
        if ((flowNodes.size() == 0) && (bpmnModel.getPools().size() == 0)
                && (nrOfLanes == 0)) {
            // Nothing to show
            minX = 0;
            minY = 0;
        }

        return new ChineseProcessDiagramCanvas((int) maxX + 10,
                (int) maxY + 10, (int) minX, (int) minY);
    }
}
