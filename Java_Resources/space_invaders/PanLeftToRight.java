import java.io.*;
import java.nio.*;

import com.jogamp.openal.*;
import com.jogamp.openal.util.*;

public class PanLeftToRight {


    static int[] buffer = new int[1];
    static int[] source = new int[1];
    static float[] sourcePos = { 0.0f, 0.0f, 0.0f };
    static float[] sourceVel = { 0.0f, 0.0f, 0.0f };
    static float[] listenerPos = { 0.0f, 0.0f, 0.0f };
    static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
    static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
    static AL al;
    static ALC alc;

    static int loadALData() {
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        int[] format = new int[1];
        int[] size = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] freq = new int[1];
        int[] loop = new int[1];

        // Load wav data into a buffer.
        al.alGenBuffers(1, buffer, 0);
        if (al.alGetError() != AL.AL_NO_ERROR)
            return AL.AL_FALSE;

        ALut.alutLoadWAVFile(
                PanLeftToRight.class.getClassLoader().getResourceAsStream("fastinvader1.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);

        al.alGenSources(1, source, 0);
        al.alSourcei(source[0], AL.AL_SOURCE_RELATIVE, AL.AL_TRUE);

        al.alSourcef(source[0], AL.AL_MAX_DISTANCE, 50.0f);
        al.alSourcef(source[0], AL.AL_REFERENCE_DISTANCE, 5.0f);
        al.alSourcef(source[0], AL.AL_PITCH, 0.5f);
        al.alSourcef(source[0], AL.AL_GAIN, 1.0f);

        al.alGenBuffers(1, buffer, 0);
        al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);

        al.alSourcei(source[0], AL.AL_BUFFER, buffer[0]);
        al.alSourcei(source[0], AL.AL_LOOPING, AL.AL_TRUE);

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

    static void killAllData() {
        al.alDeleteBuffers(1, buffer, 0);
        al.alDeleteSources(1, source, 0);
    }

    public static void main(String[] args) {
        try {
            ALut.alutInit();
            al = ALFactory.getAL();
        } catch (ALException e) {
            e.printStackTrace();
            return;
        }

        if(loadALData() == AL.AL_FALSE) {
            System.exit(1);
        };

        al.alSourcePlay(source[0]);
        long startTime = System.nanoTime();
        float playTimeSeconds = 5.f;
        float maxDistance = 50.f;
        float refDistance = 5.f;
        int count = 0;
        float x = 0.0f;
        while (true) {
            float elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0f; // seconds
            if (elapsed >= playTimeSeconds) break;

            float t = elapsed / playTimeSeconds;
            x = (t * 2.0f - 1.0f) * maxDistance;
            if (count==0) System.out.println("x: " + x);
            count++;
            float[] pos = {x, 0.0f, refDistance}; // offset z to avoid artifacts

            al.alSourcefv(source[0], AL.AL_POSITION, pos, 0);
        }
        System.out.println("x: " + x);

        startTime = System.nanoTime();
        count = 0;
        while (true) {
            float elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0f; // seconds
            if (elapsed >= playTimeSeconds) break;

            float t = elapsed / playTimeSeconds;
            x = (t * 2.0f - 1.0f) * maxDistance*-1;
            float[] pos = {x, 0.0f, refDistance}; // offset z to avoid artifacts
            if (count==0) System.out.println("x: " + x);
            count++;
            al.alSourcefv(source[0], AL.AL_POSITION, pos, 0);
        }
        System.out.println("x: " + x);

        //next let's go front to back
        startTime = System.nanoTime();
        count = 0;
        float y = 0.0f;
        while (true) {
            float elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0f; // seconds
            if (elapsed >= playTimeSeconds) break;

            float t = elapsed / playTimeSeconds;
            y = (t * 2.0f - 1.0f) * maxDistance;
            float[] pos = {0.0f, y, refDistance}; // offset z to avoid artifacts
            if (count==0) System.out.println("y: " + y);
            count++;
            al.alSourcefv(source[0], AL.AL_POSITION, pos, 0);
        }
        System.out.println("y: " + y);

        startTime = System.nanoTime();
        count = 0;
        while (true) {
            float elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0f; // seconds
            if (elapsed >= playTimeSeconds) break;

            float t = elapsed / playTimeSeconds;
            y = (t * 2.0f - 1.0f) * maxDistance*-1;
            float[] pos = {0.0f, y, refDistance}; // offset z to avoid artifacts
            if (count==0) System.out.println("y: " + y);
            count++;
            al.alSourcefv(source[0], AL.AL_POSITION, pos, 0);
        }
        System.out.println("y: " + y);


        System.exit(0);
    }
}