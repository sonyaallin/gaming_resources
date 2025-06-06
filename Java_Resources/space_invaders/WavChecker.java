import javax.sound.sampled.*;

import java.io.File;

public class WavChecker {
    public static void main(String[] args) throws Exception {
        File wavFile = new File("meow.wav");
        AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat format = stream.getFormat();

        System.out.println("Encoding: " + format.getEncoding());
        System.out.println("Sample Rate: " + format.getSampleRate());
        System.out.println("Channels: " + format.getChannels());
        System.out.println("Bits Per Sample: " + format.getSampleSizeInBits());

        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED ||
                format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
            System.out.println("✅ This is an uncompressed PCM WAV file.");
        } else {
            System.out.println("❌ This is NOT uncompressed PCM.");
        }
    }
}
