package io.github.arkosammy12.jchip.ui.debugger;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DebuggerLabel<T> extends JLabel {

    private final DebuggerSchema.TextEntry<T> textEntry;
    private final AtomicReference<T> state = new AtomicReference<>(null);
    private final AtomicBoolean stateChanged = new AtomicBoolean(false);

    public DebuggerLabel(DebuggerSchema.TextEntry<T> textEntry) {
        super(textEntry.getName().orElse(""));
        this.textEntry = textEntry;
    }

    public boolean stateHasChanged() {
        return this.stateChanged.get();
    }

    public void update() {
        T oldState = this.state.get();
        Optional<Supplier<T>> stateUpdaterOptional = this.textEntry.getStateUpdater();
        if (stateUpdaterOptional.isEmpty()) {
            this.stateChanged.set(false);
            return;
        }
        T newState = stateUpdaterOptional.get().get();
        this.state.set(newState);

        String name = this.textEntry.getName().orElse("");
        if (newState == null) {
            this.setText(name);
            this.stateChanged.set(false);
            return;
        }
        this.stateChanged.set(!Objects.equals(oldState, newState));

        String text = "";
        if (!name.isEmpty()) {
            text += name;
        }
        text += ": " + this.textEntry.getToStringFunction().orElse(Object::toString).apply(this.state.get());

        if (!text.equals(this.getText())) {
            this.setText(text);
        }
    }

}
