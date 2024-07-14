package com.example.sae401;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class TextAnimator {

    public static void animateText( TextView textView,  String text) {
        Handler handler = new Handler();
        int length = text.length();
        textView.setText(""); // Clear the text initially
        Log.d("texte dans fonction",text);
        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i < length) {
                    char currentChar = text.charAt(i);
                    // Log the current character for debugging encoding issues
                    Log.d("TextAnimator", "Current character: " + currentChar);
                    textView.append(String.valueOf(currentChar));
                    i++;
                    handler.postDelayed(this, 5); // Delay for 50 milliseconds
                }
            }
        });
    }
}