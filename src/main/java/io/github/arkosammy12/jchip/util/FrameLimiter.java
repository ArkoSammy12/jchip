package io.github.arkosammy12.jchip.util;

/// Frame pacer implementation generously provided by @janitor-raus via [his implementation](https://github.com/janitor-raus/CubeChip/blob/master/include/components/FrameLimiter.hpp)
public final class FrameLimiter {

    private boolean initTimeCheck;
    private boolean skipFirstPass;
    private final boolean skipLostFrame;
    private boolean lastFrameLost;

    private final double timeFrequency;
    private double timeOvershoot;
    private double timeVariation;

    private long timePastFrame;
    private long validFrameCount;

    public FrameLimiter(double frameRate, boolean firstPass, boolean lostFrame) {
        this.timeFrequency = 1_000_000_000L / Math.clamp(frameRate, 0.5, 1000);
        this.skipFirstPass = firstPass;
        this.skipLostFrame = lostFrame;
    }

    public boolean checkTime() {
        if (this.isValidFrame()) {
            return true;
        }
        if (this.getRemainder() >= 2.3e+6) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        } else {
            Thread.onSpinWait();
        }
        return false;
    }

    private boolean isValidFrame() {
        long timeAtCurrent = System.nanoTime();

        if (!this.initTimeCheck) {
            timePastFrame = timeAtCurrent;
            initTimeCheck = true;
        }

        if (skipFirstPass) {
            skipFirstPass = false;
            validFrameCount++;
            return true;
        }

        timeVariation = timeOvershoot + (timeAtCurrent - timePastFrame);

        if (timeVariation < timeFrequency) {
            return false;
        }

        if (skipLostFrame) {
            lastFrameLost = timeVariation >= timeFrequency + 50000;
            timeOvershoot = timeVariation % timeFrequency;

        } else {
            timeOvershoot = timeVariation - timeFrequency;
        }

        timePastFrame = timeAtCurrent;
        validFrameCount++;
        return true;
    }

    private double getRemainder() {
        return this.timeFrequency - timeVariation;
    }

}
