package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.AudioSystem;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import java.util.Arrays;

public class DefaultAudioSystem implements AudioSystem {

    private final int[] patternBuffer = new int[16];
    private int pitch = 4000;

    private Clip beepClip;
    private boolean isBeeping = false;

    public DefaultAudioSystem() {
        Arrays.fill(this.patternBuffer, 0);
    }

    @Override
    public void loadPatternByte(int index, int value) {
        this.patternBuffer[index] = value & 0xFF;
    }

    @Override
    public void setPitch(int vX) {
        this.pitch = (int) (4000 * Math.pow(2, (double) (vX - 64) / 48));
    }

    public void buzz() {
        if (!isBeeping) {
            try {
                AudioFormat af = new AudioFormat(44100f, 8, 1, true, false );
                beepClip = javax.sound.sampled.AudioSystem.getClip();
                byte[] data = new byte[44100];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte)(Math.sin(2 * Math.PI * i / ((double) 44100 / 440)) * 127);
                }
                beepClip.open(af, data, 0, data.length);
                beepClip.loop(Clip.LOOP_CONTINUOUSLY);
                isBeeping = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopBuzz() {
        if (isBeeping && beepClip != null) {
            beepClip.stop();
            beepClip.close();
            isBeeping = false;
        }
    }
}
