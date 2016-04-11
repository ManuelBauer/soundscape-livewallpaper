package net.manuelbauer.soundscape;

import android.app.UiModeManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.SurfaceHolder;

public class MainWallpaperService extends WallpaperService {
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public Engine onCreateEngine() {
        return new SoundWaveWallpaperEngine ();
    }

    private class SoundWaveWallpaperEngine extends Engine {

        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();

            }
        };

        private boolean visible;
        private float width;
        private float height;
        private long drawStartTime;
        private long drawTimeDelta;
        private float timePerValue = 500f;

        private Wave wave1;
        private Wave wave2;
        private Wave wave3;

        private int waveColor;
        private int bgGradientColor1;
        private int bgGradientColor2;
        private Paint bgPaint = new Paint();

        public SoundWaveWallpaperEngine() {

            // Enable automatic NightMode switch
            UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);

            Paint wavePaint = new Paint();
            wavePaint.setColor(waveColor);
            wavePaint.setAlpha(150);

            // Define all three waves
            wave1 = new Wave(wavePaint, 50, 15, timePerValue);
            wave2 = new Wave(wavePaint, 35, 20, timePerValue);
            wave3 = new Wave(wavePaint, 25, 25, timePerValue);

            handler.post(drawRunner);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                waveColor = ContextCompat.getColor(MainWallpaperService.this, R.color.wave);
                bgGradientColor1 = ContextCompat.getColor(MainWallpaperService.this, R.color.bgGradient1);
                bgGradientColor2 = ContextCompat.getColor(MainWallpaperService.this, R.color.bgGradient2);

                Paint wavePaint = new Paint();
                wavePaint.setColor(waveColor);
                wavePaint.setAlpha(150);
                wavePaint.setAntiAlias(true);
                wave1.setPaint(wavePaint);
                wave2.setPaint(wavePaint);
                wave3.setPaint(wavePaint);

                bgPaint = new Paint();
                bgPaint.setShader(new LinearGradient(
                        0, height, width, 0,
                        new int[] { bgGradientColor1, bgGradientColor2},

                        new float[] {0f,1f}, Shader.TileMode.MIRROR
                ));

                handler.post(drawRunner);
                SoundWaveGenerator.start(timePerValue);
            } else {
                handler.removeCallbacks(drawRunner);
                SoundWaveGenerator.close();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            this.width = width;
            this.height = height;
            super.onSurfaceChanged(holder, format, width, height);
        }

        private void drawBackground(Canvas canvas) {

            // Draw background gradient
            canvas.drawPaint(bgPaint);

            // Draw sun / moon
            Paint circle = new Paint();
            circle.setAntiAlias(true);
            circle.setColor(Color.WHITE);
            circle.setAlpha(255);

            float circleX = width*.8f;
            float circleY = height*0.3f;

            canvas.drawCircle(circleX, circleY, width/10f, circle);
            for (int i = 0; i < 5; i++) {
                circle.setAlpha(75 - 75/10*i);
                canvas.drawCircle(circleX, circleY, width/10f*i, circle);
            }

            // Draw mountains
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAlpha(50);

            Path m1 = new Path();
            m1.moveTo(-(width*.5f), height);
            m1.lineTo(width*.2f, height * 0.4f);
            m1.lineTo(width*.9f, height);
            m1.close();
            canvas.drawPath(m1, paint);

            Path m2 = new Path();
            m2.moveTo(0, height);
            m2.lineTo(width*.5f, height * 0.5f);
            m2.lineTo(width, height);
            m2.close();
            canvas.drawPath(m2, paint);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {

                    if(((drawStartTime - System.nanoTime()) / 1000000) + timePerValue < 0f) {
                        drawStartTime = System.nanoTime();

                        wave1.addAmplitude(SoundWaveGenerator.getLastAvg());
                        wave2.addAmplitude(SoundWaveGenerator.getLastAvg());
                        wave3.addAmplitude(SoundWaveGenerator.getLastAvg());
                    }

                    drawTimeDelta = (System.nanoTime() - drawStartTime) / 1000000;

                    drawBackground(canvas);

                    // 1st wave
                    wave1.draw(canvas, width, height, drawTimeDelta);

                    // 2nd wave
                    wave2.draw(canvas, width, height, drawTimeDelta);

                    // 3rd wave
                    wave3.draw(canvas, width, height, drawTimeDelta);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }

            }

            handler.removeCallbacks(drawRunner);
            if (visible) {
                // Draw 60 frames per second
                handler.postDelayed(drawRunner, 1000/60);
            }
        }
    }
}