package ru.exlmoto.canvasexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CanvasExampleView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {

    public static final int ORIGINAL_WIDTH = 800;
    public static final int ORIGINAL_HEIGHT = 480;

    public static final String PAINT_TEXT = "Canvas Example";

    private Rect mOriginalScreenRect = null;
    private Rect mOutputScreenRect = null;

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    private int mX_coord = 0;
    private int mY_coord = 0;

    private boolean mIsRunning = false;

    private Thread mMainThread = null;
    private Canvas mMainCanvas = null;
    private Paint mMainPaint = null;

    private Bitmap mGameBitmap = null;
    private Canvas mBitmapCanvas = null;

    private SurfaceHolder mSurfaceHolder = null;

    public CanvasExampleView(Context context) {
        super(context);

        mGameBitmap = Bitmap.createBitmap(ORIGINAL_WIDTH, ORIGINAL_HEIGHT, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mGameBitmap);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mMainPaint = new Paint();

        // Set screen always on
        setKeepScreenOn(true);

        // Focus
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private void render(Canvas canvas) {
        if (canvas != null) {
            mBitmapCanvas.drawColor(Color.parseColor("#FFA040"));
            mMainPaint.setColor(Color.parseColor("#6DB45D"));
            mMainPaint.setTextSize(48.0f);
            mBitmapCanvas.drawText(PAINT_TEXT, mX_coord, mY_coord, mMainPaint);

            canvas.drawBitmap(mGameBitmap, mOriginalScreenRect, mOutputScreenRect, mMainPaint);
        }
    }

    private void init() {
        mOriginalScreenRect = new Rect(0, 0, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
        mOutputScreenRect = new Rect(0, 0, mScreenWidth, mScreenHeight);
    }

    private void tick() {
        mX_coord++;
        mY_coord++;
        if (mY_coord > mScreenHeight) {
            mX_coord = 0;
            mY_coord = 0;
        }
    }

    private void start() {
        mIsRunning = true;
        mMainThread = new Thread(this);
        mMainThread.start();
    }

    public void setCoords(int x, int y) {
        mX_coord = x;
        mY_coord = y;
    }

    public int getmScreenWidth() {
        return mScreenWidth;
    }

    public int getmScreenHeight() {
        return mScreenHeight;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CanvasExampleActivity.toDebug("Surface created.");
        mScreenWidth = holder.getSurfaceFrame().width();
        mScreenHeight = holder.getSurfaceFrame().height();

        init();

        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        CanvasExampleActivity.toDebug("Surface changed: " +
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
                CanvasExampleActivity.toDebug("Error joining to Main Thread");
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
