import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.*;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;
public class WavLoader {
    public static class WavData {
        public ByteBuffer data;
        public int format;
        public int sampleRate;
        public int size;
    }

    public static WavData loadWav(String path, AL al) throws IOException, UnsupportedAudioFileException {
        File file = new File(path);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat format = ais.getFormat();

        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED &&
                format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
            throw new UnsupportedAudioFileException("Only PCM WAV files are supported.");
        }

        int channels = format.getChannels();
        int sampleRate = (int) format.getSampleRate();
        int bitsPerSample = format.getSampleSizeInBits();

        int alFormat;
        if (channels == 1) {
            alFormat = bitsPerSample == 8 ? AL.AL_FORMAT_MONO8 : AL.AL_FORMAT_MONO16;
        } else if (channels == 2) {
            alFormat = bitsPerSample == 8 ? AL.AL_FORMAT_STEREO8 : AL.AL_FORMAT_STEREO16;
        } else {
            throw new UnsupportedAudioFileException("Only mono or stereo is supported.");
        }

        byte[] audioBytes = ais.readAllBytes();
        ByteBuffer buffer = ByteBuffer.allocateDirect(audioBytes.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(audioBytes);
        buffer.flip();

        WavData result = new WavData();
        result.data = buffer;
        result.format = alFormat;
        result.sampleRate = sampleRate;
        result.size = audioBytes.length;

        ais.close();
        return result;
    }
}
