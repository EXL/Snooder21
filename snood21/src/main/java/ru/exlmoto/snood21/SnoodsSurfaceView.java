package ru.exlmoto.snood21;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import ru.exlmoto.snood21.SnoodsLauncherActivity.SnoodsSettings;

public class SnoodsSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {

    public static final int ORIGINAL_WIDTH = 800;
    public static final int ORIGINAL_HEIGHT = 480;

    private static final int CARD_BITMAPS_COUNT = 17 * 2;
    public static final int COLUMNS_COUNT = 4;

    public static final int CHARS_NUM = 13;
    public static final int TEXT_NUM = 5;

    public static final int[] ANIMATION_CARD_SPEEDS = {5, 10, 15, 20, 30, 35};
    public static final int[] ANIMATION_COLUMN_SPEEDS = {2, 5, 10, 15, 25, 30};
    public static final int[] ANIMATION_CARD_MOVE_SPEEDS = {4, 5, 10, 20, 25, 50};

    // Default values
    public static int drop_card_speed = 15;
    public static int drop_column_speed = 10;
    public static int drop_card_move_speed = 10;

    public static final int CARD_GAP = 30;

    private Rect mOriginalScreenRect = null;
    private Rect mOutputScreenRect = null;

    private static int mScreenWidth = 0;
    private static int mScreenHeight = 0;

    private final int initialCardCoordX = 10;
    private final int initialCardCoordY = 85;
    private int mX_card_coord = initialCardCoordX;
    private int mY_card_coord = initialCardCoordY;
    public static int mX_card_grab_coord = 0;
    public static int mY_card_grab_coord = 0;

    private boolean mIsRunning = false;

    private Thread mMainThread = null;
    private Canvas mMainCanvas = null;
    private Paint mMainPaint = null;

    private Bitmap mGameBitmap = null;
    private Canvas mBitmapCanvas = null;

    private SurfaceHolder mSurfaceHolder = null;

    private Context mContext = null;

    private Bitmap mCurrentCardBitmap = null;
    private Bitmap mCurrentCardBitmapToDeck = null;
    private Bitmap mBackGroundBitmap = null;

    private Bitmap mTextAllBitmap = null;
    private Bitmap[] mLabels = null;
    private Bitmap[] mChars = null;

    private Rect cardRect = null;
    private Rect[] columnRects = null;
    private int highlightColumn = 0;

    private int mX_coord_from = 0;
    private int mY_coord_from = 0;
    public boolean mIsDropingCard = false;
    public boolean mIsGrab = false;

    private int mLevel = 1;
    private int[] mDeck = null;

    private Random mRandom = null;
    private Bitmap[] cardBitmaps = null;
    private Bitmap cardAllBitmap = null;
    private Bitmap mNextCardBitmap = null;

    private ArrayList<Bitmap>[] columnsDecks = null;
    private ArrayList<Integer>[] columnsDecksValue = null;
    private int cardIndex = 32; // First level
    private int scores = 0;

    private Rect deckRect = null;

    public boolean mDeckIsEmpty = false;

    private int columnStartHeight = 111;
    private int[] columnOffsets = null;

    private SnoodsGameTimer timer = null;
    public int secs = 60 * 3;

    public boolean mIsDropingColumn = false;
    private boolean columnDropped = false;
    public boolean[] lockColumns = null;
    private int[] columnScores = null;

    public boolean mIsGameOver = false;

    private SnoodsGameActivity snoodsGameActivity = null;
    private boolean toastShown = false;

    public float progressBarPercent = 0;

    public boolean mIsTimerRun = false;

    public static boolean animateColumn = true;

    private Bitmap backGroundWinBitmap = null;
    public boolean mIsWinAnimation = false;

    private int x_anim_sprite_start = 145;
    private int y_anim_sprite_start = 200 / 6;
    private int x_anim_sprite = 0;
    private int y_anim_sprite = 0;
    private boolean showBlinkLabel = false;

    private int[] sixRandomCards = null;

    public boolean mPlayingGrabSound = true;
    public boolean mPlayingErrorSound = true;
    private boolean mPlayingGameOverSound = true;
    private boolean mPlayingWhooshSound = true;

    private SnoodsScoreManager snoodsScoreManager = null;

