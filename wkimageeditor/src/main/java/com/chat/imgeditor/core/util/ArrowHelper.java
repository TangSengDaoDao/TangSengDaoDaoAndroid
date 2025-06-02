package com.chat.imgeditor.core.util;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;

public class ArrowHelper {
    Matrix matrix = new Matrix();
    Path path = new Path();

    float arrowDegrees = (float) (Math.PI / 6);  //箭头的角度
    int arrowLength = 80; //箭头的长度
    float arrowRatio = 0.5f; //箭柄占箭头宽度的百分比

    public ArrowHelper() {

    }

    public ArrowHelper(float arrowDegrees, int arrowLength, float arrowRatio) {
        this.arrowDegrees = arrowDegrees;
        this.arrowLength = arrowLength;
        this.arrowRatio = arrowRatio;
    }

    public Path buildPath(PointF p1, PointF p2) {
        matrix.reset();

        path.reset();
        //计算两点之间形成的直线与X轴的夹角
        float degrees = 0;
        //对特殊情况进行判断
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        int Dx = (int) Math.sqrt(dy * dy + dx * dx);

        if(Dx<30){
            return new Path();
        }


        if (dx == 0) {
            if (dy > 0) {
                degrees = (float) (Math.PI / 4);
            } else {
                degrees = (float) (Math.PI * 3 / 4);
            }
        } else {
            degrees = (float) Math.atan(dy / Math.abs(dx));
        }

        //因为反正切函数只能返回-PI/2 到 PI/2 所以需要自己处理角度
        if (dx < 0) {
            degrees = (float) (Math.PI - degrees);
        }
        float dushu = (float) (180 * (degrees / Math.PI));


        path.moveTo(0, 0);


        float bx = (float) (Dx - arrowLength * Math.cos(arrowDegrees));
        float by = (float) (arrowRatio * arrowLength * Math.sin(arrowDegrees));
        path.lineTo(bx, by);


        float cx = bx;
        float cy = (float) (arrowLength * Math.sin(arrowDegrees));

        path.lineTo(cx, cy);


        path.lineTo(Dx, 0);

        float ex = bx;
        float ey = -cy;
        path.lineTo(ex, ey);

        float fx = bx;
        float fy = -by;

        path.lineTo(fx, fy);

        path.lineTo(0, 0);


        matrix.setRotate(dushu);
        matrix.postTranslate(p1.x, p1.y);

        path.transform(matrix);

        return new Path(path);
    }

}
