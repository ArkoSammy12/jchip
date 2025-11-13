package io.github.arkosammy12.jchip.ui;

public interface IODevice {

    void cycle();

    DmaStatus getDmaStatus();

     boolean isInterrupting();

    void doDmaOut(int value);

    int doDmaIn();

    void onOutput(int value);

    int onInput();

    enum DmaStatus {
        NONE,
        IN,
        OUT;

        public boolean isDma() {
            return this == IN || this == OUT;
        }

    }

}
