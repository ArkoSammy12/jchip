package io.github.arkosammy12.jchip.ui;

import javax.swing.*;
import java.util.function.Function;

public class DebuggerLabel<T> extends JLabel {

    private final String name;
    private T state;
    private Function<T, String> toStringFunction;

    public DebuggerLabel(String name) {
        super(name);
        this.name = name;
    }

    public void setToStringFunction(Function<T, String> toStringFunction) {
        this.toStringFunction = toStringFunction;
    }

    public void setState(T val) {
        if (val == null) {
            if (this.state != null) {
                this.setText(this.name);
                this.state = null;
            }
            return;
        }
        this.state = val;
        String str = this.toStringFunction != null ? this.toStringFunction.apply(val) : val.toString();
        this.setText(this.name + ": " + str);
    }

}
