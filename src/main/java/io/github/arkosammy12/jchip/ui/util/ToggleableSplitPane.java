package io.github.arkosammy12.jchip.ui.util;

import javax.swing.*;
import java.awt.*;

public class ToggleableSplitPane extends JPanel {

    private static final String SPLIT = "split";
    private static final String SINGLE = "single";

    private final CardLayout layout = new CardLayout();
    private final JSplitPane splitPane;
    private final Component left;
    private int currentDividerLocation;

    public ToggleableSplitPane(int orientation, Component left, Component right, int dividerLocation, int dividerSize, double resizeWeight) {
        this.setLayout(layout);

        this.left = left;
        this.currentDividerLocation = dividerLocation;
        this.splitPane = new JSplitPane(orientation, null, right);
        this.splitPane.setDividerLocation(dividerLocation);
        this.splitPane.setDividerSize(dividerSize);
        this.splitPane.setResizeWeight(resizeWeight);
        this.splitPane.setContinuousLayout(true);

        this.add(this.splitPane, SPLIT);
        this.add(this.left, SINGLE);

        this.layout.show(this.left.getParent(), SINGLE);

    }

    public void showSplit() {
        this.splitPane.setLeftComponent(this.left);
        this.left.setVisible(true);
        this.splitPane.setDividerLocation(this.currentDividerLocation);
        this.layout.show(this, SPLIT);
    }

    public void hideRightPanel() {
        this.currentDividerLocation = this.splitPane.getDividerLocation();
        this.add(this.left, SINGLE);
        this.layout.show(this, SINGLE);
    }

    public boolean isSplitVisible() {
        return this.splitPane.isShowing();
    }

}

