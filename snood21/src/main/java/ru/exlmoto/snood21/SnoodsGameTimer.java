package ru.exlmoto.snood21;

import android.os.CountDownTimer;
import android.widget.Toast;

public class SnoodsGameTimer extends CountDownTimer {

    private float dec = 0;

    private SnoodsSurfaceView snoodsSurfaceView = null;
    private SnoodsGameActivity snoodsGameActivity = null;

    public SnoodsGameTimer(long millisInFuture,
                           long countDownInterval,
                           SnoodsSurfaceView snoodsSurfaceView,
                           SnoodsGameActivity snoodsGameActivity) {
        super(millisInFuture, countDownInterval);

        this.snoodsSurfaceView = snoodsSurfaceView;
        this.snoodsGameActivity = snoodsGameActivity;

        dec = 353.0f / (millisInFuture / 1000.0f);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (!snoodsSurfaceView.mDeckIsEmpty) {
            snoodsSurfaceView.progressBarPercent += dec;
            snoodsSurfaceView.secs = (int) millisUntilFinished / 1000;
            if (snoodsSurfaceView.secs == 20) {
                snoodsGameActivity.showToast(snoodsGameActivity.getResources().getText(R.string.toast_hurry_up).toString(),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onFinish() {
        if (!snoodsSurfaceView.mDeckIsEmpty) {
            for (int i = 0; i < snoodsSurfaceView.COLUMNS_COUNT; ++i) {
                snoodsSurfaceView.lockColumn(i, false);
            }
            snoodsSurfaceView.secs = 0;
            snoodsSurfaceView.progressBarPercent = 0;
            snoodsGameActivity.showToast(snoodsGameActivity.getResources().getText(R.string.toast_time_up).toString(),
                    Toast.LENGTH_SHORT);
            SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_GAME_OVER);
            snoodsSurfaceView.mDeckIsEmpty = true;
            snoodsSurfaceView.mIsGameOver = true;
            this.cancel();
            snoodsSurfaceView.mIsTimerRun = false;
        }
    }
}
