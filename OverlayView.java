package com.example.urlcamera;

import android.content.Context;
import android.graphics.*;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private List<Rect> rects = new ArrayList<>();
    private Paint paint;

    public OverlayView(Context context) {
        super(context);

        paint = new Paint();
        paint.setColor(Color.argb(180, 0, 255, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
    }

    public void setRects(List<Rect> rects) {
        this.rects = rects;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Rect rect : rects) {
            canvas.drawRect(rect, paint);
        }
    }
}
