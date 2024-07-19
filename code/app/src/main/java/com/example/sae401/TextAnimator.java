package com.example.sae401;

import android.os.Handler;
import android.widget.TextView;

public class TextAnimator {

    public static void animateText(TextView textView, String text) {
        Handler handler = new Handler();
        textView.setText("");
        int delay = 50; // Délai en ms entre chaque caractère

        for (int i = 0; i < text.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                textView.append(String.valueOf(text.charAt(index)));
            }, delay * i);
        }
    }
}
