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

import java.util.ArrayList;
import java.util.Random;

public class SnoodsSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {

    public static final int ORIGINAL_WIDTH = 800;
    public static final int ORIGINAL_HEIGHT = 480;

    public static final int DROP_CARD_SPEED = 15;
    public static final int DROP_COLUMN_SPEED = 10;
    public static final int CARD_GAP = 30;

    public static final String PAINT_TEXT = "Canvas Example";

    private Rect mOriginalScreenRect = null;
    private Rect mOutputScreenRect = null;

    private static int mScreenWidth = 0;
    private static int mScreenHeight = 0;

    private final int initialCardCoordX = 10;
    private final int initialCardCoordY = 85;
    private int mX_card_coord = initialCardCoordX;
    private int mY_card_coord = initialCardCoordY;
    public static int mX_card_conv_coord = 0;
    public static int mY_card_conv_coord = 0;

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
    private Bitmap mDigitsBitmap = null;

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
    private Bitmap mNextCardBitmap = null;

    private ArrayList<Bitmap>[] columnsDecks = null;
    private int cardIndex = 20;

    private Rect deckRect = null;

    public boolean mDeckIsEmpty = false;

    private int columnStartHeight = 111;
    private int[] columnOffsets = null;

    public SnoodsSurfaceView(Context context) {
        super(context);

        mContext = context;

        mRandom = new Random();

        mGameBitmap = Bitmap.createBitmap(ORIGINAL_WIDTH, ORIGINAL_HEIGHT, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mGameBitmap);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mMainPaint = new Paint();

        mBackGroundBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bkg_moto);
        mScoreLabelBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.score_moto);
//        mCurrentCardBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto);

        columnRects = new Rect[4];
        cardBitmaps = new Bitmap[3];
        cardBitmaps[0] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto);
        cardBitmaps[1] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto2);
        cardBitmaps[2] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.card_moto3);

        columnsDecks = new ArrayList[4];
        columnOffsets = new int[4];
        for (int i = 0; i < 4; ++i) {
            columnsDecks[i] = new ArrayList<Bitmap>();
            columnOffsets[i] = 0;
        }

        // Set screen always on
        setKeepScreenOn(true);

        // Focus
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private void flushDeck() {
        mDeck = new int[20];
        for (int i = 0; i < 20; ++i) {
            mDeck[i] = mRandom.nextInt(2 + 1);
        }

        int _Change, _Tmp;
        for (int Num1 = 0; Num1 < 20; Num1++) {
            _Change = mRandom.nextInt(19 + 1);
            _Tmp = mDeck[Num1];
            mDeck[Num1] = mDeck[_Change];
            mDeck[_Change] = _Tmp;
        }

        String c = "";
        for (int i = 0; i < 20; ++i) {
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
            paint.setColor(Color.parseColor("#7733AF54"));
            canvas.drawRect(columnRects[column - 1], paint);
            paint.reset();
        }
    }

    private void render(Canvas canvas) {
        if (canvas != null) {
            // Draw background
            mBitmapCanvas.drawBitmap(mBackGroundBitmap, 0, 0, mMainPaint);

            // Draw score label
            mBitmapCanvas.drawBitmap(mScoreLabelBitmap, 419, 2, mMainPaint);

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
        for (int i = 0; i < 4; ++i) {
            int listSize = columnsDecks[i].size();
            for (int j = 0; j < listSize; j++) {
                int x = columnRects[i].centerX() - mX_card_conv_coord;
                int y = columnStartHeight + columnOffsets[i] + j * CARD_GAP;
                canvas.drawBitmap(columnsDecks[i].get(j), x, y, paint);
            }
        }
    }

    private void init() {
        mOriginalScreenRect = new Rect(0, 0, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
        mOutputScreenRect = new Rect(0, 0, mScreenWidth, mScreenHeight);

        cardRect = new Rect(0, 0, cardBitmaps[0].getWidth(), cardBitmaps[0].getHeight());
        mX_card_conv_coord = cardRect.centerX();
        mY_card_conv_coord = cardRect.centerY() / 2;

        columnRects[0] = new Rect(166, 56, 166 + 151, 56 + 424);
        columnRects[1] = new Rect(325, 56, 325 + 151, 56 + 424);
        columnRects[2] = new Rect(483, 56, 483 + 151, 56 + 424);
        columnRects[3] = new Rect(642, 56, 642 + 151, 56 + 424);

        deckRect = new Rect(10, 85, 154, 284);

        flushDeck();

        mCurrentCardBitmap = cardBitmaps[mDeck[20 - 1]];
        mNextCardBitmap = cardBitmaps[mDeck[20 - 2]];
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
        if (highlightColumn == 0) {
            x_to = initialCardCoordX;
            y_to = initialCardCoordY;
        } else {
            x_to = columnRects[highlightColumn - 1].centerX() - mX_card_conv_coord;
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
            if (highlightColumn != 0) {
                addCardToColumn();
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

    private void addCardToColumn() {
        columnsDecks[highlightColumn - 1].add(mCurrentCardBitmap);
    }

    private void dropColumn(int column) {
        columnOffsets[column] += DROP_COLUMN_SPEED;
        if (columnOffsets[column] > mScreenHeight) {
            columnsDecks[column].clear();
            columnOffsets[column] = 0;
        }
    }

    private boolean allColumnsEmpty() {
        for (int i = 0; i < 4; ++i) {
            if (!columnsDecks[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void dropAllColumns() {
        for (int i = 4 - 1; i >= 0; --i) {
            if (!columnsDecks[i].isEmpty()) {
                dropColumn(i);
                break;
            }
        }
    }

    private void tick() {
        if (mIsDropingCard) {
            dropCard();
        }

        for (int i = 0; i < 4; ++i) {
            if (columnsDecks[i].size() == 10) {
                dropColumn(i);
            }
        }

        if (mDeckIsEmpty) {
            dropAllColumns();
            if (allColumnsEmpty()) {
                SnoodsActivity.toDebug("Win!");
                resetGame();
            }
        }
    }

    private void resetGame() {
        mDeckIsEmpty = false;
        cardIndex = 20;
        flushDeck();
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
        for (int i = 0; i < 4; ++i) {
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