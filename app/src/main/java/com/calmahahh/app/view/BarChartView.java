package com.calmahahh.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simple bar chart view for displaying daily calorie intake.
 * Supports showing target line and color coding per bar.
 */
public class BarChartView extends View {

    private static final int COLOR_BAR_NORMAL = 0xFF4CAF50;
    private static final int COLOR_BAR_OVER = 0xFFF44336;
    private static final int COLOR_BAR_UNDER = 0xFF2196F3;
    private static final int COLOR_TARGET_LINE = 0xFFFF9800;
    private static final int COLOR_AXIS = 0xFF757575;
    private static final int COLOR_LABEL = 0xFF757575;
    private static final int COLOR_VALUE = 0xFF212121;

    private Paint paintBar, paintTargetLine, paintAxis, paintLabel, paintValue;

    private final List<BarData> bars = new ArrayList<>();
    private double targetValue = 0;
    private double maxValue = 0;

    public static class BarData {
        public String label;
        public double value;

        public BarData(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintBar = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBar.setStyle(Paint.Style.FILL);

        paintTargetLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTargetLine.setColor(COLOR_TARGET_LINE);
        paintTargetLine.setStrokeWidth(dpToPx(2));
        paintTargetLine.setStyle(Paint.Style.STROKE);
        paintTargetLine.setPathEffect(new android.graphics.DashPathEffect(
                new float[]{dpToPx(6), dpToPx(4)}, 0));

        paintAxis = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAxis.setColor(COLOR_AXIS);
        paintAxis.setStrokeWidth(dpToPx(1));

        paintLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLabel.setColor(COLOR_LABEL);
        paintLabel.setTextSize(dpToPx(10));
        paintLabel.setTextAlign(Paint.Align.CENTER);

        paintValue = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintValue.setColor(COLOR_VALUE);
        paintValue.setTextSize(dpToPx(9));
        paintValue.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<BarData> data, double target) {
        bars.clear();
        bars.addAll(data);
        this.targetValue = target;
        this.maxValue = target;
        for (BarData b : bars) {
            if (b.value > maxValue) maxValue = b.value;
        }
        maxValue *= 1.15; // 15% headroom
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) dpToPx(180);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty() || maxValue <= 0) return;

        float paddingLeft = dpToPx(36);
        float paddingRight = dpToPx(8);
        float paddingTop = dpToPx(16);
        float paddingBottom = dpToPx(24);

        float chartWidth = getWidth() - paddingLeft - paddingRight;
        float chartHeight = getHeight() - paddingTop - paddingBottom;

        float barWidth = chartWidth / bars.size() * 0.65f;
        float gap = chartWidth / bars.size() * 0.35f;

        // Draw axis
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight, paintAxis);
        canvas.drawLine(paddingLeft, paddingTop + chartHeight,
                getWidth() - paddingRight, paddingTop + chartHeight, paintAxis);

        // Draw bars
        for (int i = 0; i < bars.size(); i++) {
            BarData bar = bars.get(i);
            float barHeight = (float) (bar.value / maxValue * chartHeight);
            float x = paddingLeft + i * (barWidth + gap) + gap / 2f;
            float top = paddingTop + chartHeight - barHeight;

            // Color based on target
            if (targetValue > 0 && bar.value > targetValue * 1.1) {
                paintBar.setColor(COLOR_BAR_OVER);
            } else if (targetValue > 0 && bar.value < targetValue * 0.9 && bar.value > 0) {
                paintBar.setColor(COLOR_BAR_UNDER);
            } else {
                paintBar.setColor(COLOR_BAR_NORMAL);
            }

            RectF rect = new RectF(x, top, x + barWidth, paddingTop + chartHeight);
            canvas.drawRoundRect(rect, dpToPx(3), dpToPx(3), paintBar);

            // Value on top
            if (bar.value > 0) {
                canvas.drawText(String.format(Locale.US, "%.0f", bar.value),
                        x + barWidth / 2f, top - dpToPx(3), paintValue);
            }

            // Label below
            canvas.drawText(bar.label, x + barWidth / 2f,
                    paddingTop + chartHeight + dpToPx(14), paintLabel);
        }

        // Draw target line
        if (targetValue > 0) {
            float targetY = (float) (paddingTop + chartHeight - (targetValue / maxValue * chartHeight));
            canvas.drawLine(paddingLeft, targetY, getWidth() - paddingRight, targetY, paintTargetLine);

            // Target label
            Paint targetLabelPaint = new Paint(paintLabel);
            targetLabelPaint.setColor(COLOR_TARGET_LINE);
            targetLabelPaint.setTextSize(dpToPx(9));
            targetLabelPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Goal", paddingLeft + dpToPx(2), targetY - dpToPx(3), targetLabelPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
