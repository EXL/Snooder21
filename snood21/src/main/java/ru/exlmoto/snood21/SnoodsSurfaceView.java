package ru.exlmoto.snood21;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class SnoodsSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {

    public static final int ORIGINAL_WIDTH = 800;
    public static final int ORIGINAL_HEIGHT = 480;

    private static final int BITMAPS_COUNT = 15;
    private static final int COLUMNS_COUNT = 4;

    public static final int DIGITS_NUM = 13;

    public static final int DROP_CARD_SPEED = 15;
    public static final int DROP_COLUMN_SPEED = 10;
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
    private Bitmap mBackGroundBitmap = null;
    private Bitmap mScoreLabelBitmap = null;
    private Bitmap mCharBitmap = null;

    private Bitmap[] mChars = null;

    private Rect cardRect = null;
    private Rect[] columnRects = null;
    private int highlightColumn = 0;

    private int mX_coord_from = 0;
    private int mY_coord_from = 0;
    public boolean mIsDropingCard = false;
    public boolean mIsGrab = false;

    private int mLevel = 0;
    private int[] mDeck = null;

    private Random mRandom = null;
    private Bitmap[] cardBitmaps = null;
    private Bitmap mNextCardBitmap = null;

    private ArrayList<Bitmap>[] columnsDecks = null;
    private int cardIndex = 32; // First level
    private int scores = 0;

    private Rect deckRect = null;

    public boolean mDeckIsEmpty = false;

    private int columnStartHeight = 111;
    private int[] columnOffsets = null;

    private CountDownTimer timer = null;
    private int secs = 60 * 3;

    public boolean mIsDropingColumn = false;
    public boolean[] lockColumns = null;
    private int[] columnScores = null;

    public boolean mIsGameOver = false;
    //public boolean mIsLastLevel = false;

    private SnoodsActivity snoodsActivity = null;
    private boolean toastShown = false;

    private float progressBarPercent = 0;

    private boolean mIsTimerRun = false;

    public SnoodsSurfaceView(Context context, final SnoodsActivity snoodsActivity) {
        super(context);

        this.snoodsActivity = snoodsActivity;
        mContext = context;

        mRandom = new Random();

        mGameBitmap = Bitmap.createBitmap(ORIGINAL_WIDTH, ORIGINAL_HEIGHT, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mGameBitmap);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mMainPaint = new Paint();

        mBackGroundBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bkg_moto);
        mScoreLabelBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.score_moto);
        mCharBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.chars_moto);
        mChars = new Bitmap[DIGITS_NUM];
        fillDigitsBitmap();

        columnRects = new Rect[COLUMNS_COUNT];
        cardBitmaps = new Bitmap[BITMAPS_COUNT];

        /* function generateNames() {
        var size = 15;
            for (var i = 0; i < size; ++i) {
                console.log('cardBitmaps[' + i +
                '] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto' + i +
                ');');
            }
        } */

        cardBitmaps[0] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto0);
        cardBitmaps[1] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto1);
        cardBitmaps[2] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto2);
        cardBitmaps[3] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto3);
        cardBitmaps[4] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto4);
        cardBitmaps[5] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto5);
        cardBitmaps[6] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto6);
        cardBitmaps[7] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto7);
        cardBitmaps[8] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto8);
        cardBitmaps[9] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto9);
        cardBitmaps[10] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto10);
        cardBitmaps[11] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto11);
        cardBitmaps[12] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto12);
        cardBitmaps[13] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto13);
        cardBitmaps[14] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto14);

        columnsDecks = new ArrayList[COLUMNS_COUNT];
        columnOffsets = new int[COLUMNS_COUNT];
        lockColumns = new boolean[COLUMNS_COUNT];
        columnScores = new int[COLUMNS_COUNT];
        for (int i = 0; i < COLUMNS_COUNT; ++i) {
            columnsDecks[i] = new ArrayList<Bitmap>();
            columnOffsets[i] = 0;
            columnScores[i] = 0;
            lockColumns[i] = false;
        }

        // TODO: refactor overload
        timer = new CountDownTimer(60000 * 3, 1000) { // 3 min

            private float dec = 400 / 180;

            @Override
            public void onTick(long millisUntilFinished) {
                if (!mDeckIsEmpty) {
                    progressBarPercent += dec; // 3 min
                    secs = (int) millisUntilFinished / 1000;
                    if (secs == 20) {
                        snoodsActivity.showToast("Hurry up!", Toast.LENGTH_SHORT);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (!mDeckIsEmpty) {
                    secs = 0;
                    progressBarPercent = 0;
                    snoodsActivity.showToast("Time is over!", Toast.LENGTH_SHORT);
                    mDeckIsEmpty = true;
                    mIsGameOver = true;
                    this.cancel();
                    mIsTimerRun = false;
                }
            }
        }/*.start()*/;

        // Set screen always on
        setKeepScreenOn(true);

        // Focus
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private void fillDigitsBitmap() {
        for (int i = 0; i < DIGITS_NUM; ++i) {
            int x_off = i * 29;
            SnoodsActivity.toDebug("" + x_off + " " + mCharBitmap.getWidth() + " " + mCharBitmap.getHeight());
            mChars[i] = Bitmap.createBitmap(mCharBitmap, x_off, 0, 29, 52);
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
            int secondes = secs % 60;

            String timeS = "";
            if (minutes < 10) {
                timeS += 0;
            }
            timeS += minutes;
            if (secondes < 10) {
                timeS += 0;
            }
            timeS += secondes;
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
        for (int i = 0, j = 0; i < cardIndex; ++i, ++j) {
            if (j > 12) {
                j = 0;
            }
            mDeck[i] = j;

            if (i >= cardIndex - 6) {
                mDeck[i] = 13; // Jokers
            }
        }

        String c = "";
        for (int i = 0; i < cardIndex; ++i) {
            c += mDeck[i] + " ";
        }
        SnoodsActivity.toDebug(c);


//        for (int i = 1; i <= 6 + 13 + 13 * mLevel; ++i) {
//            if (i < 14) {
//                mDeck[i - 1] = i;
//            } else if (i > 13) {
//                mDeck[i - 1] = i - 14;
//            }
//        }

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
        SnoodsActivity.toDebug(c);
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
        canvas.drawRect(0, 0, progressBarPercent, 54, paint);
        paint.reset();
    }

    private void render(Canvas canvas) {
        if (canvas != null) {
            // Draw background
            mBitmapCanvas.drawBitmap(mBackGroundBitmap, 0, 0, mMainPaint);

            // Draw score label
            mBitmapCanvas.drawBitmap(mScoreLabelBitmap, 419, 2, mMainPaint);

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
            paintNumber(mBitmapCanvas, mMainPaint, secs, 124, 4, false, true);

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

            mMainPaint.setFilterBitmap(true);
            canvas.drawBitmap(mGameBitmap, mOriginalScreenRect, mOutputScreenRect, mMainPaint);
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
    }

    public void putCardToColumn(int x, int y) {
        mX_coord_from = x;
        mY_coord_from = y;
        mIsDropingCard = true;
    }

    private void dropCard() {
        int x_to, y_to;
        if (highlightColumn == 0 || lockColumns[highlightColumn - 1]) {
            x_to = initialCardCoordX;
            y_to = initialCardCoordY;
        } else {
            x_to = columnRects[highlightColumn - 1].centerX() - mX_card_grab_coord;
            y_to = columnStartHeight + columnsDecks[highlightColumn - 1].size() * CARD_GAP;
        }

        if (x_to > mX_coord_from) {
            mX_coord_from += DROP_CARD_SPEED;
            if (mX_coord_from > x_to) {
                mX_coord_from = x_to;
            }
        }

        if (x_to < mX_coord_from) {
            mX_coord_from -= DROP_CARD_SPEED;
            if (mX_coord_from < x_to) {
                mX_coord_from = x_to;
            }
        }

        if (y_to > mY_coord_from) {
            mY_coord_from += DROP_CARD_SPEED;
            if (mY_coord_from > y_to) {
                mY_coord_from = y_to;
            }
        }

        if (y_to < mY_coord_from) {
            mY_coord_from -= DROP_CARD_SPEED;
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
            mIsDropingCard = false;
            mIsGrab = false;
            highlightColumn = 0;
            mX_coord_from = 0;
            mY_coord_from = 0;
            return;
        }
    }

    private void refreshScores(int column) {
        int cardScore = mDeck[cardIndex - 1];
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
            SnoodsActivity.toDebug("Joker");
            columnScores[column] = 21;
        }
    }

    private void switchToNextCard() {
        cardIndex--;
        if (cardIndex > 0) {
            mCurrentCardBitmap = cardBitmaps[mDeck[cardIndex - 1]];
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
        refreshScores(column);
        columnsDecks[column].add(mCurrentCardBitmap);
    }

    private void dropColumn(int column, boolean all) {
        mIsDropingColumn = true;
        highlightColumn = 0;
        columnOffsets[column] += DROP_COLUMN_SPEED;
        if (columnOffsets[column] > mScreenHeight) {
            mIsDropingColumn = false;
            if (!all) {
                scores += 100 * (column + 1);
            } else {
                if (!mIsGameOver) {
                    scores += columnsDecks[column].size() * 200;
                }
            }
            columnsDecks[column].clear();
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

    private void lockColumn(int column) {
        for (int i = 0; i < columnsDecks[column].size(); ++i) {
            columnsDecks[column].set(i, cardBitmaps[14]);
        }
        lockColumns[column] = true;
    }

    private void tick() {
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
                lockColumn(i);
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
        }

        if (!toastShown) {
            if (mIsGameOver) {
                snoodsActivity.showToast("Game over!", Toast.LENGTH_LONG);
                toastShown = true;
            } else if (mDeckIsEmpty) {
                if (mLevel == 4) {
                    snoodsActivity.showToast("Congratulations!", Toast.LENGTH_LONG);
                } else {
                    snoodsActivity.showToast("You Win! Enjoy next level!", Toast.LENGTH_LONG);
                }
                toastShown = true;
            }
        }

        if (mDeckIsEmpty) {
            dropAllColumns();
            if (allColumnsEmpty()) {
                SnoodsActivity.toDebug("Game End! Game Over: " + mIsGameOver);
                resetGame(mIsGameOver);
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
        timer.cancel();
        mIsTimerRun = false;
        secs = 60 * 3;
        //timer.start();
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
    }

    private void resetDeckCards() {
        mCurrentCardBitmap = cardBitmaps[mDeck[cardIndex - 1]];
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
        SnoodsActivity.toDebug("Surface created.");
        mScreenWidth = holder.getSurfaceFrame().width();
        mScreenHeight = holder.getSurfaceFrame().height();

        init();

        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SnoodsActivity.toDebug("Surface changed: " +
                width + "x" + height + " | " +
                mScreenWidth + "x" + mScreenHeight + ".");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean shutdown = false;
        mIsRunning = false;
        while (!shutdown) {
            try {
                if (mMainThread != null) {
                    mMainThread.join();
                }
                shutdown = true;
            } catch (InterruptedException e) {
                SnoodsActivity.toDebug("Error joining to Main Thread");
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
}