    public SnoodsSurfaceView(Context context, final SnoodsGameActivity snoodsGameActivity) {
        super(context);

        this.snoodsGameActivity = snoodsGameActivity;
        mContext = context;

        mRandom = new Random();

        mGameBitmap = Bitmap.createBitmap(ORIGINAL_WIDTH, ORIGINAL_HEIGHT, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mGameBitmap);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mMainPaint = new Paint();

        drop_card_speed = ANIMATION_CARD_SPEEDS[SnoodsSettings.animationSpeed];
        drop_column_speed = ANIMATION_COLUMN_SPEEDS[SnoodsSettings.animationSpeed];
        drop_card_move_speed = ANIMATION_CARD_MOVE_SPEEDS[SnoodsSettings.animationSpeed];

        switch (SnoodsSettings.themeId) {
            default:
            case SnoodsLauncherActivity.THEME_PAPER: // TODO: Remove this
            case SnoodsLauncherActivity.THEME_MOTO: {
                mBackGroundBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bkg_moto);
                backGroundWinBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.win_moto);
                mTextAllBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.text_moto);
                cardAllBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.deck_moto);
                break;
            }
        }

        mLabels = new Bitmap[TEXT_NUM];
        fillLabelsBitmap();

        mChars = new Bitmap[CHARS_NUM];
        fillDigitsBitmap();

        columnRects = new Rect[COLUMNS_COUNT];
        cardBitmaps = new Bitmap[CARD_BITMAPS_COUNT];
        fillDecksBitmap();

        columnsDecks = new ArrayList[COLUMNS_COUNT];
        columnsDecksValue = new ArrayList[COLUMNS_COUNT];
        columnOffsets = new int[COLUMNS_COUNT];
        lockColumns = new boolean[COLUMNS_COUNT];
        columnScores = new int[COLUMNS_COUNT];
        for (int i = 0; i < COLUMNS_COUNT; ++i) {
            columnsDecks[i] = new ArrayList<Bitmap>();
            columnsDecksValue[i] = new ArrayList<Integer>();
            columnOffsets[i] = 0;
            columnScores[i] = 0;
            lockColumns[i] = false;
        }

        sixRandomCards = new int[6];
        setSixRandomCardsForAnimations();

        snoodsScoreManager = new SnoodsScoreManager(SnoodsSettings.playerName, snoodsGameActivity);

        createCountDownTimer(60000 * 3, 1000);

        // Set screen always on
        setKeepScreenOn(true);

        // Focus
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public int getScores() {
        return scores;
    }

    public SnoodsScoreManager getSnoodsScoreManager() {
        return snoodsScoreManager;
    }

    private void setSixRandomCardsForAnimations() {
        for (int i = 0; i < 6; ++i) {
            sixRandomCards[i] = mRandom.nextInt(CARD_BITMAPS_COUNT / 2 - 1); // Without "X" card
        }
    }

    private void stopTimer() {
        timer.cancel();
        timer = null;
    }

    private void createCountDownTimer(final long millisInFuture,
                                      final long countDownInterval) {
        final SnoodsSurfaceView snoodsSurfaceView = this;
        snoodsGameActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                timer = new SnoodsGameTimer(millisInFuture, countDownInterval,
                        snoodsSurfaceView, snoodsGameActivity);
            }
        });
    }

    private void fillDecksBitmap() {
        for (int i = 0; i < CARD_BITMAPS_COUNT; ++i) {
            int x_off = (i > 16) ? (i - 17) * 145 : i * 145;
            int y_off = (i > 16) ? 200 : 0;

            SnoodsGameActivity.toDebug("" + i + " " + x_off + " " + y_off);
            cardBitmaps[i] = Bitmap.createBitmap(cardAllBitmap, x_off, y_off, 145, 200);
        }
    }

    private void fillLabelsBitmap() {
        for (int i = 0; i < TEXT_NUM; ++i) {
            int y_off = i * 52 + i;
            mLabels[i] = Bitmap.createBitmap(mTextAllBitmap, 0, y_off, mTextAllBitmap.getWidth(), 52);
        }
    }

    private void fillDigitsBitmap() {
        for (int i = 0; i < CHARS_NUM; ++i) {
            int x_off = i * 29;
            mChars[i] = Bitmap.createBitmap(mTextAllBitmap, x_off, 52 * 5 + 4, 29, 52);
        }
    }

    private void paintNumber(Canvas canvas, Paint paint, int number, int x, int y, boolean centered, boolean time) {
        if (!time) {
            String num = "" + number;
            int len = num.length();

            for (int i = 0; i < len; ++i) {
                if (!centered) {
                    canvas.drawBitmap(mChars[Character.getNumericValue(num.charAt(i))], x + i * 29, y, paint);
                } else {
                    canvas.drawBitmap(mChars[Character.getNumericValue(num.charAt(i))], x - (len - 1) * 29 / 2 + i * 29, y, paint);
                }
            }
        } else {
            int minutes = secs / 60;
            int seconds = secs % 60;

            String timeS = "";
            if (minutes < 10) {
                timeS += 0;
            }
            timeS += minutes;
            if (seconds < 10) {
                timeS += 0;
            }
            timeS += seconds;
            int len = timeS.length();
            int r = 0;
            for (int i = 0; i < len; ++i) {
                canvas.drawBitmap(mChars[Character.getNumericValue(timeS.charAt(i))], x + r + i * 29, y, paint);
                if (i == 1) {
                    r += 29;
                    canvas.drawBitmap(mChars[10], x + r + i * 29, y, paint);
                }
            }
        }
    }

    private void flushDeck() {
        cardIndex = 6 + 13 + 13 * ((mLevel == 4) ? 3 : mLevel);
        mDeck = new int[cardIndex];

        // Add cards
        for (int i = 0, j = 0, k = 0; i < cardIndex; ++i, ++j) {
            if (j > 12) {
                j = 0;
            }
            mDeck[i] = j;

            if (i >= cardIndex - 6) {
                mDeck[i] = 13 + k; // Jokers
                if (k < 2) {
                    k++;
                } else {
                    k = 0;
                }
            }
        }

        String c = "";
        for (int i = 0; i < cardIndex; ++i) {
            c += mDeck[i] + " ";
        }
        SnoodsGameActivity.toDebug(c);

        int _Change, _Tmp;
        for (int Num1 = 0; Num1 < cardIndex; Num1++) {
            _Change = mRandom.nextInt(cardIndex);
            _Tmp = mDeck[Num1];
            mDeck[Num1] = mDeck[_Change];
            mDeck[_Change] = _Tmp;
        }

        c = "";
        for (int i = 0; i < cardIndex; ++i) {
            c += mDeck[i] + " ";
        }
        SnoodsGameActivity.toDebug(c);
    }

    public void setHighlightColumn(int column) {
        highlightColumn = column;
    }

    private void highlightColumn(Canvas canvas, Paint paint, int column) {
        if (column == 0) {
            return;
        } else {
            if (lockColumns[column - 1]) {
                paint.setColor(Color.parseColor("#77CF5B56"));
            } else {
                paint.setColor(Color.parseColor("#7733AF54"));
            }
            canvas.drawRect(columnRects[column - 1], paint);
            paint.reset();
        }
    }

    private void paintColumnScores(Canvas canvas, Paint paint) {
        for (int i = 0, j = 227; i < COLUMNS_COUNT; ++i, j += 159) {
            if (lockColumns[i]) {
                canvas.drawBitmap(mChars[12], j, 62, paint);
            } else {
                paintNumber(canvas, paint, columnScores[i], j, 62, true, false);
            }
        }
    }

    private void paintProgressBar(Canvas canvas, Paint paint) {
        paint.setColor(Color.parseColor("#CF5B56"));
        canvas.drawRect(8, 5, 8 + progressBarPercent, 51, paint);
        paint.reset();
    }

    private void render(Canvas canvas) {
        if (canvas != null) {
            if (!mIsWinAnimation) {
                // Draw background
                mBitmapCanvas.drawBitmap(mBackGroundBitmap, 0, 0, mMainPaint);

                // Draw score label
                mBitmapCanvas.drawBitmap(mLabels[4], 419, 2, mMainPaint);

                // Draw scores
                paintNumber(mBitmapCanvas, mMainPaint, scores, 600, 6, false, false);

                // Draw column scores
                paintColumnScores(mBitmapCanvas, mMainPaint);

                // Draw cards count
                paintNumber(mBitmapCanvas, mMainPaint, cardIndex, 20, 288, false, false);

                // Draw "L" letter
                mBitmapCanvas.drawBitmap(mChars[11], 97, 288, mMainPaint);

                // Draw level
                paintNumber(mBitmapCanvas, mMainPaint, mLevel, 97 + 29, 288, false, false);

                // Draw progress bar
                paintProgressBar(mBitmapCanvas, mMainPaint);

                // Draw time
                paintNumber(mBitmapCanvas, mMainPaint, secs, 104, 4, false, true);

                // Draw card decks
                drawCardDecs(mBitmapCanvas, mMainPaint);

                // Draw Highlight
                highlightColumn(mBitmapCanvas, mMainPaint, highlightColumn);

                // Draw cards
                if (cardIndex > 2) {
                    mBitmapCanvas.drawBitmap(mNextCardBitmap, initialCardCoordX, initialCardCoordY - 10, mMainPaint);
                }
                if (cardIndex > 1) {
                    mBitmapCanvas.drawBitmap(mNextCardBitmap, initialCardCoordX, initialCardCoordY - 5, mMainPaint);
                }
                if (!mDeckIsEmpty) {
                    mBitmapCanvas.drawBitmap(mCurrentCardBitmap, mX_card_coord, mY_card_coord, mMainPaint);
                }

                // Draw FPS
                if (SnoodsSettings.showFps) {
                    paintNumber(mBitmapCanvas, mMainPaint, getTimesPerSecond(), 9, 340, false, false);
                }
            } else {
                paintWinAnimation(mBitmapCanvas, mMainPaint);
            }

            mMainPaint.setFilterBitmap(true);
            canvas.drawBitmap(mGameBitmap, mOriginalScreenRect, mOutputScreenRect, mMainPaint);
        }
    }

    private void drawVerticalCardAnimation(Canvas canvas, Paint paint) {
        for (int i = 0; i < 6; ++i) {
            canvas.drawBitmap(cardBitmaps[(showBlinkLabel) ? sixRandomCards[i] : sixRandomCards[i] + 17],
                    x_anim_sprite_start,
                    y_anim_sprite + (200 + CARD_GAP) * i - (200 + CARD_GAP) * 6, paint);
            canvas.drawBitmap(cardBitmaps[(showBlinkLabel) ? sixRandomCards[i] : sixRandomCards[i] + 17],
                    ORIGINAL_WIDTH - x_anim_sprite_start - 145,
                    -y_anim_sprite + (200 + CARD_GAP) * i + ORIGINAL_HEIGHT, paint);
        }
    }

    private void drawHorizontalCardAnimation(Canvas canvas, Paint paint) {
        for (int i = 0; i < 6; ++i) {
            canvas.drawBitmap(cardBitmaps[(showBlinkLabel) ? sixRandomCards[i] : sixRandomCards[i] + 17],
                    x_anim_sprite + (145 + CARD_GAP) * i - (145 + CARD_GAP) * 6,
                    y_anim_sprite_start, paint);
            canvas.drawBitmap(cardBitmaps[(showBlinkLabel) ? sixRandomCards[i] : sixRandomCards[i] + 17],
                    -x_anim_sprite + (145 + CARD_GAP) * i + ORIGINAL_WIDTH,
                    ORIGINAL_HEIGHT - y_anim_sprite_start - 200, paint);
        }
    }

    private void paintWinAnimation(Canvas canvas, Paint paint) {
        canvas.drawBitmap(backGroundWinBitmap, 0, 0, paint);
        int x_or_y = 0;
        switch (mLevel) {
            default: {
                drawHorizontalCardAnimation(canvas, paint);
                x_or_y = x_anim_sprite;
                break;
            }
            case 1: {
                drawVerticalCardAnimation(canvas, paint);
                x_or_y = y_anim_sprite;
                break;
            }
        }
        if (showBlinkLabel) {
            canvas.drawBitmap(mLabels[mLevel - 1],
                    -(x_or_y / 2) + ORIGINAL_WIDTH,
                    ORIGINAL_HEIGHT / 2 - mLabels[mLevel - 1].getHeight() / 2,
                    paint);
        }
    }

    private void drawCardDecs(Canvas canvas, Paint paint) {
        for (int i = 0; i < COLUMNS_COUNT; ++i) {
            int listSize = columnsDecks[i].size();
            for (int j = 0; j < listSize; j++) {
                int x = columnRects[i].centerX() - mX_card_grab_coord;
                int y = columnStartHeight + columnOffsets[i] + j * CARD_GAP;
                canvas.drawBitmap(columnsDecks[i].get(j), x, y, paint);
            }
        }
    }

    private void init() {
        mOriginalScreenRect = new Rect(0, 0, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
        mOutputScreenRect = new Rect(0, 0, mScreenWidth, mScreenHeight);

        cardRect = new Rect(0, 0, cardBitmaps[0].getWidth(), cardBitmaps[0].getHeight());
        mX_card_grab_coord = cardRect.centerX();
        mY_card_grab_coord = cardRect.centerY() / 2;

        columnRects[0] = new Rect(166, 56, 166 + 151, 56 + 424);
        columnRects[1] = new Rect(325, 56, 325 + 151, 56 + 424);
        columnRects[2] = new Rect(483, 56, 483 + 151, 56 + 424);
        columnRects[3] = new Rect(642, 56, 642 + 151, 56 + 424);

        deckRect = new Rect(10, 85, 154, 284);

        flushDeck();

        resetDeckCards();
    }

    public void touchInDeckRect(int x, int y) {
        mIsGrab = deckRect.contains(x, y);
        if (mIsGrab && mPlayingGrabSound) {
            SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_GRAB);
            mPlayingGrabSound = false;
        }
    }

    public void putCardToColumn(int x, int y) {
        mX_coord_from = x;
        mY_coord_from = y;
        mIsDropingCard = true;
    }

    private void dropCard() {
        int x_to, y_to;
        if (highlightColumn == 0 || lockColumns[highlightColumn - 1]) {
            if (mPlayingErrorSound) {
                SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_ERROR);
                mPlayingErrorSound = false;
            }
            x_to = initialCardCoordX;
            y_to = initialCardCoordY;
        } else {
            x_to = columnRects[highlightColumn - 1].centerX() - mX_card_grab_coord;
            y_to = columnStartHeight + columnsDecks[highlightColumn - 1].size() * CARD_GAP;
        }

        if (x_to > mX_coord_from) {
            mX_coord_from += drop_card_speed;
            if (mX_coord_from > x_to) {
                mX_coord_from = x_to;
            }
        }

        if (x_to < mX_coord_from) {
            mX_coord_from -= drop_card_speed;
            if (mX_coord_from < x_to) {
                mX_coord_from = x_to;
            }
        }

        if (y_to > mY_coord_from) {
            mY_coord_from += drop_card_speed;
            if (mY_coord_from > y_to) {
                mY_coord_from = y_to;
            }
        }

        if (y_to < mY_coord_from) {
            mY_coord_from -= drop_card_speed;
            if (mY_coord_from < y_to) {
                mY_coord_from = y_to;
            }
        }

        setCardCoords(mX_coord_from, mY_coord_from);

        if (x_to == mX_coord_from && y_to == mY_coord_from) {
            if (highlightColumn != 0 && !lockColumns[highlightColumn - 1]) {
                if (!mIsTimerRun) {
                    timer.start();
                    mIsTimerRun = true;
                }
                scores += 50;
                addCardToColumn(highlightColumn - 1);
                switchToNextCard();
            }
            mPlayingErrorSound = true;
            mIsDropingCard = false;
            mIsGrab = false;
            highlightColumn = 0;
            mX_coord_from = 0;
            mY_coord_from = 0;
        }
    }

    private void refreshScores(int column) {
        int cardScore = mDeck[cardIndex - 1];
        columnsDecksValue[column].add(cardScore);
        if (cardScore < 9) {
            columnScores[column] += cardScore + 2;
        } else if (cardScore >= 9 && cardScore < 12) {
            columnScores[column] += 10;
        } else if (cardScore == 12) {
            if (columnScores[column] > 10) {
                columnScores[column] += 1;
            } else {
                columnScores[column] += 11;
            }
        } else {
            SnoodsGameActivity.toDebug("Joker");
            columnScores[column] = 21;
        }
    }

    private void switchToNextCard() {
        cardIndex--;
        if (cardIndex > 0) {
            mCurrentCardBitmap = cardBitmaps[mDeck[cardIndex - 1]];
            mCurrentCardBitmapToDeck = cardBitmaps[mDeck[cardIndex - 1] + 17];
            if (cardIndex > 1) {
                mNextCardBitmap = cardBitmaps[mDeck[cardIndex - 2]];
            }
        } else {
            mDeckIsEmpty = true;
        }
        mX_card_coord = initialCardCoordX;
        mY_card_coord = initialCardCoordY;
    }

    private void addCardToColumn(int column) {
        columnDropped = false;
        SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_DROP);
        refreshScores(column);
        columnsDecks[column].add(mCurrentCardBitmapToDeck);
    }

    private void dropColumn(final int column, boolean all) {
        columnDropped = true;
        mIsDropingColumn = true;
        highlightColumn = 0;
        columnOffsets[column] += drop_column_speed;
        if (animateColumn) {
            SnoodsGameActivity.toDebug("Once ?");
            snoodsGameActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    new SnoodsAnimationTimer(1000, 250, columnsDecks,
                            columnsDecksValue, lockColumns, column, cardBitmaps, false).start();
                }
            });
        }
        if (mPlayingWhooshSound) {
                SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_WHOOSH);
            if (!mDeckIsEmpty) {
                SnoodsLauncherActivity.doVibrate(SnoodsLauncherActivity.VIBRATE_SHORT);
            }
            mPlayingWhooshSound = false;
        }
        if (columnOffsets[column] > ORIGINAL_HEIGHT) {
            mPlayingWhooshSound = true;
            mIsDropingColumn = false;
            columnDropped = false;
            animateColumn = true;
            if (!all) {
                scores += 100 * (column + 1);
            } else {
                if (!mIsGameOver) {
                    scores += columnsDecks[column].size() * 200;
                }
            }
            columnsDecks[column].clear();
            columnsDecksValue[column].clear();
            columnOffsets[column] = 0;
            columnScores[column] = 0;
        }
    }

    private boolean allColumnsEmpty() {
        for (int i = 0; i < COLUMNS_COUNT; ++i) {
            if (!columnsDecks[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void dropAllColumns() {
        for (int i = COLUMNS_COUNT - 1; i >= 0; --i) {
            if (!columnsDecks[i].isEmpty()) {
                dropColumn(i, true);
                break;
            }
        }
    }

    public void lockColumn(final int column, boolean anim) {
        if (!lockColumns[column]) {
            for (int i = 0; i < columnsDecks[column].size(); ++i) {
                SnoodsGameActivity.toDebug("--------");
                columnsDecks[column].set(i, cardBitmaps[16]);
            }
            if (anim) {
                snoodsGameActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        new SnoodsAnimationTimer(1000, 250, columnsDecks,
                                columnsDecksValue, lockColumns, column, cardBitmaps, true).start();
                    }
                });
            }
            lockColumns[column] = true;
        }
    }

    private void tick() {
        if (!mIsWinAnimation) {
            if (mIsDropingCard) {
                dropCard();
            }

            int locked = 0;
            for (int i = 0; i < COLUMNS_COUNT; ++i) {
                if (columnsDecks[i].size() == 5 && columnScores[i] < 21) {
                    columnScores[i] = 21;
                    dropColumn(i, false);
                }

                if (columnScores[i] > 21) {
                    lockColumn(i, true);
                }

                if (columnScores[i] == 21) {
                    dropColumn(i, false);
                }

                if (lockColumns[i]) {
                    locked++;
                }
            }

            if (locked == COLUMNS_COUNT) {
                mDeckIsEmpty = true;
                mIsGameOver = true;
                if (mPlayingGameOverSound) {
                    SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_GAME_OVER);
                    mPlayingGameOverSound = false;
                }
            }

            if (!toastShown) {
                if (mIsGameOver) {
                    snoodsGameActivity.showToast("Game over!", Toast.LENGTH_LONG);
                    toastShown = true;
                } else if (mDeckIsEmpty) {
                    if (mLevel == 4) {
                        snoodsGameActivity.showToast("Congratulations!", Toast.LENGTH_LONG);
                    } else {
                        snoodsGameActivity.showToast("You Win! Enjoy next level!", Toast.LENGTH_LONG);
                    }
                    toastShown = true;
                }
            }

            if (mDeckIsEmpty) {
                if (!columnDropped) {
                    dropAllColumns();
                    columnDropped = false;
                }
                if (allColumnsEmpty()) {
                    if (!mIsGameOver) {
                        mIsWinAnimation = true;
                        SnoodsLauncherActivity.playSound(SnoodsLauncherActivity.SOUND_WIN);
                    } else {
                        snoodsScoreManager.checkHighScore(scores);
                    }
                    SnoodsGameActivity.toDebug("Game End! Game Over: " + mIsGameOver);
                    resetGame(mIsGameOver);
                }
            }
        } else { // mIsWinAnimation == true
            switch (mLevel) {
                default: {
                    if (x_anim_sprite % 100 == 0) {
                        showBlinkLabel = !showBlinkLabel;
                    }
                    x_anim_sprite += drop_column_speed;
                    if (x_anim_sprite > ORIGINAL_WIDTH + 200 + (145 * 6) + CARD_GAP) {
                        mIsWinAnimation = false;
                        x_anim_sprite = 0;
                    }
                    break;
                }
                case 1: {
                    if (y_anim_sprite % 100 == 0) {
                        showBlinkLabel = !showBlinkLabel;
                    }
                    y_anim_sprite += drop_column_speed;
                    if (y_anim_sprite > ORIGINAL_HEIGHT + 200 + (200 * 6) + CARD_GAP) {
                        mIsWinAnimation = false;
                        y_anim_sprite = 0;
                    }
                    break;
                }
            }
        }
    }

    private void resetGame(boolean gameOver) {
        if (!gameOver) {
            if (mLevel < 4) {
                mLevel++;
            } else {
                mLevel = 1;
            }
        } else {
            mLevel = 1;
            scores = 0;
        }
        mDeckIsEmpty = false;
        mIsDropingCard = false;
        mIsGrab = false;
        cardIndex = 6 + 13 + 13 * ((mLevel == 4) ? 3 : mLevel);

        setSixRandomCardsForAnimations();

        stopTimer();
        mIsTimerRun = false;
        switch (mLevel) {
            case 1:
            default: {
                secs = 60 * 3;
                createCountDownTimer(60000 * 3, 1000);
                break;
            }
            case 2: {
                secs = 60 * 3 - 10;
                createCountDownTimer(60000 * 3 - 10000, 1000);
                break;
            }
            case 3: {
                secs = 60 * 3 - 15;
                createCountDownTimer(60000 * 3 - 15000, 1000);
                break;
            }
            case 4: {
                secs = 60 * 3 - 20;
                createCountDownTimer(60000 * 3 - 20000, 1000);
                break;
            }
        }

        mX_card_coord = initialCardCoordX;
        mY_card_coord = initialCardCoordY;
        highlightColumn = 0;
        for (int i = 0; i < COLUMNS_COUNT; ++i) {
            lockColumns[i] = false;
        }
        flushDeck();
        resetDeckCards();
        mIsGameOver = false;
        toastShown = false;
        progressBarPercent = 0;
        mPlayingGameOverSound = true;
    }

    private void resetDeckCards() {
        mCurrentCardBitmap = cardBitmaps[mDeck[cardIndex - 1]];
        mCurrentCardBitmapToDeck = cardBitmaps[mDeck[cardIndex - 1] + 17];
        mNextCardBitmap = cardBitmaps[mDeck[cardIndex - 2]];
    }

    private void start() {
        mIsRunning = true;
        mMainThread = new Thread(this);
        mMainThread.start();
    }

    public void setCardCoords(int x, int y) {
        mX_card_coord = x;
        mY_card_coord = y;
    }

    public int detectColumn(int x, int y) {
        // 1-4 column
        for (int i = 0; i < COLUMNS_COUNT; ++i) {
            if (columnRects[i].contains(x, y)) {
                return i + 1;
            }
        }
        return 0;
    }

    public static int getmScreenWidth() {
        return mScreenWidth;
    }

    public static int getmScreenHeight() {
        return mScreenHeight;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SnoodsGameActivity.toDebug("Surface created.");
        mScreenWidth = holder.getSurfaceFrame().width();
        mScreenHeight = holder.getSurfaceFrame().height();

        init();

        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SnoodsGameActivity.toDebug("Surface changed: " +
                width + "x" + height + " | " +
                mScreenWidth + "x" + mScreenHeight + ".");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        timer.cancel();
        boolean shutdown = false;
        mIsRunning = false;
        while (!shutdown) {
            try {
                if (mMainThread != null) {
                    mMainThread.join();
                }
                shutdown = true;
            } catch (InterruptedException e) {
                SnoodsGameActivity.toDebug("Error joining to Main Thread");
            }
        }
    }

    @Override
    public void run() {
        while (mIsRunning) {
            tick();
            try {
                mMainCanvas = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    render(mMainCanvas);
                }
            } finally {
                if (mMainCanvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mMainCanvas);
                }
            }
        }
    }

    // TODO: to new class ?
    private long m_lLastCallTime = 0L;

    public int getTimesPerSecond() {
        int i = 0;
        long l1 = System.currentTimeMillis();
        long l2 = l1 - this.m_lLastCallTime;
        if (0L != l2) {
            i = (int) (1000L / l2);
        } else {
            i = 0;
        }
        this.m_lLastCallTime = l1;
        return i;
    }
}
