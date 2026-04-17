public class OverlayView extends View {

    private List<Rect> rects = new ArrayList<>();
    private Rect centerRect;

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

    public void setCenterRect(Rect rect) {
        this.centerRect = rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Rect rect : rects) {
            canvas.drawRect(rect, paint);
        }

        if (centerRect != null) {
            Paint guide = new Paint();
            guide.setColor(Color.argb(120, 255, 255, 255));
            guide.setStyle(Paint.Style.STROKE);
            guide.setStrokeWidth(4f);

            canvas.drawRect(centerRect, guide);
        }
    }
}
