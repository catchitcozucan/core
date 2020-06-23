/**
 *    Original work by Ola Aronsson 2020
 *    Courtesy of nollettnoll AB &copy; 2012 - 2020
 *
 *    Licensed under the Creative Commons Attribution 4.0 International (the "License")
 *    you may not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *                https://creativecommons.org/licenses/by/4.0/
 *
 *    The software is provided “as is”, without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other liability,
 *    whether in an action of contract, tort or otherwise, arising from, out of or
 *    in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl.source.processor.bpm;

import static com.github.catchitcozucan.core.impl.source.processor.bpm.BpmSchemeElementDescriptor.generateIdForType;
import static com.github.catchitcozucan.core.impl.source.processor.bpm.BpmSchemeElementDescriptor.generateIdForTypeInBetween;
import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.NL;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.domain.ToStringBuilder;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class BpmSchemeGenerator extends BaseDomainObject {


    private final File xmlFile;
    private final List<BpmSchemeElementDescriptor> descriptors;
    private final Integer bpmActivitiesPercolumn;

    private static final Integer FLOW_HEIGHT = 73;
    private static final String INTENDENT_ONE = "   ";
    private static final String INTENDENT_TWO = "      ";
    private static final String INTENDENT_THREE = "         ";
    private static final String INTENDENT_FOUR = "            ";
    private static final String DEF_END = new StringBuilder("</bpmn:definitions>").append(NL).toString();
    private static final String PROCESS_END = new StringBuilder(INTENDENT_ONE).append("</bpmn:process>").append(NL).toString();
    private static final String DIAGRAM_END = new StringBuilder(INTENDENT_TWO).append("</bpmndi:BPMNPlane>").append(NL).append(INTENDENT_ONE).append("</bpmndi:BPMNDiagram>").append(NL).toString();
    // @formatter:off
	private static final String HEADER = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(NL)
			.append("<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ")
			.append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" ")
			.append("xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"%s\" targetNamespace=\"http://bpmn.io/schema/bpmn\" ")
			.append("exporter=\"catchitcozucan (https://www.github.com/catchitcozucan/core)\" exporterVersion=\"1.0\">").append(NL)
			.append(INTENDENT_ONE).append("<bpmn:process id=\"%s\" isExecutable=\"true\">").append(NL).toString();
	// @formatter:on

    private static final String ENTERING = new StringBuilder("ENTERING ").append("%s" .toUpperCase()).toString();

    // @formatter:off
    private static final String DIAGRAM_START_TAG = new StringBuilder()
            .append(INTENDENT_ONE).append("<bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">").append(NL)
            .append(INTENDENT_TWO).append("<bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"%s\">").append(NL).toString();
    // @formatter:on

    private static final String FLOW_ON = "<!-- FLOW_ON -->";

    // @formatter:off
    private static final String STARTEVENT = new StringBuilder()
            .append(INTENDENT_TWO).append("<bpmn:startEvent id=\"%s\" name=\"%s\">").append(NL)
            .append(INTENDENT_THREE).append(FLOW_ON).append(NL)
            .append(INTENDENT_TWO).append("</bpmn:startEvent>").append(NL).toString();
    // @formatter:on

    public static final String BPMNDI_BPMNSHAPE = "</bpmndi:BPMNShape>";
    private static final String START_EVENT_SHAPE = new StringBuilder()
            .append(INTENDENT_TWO).append("<bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"%s\">").append(NL)
            .append(INTENDENT_THREE).append("<dc:Bounds x=\"246\" y=\"81\" width=\"36\" height=\"36\" />").append(NL)
            .append(INTENDENT_THREE).append("<bpmndi:BPMNLabel>").append(NL)
            .append(INTENDENT_FOUR).append("<dc:Bounds x=\"187\" y=\"40\" width=\"150\" height=\"28\" />").append(NL)
            .append(INTENDENT_THREE).append("</bpmndi:BPMNLabel>").append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE).append(NL)
            .toString();
    // @formatter:on

    public static final String BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S = "<bpmndi:BPMNEdge id=\"%s_di\" bpmnElement=\"%s\">";
    public static final String BPMNDI_BPMNEDGE = "</bpmndi:BPMNEdge>";

    // ------------------- A WHOLE STEP ------------------- //

    public static final String BPMN_INCOMING_S_BPMN_INCOMING = "<bpmn:incoming>%s</bpmn:incoming>";
    public static final String BPMN_OUTGOING_S_BPMN_OUTGOING = "<bpmn:outgoing>%s</bpmn:outgoing>";
    public static final String BPMN_TASK = "</bpmn:task>";
    public static final String BPMN_TASK_ID_S_NAME_S = "<bpmn:task id=\"%s\" name=\"%s\">";
    private static final String TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_TASK_ID_S_NAME_S).append(NL)   // the task : id & name
            .append(INTENDENT_THREE).append(BPMN_INCOMING_S_BPMN_INCOMING).append(NL) // flow going in from start/other activity
            .append(INTENDENT_THREE).append(BPMN_INCOMING_S_BPMN_INCOMING).append(NL) // flow back in from failure
            .append(INTENDENT_THREE).append(BPMN_OUTGOING_S_BPMN_OUTGOING).append(NL) // flow to gw
            .append(INTENDENT_TWO).append(BPMN_TASK).append(NL).toString();
    // @formatter:on

    public static final String BPMN_SEQUENCE_FLOW_ID_S_SOURCE_REF_S_TARGET_REF_S = "<bpmn:sequenceFlow id=\"%s\" sourceRef=\"%s\" targetRef=\"%s\" />";
    private static final String FLOW_FROM_START_TO_TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_SEQUENCE_FLOW_ID_S_SOURCE_REF_S_TARGET_REF_S).append(NL).toString();
    // @formatter:on

    // @formatter:off
    private static final String GW = new StringBuilder()
            .append(INTENDENT_TWO).append("<bpmn:exclusiveGateway id=\"%s\" name=\"execution outcome\">").append(NL) // outcome gw : id & name
            .append(INTENDENT_THREE).append(BPMN_INCOMING_S_BPMN_INCOMING).append(NL) // incoming flow from task
            .append(INTENDENT_THREE).append(BPMN_OUTGOING_S_BPMN_OUTGOING).append(NL) // outgoing flow to fail task
            .append(INTENDENT_THREE).append(FLOW_ON).append(NL)                             // future flow connecting to the next task
            .append(INTENDENT_TWO).append("</bpmn:exclusiveGateway>").append(NL).toString();
    // @formatter:on

    // @formatter:off
    private static final String FLOW_FROM_TASK_TO_GW = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_SEQUENCE_FLOW_ID_S_SOURCE_REF_S_TARGET_REF_S).append(NL).toString();
    // @formatter:on

    // @formatter:off
    private static final String FAILURE_TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_TASK_ID_S_NAME_S).append(NL)   // fail task : id & name
            .append(INTENDENT_THREE).append(BPMN_INCOMING_S_BPMN_INCOMING).append(NL) // flow from gw
            .append(INTENDENT_THREE).append(BPMN_OUTGOING_S_BPMN_OUTGOING).append(NL) // flow back to orinal task
            .append(INTENDENT_TWO).append(BPMN_TASK).append(NL).toString();
    // @formatter:on

    // @formatter:off
    private static final String FLOW_FROM_GW_TO_TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_SEQUENCE_FLOW_ID_S_SOURCE_REF_S_TARGET_REF_S).append(NL).toString();
    // @formatter:on

    // @formatter:off
    private static final String FLOW_FAIL_BACK_TO_TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_SEQUENCE_FLOW_ID_S_SOURCE_REF_S_TARGET_REF_S).append(NL).toString();
    // @formatter:on

    // @formatter:off
    private static final String FINISH_TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMN_TASK_ID_S_NAME_S).append(NL)   // the finish task : id & name
            .append(INTENDENT_THREE).append(BPMN_INCOMING_S_BPMN_INCOMING).append(NL) // flow coming in from the tree above
            .append(INTENDENT_TWO).append(BPMN_TASK).append(NL).toString();
    // @formatter:on

    private static final String FLOW_OUTGOING = BPMN_OUTGOING_S_BPMN_OUTGOING;

    // ------------------- END A WHOLE STEP ------------------- //

    private static final Point[] DIAGRAM_POSITIONS = new Point[] { new Point(214, 190), new Point(264, 117), new Point(264, 190), new Point(239, 355), new Point(161, 370), new Point(264, 270), new Point(264, 355), new Point(350, 270), new Point(289, 380), new Point(400, 380), new Point(400, 350), new Point(400, 270), new Point(400, 230), new Point(314, 230), new Point(214, 480), new Point(264,
            380), new Point(264, 480) };
    private static final int DIAG_MAIN_ACTIVITY = 0;
    private static final int DIAG_FLOW_FROM_TREE = 2;
    private static final int DIAG_GW = 3;
    private static final int DIAG_FLOW_TO_GW = 4;
    private static final int DIAG_FAIL_STATE = 5;
    private static final int DIAG_FLOW_GW_TO_FAIL = 6;
    private static final int DIAG_FLOW_FAIL_TO_TASK = 7;
    private static final int DIAG_FINISH_TASK = 8;
    private static final int DIAG_FLOW_TO_FINISH_TASK = 9;
    private static final int STEP_UP_PER_DEPTH = 250 + (FLOW_HEIGHT / 2);
    private static final int COLON_WIDTH = 320;

    // @formatter:off
    private static final Point[] getElementYs(int component, int colon, int depth) {
        switch (component) {
            case DIAG_MAIN_ACTIVITY:
                return new Point[] { new Point(DIAGRAM_POSITIONS[0].x + (COLON_WIDTH * colon),  DIAGRAM_POSITIONS[0].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FLOW_FROM_TREE:
                return new Point[] { new Point(DIAGRAM_POSITIONS[1].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[1].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point(DIAGRAM_POSITIONS[2].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[2].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_GW:
                return new Point[] { new Point(DIAGRAM_POSITIONS[3].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[3].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point(DIAGRAM_POSITIONS[4].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[4] .y+ (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FLOW_TO_GW:
                return new Point[] { new Point(DIAGRAM_POSITIONS[5].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[5].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point(DIAGRAM_POSITIONS[6].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[6].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FAIL_STATE:
                return new Point[] { new Point(DIAGRAM_POSITIONS[7].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[7].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FLOW_GW_TO_FAIL:
                return new Point[] { new Point(DIAGRAM_POSITIONS[8].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[8].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point(DIAGRAM_POSITIONS[9].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[9].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point(DIAGRAM_POSITIONS[10].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[10].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FLOW_FAIL_TO_TASK:
                return new Point[] { new Point(DIAGRAM_POSITIONS[11].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[11].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point( DIAGRAM_POSITIONS[12].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[12].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point(DIAGRAM_POSITIONS[13].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[13].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FINISH_TASK:
                return new Point[] { new Point(DIAGRAM_POSITIONS[14].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[14].y + (STEP_UP_PER_DEPTH * depth))};
            case DIAG_FLOW_TO_FINISH_TASK:
                return new Point[] { new Point(DIAGRAM_POSITIONS[15].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[15].y + (STEP_UP_PER_DEPTH * depth)),
                                     new Point( DIAGRAM_POSITIONS[16].x + (COLON_WIDTH * colon), DIAGRAM_POSITIONS[16].y + (STEP_UP_PER_DEPTH * depth))};
            default: throw new ProcessRuntimeException(String.format("Oops. Get unsupported DIAG-switch : %d", component));
        }
    }
    // @formatter:on

    // ------------------- DIAGRAM A WHOLE STEP --------------- //

    public static final String BPMNDI_BPMNSHAPE_ID_S_DI_BPMN_ELEMENT_S = "<bpmndi:BPMNShape id=\"%s_di\" bpmnElement=\"%s\">";
    public static final String DC_BOUNDS_X_D_Y_D_WIDTH_100_HEIGHT_80 = "<dc:Bounds x=\"%d\" y=\"%d\" width=\"100\" height=\"80\" />";
    private static final String DIAG_COMP_MAIN_ACTIVITY = new StringBuilder()
          .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_THREE).append(DC_BOUNDS_X_D_Y_D_WIDTH_100_HEIGHT_80).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE).append(NL).toString();

    public static final String DI_WAYPOINT_X_D_Y_D = "<di:waypoint x=\"%d\" y=\"%d\" />";
    private static final String DIAG_COMP_FLOW_FROM_TREE = new StringBuilder()
           .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE).append(NL).toString();

    private static final String DIAG_COMP_GW = new StringBuilder()
          .append(INTENDENT_TWO).append("<bpmndi:BPMNShape id=\"%s_di\" bpmnElement=\"%s\" isMarkerVisible=\"true\">").append(NL)
            .append(INTENDENT_THREE).append("<dc:Bounds x=\"%d\" y=\"%d\" width=\"50\" height=\"50\" />").append(NL)
            .append(INTENDENT_THREE).append("<bpmndi:BPMNLabel>").append(NL)
            .append(INTENDENT_FOUR).append("<dc:Bounds x=\"%d\" y=\"%d\" width=\"47\" height=\"27\" />").append(NL)
            .append(INTENDENT_THREE).append("</bpmndi:BPMNLabel>").append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE).append(NL).toString();

    private static final String DIAG_COMP_FLOW_TO_GW = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE).append(NL).toString();

    private static final String DIAG_COMP_FAIL_STATE = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_TWO).append(DC_BOUNDS_X_D_Y_D_WIDTH_100_HEIGHT_80).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE).append(NL).toString();

    private static final String DIAG_COMP_FLOW_GW_TO_FAIL = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_TWO).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE).append(NL).toString();

    private static final String DIAG_COMP_FLOW_FAIL_TO_TASK = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_TWO).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE).append(NL).toString();

    private static final String DIAG_COMP_FINSH_ACTIVITY = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_THREE).append(DC_BOUNDS_X_D_Y_D_WIDTH_100_HEIGHT_80).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNSHAPE).append(NL).toString();

    private static final String DIAG_COMP_FLOW_TO_FINISH = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE).append(NL).toString();

    private static final String DIAG_COMP_FLOW_TO_NEXT_COLUMN = new StringBuilder()
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE_ID_S_DI_BPMN_ELEMENT_S).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_THREE).append(DI_WAYPOINT_X_D_Y_D).append(NL)
            .append(INTENDENT_TWO).append(BPMNDI_BPMNEDGE).append(NL).toString();

// @formatter:on

    public BpmSchemeGenerator(File xmlFile, List<BpmSchemeElementDescriptor> descriptors, Integer bpmActivitiesPercolumn) {
        this.xmlFile = xmlFile;
        this.descriptors = descriptors;
        this.bpmActivitiesPercolumn = bpmActivitiesPercolumn;
    }

    public void generateAndWriteScheme() {
        String processId = generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.PROCESS);
        StringBuilder processSection = new StringBuilder(String.format(HEADER, generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.DEFINITIONS), processId));
        StringBuilder diagramSection = new StringBuilder(String.format(DIAGRAM_START_TAG, processId));
        String flowToNext = null;
        int depth = -1;
        int colon = 0;
        boolean currentRenderingIsAColumnChange;
        boolean columnChangeIsActive = bpmActivitiesPercolumn > 1;
        boolean oneTaskFlow = descriptors.size() == 1;
        for (BpmSchemeElementDescriptor d : descriptors) {
            depth += 1;
            currentRenderingIsAColumnChange = false;
            if (columnChangeIsActive && depth + 1 > bpmActivitiesPercolumn) {
                colon += 1;
                depth = 0;
                currentRenderingIsAColumnChange = true;
            }
            flowToNext = appendActivityGwAndFailActivity(flowToNext, d, processSection, diagramSection, colon, depth, currentRenderingIsAColumnChange, oneTaskFlow);
        }
        processSection.append(PROCESS_END);
        diagramSection.append(DIAGRAM_END);
        StringBuilder content = new StringBuilder(processSection).append(diagramSection).append(DEF_END);
        try {
            IO.overwriteStringToFileWithEncoding(xmlFile.getAbsolutePath(), content.toString(), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new ProcessRuntimeException(String.format("Could not write BPM 2.0 XML file : %s", xmlFile.getAbsolutePath()), e);
        }
    }


    private String appendActivityGwAndFailActivity(String idSource, BpmSchemeElementDescriptor d, StringBuilder processSection, StringBuilder diagramSection, int colon, int depth, boolean currentRenderingIsAColumnChange, boolean oneTaskFlow) { //NOSONAR
        switch (d.getExpectedTypeBefore()) {
            case START_EVENT:
                String startEventIdHolder = generateIdForType(BpmSchemeElementDescriptor.Type.START_EVENT);
                processSection.append(String.format(STARTEVENT, startEventIdHolder, String.format(ENTERING, d.getProcessName())));
                diagramSection.append(String.format(START_EVENT_SHAPE, startEventIdHolder));
                if (!oneTaskFlow) {
                    return makeStepWholeStep(startEventIdHolder, d, processSection, diagramSection, colon, depth, false);
                } else {
                    String sourceForTheNextTree = makeStepWholeStep(startEventIdHolder, d, processSection, diagramSection, colon, depth, false);
                    makeFinish(sourceForTheNextTree, d, processSection, diagramSection, colon, depth);
                    return null;
                }
            case ACTIVITY:
                String sourceForTheNextTree = null;
                if (idSource != null) {
                    sourceForTheNextTree = makeStepWholeStep(idSource, d, processSection, diagramSection, colon, depth, currentRenderingIsAColumnChange);
                    if (d.getExpectedTypeAfter().equals(BpmSchemeElementDescriptor.Type.FINISH_STATE)) {
                        makeFinish(sourceForTheNextTree, d, processSection, diagramSection, colon, depth);
                        return null;
                    }
                }
                return sourceForTheNextTree;
            default:
                throw new ProcessRuntimeException(String.format("Ooops. Got unexpected type-before : %s", d.getExpectedTypeBefore()));
        }
    }

    private String makeStepWholeStep(String idSource, BpmSchemeElementDescriptor d, StringBuilder processSection, StringBuilder diagramSection, int colon, int depth, boolean currentRenderingIsAColumnChange) {

        // -- THE PROCESS PART -----------------

        // a. Generate the new flow, replace the FLOW_ON with this flow in what's already in the
        // process XML
        String flowTaskIncoming = generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.FLOW);
        processSection.replace(processSection.indexOf(FLOW_ON), processSection.indexOf(FLOW_ON) + FLOW_ON.length(), String.format(FLOW_OUTGOING, flowTaskIncoming));

        // b. this is th main task of this descriptor. It has two incoming flows,
        //  - the "above" incoming
        //  - the incoming from connected failure state
        // and one outing flow to the execution GW
        String taskId = generateIdForType(BpmSchemeElementDescriptor.Type.ACTIVITY);
        String failureBackToTask = generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.FLOW);
        String flowToGw = generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.FLOW);
        processSection.append(String.format(TASK, taskId, d.getTaskName(), flowTaskIncoming, failureBackToTask, flowToGw));

        // c. now we connect above elements through a flow : from idSource to this task
        processSection.append(String.format(FLOW_FROM_START_TO_TASK, flowTaskIncoming, idSource, taskId));

        // d. next we make the execution GW. It ha one incoming flow, that is
        //  - incoming from the main task
        //  - outgoing flow to the failure state and it will include the outgoing flow to the next loop
        String gwId = BpmSchemeElementDescriptor.generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.GATEWAY);
        String flowFromGwToFailure = generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.FLOW);
        processSection.append(String.format(GW, gwId, flowToGw, flowFromGwToFailure));

        // e. then we connect these elements, task and gw, with a flow
        processSection.append(String.format(FLOW_FROM_TASK_TO_GW, flowToGw, taskId, gwId));

        // f. next : construct the failure state/task. It has
        // - incoming flow from the GW
        // - outgoing flow back to our main task above
        String taskIdFailure = generateIdForType(BpmSchemeElementDescriptor.Type.ACTIVITY);
        processSection.append(String.format(FAILURE_TASK, taskIdFailure, d.getStatusUponFailure(), flowFromGwToFailure, failureBackToTask));

        // g. now connect the GW with the failure state/task
        processSection.append(String.format(FLOW_FROM_GW_TO_TASK, flowFromGwToFailure, gwId, taskIdFailure));

        // h. now connect the failure state/task with out main originating task
        processSection.append(String.format(FLOW_FAIL_BACK_TO_TASK, failureBackToTask, taskIdFailure, taskId));

        // i : finally, the source to append our next tree shall be our GW
        String idSourceNext = gwId;

        // -- THE DIAGRAM PART -----------------

        Point[] mainYs = getElementYs(DIAG_MAIN_ACTIVITY, colon, depth);
        Point[] flowFromTree = getElementYs(DIAG_FLOW_FROM_TREE, colon, depth);
        Point[] gwYs = getElementYs(DIAG_GW, colon, depth);
        Point[] glowToGwYs = getElementYs(DIAG_FLOW_TO_GW, colon, depth);
        Point[] failYs = getElementYs(DIAG_FAIL_STATE, colon, depth);
        Point[] flowGwToFailYs = getElementYs(DIAG_FLOW_GW_TO_FAIL, colon, depth);
        Point[] flowFailToTaskYs = getElementYs(DIAG_FLOW_FAIL_TO_TASK, colon, depth);

        diagramSection.append(String.format(DIAG_COMP_MAIN_ACTIVITY, taskId, taskId, mainYs[0].x, mainYs[0].y));
        if (!currentRenderingIsAColumnChange) {
            diagramSection.append(String.format(DIAG_COMP_FLOW_FROM_TREE, flowTaskIncoming, flowTaskIncoming, flowFromTree[0].x, flowFromTree[0].y, flowFromTree[1].x, flowFromTree[1].y));
        } else {
            int x1 = flowFromTree[0].x - 320;
            int x2 = flowFromTree[0].x - 114;
            int y1 = (int) Math.round((double) (STEP_UP_PER_DEPTH * bpmActivitiesPercolumn) + (double) 117);
            int y2 = y1 + 13;
            int y3 = flowFromTree[0].y - 30;
            diagramSection.append(String.format(DIAG_COMP_FLOW_TO_NEXT_COLUMN, flowTaskIncoming, flowTaskIncoming, x1, y1, x1, y2, x2, y2, x2, y3, flowFromTree[1].x, y3, flowFromTree[1].x, flowFromTree[1].y));
        }
        diagramSection.append(String.format(DIAG_COMP_GW, gwId, gwId, gwYs[0].x, gwYs[0].y, gwYs[1].x, gwYs[1].y));
        diagramSection.append(String.format(DIAG_COMP_FLOW_TO_GW, flowToGw, flowToGw, glowToGwYs[0].x, glowToGwYs[0].y, glowToGwYs[1].x, glowToGwYs[1].y));
        diagramSection.append(String.format(DIAG_COMP_FAIL_STATE, taskIdFailure, taskIdFailure, failYs[0].x, failYs[0].y));
        diagramSection.append(String.format(DIAG_COMP_FLOW_GW_TO_FAIL, flowFromGwToFailure, flowFromGwToFailure, flowGwToFailYs[0].x, flowGwToFailYs[0].y, flowGwToFailYs[1].x, flowGwToFailYs[1].y, flowGwToFailYs[2].x, flowGwToFailYs[2].y));
        diagramSection.append(String.format(DIAG_COMP_FLOW_FAIL_TO_TASK, failureBackToTask, failureBackToTask, flowFailToTaskYs[0].x, flowFailToTaskYs[0].y, flowFailToTaskYs[1].x, flowFailToTaskYs[1].y, flowFailToTaskYs[2].x, flowFailToTaskYs[2].y));
        return idSourceNext;
    }

    private void makeFinish(String idSource, BpmSchemeElementDescriptor d, StringBuilder processSection, StringBuilder diagramSection, int colon, int depth) {

        // -- THE PROCESS PART -----------------

        // a. Generate the new flow, replace the FLOW_ON with this flow in what's already in the
        // process XML
        String flowTaskIncoming = generateIdForTypeInBetween(BpmSchemeElementDescriptor.TypeInBetween.FLOW);
        processSection.replace(processSection.indexOf(FLOW_ON), processSection.indexOf(FLOW_ON) + FLOW_ON.length(), String.format(FLOW_OUTGOING, flowTaskIncoming));

        // b. this is simply the finish state. It has only one incoming flow, namely, the flow from the
        // tree that took as here..
        String taskId = generateIdForType(BpmSchemeElementDescriptor.Type.ACTIVITY);
        processSection.append(String.format(FINISH_TASK, taskId, new StringBuilder("FINISHED:&#10;").append(d.getStatusUponSuccess()).toString(), flowTaskIncoming));

        // c. now we connect above elements through a flow : from idSource to this task
        processSection.append(String.format(FLOW_FROM_START_TO_TASK, flowTaskIncoming, idSource, taskId));

        // -- THE DIAGRAM PART -----------------
        Point[] finishYs = getElementYs(DIAG_FINISH_TASK, colon, depth);
        Point[] flowFromTree = getElementYs(DIAG_FLOW_TO_FINISH_TASK, colon, depth);
        diagramSection.append(String.format(DIAG_COMP_FINSH_ACTIVITY, taskId, taskId, finishYs[0].x, finishYs[0].y));
        diagramSection.append(String.format(DIAG_COMP_FLOW_TO_FINISH, flowTaskIncoming, flowTaskIncoming, flowFromTree[0].x, flowFromTree[0].y, flowFromTree[1].x, flowFromTree[1].y));
    }

    @Override
    public String doToString() {
        ToStringBuilder b = new ToStringBuilder("file", xmlFile.getAbsolutePath());
        descriptors.stream().forEachOrdered(d -> b.append(d.getTaskName(), d.toString()));
        return b.toString();
    }
}

