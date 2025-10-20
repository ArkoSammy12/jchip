package io.github.arkosammy12.jchip.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLES_PER_FRAME;
import static io.github.arkosammy12.jchip.sound.SoundSystem.SAMPLE_RATE;

import javax.sound.sampled.*;

public final class SoundWriter {

    private static final byte[] EMPTY_SAMPLES = new byte[SAMPLES_PER_FRAME];
    private static SoundWriter soundWriterInstance;

    private final SourceDataLine audioLine;
    private final FloatControl volumeControl;
    private int volume = 75;
    private boolean soundHasPlayed;

    private SoundWriter() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            audioLine = AudioSystem.getSourceDataLine(format);
            audioLine.open(format, SAMPLE_RATE);
            audioLine.start();

            FloatControl control = null;
            if (audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                control = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(20.0f * (float) Math.log10(volume / 100.0));
            }
            this.volumeControl = control;

        } catch (Exception e) {
            throw new RuntimeException("Error while initializing Source Data Line for audio: ", e);
        }
    }

    public static SoundWriter getInstance() {
        if (soundWriterInstance == null) {
            soundWriterInstance = new SoundWriter();
        }
        if (!soundWriterInstance.audioLine.isRunning() && soundWriterInstance.audioLine.isOpen()) {
            soundWriterInstance.audioLine.start();
        }
        return soundWriterInstance;
    }

    public void writeSamples(byte[] buf) {
        if (!soundHasPlayed) {
            if (this.audioLine.available() < this.audioLine.getBufferSize()) {
                this.audioLine.flush();
            }
        }
        this.audioLine.write(buf, 0, buf.length);
        soundHasPlayed = true;
    }

    public void silence() {
        if (soundHasPlayed) {
            this.audioLine.write(EMPTY_SAMPLES, 0, EMPTY_SAMPLES.length);
        }
    }

    public void volumeUp() {
        if (this.volumeControl != null) {
            this.volume = Math.clamp(this.volume + 25, 0, 100);
            this.volumeControl.setValue(20.0f * (float) Math.log10(volume / 100.0));
        }
    }

    public void volumeDown() {
        if (this.volumeControl != null) {
            this.volume = Math.clamp(this.volume - 25, 0, 100);
            this.volumeControl.setValue( 20.0f * (float) Math.log10(volume / 100.0));
        }
    }

    public void stop() {
        this.audioLine.stop();
        this.audioLine.flush();
        soundHasPlayed = false;
    }

    public static void close() {
        if (soundWriterInstance != null) {
            soundWriterInstance.stop();
            soundWriterInstance.audioLine.close();
            soundWriterInstance = null;
        }
    }
}
