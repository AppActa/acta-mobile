package br.com.acta;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class KeyboardInsets {
    private KeyboardInsets() {}

    public static void apply(View root) {
        apply(root, true);
    }

    public static void apply(View root, boolean includeTopSystemBar) {
        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, keyboard.bottom);

            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + (includeTopSystemBar ? systemBars.top : 0),
                    initialRight + systemBars.right,
                    initialBottom + bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}