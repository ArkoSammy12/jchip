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

    public void update() {
        this.textEntry.getStateUpdater().ifPresent(stateUpdater -> this.state.set(stateUpdater.get()));
        T val = this.state.get();
        if (val == null) {
            this.setText(this.name);
            this.state.set(null);
            return;
        }

        String descriptionString = this.textEntry.getDescription().orElse("");
        String nameString = this.textEntry.getName().orElse("");

        String labelString = "";
        if (!nameString.isEmpty()) {
            labelString += nameString;
        }
        if (!descriptionString.isEmpty()) {
            labelString += " (" + descriptionString + ")";
        }
        labelString += ": " + this.textEntry.getToStringFunction().orElse(Object::toString).apply(this.state.get());

        if (!labelString.equals(this.getText())) {
            this.setText(labelString);
        }
    }

}
