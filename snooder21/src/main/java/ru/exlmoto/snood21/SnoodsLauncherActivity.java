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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
//import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SnoodsLauncherActivity extends Activity {

    public static final int THEME_MOTO = 0;
    public static final int THEME_PAPER = 1;

    public static final int VIBRATE_SHORT = 20;
    public static final int VIBRATE_LONG = 70;

    public static final int HIGH_SCORE_PLAYERS = 10;

    public static int SOUND_GRAB;
    public static int SOUND_DROP;
    public static int SOUND_WIN;
    public static int SOUND_LOCK;
    public static int SOUND_GAME_OVER;
    public static int SOUND_WHOOSH;
    public static int SOUND_ERROR;

    public static class SnoodsSettings {

        public static boolean vibration = true;
        public static boolean sound = true;
        public static boolean showToasts = true;
        public static boolean showFps = false;
        public static boolean writeScores = true;
        public static boolean antialiasing = true;

        public static int animationSpeed = 2;
        public static int themeId = 0;

        // Scores Section
        public static String playerName = "Player";
        public static String[] playerNames = {
                "maxcom", "tailgunner", "JB", "Aceler", "beastie",
                "mono", "leave", "Pinkbyte", "Shaman007", "shell-script"
        };
        public static final int[] playerScores = {
                85000, 70000, 60000, 55000, 50000,
                45000, 40000, 35000, 30000, 25000
        };
    }

    private AlertDialog aboutDialog = null;
    private AlertDialog helpDialog = null;

    private CheckBox vibrationCheckBox = null;
    private CheckBox soundCheckBox = null;
    private CheckBox showToastCheckBox = null;
    private CheckBox showFpsCheckBox = null;
    private CheckBox writeHighScoresCheckBox = null;
    private CheckBox antialiasingCheckBox = null;

    private TextView playerNameEditText = null;

    private SeekBar animationSpeedSeekBar = null;
    private TextView animationSpeedTextView = null;

