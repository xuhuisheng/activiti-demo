package com.mossle.bpm.cmd;

import java.awt.Font;

import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas;

public class ChineseProcessDiagramCanvas extends ProcessDiagramCanvas {
    public ChineseProcessDiagramCanvas(int width, int height) {
        super(width, height);

        Font font = new Font("微软雅黑", Font.BOLD, 12);
        g.setFont(font);
    }

    public ChineseProcessDiagramCanvas(int width, int height, int minX, int minY) {
        this(width, height);

        this.minX = minX;
        this.minY = minY;
    }
}
