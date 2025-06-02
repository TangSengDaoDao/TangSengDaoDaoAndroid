package com.chat.imgeditor.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

/**
 * Created by felix on 2017/11/22 下午6:13.
 */

public class IMGPath {

    protected Path path;
    protected PointF firstPoint = new PointF();
    protected PointF lastPoint = new PointF();

    private int color = Color.RED;

    private float width = BASE_MOSAIC_WIDTH;

    private IMGMode mode = IMGMode.DOODLE;

    public static final float BASE_DOODLE_WIDTH = 10f;

    public static final float BASE_MOSAIC_WIDTH = 72f;

    public IMGPath() {
        this(new Path());
    }

    public IMGPath(Path path) {
        this(path, IMGMode.DOODLE);
    }

    public IMGPath(Path path, IMGMode mode) {
        this(path, mode, Color.RED);
    }

    public IMGPath(Path path, IMGMode mode, int color) {
        this(path, mode, color, BASE_MOSAIC_WIDTH);
    }

    public IMGPath(Path path, IMGMode mode, int color, float width) {
        this.path = path;
        this.mode = mode;
        this.color = color;
        this.width = width;
        if (mode == IMGMode.MOSAIC) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }



    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public IMGMode getMode() {
        return mode;
    }

    public void setMode(IMGMode mode) {
        this.mode = mode;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public void onDrawDoodle(Canvas canvas, Paint paint) {
        if (mode == IMGMode.DOODLE) {
            paint.setColor(color);
            // rewind
            paint.setStrokeWidth(width);
            canvas.drawPath(path, paint);
        }
    }


    public void onDrawBox(Canvas canvas, Paint paint) {
        if (mode == IMGMode.BOX) {
            paint.setColor(color);
            // rewind
            paint.setStrokeWidth(width);
//            canvas.drawPath(path, paint);
            canvas.drawPath(path, paint);

        }
    }

    public void onDrawMosaic(Canvas canvas, Paint paint) {
        if (mode == IMGMode.MOSAIC) {
            paint.setStrokeWidth(width);
            canvas.drawPath(path, paint);
        }
    }

    public void transform(Matrix matrix) {
        path.transform(matrix);
    }

    public void onDrawRound(Canvas canvas, Paint paint) {
        if (mode == IMGMode.ROUND) {
            paint.setColor(color);
            // rewind
            paint.setStrokeWidth(width);

            canvas.drawPath(path,paint);


        }
    }
    public void onDrawArrow(Canvas canvas, Paint paint) {
        if (mode == IMGMode.ARROW) {
            paint.setColor(color);
            // rewind
            paint.setStrokeWidth(width);

            canvas.drawPath(path,paint);


        }
    }
}
