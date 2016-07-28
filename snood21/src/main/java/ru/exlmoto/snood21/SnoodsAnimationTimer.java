package ru.exlmoto.snood21;

import android.graphics.Bitmap;
import android.os.CountDownTimer;

import java.util.ArrayList;

public class SnoodsAnimationTimer extends CountDownTimer {

    private ArrayList<Bitmap>[] columnsDecks = null;
    ArrayList<Integer>[] columnsDecksValue = null;
    private boolean[] lockColumns = null;
    private int column;
    private Bitmap[] bitmaps = null;
    private boolean firstFrame = true;

    private boolean lock = false;

    private boolean playSound = true;

    public SnoodsAnimationTimer(long millisInFuture,
                                long countDownInterval,
                                ArrayList<Bitmap>[] columnsDecks,
                                ArrayList<Integer>[] columnsDecksValue,
                                boolean[] lockColumns,
                                int column,
                                Bitmap[] bitmaps,
                                boolean lock) {
        super(millisInFuture, countDownInterval);

        this.columnsDecks = columnsDecks;
        this.columnsDecksValue = columnsDecksValue;
        this.lockColumns = lockColumns;
        this.column = column;
        this.bitmaps = bitmaps;
        this.lock = lock;

        if (!lock) {
            SnoodsSurfaceView.animateColumn = false;
        }
    }

    private void animate(int offset) {
        for (int i = 0; i < columnsDecks[column].size(); ++i) {
            columnsDecks[column].set(i, bitmaps[offset + columnsDecksValue[column].get(i)]);
        }
    }

    private void animate(Bitmap bitmap) {
        for (int i = 0; i < columnsDecks[column].size(); ++i) {
            columnsDecks[column].set(i, bitmap);
        }
    }

    private void animateFirstFrame() {
        if (lock || lockColumns[column]) {
            animate(bitmaps[17 + 16]);
        } else {
            animate(17);
        }
    }

    private void animateSecondFrame() {
        if (lock || lockColumns[column]) {
            animate(bitmaps[16]);
        } else {
            animate(0);
        }
    }

    public int getCountOfLockColumns() {
        int lockedColumns = 0;
        for (int i = 0; i < SnoodsSurfaceView.COLUMNS_COUNT; ++i) {
            if (lockColumns[i]) {
                lockedColumns++;
            }
        }
        return lockedColumns;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (lock) {
            if (playSound && getCountOfLockColumns() < 3) {
                SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_LOCK);
                playSound = false;
            }
            SnoodsLauncherActivity.doVibrate(SnoodsLauncherActivity.VIBRATE_SHORT);
        }
        if (firstFrame) {
            animateFirstFrame();
            firstFrame = false;
        } else {
            animateSecondFrame();
            firstFrame = true;
        }
    }

    @Override
    public void onFinish() {
        animateSecondFrame();
    }
}
