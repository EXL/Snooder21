/************************************************************************************
** The MIT License (MIT)
**
** Copyright (c) 2016 Serg "EXL" Koles
**
** Permission is hereby granted, free of charge, to any person obtaining a copy
** of this software and associated documentation files (the "Software"), to deal
** in the Software without restriction, including without limitation the rights
** to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
** copies of the Software, and to permit persons to whom the Software is
** furnished to do so, subject to the following conditions:
**
** The above copyright notice and this permission notice shall be included in all
** copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
** IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
** FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
** AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
** LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
** OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
** SOFTWARE.
************************************************************************************/

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

        dec = 353.0f / (millisInFuture / 1000.0f); // Hardcoded progress bar coordinates
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
