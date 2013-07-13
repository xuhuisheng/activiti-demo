/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mossle.bpm.support;

import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
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

/**
 * Class to generate an image based the diagram interchange information in a
 * BPMN 2.0 process.
 *
 * @author Joram Barrez
 */
public class CustomProcessDiagramGenerator extends ProcessDiagramGenerator {

  protected static void drawActivity(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode, List<String> highLightedActivities, List<String> highLightedFlows) {

    ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(flowNode.getClass());
    if (drawInstruction != null) {

      drawInstruction.draw(processDiagramCanvas, bpmnModel, flowNode);

      // Gather info on the multi instance marker
      boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
      if (flowNode instanceof Activity) {
        Activity activity = (Activity) flowNode;
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity.getLoopCharacteristics();
        if (multiInstanceLoopCharacteristics != null) {
          multiInstanceSequential = multiInstanceLoopCharacteristics.isSequential();
          multiInstanceParallel = !multiInstanceSequential;
        }
      }

      // Gather info on the collapsed marker
      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
      if (flowNode instanceof SubProcess) {
        collapsed = !graphicInfo.isExpanded();
      } else if (flowNode instanceof CallActivity) {
        collapsed = true;
      }

      // Actually draw the markers
      processDiagramCanvas.drawActivityMarkers((int) graphicInfo.getX(), (int) graphicInfo.getY(),(int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(),
              multiInstanceSequential, multiInstanceParallel, collapsed);

      // Draw highlighted activities
      if (highLightedActivities.contains(flowNode.getId())) {
        drawHighLight(processDiagramCanvas, bpmnModel.getGraphicInfo(flowNode.getId()));
      }

    }

    // Outgoing transitions of activity
    for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
      List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId());
      boolean drawedLabel = false;
      for (int i=1; i<graphicInfoList.size(); i++) {

        GraphicInfo graphicInfo = graphicInfoList.get(i);
        GraphicInfo previousGraphicInfo = graphicInfoList.get(i-1);

        boolean highLighted = (highLightedFlows.contains(sequenceFlow.getId()));
        boolean drawConditionalIndicator = (i == 1) && sequenceFlow.getConditionExpression() != null && !(flowNode instanceof Gateway);

        if (i < graphicInfoList.size() - 1) {
          processDiagramCanvas.drawSequenceflowWithoutArrow((int) previousGraphicInfo.getX(), (int) previousGraphicInfo.getY(),
                  (int) graphicInfo.getX(), (int) graphicInfo.getY(), drawConditionalIndicator, highLighted);
        } else {
          processDiagramCanvas.drawSequenceflow((int) previousGraphicInfo.getX(), (int) previousGraphicInfo.getY(),
                  (int) graphicInfo.getX(), (int) graphicInfo.getY(), drawConditionalIndicator, highLighted);
          if (!drawedLabel) {
            GraphicInfo labelGraphicInfo = bpmnModel.getLabelGraphicInfo(sequenceFlow.getId());
            if (labelGraphicInfo != null) {
				previousGraphicInfo = graphicInfoList.get(0);

              // int middleX = (int) (((previousGraphicInfo.getX() + labelGraphicInfo.getX()) + (graphicInfo.getX()+ labelGraphicInfo.getX())) / 2);
              // int middleY = (int) (((previousGraphicInfo.getY() + labelGraphicInfo.getY()) + (graphicInfo.getY()+ labelGraphicInfo.getY())) / 2);
              // middleX += 15;

			  int[] point = findMiddlePoint(graphicInfoList);
			  int middleX = (int) (point[0] + labelGraphicInfo.getX());
			  int middleY = (int) (point[1] + labelGraphicInfo.getY()) - 15;

			  processDiagramCanvas.drawLabel(sequenceFlow.getName(), middleX, middleY,
                      (int) labelGraphicInfo.getWidth(), (int) labelGraphicInfo.getHeight());
              drawedLabel = true;
            }
          }
        }
      }
    }

    // TODO: curvy parameter

    // Outgoing transitions of activity
//    for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
//      boolean highLighted = (highLightedFlows.contains(sequenceFlow.getId()));
//      boolean drawConditionalIndicator = sequenceFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION) != null
//              && !((String) activity.getProperty("type")).toLowerCase().contains("gateway");
//      boolean isDefault = sequenceFlow.getId().equals(activity.getProperty("default"))
//    		  && ((String) activity.getProperty("type")).toLowerCase().contains("gateway");
//
//      List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
//      int xPoints[]= new int[waypoints.size()/2];
//      int yPoints[]= new int[waypoints.size()/2];
//      for (int i=0, j=0; i < waypoints.size(); i+=2, j++) { // waypoints.size()
//                                                      // minimally 4: x1, y1,
//                                                      // x2, y2
//      	xPoints[j] = waypoints.get(i);
//      	yPoints[j] = waypoints.get(i+1);
//      }
//      processDiagramCanvas.drawSequenceflow(xPoints, yPoints, drawConditionalIndicator, isDefault, highLighted);
//    }

    // Nested elements
    if (flowNode instanceof FlowElementsContainer) {
      for (FlowElement nestedFlowElement : ((FlowElementsContainer) flowNode).getFlowElements()) {
        if (nestedFlowElement instanceof FlowNode) {
          drawActivity(processDiagramCanvas, bpmnModel, (FlowNode) nestedFlowElement, highLightedActivities, highLightedFlows);
        }
      }
    }
  }

    private static int[] findMiddlePoint(List<GraphicInfo> graphicInfoList) {
		double totalLength = 0D;
		GraphicInfo previousNode = null;
		for (GraphicInfo graphicInfo : graphicInfoList) {
			if (previousNode != null) {
				totalLength += getLength(previousNode.getX(), graphicInfo.getX(),
					previousNode.getY(), graphicInfo.getY());
			}
			previousNode = graphicInfo;
		}

		double halfLength = totalLength / 2;
		totalLength = 0D;
		previousNode = null;
		for (GraphicInfo graphicInfo : graphicInfoList) {
			if (previousNode != null) {
				double length = getLength(previousNode.getX(), graphicInfo.getX(),
					previousNode.getY(), graphicInfo.getY());

				if (length >= halfLength) {
					double rate = halfLength / length;
					double x0 = previousNode.getX();
					double y0 = previousNode.getY();
					double x1 = graphicInfo.getX();
					double y1 = graphicInfo.getY();
					int[] point = new int[2];
					point[0] = (int) (x0 + (x1 - x0) * rate);
					point[1] = (int) (y0 + (y1 - y0) * rate);
					return point;
				} else {
					halfLength -= length;
				}
			}
			previousNode = graphicInfo;
		}

		return null;
	}

	private static double getLength(double x0, double x1, double y0, double y1) {
		return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
	}

  protected static void drawHighLight(ProcessDiagramCanvas processDiagramCanvas, GraphicInfo graphicInfo) {
    processDiagramCanvas.drawHighLight((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());

  }
}
