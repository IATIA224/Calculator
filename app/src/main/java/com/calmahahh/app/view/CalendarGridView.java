package com.calmahahh.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Custom calendar view that displays days colored by calorie status:
 * Green = within goal, Red = exceeded, Blue = deficit, Gray = no data.
 */
public class CalendarGridView extends View {

    public interface OnDateClickListener {
        void onDateClick(String date); // yyyy-MM-dd
    }

    private static final String[] DAY_LABELS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final int COLS = 7;

    // Colors
    private static final int COLOR_GREEN = 0xFF4CAF50;   // within goal
    private static final int COLOR_RED = 0xFFF44336;     // exceeded
    private static final int COLOR_BLUE = 0xFF2196F3;    // deficit
    private static final int COLOR_EMPTY = 0xFFE0E0E0;   // no data
    private static final int COLOR_TODAY = 0xFF388E3C;    // today border
    private static final int COLOR_SELECTED = 0xFFFF9800; // selected border
    private static final int COLOR_HEADER = 0xFF757575;
    private static final int COLOR_TEXT_DARK = 0xFF212121;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_OTHER_MONTH = 0xFFBDBDBD;

    private Paint paintDayBg, paintText, paintHeader, paintBorder, paintCalText;
    private Calendar displayMonth;
    private Calendar today;
    private String selectedDate;
    private int targetCalories = 2000;

    // date -> totalCalories for the displayed month
    private Map<String, Double> calorieData = new HashMap<>();

    private OnDateClickListener listener;
    private GestureDetector gestureDetector;

    private float cellWidth, cellHeight;
    private int rows;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public CalendarGridView(Context context) {
        super(context);
        init();
    }

    public CalendarGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        displayMonth = Calendar.getInstance();
        today = Calendar.getInstance();

        paintDayBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDayBg.setStyle(Paint.Style.FILL);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintHeader = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHeader.setTextAlign(Paint.Align.CENTER);
        paintHeader.setColor(COLOR_HEADER);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(4f);

        paintCalText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCalText.setTextAlign(Paint.Align.CENTER);

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handleTap(e.getX(), e.getY());
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX();
                if (Math.abs(dx) > 100) {
                    if (dx > 0) previousMonth();
                    else nextMonth();
                    return true;
                }
                return false;
            }
        });

        calculateRows();
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.listener = listener;
    }

    public void setTargetCalories(int target) {
        this.targetCalories = target;
        invalidate();
    }

    public void setCalorieData(Map<String, Double> data) {
        this.calorieData = data != null ? data : new HashMap<>();
        invalidate();
    }

    public void setSelectedDate(String date) {
        this.selectedDate = date;
        invalidate();
    }

    public void setDisplayMonth(int year, int month) {
        displayMonth.set(Calendar.YEAR, year);
        displayMonth.set(Calendar.MONTH, month);
        calculateRows();
        invalidate();
    }

    public void nextMonth() {
        displayMonth.add(Calendar.MONTH, 1);
        calculateRows();
        invalidate();
    }

    public void previousMonth() {
        displayMonth.add(Calendar.MONTH, -1);
        calculateRows();
        invalidate();
    }

    public String getDisplayMonthLabel() {
        return new SimpleDateFormat("MMMM yyyy", Locale.US).format(displayMonth.getTime());
    }

    public int getDisplayYear() { return displayMonth.get(Calendar.YEAR); }
    public int getDisplayMonthIndex() { return displayMonth.get(Calendar.MONTH); }

    private void calculateRows() {
        Calendar cal = (Calendar) displayMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        rows = (int) Math.ceil((firstDayOfWeek + daysInMonth) / 7.0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float headerHeight = dpToPx(28);
        cellWidth = width / (float) COLS;
        cellHeight = cellWidth * 0.85f;
        int height = (int) (headerHeight + rows * cellHeight + dpToPx(8));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float headerHeight = dpToPx(28);
        cellWidth = getWidth() / (float) COLS;
        cellHeight = (getHeight() - headerHeight - dpToPx(8)) / (float) rows;

        // Draw day-of-week headers
        paintHeader.setTextSize(dpToPx(12));
        for (int c = 0; c < COLS; c++) {
            float x = c * cellWidth + cellWidth / 2f;
            float y = dpToPx(16);
            canvas.drawText(DAY_LABELS[c], x, y, paintHeader);
        }

        // Draw day cells
        Calendar cal = (Calendar) displayMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        paintText.setTextSize(dpToPx(13));
        paintCalText.setTextSize(dpToPx(8));

        for (int day = 1; day <= daysInMonth; day++) {
            int pos = firstDayOfWeek + day - 1;
            int row = pos / COLS;
            int col = pos % COLS;

            float x = col * cellWidth;
            float y = headerHeight + row * cellHeight;

            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = dateFormat.format(cal.getTime());

            // Determine color
            RectF rect = new RectF(x + 3, y + 3, x + cellWidth - 3, y + cellHeight - 3);
            Double cals = calorieData.get(dateStr);

            if (cals != null && cals > 0) {
                double ratio = cals / targetCalories;
                if (ratio > 1.1) {
                    paintDayBg.setColor(COLOR_RED);
                } else if (ratio < 0.9) {
                    paintDayBg.setColor(COLOR_BLUE);
                } else {
                    paintDayBg.setColor(COLOR_GREEN);
                }
            } else {
                paintDayBg.setColor(COLOR_EMPTY);
            }

            canvas.drawRoundRect(rect, dpToPx(6), dpToPx(6), paintDayBg);

            // Today border
            boolean isToday = today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == day;
            if (isToday) {
                paintBorder.setColor(COLOR_TODAY);
                paintBorder.setStrokeWidth(dpToPx(2));
                canvas.drawRoundRect(rect, dpToPx(6), dpToPx(6), paintBorder);
            }

            // Selected border
            if (dateStr.equals(selectedDate)) {
                paintBorder.setColor(COLOR_SELECTED);
                paintBorder.setStrokeWidth(dpToPx(2.5f));
                canvas.drawRoundRect(rect, dpToPx(6), dpToPx(6), paintBorder);
            }

            // Day number
            boolean hasData = cals != null && cals > 0;
            paintText.setColor(hasData ? COLOR_TEXT_LIGHT : COLOR_TEXT_DARK);
            float textY = y + cellHeight / 2f - dpToPx(2);
            canvas.drawText(String.valueOf(day), x + cellWidth / 2f, textY, paintText);

            // Small calorie text under day number
            if (hasData) {
                paintCalText.setColor(0xCCFFFFFF);
                canvas.drawText(String.format(Locale.US, "%.0f", cals),
                        x + cellWidth / 2f, textY + dpToPx(12), paintCalText);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private void handleTap(float tx, float ty) {
        float headerHeight = dpToPx(28);
        if (ty < headerHeight) return;

        int col = (int) (tx / cellWidth);
        int row = (int) ((ty - headerHeight) / cellHeight);

        Calendar cal = (Calendar) displayMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int day = row * COLS + col - firstDayOfWeek + 1;
        if (day >= 1 && day <= daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = dateFormat.format(cal.getTime());
            selectedDate = dateStr;
            invalidate();
            if (listener != null) {
                listener.onDateClick(dateStr);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
