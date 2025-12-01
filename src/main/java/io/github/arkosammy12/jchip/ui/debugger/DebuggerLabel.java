package io.github.arkosammy12.jchip.ui.debugger;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

public class DebuggerLabel<T> extends JLabel {

    private final String name;
    private final AtomicReference<T> state = new AtomicReference<>(null);
    DebuggerInfo.TextEntry<T> textEntry;

    public DebuggerLabel(DebuggerInfo.TextEntry<T> textEntry) {
        String name = textEntry.getName().orElse("");
        super(name);
        this.name = name;
        this.textEntry = textEntry;
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

        String finalString = "";
        if (!nameString.isEmpty()) {
            finalString += nameString;
        }
        if (!descriptionString.isEmpty()) {
            finalString += " (" + descriptionString + ")";
        }
        finalString += ": " + stateString;
        this.setText(finalString);
    }

}
