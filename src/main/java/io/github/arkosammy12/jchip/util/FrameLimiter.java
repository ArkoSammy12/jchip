package io.github.arkosammy12.jchip.util;

/// Frame pacer implementation generously provided by @janitor-raus via [his implementation](https://github.com/janitor-raus/CubeChip/blob/master/include/components/FrameLimiter.hpp)
public final class FrameLimiter {

    private boolean doneFirstRunSetup;
    private boolean forceInitialFrame;
    private final boolean allowMissedFrames;
    private boolean previousFrameSkip;

    private double targetFramePeriod;
    private double elapsedOverTarget;
    private double previousTimeDelta;

    private long previousFrameTime;
    private long validFrameCount;

    public FrameLimiter(double frameRate, boolean firstPass, boolean lostFrame) {
        this.setLimiterProperties(frameRate);
        this.forceInitialFrame = firstPass;
        this.allowMissedFrames = lostFrame;
    }

    public void setLimiterProperties(double frameRate) {
        this.targetFramePeriod = 1_000_000_000L / Math.clamp(frameRate, 0.5, 1000);
    }

    public boolean checkTime() {
        if (this.hasPeriodElapsed()) {
            return true;
        }
        if (this.getRemainder() >= 2.3E+6) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        } else {
            Thread.onSpinWait();
        }
        return false;
    }

    private boolean hasPeriodElapsed() {
        long currentTimePoint = System.nanoTime();

        if (!this.doneFirstRunSetup) {
            this.previousFrameTime = currentTimePoint;
            this.doneFirstRunSetup = true;
        }

        if (this.forceInitialFrame) {
            this.forceInitialFrame = false;
            this.validFrameCount++;
            return true;
        }

        this.previousTimeDelta = this.elapsedOverTarget + (currentTimePoint - this.previousFrameTime);

        if (this.previousTimeDelta < this.targetFramePeriod) {
            return false;
        }

        if (this.allowMissedFrames) {
            this.previousFrameSkip = this.previousTimeDelta >= this.targetFramePeriod + 50000;
            this.elapsedOverTarget = this.previousTimeDelta % this.targetFramePeriod;
        } else {
            this.elapsedOverTarget = this.previousTimeDelta - this.targetFramePeriod;
        }

        this.previousFrameTime = currentTimePoint;
        this.validFrameCount++;
        return true;
    }

    private double getRemainder() {
        return this.targetFramePeriod - this.previousTimeDelta;
    }

}
