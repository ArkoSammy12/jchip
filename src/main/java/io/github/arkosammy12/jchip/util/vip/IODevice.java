package io.github.arkosammy12.jchip.util.vip;

public interface IODevice {

    void cycle();

    DmaStatus getDmaStatus();

    boolean isInterrupting();

    default void doDmaOut(int value) {}

    default int doDmaIn() {
        // Data bus lines are pulled up on the VIP
        return 0xFF;
    }

    default void onOutput(int value) {}

    default int onInput() {
        // Data bus lines are pulled up on the VIP
        return 0xFF;
    }

    enum DmaStatus {
        NONE,
        IN,
        OUT;

        public boolean isDma() {
            return this == IN || this == OUT;
        }

    }

}
