package io.github.arkosammy12.jchip.ui.util;

import javax.swing.*;
import java.awt.*;

import static java.awt.ComponentOrientation.getOrientation;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

public class ToggleableSplitPane extends JPanel {

    private static final String SPLIT = "split";
    private static final String SINGLE = "single";

    private final CardLayout layout = new CardLayout();
    private final JSplitPane splitPane;
    private final Component left;
    private double currentProportionalDividerLocation;

    public ToggleableSplitPane(int orientation, Component left, Component right, double startingProportionalDividerLocation, int dividerSize, double resizeWeight) {
        this.setLayout(layout);
        this.setFocusable(false);

        this.left = left;
        this.currentProportionalDividerLocation = startingProportionalDividerLocation;

        this.splitPane = new JSplitPane(orientation, null, right);
        this.splitPane.setDividerSize(dividerSize);
        this.splitPane.setResizeWeight(resizeWeight);
        this.splitPane.setContinuousLayout(true);
        this.splitPane.setFocusable(false);

        this.add(this.splitPane, SPLIT);
        this.add(this.left, SINGLE);

        this.layout.show(this.left.getParent(), SINGLE);

    }

    public void showSplit() {
        this.splitPane.setLeftComponent(this.left);
        this.left.setVisible(true);
        this.layout.show(this, SPLIT);
        this.splitPane.setDividerLocation(this.currentProportionalDividerLocation);
    }

    public void hideRightPanel() {
        this.currentProportionalDividerLocation = this.getProportionalDividerLocation();
        this.add(this.left, SINGLE);
        this.layout.show(this, SINGLE);
    }

    public boolean isSplitVisible() {
        return this.splitPane.isShowing();
    }

    private double getProportionalDividerLocation() {
        if (this.splitPane.getOrientation() == VERTICAL_SPLIT) {
            return (double) this.splitPane.getDividerLocation() / (this.splitPane.getHeight() - this.splitPane.getDividerSize());
        } else {
            return (double) this.splitPane.getDividerLocation() / (this.splitPane.getWidth() - this.splitPane.getDividerSize());
        }
    }

}

