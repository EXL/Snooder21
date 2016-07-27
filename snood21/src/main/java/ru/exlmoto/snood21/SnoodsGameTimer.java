package ru.exlmoto.snood21;

import android.os.CountDownTimer;
import android.widget.Toast;

public class SnoodsGameTimer extends CountDownTimer {

    private float dec = 0;

    private SnoodsSurfaceView snoodsSurfaceView = null;
    private SnoodsActivity snoodsActivity = null;

    public SnoodsGameTimer(long millisInFuture,
                           long countDownInterval,
                           SnoodsSurfaceView snoodsSurfaceView,
                           SnoodsActivity snoodsActivity) {
        super(millisInFuture, countDownInterval);

        this.snoodsSurfaceView = snoodsSurfaceView;
        this.snoodsActivity = snoodsActivity;

        dec = 361.0f / (millisInFuture / 1000.0f);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (!snoodsSurfaceView.mDeckIsEmpty) {
            snoodsSurfaceView.progressBarPercent += dec; // 3 min
            snoodsSurfaceView.secs = (int) millisUntilFinished / 1000;
            if (snoodsSurfaceView.secs == 20) {
                snoodsActivity.showToast("Hurry up!", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onFinish() {
        if (!snoodsSurfaceView.mDeckIsEmpty) {
            snoodsSurfaceView.secs = 0;
            snoodsSurfaceView.progressBarPercent = 0;
            snoodsActivity.showToast("Time is over!", Toast.LENGTH_SHORT);
            snoodsSurfaceView.mDeckIsEmpty = true;
            snoodsSurfaceView.mIsGameOver = true;
            this.cancel();
            snoodsSurfaceView.mIsTimerRun = false;
        }
    }
}
