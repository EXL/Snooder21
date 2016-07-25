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

    public static int convertX(float x) {
        return Math.round(x * SnoodsSurfaceView.ORIGINAL_WIDTH / SnoodsSurfaceView.getmScreenWidth());
    }

    public static int convertY(float y) {
        return Math.round(y * SnoodsSurfaceView.ORIGINAL_HEIGHT / SnoodsSurfaceView.getmScreenHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int[] win_coords = new int[2];
        mSnoodsSurfaceView.getLocationInWindow(win_coords);
        int actionMasked = event.getActionMasked();
        int x = convertX(event.getRawX()) - win_coords[0];
        int y = convertY(event.getRawY()) - win_coords[1];
        int x_c = x - SnoodsSurfaceView.mX_card_conv_coord;
        int y_c = y - SnoodsSurfaceView.mY_card_conv_coord;
        if (!mSnoodsSurfaceView.mIsDropingCard && !mSnoodsSurfaceView.mDeckIsEmpty && !mSnoodsSurfaceView.mIsDropingColumn) {
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN: {
                    mSnoodsSurfaceView.touchInDeckRect(x, y);
                    if (mSnoodsSurfaceView.mIsGrab) {
                        mSnoodsSurfaceView.setCardCoords(x_c, y_c);
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (mSnoodsSurfaceView.mIsGrab) {
                        mSnoodsSurfaceView.setCardCoords(x_c, y_c);
                        mSnoodsSurfaceView.setHighlightColumn(mSnoodsSurfaceView.detectColumn(x, y));
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (mSnoodsSurfaceView.mIsGrab) {
                        mSnoodsSurfaceView.putCardToColumn(x_c, y_c);
                    }
                    break;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
