package ru.exlmoto.snood21;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
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

        public static int animationSpeed = 2;
        public static int themeId = 0;

        // Scores Section
        public static String playerName = "Player";
        public static String[] playerNames = {
                "Zorge.R", "baat", "Osta", "Armhalfer", "J()KER",
                "a1batross", "mvb06", "NoPH8", "PUSYA", "Neko-mata"
        };
        public static final int[] playerScores = {
                100000, 80000, 70000, 50000, 40000,
                30000, 20000, 10000, 5000, 1000
        };
    }

    private Dialog aboutDialog = null;
    private Dialog helpDialog = null;

    private CheckBox vibrationCheckBox = null;
    private CheckBox soundCheckBox = null;
    private CheckBox showToastCheckBox = null;
    private CheckBox showFpsCheckBox = null;
    private CheckBox writeHighScoresCheckBox = null;

    private TextView playerNameTextView = null;

    private SeekBar animationSpeedSeekBar = null;
    private TextView animationSpeedTextView = null;

    private RadioButton motoRadioButton = null;
    private RadioButton paperRadioButton = null;

    // TODO: Why Static?
    public static SharedPreferences settingStorage = null;

    private static TextView[] playerNamesView = null;
    private static TextView[] playerScoresView = null;

    private static Vibrator vibrator = null;
    private static SoundPool soundPool = null;

    public void fillSettingsByLayout() {
        SnoodsSettings.vibration = vibrationCheckBox.isChecked();
        SnoodsSettings.sound = soundCheckBox.isChecked();
        SnoodsSettings.showToasts = showToastCheckBox.isChecked();
        SnoodsSettings.showFps = showFpsCheckBox.isChecked();
        SnoodsSettings.writeScores = writeHighScoresCheckBox.isChecked();

        if (motoRadioButton.isChecked()) {
            SnoodsSettings.themeId = THEME_MOTO;
        } else if (paperRadioButton.isChecked()) {
            SnoodsSettings.themeId = THEME_PAPER;
        }

        SnoodsSettings.playerName = playerNameTextView.getText().toString();

        SnoodsSettings.animationSpeed = animationSpeedSeekBar.getProgress();
    }

    public void fillLayoutBySettings() {
        vibrationCheckBox.setChecked(SnoodsSettings.vibration);
        soundCheckBox.setChecked(SnoodsSettings.sound);
        showToastCheckBox.setChecked(SnoodsSettings.showToasts);
        showFpsCheckBox.setChecked(SnoodsSettings.showFps);
        writeHighScoresCheckBox.setChecked(SnoodsSettings.writeScores);

        switch (SnoodsSettings.themeId) {
            case THEME_MOTO: {
                motoRadioButton.setChecked(true);
                break;
            }
            case THEME_PAPER: {
                paperRadioButton.setChecked(true);
                break;
            }
            default: {
                break;
            }
        }

        playerNameTextView.setText(SnoodsSettings.playerName);

        animationSpeedTextView.setText(String.format("%d", SnoodsSettings.animationSpeed + 1));
        animationSpeedSeekBar.setProgress(SnoodsSettings.animationSpeed);

        updateHighScoreTable();
    }

    public static void updateHighScoreTable() {
        for (int i = 0; i < HIGH_SCORE_PLAYERS; ++i) {
            playerNamesView[i].setText(SnoodsSettings.playerNames[i]);
            playerScoresView[i].setText(String.format("%d", SnoodsSettings.playerScores[i]));
        }
    }

    private void readSettings() {
        SnoodsSettings.vibration = settingStorage.getBoolean("vibration", true);
        SnoodsSettings.sound = settingStorage.getBoolean("sound", true);
        SnoodsSettings.showToasts = settingStorage.getBoolean("showToasts", true);
        SnoodsSettings.showFps = settingStorage.getBoolean("showFps", false);
        SnoodsSettings.writeScores = settingStorage.getBoolean("writeScores", true);

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

        playerNameTextView = (TextView) findViewById(R.id.player_EditText);

        animationSpeedTextView = (TextView) findViewById(R.id.animationValueView);
        animationSpeedSeekBar = (SeekBar) findViewById(R.id.animationBar);

        motoRadioButton = (RadioButton) findViewById(R.id.motoThemeRadioButton);
        paperRadioButton = (RadioButton) findViewById(R.id.paperThemeRadioButton);

        playerNamesView = new TextView[HIGH_SCORE_PLAYERS];
        playerScoresView = new TextView[HIGH_SCORE_PLAYERS];

        /* Generated by node.js with following code:
         * function generateInitTable() {
         *     var players = 10;
         *     for (var i = 0, j = 1; i < players; ++j, ++i) {
         *           console.log('playerNamesView[' + i + '] = (TextView) findViewById(R.id.player_Name' + j + ');');
         *           console.log('playerScoresView[' + i + '] = (TextView) findViewById(R.id.player_Score' + j + ');');
         *     }
         * }
         * generateInitTable(); */

        playerNamesView[0] = (TextView) findViewById(R.id.player_Name1);
        playerScoresView[0] = (TextView) findViewById(R.id.player_Score1);
        playerNamesView[1] = (TextView) findViewById(R.id.player_Name2);
        playerScoresView[1] = (TextView) findViewById(R.id.player_Score2);
        playerNamesView[2] = (TextView) findViewById(R.id.player_Name3);
        playerScoresView[2] = (TextView) findViewById(R.id.player_Score3);
        playerNamesView[3] = (TextView) findViewById(R.id.player_Name4);
        playerScoresView[3] = (TextView) findViewById(R.id.player_Score4);
        playerNamesView[4] = (TextView) findViewById(R.id.player_Name5);
        playerScoresView[4] = (TextView) findViewById(R.id.player_Score5);
        playerNamesView[5] = (TextView) findViewById(R.id.player_Name6);
        playerScoresView[5] = (TextView) findViewById(R.id.player_Score6);
        playerNamesView[6] = (TextView) findViewById(R.id.player_Name7);
        playerScoresView[6] = (TextView) findViewById(R.id.player_Score7);
        playerNamesView[7] = (TextView) findViewById(R.id.player_Name8);
        playerScoresView[7] = (TextView) findViewById(R.id.player_Score8);
        playerNamesView[8] = (TextView) findViewById(R.id.player_Name9);
        playerScoresView[8] = (TextView) findViewById(R.id.player_Score9);
        playerNamesView[9] = (TextView) findViewById(R.id.player_Name10);
        playerScoresView[9] = (TextView) findViewById(R.id.player_Score10);
    }

    private void showAboutDialog() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                aboutDialog.setContentView(R.layout.dialog_about);
                aboutDialog.setCancelable(true);
                aboutDialog.setTitle(R.string.app_name);

                Button okAboutButton = (Button) aboutDialog.findViewById(R.id.okAboutButton);
                okAboutButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (aboutDialog != null) {
                            aboutDialog.cancel();
                        }
                    }
                });

                aboutDialog.show();
            }
        });
    }

    private void showHelpDialog() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                helpDialog.setContentView(R.layout.dialog_help);
                helpDialog.setCancelable(true);
                helpDialog.setTitle(R.string.app_name);

                Button okHelpButton = (Button) helpDialog.findViewById(R.id.okHelpButton);
                okHelpButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (helpDialog != null) {
                            helpDialog.cancel();
                        }
                    }
                });

                helpDialog.show();
            }
        });
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
        if (settingStorage.getBoolean("firstRun", true)) {
            settingStorage.edit().putBoolean("firstRun", false).commit();
        } else {
            readSettings();
        }

        aboutDialog = new Dialog(this);
        helpDialog = new Dialog(this);

        initWidgets();

        fillLayoutBySettings();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // TODO: 5?
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

        SOUND_GRAB = soundPool.load(this, R.raw.grab, 1);
        SOUND_DROP = soundPool.load(this, R.raw.drop, 1);
        SOUND_LOCK = soundPool.load(this, R.raw.lock, 1);
        SOUND_WIN = soundPool.load(this, R.raw.game_win, 1);
        SOUND_GAME_OVER = soundPool.load(this, R.raw.game_over, 1);
        SOUND_WHOOSH = soundPool.load(this, R.raw.whoosh, 1);
        SOUND_ERROR = soundPool.load(this, R.raw.error, 1);

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
                showAboutDialog();
            }
        });

        Button helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showHelpDialog();
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

    @Override
    protected void onDestroy() {
        writeSettings();

        aboutDialog.dismiss();
        helpDialog.dismiss();

        super.onDestroy();
    }
}
