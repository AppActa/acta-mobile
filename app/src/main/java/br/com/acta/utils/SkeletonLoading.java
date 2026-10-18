package br.com.acta.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.WeakHashMap;
import br.com.acta.Client.RepositoryCallback;
import static android.R.attr.colorBackground;


public final class SkeletonLoading {
    private static final WeakHashMap<View, Placeholder> ACTIVE = new WeakHashMap<>();

    private SkeletonLoading() {}

    public static <T> RepositoryCallback<T> wrap(View root, RepositoryCallback<T> callback) {
        if (root == null || !root.isAttachedToWindow()) {
            return callback;
        }
        Placeholder placeholder = null;
        if (root instanceof ViewGroup) {
            placeholder = ACTIVE.get(root);
            if (placeholder == null) {
                placeholder = new Placeholder((ViewGroup) root);
                ACTIVE.put(root, placeholder);
                placeholder.show();
            }
            placeholder.pending++;
        }
        final Placeholder loading = placeholder;
        return new RepositoryCallback<>() {
            private boolean finished;

            private void finish() {
                if (!finished) {
                    finished = true;
                    if (loading != null && --loading.pending == 0) loading.hide();
                }
            }

            @Override
            public void onSuccess(T value) {
                try {
                    callback.onSuccess(value);
                } finally {
                    finish();
                }
            }

            @Override
            public void onError(int code, String message) {
                try {
                    callback.onError(code, message);
                } finally {
                    finish();
                }
            }
        };
    }

    private static final class Placeholder extends View implements View.OnAttachStateChangeListener {
        private final ViewGroup root;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect bounds = new Rect();
        private final float density;
        private final int background;
        private final View.OnLayoutChangeListener layoutListener;
        private int pending;
        private boolean visible = true;

        Placeholder(ViewGroup root) {
            super(root.getContext());
            this.root = root;
            density = getResources().getDisplayMetrics().density;
            TypedValue color = new TypedValue();
            getContext().getTheme().resolveAttribute(colorBackground, color, true);
            background = color.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && color.type <= TypedValue.TYPE_LAST_COLOR_INT ? color.data : Color.WHITE;
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            layoutListener = (v, l, t, r, b, ol, ot, or, ob) -> layout(0, 0, r - l, b - t);
        }

        void show() {
            layout(0, 0, root.getWidth(), root.getHeight());
            root.getOverlay().add(this);
            root.addOnLayoutChangeListener(layoutListener);
            root.addOnAttachStateChangeListener(this);
        }

        void hide() {
            visible = false;
            root.getOverlay().remove(this);
            root.removeOnLayoutChangeListener(layoutListener);
            root.removeOnAttachStateChangeListener(this);
            if (ACTIVE.get(root) == this) ACTIVE.remove(root);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(background);
            float pulse = (float) (0.5 + 0.5 * Math.sin(SystemClock.uptimeMillis() / 240.0));
            boolean dark = Color.red(background) + Color.green(background) + Color.blue(background) < 384;
            int shade = dark ? (int) (55 + 20 * pulse) : (int) (220 + 15 * pulse);
            paint.setColor(Color.rgb(shade, shade, shade));
            drawChildren(canvas, root);
            if (visible) postInvalidateOnAnimation();
        }

        private void drawChildren(Canvas canvas, View view) {
            if (view.getVisibility() != VISIBLE) return;
            if (view instanceof RecyclerView) {
                bounds.set(0, 0, view.getWidth(), view.getHeight());
                root.offsetDescendantRectToMyCoords(view, bounds);
                float left = bounds.left + 16 * density;
                float right = bounds.right - 16 * density;
                float top = bounds.top + 8 * density;
                float bottom = Math.min(getHeight(), Math.max(bounds.bottom, top + 240 * density));
                for (; top < bottom; top += 112 * density) {
                    block(canvas, left, top, right, Math.min(top + 96 * density, bottom));
                }
            } else if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i++) drawChildren(canvas, group.getChildAt(i));
            } else if (view instanceof TextView || view instanceof ImageView) {
                bounds.set(0, 0, view.getWidth(), view.getHeight());
                root.offsetDescendantRectToMyCoords(view, bounds);
                float inset = view instanceof TextView ? Math.max(0, (bounds.height() - 16 * density) / 2) : 0;
                block(canvas, bounds.left, bounds.top + inset, bounds.right, bounds.bottom - inset);
            }
        }

        private void block(Canvas canvas, float left, float top, float right, float bottom) {
            if (right > left && bottom > top) {
                canvas.drawRoundRect(left, top, right, bottom, 8 * density, 8 * density, paint);
            }
        }

        @Override
        public void onViewAttachedToWindow(View view) {}

        @Override
        public void onViewDetachedFromWindow(View view) {
            hide();
        }
    }
}