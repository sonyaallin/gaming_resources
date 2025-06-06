import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class SoundModuleAdventure {

    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int CAT = 2;

    static final int NUM_BUFFERS = 3;
    static final int NUM_SOURCES = 3;

    static int[] buffers = new int[NUM_BUFFERS];
    static int[] sources = new int[NUM_SOURCES];

    static float[][] sourcePos = new float[NUM_SOURCES][3];
    static float[][] sourceVel = new float[NUM_SOURCES][3];
    static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
    static float[] listenerOri = { 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f }; //Z offset

    static AL al;
    static ALC alc;

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

    }
//
//    private void setCatPosition() {
//
//        this.al.alSource3f(sources[CAT], AL.AL_POSITION, 0f, 0f, -1f);
//
//        this.al.alSourcei(sources[CAT], AL.AL_LOOPING, AL.AL_TRUE);
//
//        this.al.alSourcef(sources[CAT], AL.AL_GAIN, 1.0f);
//        this.al.alSourcef(sources[CAT], AL.AL_PITCH, 1.0f);
//
//        this.al.alListener3f(AL.AL_POSITION, 0f, 0f, 0f);
//
//        System.out.println("Playing MEOW");
//    }

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

        System.out.println(orientation[0] + " " + orientation[1] + " " + orientation[2]);
        al.alListenerfv(AL.AL_ORIENTATION, orientation, 0);

//        this.al.alSourcefv(sources[CAT], AL.AL_POSITION, orientation, 0);
    }


    static int loadALData() {
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
                RotatingImage.class.getClassLoader().getResourceAsStream("left.wav"),
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
                RotatingImage.class.getClassLoader().getResourceAsStream("right.wav"),
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

        ALut.alutLoadWAVFile(
                RotatingImage.class.getClassLoader().getResourceAsStream("meow.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[CAT],
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

        al.alSourcei(sources[CAT], AL.AL_BUFFER, buffers[CAT]);
        al.alSourcef(sources[CAT], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[CAT], AL.AL_GAIN, 1.0f);

        sourcePos[CAT][0] = 0.0f;
        sourcePos[CAT][0] = 0.0f;
        sourcePos[CAT][0] = -3.0f;
        al.alSourcefv(sources[CAT], AL.AL_POSITION, sourcePos[CAT], 0);
        al.alSourcei(sources[CAT], AL.AL_LOOPING, AL.AL_TRUE);

//        float[] orientation = {0f, 0f, -1f, 0f, 1f, 0f}; // Facing negative Z
//        al.alListenerfv(AL.AL_ORIENTATION, FloatBuffer.wrap(orientation));
        al.alSourcePlay(sources[CAT]);

        // do another error check and return
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        return AL.AL_TRUE;
    }

    static void setListenerValues() {
        float[] orientation = {
                0f, 0f, -1f, // at
                0f, 1f, 0f   // up
        };
        float[] position = { 0.0f, 0.0f, 0.0f };
        al.alListenerfv(AL.AL_ORIENTATION, FloatBuffer.wrap(orientation));
        al.alListenerfv(AL.AL_POSITION, position, 0);
    }
}
