package ru.exlmoto.snood21;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;

import ru.exlmoto.snood21.SnoodsLauncherActivity.SnoodsSettings;

public class SnoodsGameActivity extends Activity {

    public static final String APP_DEBUG_TAG = "Snood21";

    private SnoodsSurfaceView mSnoodsSurfaceView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (SnoodsSettings.sound) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        mSnoodsSurfaceView = new SnoodsSurfaceView(this, this);
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
        if (!mSnoodsSurfaceView.mIsDropingCard &&
                !mSnoodsSurfaceView.mDeckIsEmpty &&
                !mSnoodsSurfaceView.mIsDropingColumn &&
                !mSnoodsSurfaceView.mIsWinAnimation) {
            int[] winCoordinates = new int[2];
            mSnoodsSurfaceView.getLocationInWindow(winCoordinates);
            int actionMasked = event.getActionMasked();
            int x = convertX(event.getRawX()) - winCoordinates[0];
            int y = convertY(event.getRawY()) - winCoordinates[1];
            int x_c = x - SnoodsSurfaceView.mX_card_grab_coord;
            int y_c = y - SnoodsSurfaceView.mY_card_grab_coord;
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN: {
                    mSnoodsSurfaceView.touchInDeckRect(x, y);
                    int inColumnRect = mSnoodsSurfaceView.detectColumn(x, y);
                    if (mSnoodsSurfaceView.mIsGrab) {
                        mSnoodsSurfaceView.setCardCoords(x_c, y_c);
                    } else if (inColumnRect > 0) {
                        mSnoodsSurfaceView.setHighlightColumn(inColumnRect);
                        mSnoodsSurfaceView.putCardToColumn(x_c, y_c);
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
                    mSnoodsSurfaceView.mPlayingGrabSound = true;
                    if (mSnoodsSurfaceView.mIsGrab) {
                        mSnoodsSurfaceView.putCardToColumn(x_c, y_c);
                    }
                    break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void showToast(final String text, final int delay) {
        final SnoodsGameActivity activity = this;
        activity.runOnUiThread(new Runnable() {

            public void run() {
                if (SnoodsSettings.showToasts) {
                    Toast.makeText(activity, text, delay).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        mSnoodsSurfaceView.getSnoodsScoreManager().checkHighScore(mSnoodsSurfaceView.getScores());
        super.onBackPressed();
    }
}
