package io.github.arkosammy12.jchip.ui.debugger;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

public class DebuggerLabel<T> extends JLabel {

    private final String name;
    private final AtomicReference<T> state = new AtomicReference<>(null);
    private final Debugger.TextEntry<T> textEntry;

    public DebuggerLabel(Debugger.TextEntry<T> textEntry) {
        String name = textEntry.getName().orElse("");
        this.name = textEntry.getName().orElse("");
        this.textEntry = textEntry;
        super(name);
    }

    public void updateState() {
        this.textEntry.getStateUpdater().ifPresent(stateUpdater -> this.state.set(stateUpdater.get()));
        this.setState(this.state.get());
    }

    public void setState(T val) {
        if (val == null) {
            if (this.state != null) {
                this.setText(this.name);
                this.state.set(null);
            }
            return;
        }
        this.state.set(val);
        String stateString = this.textEntry.getToStringFunction().orElse(Object::toString).apply(this.state.get());
        String descriptionString = this.textEntry.getDescription().orElse("");
        String nameString = this.textEntry.getName().orElse("");

        String labelString = "";
        if (!nameString.isEmpty()) {
            labelString += nameString;
        }
        if (!descriptionString.isEmpty()) {
            labelString += " (" + descriptionString + ")";
        }
        labelString += ": " + stateString;
        this.setText(labelString);
    }

}