//    private RadioButton motoRadioButton = null;
//    private RadioButton paperRadioButton = null;

    public static SharedPreferences settingStorage = null;

    private static TextView playerNamesView = null;
    private static TextView playerScoresView = null;

    private static Vibrator vibrator = null;
    private static SoundPool soundPool = null;

    public void fillSettingsByLayout() {
        SnoodsSettings.vibration = vibrationCheckBox.isChecked();
        SnoodsSettings.sound = soundCheckBox.isChecked();
        SnoodsSettings.showToasts = showToastCheckBox.isChecked();
        SnoodsSettings.showFps = showFpsCheckBox.isChecked();
        SnoodsSettings.writeScores = writeHighScoresCheckBox.isChecked();
        SnoodsSettings.antialiasing = antialiasingCheckBox.isChecked();

//        if (motoRadioButton.isChecked()) {
//            SnoodsSettings.themeId = THEME_MOTO;
//        } else if (paperRadioButton.isChecked()) {
//            SnoodsSettings.themeId = THEME_PAPER;
//        }

        SnoodsSettings.playerName = playerNameEditText.getText().toString();

        SnoodsSettings.animationSpeed = animationSpeedSeekBar.getProgress();
    }

    public void fillLayoutBySettings() {
        vibrationCheckBox.setChecked(SnoodsSettings.vibration);
        soundCheckBox.setChecked(SnoodsSettings.sound);
        showToastCheckBox.setChecked(SnoodsSettings.showToasts);
        showFpsCheckBox.setChecked(SnoodsSettings.showFps);
        writeHighScoresCheckBox.setChecked(SnoodsSettings.writeScores);
        antialiasingCheckBox.setChecked(SnoodsSettings.antialiasing);

//        switch (SnoodsSettings.themeId) {
//            case THEME_MOTO: {
//                motoRadioButton.setChecked(true);
//                break;
//            }
//            case THEME_PAPER: {
//                paperRadioButton.setChecked(true);
//                break;
//            }
//            default: {
//                break;
//            }
//        }

        playerNameEditText.setText(SnoodsSettings.playerName);

        animationSpeedTextView.setText(String.format("%d", SnoodsSettings.animationSpeed + 1));
        animationSpeedSeekBar.setProgress(SnoodsSettings.animationSpeed);

        updateHighScoreTable();
    }

    public static void updateHighScoreTable() {
        String players = "";
        String scores = "";
        for (int i = 0; i < HIGH_SCORE_PLAYERS; ++i) {
            players += SnoodsSettings.playerNames[i];
            scores += SnoodsSettings.playerScores[i];
            if (i < HIGH_SCORE_PLAYERS - 1) {
                players += "\n";
                scores += "\n";
            }
        }
        playerNamesView.setText(players);
        playerScoresView.setText(scores);
    }

    private void readSettings() {
        SnoodsSettings.vibration = settingStorage.getBoolean("vibration", true);
        SnoodsSettings.sound = settingStorage.getBoolean("sound", true);
        SnoodsSettings.showToasts = settingStorage.getBoolean("showToasts", true);
        SnoodsSettings.showFps = settingStorage.getBoolean("showFps", false);
        SnoodsSettings.writeScores = settingStorage.getBoolean("writeScores", true);
        SnoodsSettings.antialiasing = settingStorage.getBoolean("antialiasing", true);

        SnoodsSettings.themeId = settingStorage.getInt("themeId", THEME_MOTO);

        SnoodsSettings.playerName = settingStorage.getString("playerName", "Player");

        SnoodsSettings.animationSpeed = settingStorage.getInt("animationSpeed", 2);

        for (int i = 0; i < HIGH_SCORE_PLAYERS; ++i) {
            SnoodsSettings.playerNames[i] = settingStorage.getString("player" + i, SnoodsSettings.playerNames[i]);
            SnoodsSettings.playerScores[i] = settingStorage.getInt("score" + i, SnoodsSettings.playerScores[i]);
        }
    }

    private void writeSettings() {
        SnoodsGameActivity.toDebug("Write Settings!");

        fillSettingsByLayout();

        SharedPreferences.Editor editor = settingStorage.edit();

        editor.putBoolean("vibration", SnoodsSettings.vibration);
        editor.putBoolean("sound", SnoodsSettings.sound);
        editor.putBoolean("showToasts", SnoodsSettings.showToasts);
        editor.putBoolean("showFps", SnoodsSettings.showFps);
        editor.putBoolean("writeScores", SnoodsSettings.writeScores);
        editor.putBoolean("antialiasing", SnoodsSettings.antialiasing);

        editor.putInt("themeId", SnoodsSettings.themeId);

        editor.putString("playerName", SnoodsSettings.playerName);

        editor.putInt("animationSpeed", SnoodsSettings.animationSpeed);

        editor.commit();
    }

    private void initWidgets() {
        vibrationCheckBox = (CheckBox) findViewById(R.id.checkBoxVibration);
        soundCheckBox = (CheckBox) findViewById(R.id.checkBoxSound);
        showToastCheckBox = (CheckBox) findViewById(R.id.checkBoxToasts);
        showFpsCheckBox = (CheckBox) findViewById(R.id.checkBoxFps);
        writeHighScoresCheckBox = (CheckBox) findViewById(R.id.checkBoxWriteScores);
        antialiasingCheckBox = (CheckBox) findViewById(R.id.checkBoxAntialiasing);

        playerNameEditText = (TextView) findViewById(R.id.player_EditText);

        animationSpeedTextView = (TextView) findViewById(R.id.animationValueView);
        animationSpeedSeekBar = (SeekBar) findViewById(R.id.animationBar);

//        motoRadioButton = (RadioButton) findViewById(R.id.motoThemeRadioButton);
//        paperRadioButton = (RadioButton) findViewById(R.id.paperThemeRadioButton);

        playerNamesView = (TextView) findViewById(R.id.player_Names);
        playerScoresView = (TextView) findViewById(R.id.player_Scores);
    }

    private void initAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.app_name);
        builder.setPositiveButton(R.string.ok, null);
        aboutDialog = builder.create();
    }

    private void initHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_help, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.app_name);
        builder.setPositiveButton(R.string.ok, null);
        helpDialog = builder.create();
    }

    public static void playSound(final int soundId) {
        if (SnoodsSettings.sound && (soundId != 0)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }).start();
        }
    }

    public static void doVibrate(int duration) {
        if (SnoodsSettings.vibration) {
            vibrator.vibrate(duration);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_snoods_launcher);

        settingStorage = getSharedPreferences("ru.exlmoto.snood21", MODE_PRIVATE);
        // Check the first run
        boolean firstRun = false;
        if (settingStorage.getBoolean("firstRun", true)) {
            firstRun = true;
            settingStorage.edit().putBoolean("firstRun", false).commit();
        } else {
            readSettings();
        }

        initAboutDialog();
        initHelpDialog();

        initWidgets();

        fillLayoutBySettings();

        if (firstRun) {
            playerNameEditText.setText(SnoodsScoreManager.generatePlayerName());
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // TODO: 5?
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

        SOUND_GRAB = soundPool.load(this, R.raw.sfx_grab, 1);
        SOUND_DROP = soundPool.load(this, R.raw.sfx_drop, 1);
        SOUND_LOCK = soundPool.load(this, R.raw.sfx_lock, 1);
        SOUND_WIN = soundPool.load(this, R.raw.sfx_game_win, 1);
        SOUND_GAME_OVER = soundPool.load(this, R.raw.sfx_game_over, 1);
        SOUND_WHOOSH = soundPool.load(this, R.raw.sfx_whoosh, 1);
        SOUND_ERROR = soundPool.load(this, R.raw.sfx_error, 1);

        Button snood21RunButton = (Button) findViewById(R.id.runSnoodButton);
        snood21RunButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                writeSettings();

                Intent intent = new Intent(v.getContext(), SnoodsGameActivity.class);
                startActivity(intent);
            }
        });

        Button aboutButton = (Button) findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showMyDialog(aboutDialog);
            }
        });

        Button helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showMyDialog(helpDialog);
            }
        });

        animationSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                animationSpeedTextView.setText(String.format("%d", progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing here
            }
        });
    }

    // Prevent dialog dismiss when orientation changes
    // http://stackoverflow.com/a/27311231/2467443
    private static void doKeepDialog(AlertDialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    private void showMyDialog(AlertDialog dialog) {
        dialog.show();
        doKeepDialog(dialog);
    }

    @Override
    protected void onDestroy() {
        writeSettings();

        super.onDestroy();
    }
}
