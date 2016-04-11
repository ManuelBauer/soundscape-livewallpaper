package net.manuelbauer.soundscape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Vector;

public class Wave {

    private Paint paint = new Paint();
    private Path path = new Path();
    private float firstDrawX = 0f;
    private float firstDrawY = 0f;
    private float offset = 50f;

    private int stackSize = 20;
    private float timePerValue;

    private Vector<Float> amplitudes = new Vector<>();

    public Wave(Paint paint, float offset, int stackSize, float timePerValue) {
        this.paint = paint;
        this.timePerValue = timePerValue;
        this.stackSize = stackSize;

        this.offset = offset;

        for (int i = 0; i < this.stackSize; i++) {
            this.amplitudes.add(1f);
        }
    }

    public void draw(Canvas canvas, float width, float height, float drawTimeDelta) {
        float prevX = firstDrawX;
        float prevY = firstDrawY;

        int amplitudesSize = amplitudes.size();

        int DRAW_OFFSET = 2;
        float xCount = amplitudesSize - 1;

        float widthSlice = width / (xCount - DRAW_OFFSET*2);

        path.reset();

        path.moveTo(-(widthSlice)*2, height);

        for (int i = 0; i < xCount; i++) {

            float x = (widthSlice * i) - (widthSlice * (drawTimeDelta / timePerValue));
            float y = (float) (height - (Math.log(amplitudes.get(i)) * this.offset));

            if(i==0) firstDrawX = x;
            if(i==0) firstDrawY = y;

            float pathX = (prevX + x) / 2;
            float pathY = (prevY + y) / 2;

            path.quadTo(prevX, prevY, pathX, pathY);

            prevX = x;
            prevY = y;
        }

        path.quadTo(prevX, prevY, width + widthSlice * 2, height);
        path.close();

        canvas.drawPath(path, paint);
    }

    public void addAmplitude(float a) {
        this.amplitudes.add(a);
        this.amplitudes.remove(0);
    }

    public void setPaint(Paint p) {
        this.paint = p;
    }
}
