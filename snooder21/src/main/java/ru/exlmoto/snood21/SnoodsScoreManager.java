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

import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import java.util.Locale;

import ru.exlmoto.snood21.SnoodsLauncherActivity.SnoodsSettings;

public class SnoodsScoreManager {

    private String playerName = "";
    private SnoodsGameActivity snoodsGameActivity = null;

    public SnoodsScoreManager(String playerName, SnoodsGameActivity snoodsGameActivity) {
        this.playerName = normalizePlayerName(playerName);
        this.snoodsGameActivity = snoodsGameActivity;
    }

    public static String generatePlayerName() {
        String modelName = Build.MANUFACTURER.subSequence(0, 3).toString();
        modelName = modelName.toUpperCase(Locale.getDefault());
        modelName += "-" + Build.MODEL;
        return normalizePlayerName(modelName);
    }

    private static String normalizePlayerName(String name) {
        if (name.equals("")) {
            name = "Player";
        }
        if (name.length() > 11) {
            name = name.subSequence(0, 11).toString();
        }
        return name;
    }

    private void saveHiScores() {
        if (SnoodsLauncherActivity.settingStorage != null) {
            SharedPreferences.Editor editor = SnoodsLauncherActivity.settingStorage.edit();
            for (int i = 0; i < SnoodsLauncherActivity.HIGH_SCORE_PLAYERS; ++i) {
                editor.putString("player" + i, SnoodsSettings.playerNames[i]);
                editor.putInt("score" + i, SnoodsSettings.playerScores[i]);
            }
            editor.commit();
        } else {
            SnoodsGameActivity.toDebug("Error: settingStorage is null!");
        }
    }

    private void insertScore(String name, int score, int i) {
        if (i != -1) {
            String localObject = SnoodsSettings.playerNames[i];
            String str = "";
            int j = SnoodsSettings.playerScores[i];
            int k = 0;
            for (int m = i + 1; m < SnoodsLauncherActivity.HIGH_SCORE_PLAYERS; m++) {
                k = SnoodsSettings.playerScores[m];
                str = SnoodsSettings.playerNames[m];
                SnoodsSettings.playerScores[m] = j;
                SnoodsSettings.playerNames[m] = localObject;
                j = k;
                localObject = str;
            }
            SnoodsSettings.playerNames[i] = name;
            SnoodsSettings.playerScores[i] = score;

            saveHiScores();
            snoodsGameActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    SnoodsLauncherActivity.updateHighScoreTable();
                }
            });
        }
    }

    private int getScorePosition(int score) {
        for (int i = 0; i < SnoodsLauncherActivity.HIGH_SCORE_PLAYERS; ++i) {
            if (score > SnoodsSettings.playerScores[i]) {
                return i;
            }
        }
        return -1;
    }

    public int checkHighScore(int highScore) {
        SnoodsGameActivity.toDebug("Score is:" + highScore);
        int i = getScorePosition(highScore);
        if (i == -1) {
            return i;
        }
        String answer = snoodsGameActivity.getResources().getText(R.string.toast_high_score_ch1).toString() + " ";
        answer += highScore + " ";
        answer += snoodsGameActivity.getResources().getText(R.string.toast_high_score_ch2).toString();
        if (SnoodsSettings.writeScores) {
            answer +=  " " + snoodsGameActivity.getResources().getText(R.string.toast_high_score_ch3).toString();
            insertScore(playerName, highScore, i);
        }
        snoodsGameActivity.showToast(answer, Toast.LENGTH_SHORT);
        return i;
    }
}
