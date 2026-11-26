package com.example.kaamkaaj;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.color.MaterialColors;

/** Simple donut chart with a label in the middle. */
public class DonutView extends View {
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float[] values = new float[0];
    private int[] colors = new int[]{0xFF5B5FEF};
    private String center = "";

    public DonutView(Context c) {
        this(c, null);
    }

    public DonutView(Context c, AttributeSet a) {
        super(c, a);
        arc.setStyle(Paint.Style.STROKE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
    }

    void setData(float[] v, int[] c, String centerText) {
        values = v;
        colors = c;
        center = centerText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float size = Math.min(w, h);
        float stroke = size * 0.16f;
        float pad = stroke / 2f + 4f;
        arc.setStrokeWidth(stroke);
        rect.set((w - size) / 2f + pad, (h - size) / 2f + pad, (w + size) / 2f - pad, (h + size) / 2f - pad);

        float total = 0;
        for (float v : values) total += v;
        if (total <= 0) {
            arc.setColor(0x33888888);
            canvas.drawArc(rect, 0, 360, false, arc);
        } else {
            float start = -90;
            for (int i = 0; i < values.length; i++) {
                float sweep = 360f * values[i] / total;
                arc.setColor(colors[i % colors.length]);
                canvas.drawArc(rect, start, Math.max(0f, sweep - 2f), false, arc);
                start += sweep;
            }
        }
        text.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface));
        text.setTextSize(size * 0.12f);
        canvas.drawText(center, w / 2f, h / 2f + text.getTextSize() / 3f, text);
    }
}
