import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.io.File;

public class SoundModuleAdventure {

    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int VOICE = 2;

    static final int NUM_BUFFERS = 2;
    static final int NUM_SOURCES = 2;

    static int[] buffers = new int[NUM_BUFFERS];
    static int[] sources = new int[NUM_SOURCES];

    static float[][] sourcePos = new float[NUM_SOURCES][NUM_BUFFERS];

    static AL al;

    public static SynthesisInput input;
    public static VoiceSelectionParams voice;
    public static AudioConfig audioConfig;

    public SoundModuleAdventure() {
        //initialize OpenAL
        try {
            ALut.alutInit();
            this.al = ALFactory.getAL();
        } catch (
                ALException e) {
            e.printStackTrace();
            return;
        }

        if (loadALData() == AL.AL_FALSE) {
            System.exit(1);
        }

        setListenerValues();

        audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.LINEAR16) // PCM encoding
                .build();

        System.out.println("Created Sound Model");

    }

    public void playLeft() {
        this.al.alSourcePlay(sources[LEFT]);
    }

    public void playRight() {
        this.al.alSourcePlay(sources[RIGHT]);
    }

    public void playMeow(double angle) {
        float atX = (float) Math.sin(angle);
        float atZ = (float) -Math.cos(angle); // Negative because Z forward

        // "Up" vector stays fixed — Y-up world
        float[] orientation = {
                atX, 0f, atZ,  // "at" vector
                0f, 1f, 0f     // "up" vector
        };

        //System.out.println(orientation[0] + " " + orientation[1] + " " + orientation[2]);
        al.alListenerfv(AL.AL_ORIENTATION, orientation, 0);

//        this.al.alSourcefv(sources[CAT], AL.AL_POSITION, orientation, 0);
    }

    public int playALData(String s, int gender, String voiceName) {

        // Set the text input to be synthesized
        String filename = s.replace(" ","-");
        if (filename.length() > 50) filename = filename.length() > 50 ? filename.substring(0, 50) : filename;
        filename = "story/" + filename + ".wav";

        File f = new File(filename);
        System.out.println(filename + " " + f.exists());

        if(!f.exists()) {
            SsmlVoiceGender vGender;
            if (gender == 0) vGender = SsmlVoiceGender.MALE;
            else if (gender == 1) vGender = SsmlVoiceGender.NEUTRAL;
            else vGender = SsmlVoiceGender.FEMALE;

            input = SynthesisInput.newBuilder().setText(s).build();
            voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")
                    .setName(voiceName)
                    .setSsmlGender(vGender)
                    .build();

            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                ByteString audioContents = response.getAudioContent();
                // Save to a WAV file
                byte[] audioData = audioContents.toByteArray();
                // Define audio format (must match LINEAR16 parameters: 16-bit PCM, 1 channel, 16000 Hz)
                AudioFormat audioFormat = new AudioFormat(24000, 16, 1, true, false);
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream audioStream = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, f);
                System.out.println("Audio content written to file: " + filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 2. Generate a buffer and source
        int[] buffer = new int[1];
        int[] source = new int[1];

        al.alGenBuffers(1, buffer, 0);
        al.alGenSources(1, source, 0);

        // 3. Load WAV file data
        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(f);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AudioFormat format = ais.getFormat();

        // Convert to PCM_SIGNED if needed
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
            );
            ais = AudioSystem.getAudioInputStream(format, ais);
        }

        byte[] data = new byte[0];
        try {
            data = ais.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Determine OpenAL format
        int alFormat;
        if (format.getChannels() == 1) {
            alFormat = (format.getSampleSizeInBits() == 8) ? AL.AL_FORMAT_MONO8 : AL.AL_FORMAT_MONO16;
        } else {
            alFormat = (format.getSampleSizeInBits() == 8) ? AL.AL_FORMAT_STEREO8 : AL.AL_FORMAT_STEREO16;
        }

        // 4. Fill the buffer
        al.alBufferData(buffer[0], alFormat, java.nio.ByteBuffer.wrap(data), data.length, (int) format.getSampleRate());

        // 5. Attach buffer to source
        al.alSourcei(source[0], AL.AL_BUFFER, buffer[0]);

        // 6. Position the source and listener
        float[] sourcePos = new float[] { 0.0f, 0.0f, -2.0f }; // 2 units in front of the listener
        float[] listenerPos = new float[] { 0.0f, 0.0f, 0.0f };
        float[] listenerOri = new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }; // "at" then "up"

        al.alSourcefv(source[0], AL.AL_POSITION, sourcePos, 0);
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);

        // 7. Play the sound
        al.alSourcePlay(source[0]);

        // Wait for it to finish
        int[] state = new int[1];
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            al.alGetSourcei(source[0], AL.AL_SOURCE_STATE, state, 0);
        } while (state[0] == AL.AL_PLAYING);

        // 8. Cleanup
        al.alSourceStop(source[0]);
        al.alSourcei(source[0], AL.AL_BUFFER, 0); // Detach buffer
        al.alDeleteSources(1, source, 0);
        al.alDeleteBuffers(1, buffer, 0);

        System.out.println("Playback finished and resources cleaned up.");
        return AL.AL_TRUE;
    }

    public int loadALData() {
        //variables to load into
        int[] format = new int[1];
        int[] size = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] freq = new int[1];
        int[] loop = new int[1];

        // load wav data into buffers
        al.alGenBuffers(NUM_BUFFERS, buffers, 0);
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        ALut.alutLoadWAVFile(
                NavigationPanel.class.getClassLoader().getResourceAsStream("./assets" + File.separator + "left.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[LEFT],
                format[0],
                data[0],
                size[0],
                freq[0]);
        ALut.alutLoadWAVFile(
                NavigationPanel.class.getClassLoader().getResourceAsStream("./assets" + File.separator + "right.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[RIGHT],
                format[0],
                data[0],
                size[0],
                freq[0]);

        // bind buffers into audio sources
        al.alGenSources(NUM_SOURCES, sources, 0);

        al.alSourcei(sources[LEFT], AL.AL_BUFFER, buffers[LEFT]);
        al.alSourcef(sources[LEFT], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[LEFT], AL.AL_GAIN, 1.0f);

        al.alSourcei(sources[RIGHT], AL.AL_BUFFER, buffers[RIGHT]);
        al.alSourcef(sources[RIGHT], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[RIGHT], AL.AL_GAIN, 1.0f);

        // do another error check and return
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        return AL.AL_TRUE;
    }

    public void setListenerValues() {
        float[] orientation = {
                0f, 0f, -1f, // at
                0f, 1f, 0f   // up
        };
        float[] position = { 0.0f, 0.0f, 0.0f };
        al.alListenerfv(AL.AL_ORIENTATION, FloatBuffer.wrap(orientation));
        al.alListenerfv(AL.AL_POSITION, position, 0);
    }
}
