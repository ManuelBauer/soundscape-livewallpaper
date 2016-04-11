package net.manuelbauer.soundscape;

import android.media.MediaRecorder;
import android.util.Log;

import java.util.Vector;

public class SoundWaveGenerator {

    private static MediaRecorder mRecorder;
    private static boolean isActive = false;
    private static Vector<Float> mTempCalcAmplitudes = new Vector<>();
    private static final int SAMPLE_COUNT = 5;
    private static Thread recordThread;

    private static float lastAvg = 1f;


    private SoundWaveGenerator() {
    }

    public static void start(final float timePerValue) {
        Log.d("SoundWaveGenerator", "Start called");

        if(mRecorder != null || isActive) {
            return;
        }

        try {
            // Prepare audio recorder
            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            mRecorder.prepare();
            mRecorder.start();

            if(recordThread != null) {
                isActive = false;
                recordThread.join(500);
            }

            isActive = true;

            recordThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isActive) {
                        float a = getAmplitude();

                        if(mTempCalcAmplitudes.size() < SAMPLE_COUNT) {
                            mTempCalcAmplitudes.add(a);
                        } else {
                            float avg = 0;

                            int count = mTempCalcAmplitudes.size();
                            for (int i = 0; i < count; i++) {
                                avg += mTempCalcAmplitudes.get(i);
                            }

                            avg = avg / count;

                            if(avg == 0) {
                                avg = 1;
                            }

                            lastAvg = avg;
                            mTempCalcAmplitudes.clear();
                        }

                        try {
                            Thread.sleep((long)timePerValue/SAMPLE_COUNT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            recordThread.start();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        Log.d("SoundWaveGenerator", "Stop called");
        if(mRecorder != null && isActive) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isActive = false;
            mRecorder = null;
        }
    }

    private static float getAmplitude() {
        if (mRecorder != null && isActive) {
            return (mRecorder.getMaxAmplitude());
        } else {
            return 0;
        }
    }

    public static float getLastAvg() {
        return lastAvg;
    }
}
