import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

import java.nio.ByteBuffer;


public class SoundModule {

    static final int SHIP = 0;
    static final int BULLET = 1;
    static final int EXPLOSION = 2;

    static final int NUM_BUFFERS = 3;
    static final int NUM_SOURCES = 3;

    static int[] buffers = new int[NUM_BUFFERS];
    static int[] sources = new int[NUM_SOURCES];

    static float[][] sourcePos = new float[NUM_SOURCES][3];
    static float[][] sourceVel = new float[NUM_SOURCES][3];
    static float[] listenerPos = { 0.0f, 0.0f, 0.0f };
    static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
    static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

    static AL al;
    static ALC alc;

    public SoundModule() {
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
        ;

        setListenerValues();
    }

    public void playShipSound() {
        this.al.alSourcePlay(sources[SHIP]);
    }

    public void playShipSound(float[] pos1) {
        this.al.alSourcefv(sources[SHIP], AL.AL_POSITION, pos1, 0);
    }

    public void playBulletSound(float[] pos) {
        this.al.alSourcefv(sources[EXPLOSION], AL.AL_POSITION, pos, 0);
        this.al.alSourcePlay(sources[EXPLOSION]);
    }

    public void playBulletSound() {
        this.al.alSourcePlay(sources[BULLET]);
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
                SpaceInvaders.class.getClassLoader().getResourceAsStream("fastinvader1.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[SHIP],
                format[0],
                data[0],
                size[0],
                freq[0]);

        ALut.alutLoadWAVFile(
                SpaceInvaders.class.getClassLoader().getResourceAsStream("shot.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[BULLET],
                format[0],
                data[0],
                size[0],
                freq[0]);

        ALut.alutLoadWAVFile(
                SpaceInvaders.class.getClassLoader().getResourceAsStream("explosion.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[EXPLOSION],
                format[0],
                data[0],
                size[0],
                freq[0]);

        // bind buffers into audio sources
        al.alGenSources(NUM_SOURCES, sources, 0);

        al.alSourcei(sources[SHIP], AL.AL_BUFFER, buffers[SHIP]);
        al.alSourcef(sources[SHIP], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[SHIP], AL.AL_GAIN, 1.0f);
        al.alSourcefv(sources[SHIP], AL.AL_POSITION, sourcePos[SHIP], 0);
        al.alSourcefv(sources[SHIP], AL.AL_POSITION, sourceVel[SHIP], 0);
        al.alSourcei(sources[SHIP], AL.AL_LOOPING, AL.AL_TRUE);

        al.alSourcei(sources[BULLET], AL.AL_BUFFER, buffers[BULLET]);
        al.alSourcef(sources[BULLET], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[BULLET], AL.AL_GAIN, 0.25f);
        al.alSourcefv(sources[BULLET], AL.AL_POSITION, sourcePos[BULLET], 0);
        al.alSourcefv(sources[BULLET], AL.AL_POSITION, sourceVel[BULLET], 0);
        al.alSourcei(sources[BULLET], AL.AL_LOOPING, AL.AL_FALSE);

        al.alSourcei(sources[EXPLOSION], AL.AL_BUFFER, buffers[EXPLOSION]);
        al.alSourcef(sources[EXPLOSION], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[EXPLOSION], AL.AL_GAIN, 1.00f);
        al.alSourcefv(sources[EXPLOSION], AL.AL_POSITION, sourcePos[EXPLOSION], 0);
        al.alSourcefv(sources[EXPLOSION], AL.AL_POSITION, sourceVel[EXPLOSION], 0);
        al.alSourcei(sources[EXPLOSION], AL.AL_LOOPING, AL.AL_FALSE);

        // do another error check and return
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        return AL.AL_TRUE;
    }

    static void setListenerValues() {
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
    }
}
