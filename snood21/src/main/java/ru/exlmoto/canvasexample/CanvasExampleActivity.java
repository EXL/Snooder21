package ru.exlmoto.canvasexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

public class CanvasExampleActivity extends Activity {

    public static final String APP_DEBUG_TAG = "StackAttack";

    CanvasExampleView mCanvasExampleView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mCanvasExampleView = new CanvasExampleView(this);
        setContentView(mCanvasExampleView);
    }

    public static void toDebug(String message) {
        Log.d(APP_DEBUG_TAG, message);
    }

    private int[] convertCoords(float x, float y) {
        int[] coords = new int[2];
        coords[0] = Math.round(x * CanvasExampleView.ORIGINAL_WIDTH / mCanvasExampleView.getmScreenWidth());
        coords[1] = Math.round(y * CanvasExampleView.ORIGINAL_HEIGHT / mCanvasExampleView.getmScreenHeight());
        return coords;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        int[] coords = convertCoords(event.getX(), event.getY());
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                mCanvasExampleView.setCoords(coords[0], coords[1]);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mCanvasExampleView.setCoords(coords[0], coords[1]);
                break;
            }
        }
        return super.onTouchEvent(event);
    }
}
