package io.github.arkosammy12.jchip.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class StartingDividerLocationSplitPane extends JSplitPane {

    public StartingDividerLocationSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent, double startingProportionalDividerLocation) {
        super(newOrientation, newLeftComponent, newRightComponent);
        this.setFocusable(false);
        this.addComponentListener(new ComponentAdapter() {

            private boolean alreadyShown = false;

            @Override
            public void componentResized(ComponentEvent e) {
                if (!this.alreadyShown) {
                    setDividerLocation(startingProportionalDividerLocation);
                    this.alreadyShown = true;
                }
            }

        });
    }

}
