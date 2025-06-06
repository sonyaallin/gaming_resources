import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

public class RotationCat {
    private AL al;
    private int source;
    private int buffer;

    public void run() {
        try {
            // Initialize OpenAL and get AL instance
            ALut.alutInit();
            al = ALFactory.getAL();

            // Load WAV manually and fill OpenAL buffer
            WavLoader.WavData cat = WavLoader.loadWav("meow.wav", al);
            int[] buffers = new int[1];
            al.alGenBuffers(1, buffers, 0);
            buffer = buffers[0];

            al.alBufferData(buffer, cat.format, cat.data, cat.size, cat.sampleRate);

            // Create and configure source
            int[] sources = new int[1];
            al.alGenSources(1, sources, 0);
            source = sources[0];

            al.alSourcei(source, AL.AL_BUFFER, buffer);
            al.alSourcei(source, AL.AL_LOOPING, AL.AL_TRUE);
            al.alSourcef(source, AL.AL_GAIN, 1f);
            al.alSource3f(source, AL.AL_POSITION, 0f, 0f, -10f);

            // Listener position and orientation
            al.alListener3f(AL.AL_POSITION, 0f, 0f, 0f);
            float[] orientation = { 0f, 0f, -1f, 0f, 1f, 0f };
            al.alListenerfv(AL.AL_ORIENTATION, orientation, 0);

            // Play sound
            al.alSourcePlay(source);

            // Rotate the listener
            rotateListener();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ALut.alutExit();
        }
    }

    private void rotateListener() {
        float angle = 0f;
        float angularSpeed = 0.02f;
        int intervalMs = 33;

        System.out.println("Rotating listener...");

        while (true) {
            float atX = (float) Math.sin(angle);
            float atZ = (float) -Math.cos(angle);

            float[] orientation = { atX, 0f, atZ, 0f, 1f, 0f };
            al.alListenerfv(AL.AL_ORIENTATION, orientation, 0);

            angle += angularSpeed;

            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        new RotationCat().run();
    }
}
