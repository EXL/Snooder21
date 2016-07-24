package ru.exlmoto.snood21;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

public class SnoodsActivity extends Activity {

    public static final String APP_DEBUG_TAG = "Snood21";

    SnoodsSurfaceView mSnoodsSurfaceView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mSnoodsSurfaceView = new SnoodsSurfaceView(this);
        setContentView(mSnoodsSurfaceView);
    }

    public static void toDebug(String message) {
        Log.d(APP_DEBUG_TAG, message);
    }

    private int[] convertCoords(float x, float y) {
        int[] coords = new int[2];
        coords[0] = Math.round(x * SnoodsSurfaceView.ORIGINAL_WIDTH / mSnoodsSurfaceView.getmScreenWidth());
        coords[1] = Math.round(y * SnoodsSurfaceView.ORIGINAL_HEIGHT / mSnoodsSurfaceView.getmScreenHeight());
        return coords;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        int[] coords = convertCoords(event.getX(), event.getY());
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                mSnoodsSurfaceView.setCoords(coords[0], coords[1]);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mSnoodsSurfaceView.setCoords(coords[0], coords[1]);
                break;
            }
        }
        return super.onTouchEvent(event);
    }
}
