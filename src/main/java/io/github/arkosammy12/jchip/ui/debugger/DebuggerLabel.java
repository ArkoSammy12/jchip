package io.github.arkosammy12.jchip.ui.debugger;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DebuggerLabel<T> extends JLabel {

    private final String name;
    private final AtomicReference<T> state = new AtomicReference<>(null);
    private final AtomicBoolean stateChanged = new AtomicBoolean(false);
    private final DebuggerSchema.TextEntry<T> textEntry;

    public DebuggerLabel(DebuggerSchema.TextEntry<T> textEntry) {
        String name = textEntry.getName().orElse("");
        this.name = textEntry.getName().orElse("");
        this.textEntry = textEntry;
        super(name);
    }

    public boolean stateHasChanged() {
        return this.stateChanged.get();
    }

    public void update() {
        T oldState = this.state.get();

        Optional<Supplier<T>> updaterOpt = this.textEntry.getStateUpdater();
        if (updaterOpt.isEmpty()) {
            this.stateChanged.set(false);
            return;
        }

        T newState = updaterOpt.get().get();
        this.state.set(newState);

        if (newState == null) {
            this.setText(this.name);
            this.stateChanged.set(false);
            return;
        }

        this.stateChanged.set(!Objects.equals(oldState, newState));
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
