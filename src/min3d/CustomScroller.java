package min3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.OverScroller;


public class CustomScroller {
    private final static String TAG = "CustomScroller";
    private final static boolean DEBUG = false;

    public enum Mode{X,Y};

    private final static int FLING_HANDLER_ACTION = 0;
    private final static int FLING_HANDLER_INTERVEL = 15; // Should be shorter than 1000 / 60
    public final static float FLING_DECELERATION_INTERPOLATOR = 1.5f;

    private final static int CURRENT_VELOCITY_UNIT = 750;
    private final static int FLING_MIN_VELOCITY_VALUE = 500;
    private final static int MAX_DURATION_VALUE = 2000;

    private final static int BASE_PARAMETER_FLING = 9000;
    private final static int DURATION_SCROLLING_AUTOALIGN = 300;
    private final static int DURATION_REBOUNDING = 500;

    private static final int INVALID_POINTER = -1;

    // for check rebounding.
    public static final int FEEDBACK_WHAT = 0;
    public static final int FEEDBACK_SCROLL_TO_WHERE = 1;

    // for rebounding, 0 represents "No need"; the other represents "Need".
    public static final int NO_NEED_REBOUNDING = 0; // no need for rebounding
    public static final int NEED_REBOUNDING_LOWER = 1;
    public static final int NEED_REBOUNDING_UPPER = 2;

    private Context mContext;
    private OverScroller mScroller;
    private Handler mHandler;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    public int mScrollX = 0;
    public int mScrollY = 0;

    private float mItemSize = 0;
    private int mContentWidth;
    private int mWidth;
    private int mContentHeight;
    private int mHeight;
    private int mPadding = 0;
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId = INVALID_POINTER;

    private boolean mScrolling = false;
    private ScrollerListener mPositionListener;

    private Mode mMode = Mode.X;

    public CustomScroller(Context context, Mode mode , Interpolator interpolator) {
        mContext = context;
        mMode = mode;
        mScroller = new OverScroller(mContext, interpolator, 1, 1, true);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setHandler(Handler handler, GLSurfaceView view) {
        mHandler = handler;
    }

    public void setContentWidth(int width) {
        mContentWidth = width;
    }

    public int getContentWidth() {
        return mContentWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int setWidth() {
        return mWidth;
    }

    public void setContentHeight(int height) {
        mContentHeight = height;
    }

    public int getContentHeight() {
        return mContentHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setItemSize(float size) {
        mItemSize = size;
    }

    public void setPositionListener(ScrollerListener listener) {
        mPositionListener = listener;
    }

    public void setPadding(int value) {
        if (mMode == Mode.X) {
            mScrollX = mScrollX - mPadding + value;
        } else {
            mScrollY = mScrollY - mPadding + value;
        }

        mPadding = value;
        scrollTo(getScrollX(), getScrollY(), 0);
    }

    public int getScrollX() {
        return mScrollX - mPadding;
    }

    public int getScrollY() {
        return mScrollY - mPadding;
    }

    public int getScroll() {
        return (mMode == Mode.X)?getScrollX():getScrollY();
    }

    public void fling(int velocity) {
        final int maxX = mContentWidth - mWidth + 2 * mPadding;;
        final int maxY = mContentHeight - mHeight + 2 * mPadding;;
        int scroll = ((mMode == Mode.X)?mScroller.getCurrX():mScroller.getCurrY());
        if (mMode == Mode.X) {
            mScroller.fling(scroll, 0, velocity, 0, 0, Math.max(0, maxX), 0, 0);
        } else {
            mScroller.fling(0, scroll, 0, velocity, 0, 0, 0, Math.max(0, maxY));
        }

        computeScroll();
    }

    public void flingWithAlign(float velocity, int duration) {
        final int maxX = mContentWidth - mWidth + 2 * mPadding;
        final int maxY = mContentHeight - mHeight + 2 * mPadding;
        int forwardLimit = (mMode == Mode.X) ? maxX : maxY;
        int backwardLimit = 0;
        float ry = (10f * mItemSize) * (float) (((float) velocity / ((float) mMaximumVelocity)));
        int delta;
        int scroll = ((mMode == Mode.X)?mScroller.getCurrX():mScroller.getCurrY());
        if (velocity > 0) {
            float dyForward = (int)(ry - ry % mItemSize) - (scroll - mPadding) % mItemSize;
            if (scroll + dyForward > forwardLimit) {
                dyForward -= dyForward - forwardLimit;
            }
            delta = (int) dyForward;
        } else {
            float dyBackward = -(mItemSize - (scroll - mPadding) % mItemSize)
                    - (ry - ry % mItemSize);
            if (scroll + dyBackward < backwardLimit) {
                dyBackward -= backwardLimit - dyBackward;
            }
            delta = (int) -dyBackward;
        }
        if (mMode == Mode.X) {
            mScroller.startScroll(scroll, 0, delta, 0, duration);
        } else {
            mScroller.startScroll(0, scroll, 0, delta, duration);
        }
        computeScroll();
    }

    public void scrollWithAlign(float velocity, int duration) {
        int scroll = ((mMode == Mode.X)?mScroller.getCurrX():mScroller.getCurrY());
        int delta = (int) (((scroll - mPadding) % mItemSize > mItemSize / 2) ? ((int)(((float)scroll / mItemSize) + 1)
                * mItemSize - scroll) - (mItemSize - mPadding)
                : -((scroll - mPadding) % mItemSize));

        if (mMode == Mode.X) {
            mScroller.startScroll(scroll, 0, delta, 0, duration);
        } else {
            mScroller.startScroll(0, scroll, 0, delta, duration);
        }
        computeScroll();
    }

    public void scrollTo(int x, int y, int duration) {
        int scrollX = x + mPadding;
        int scrollY = y + mPadding;
        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), scrollX - mScroller.getCurrX(), scrollY - mScroller.getCurrY(), duration);
        computeScroll();
    }

    public boolean processScroll(MotionEvent ev) {
        boolean handled = false;
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mLastMotionX = ev.getX();
            mLastMotionY = ev.getY();
            mActivePointerId = ev.getPointerId(0);
            mIsBeingDragged = true;
            handled = true;
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            final int pointerId = mActivePointerId;

            if (mIsBeingDragged && INVALID_POINTER != pointerId) {

                final int pointerIndex = ev.findPointerIndex(pointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                if (mMode == Mode.X) {
                    int deltaX = (int) (mLastMotionX - x);

                    if (mScrolling || Math.abs(deltaX) > mTouchSlop) {
                        mLastMotionX = x;

                        mScroller.startScroll(mScrollX, 0, deltaX, 0, 0);
                        computeScroll();
                        mScrolling = true;
                        handled = true;
                    }
                } else {
                    int deltaY = (int) (mLastMotionY - y);

                    if (mScrolling || Math.abs(deltaY) > mTouchSlop) {
                        mLastMotionY = y;

                        mScroller.startScroll(0, mScrollY, 0, deltaY, 0);
                        computeScroll();
                        mScrolling = true;
                        handled = true;
                    }
                }

            }
            break;
        }
        case MotionEvent.ACTION_UP: {
            final int pointerId = mActivePointerId;
            if (mIsBeingDragged && INVALID_POINTER != pointerId) {
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(CURRENT_VELOCITY_UNIT, mMaximumVelocity);


                int initialVelocity = (mMode == Mode.X) ? (int) velocityTracker
                        .getXVelocity(pointerId) : (int) velocityTracker
                        .getYVelocity(pointerId);
                int scroll = ((mMode == Mode.X) ? mScroller.getCurrX() : mScroller.getCurrY());
                int length = ((mMode == Mode.X) ? mContentWidth : mContentHeight);
                if (Math.abs(initialVelocity) > FLING_MIN_VELOCITY_VALUE
                        && (scroll >= mPadding && scroll <= (length + mPadding))) {
                    if (mItemSize != 0) {
                        int preDuration = (int)((float)Math.abs(initialVelocity) / BASE_PARAMETER_FLING * 1000);
                        int duration = (int)(preDuration * (Math.sqrt(0.4 * (MAX_DURATION_VALUE / preDuration))));
                        flingWithAlign(-initialVelocity, duration);
                    } else {
                        fling(-initialVelocity);
                    }
                } else {
                    int [] feedback = isNeedRebounding((mMode == Mode.X)?mScrollX:mScrollY);
                    if (feedback[FEEDBACK_WHAT] == NO_NEED_REBOUNDING) {
                        if (mItemSize != 0) {
                            scrollWithAlign(-initialVelocity, DURATION_SCROLLING_AUTOALIGN);
                        } else {
                            if (!mIsBeingDragged && mPositionListener != null) {
                                mPositionListener.onScrollFinished();
                            }
                        }
                    } else {
                        rebounding(feedback);
                    }
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                mScrolling = false;
                handled = true;
            }
            break;
        }
        case MotionEvent.ACTION_CANCEL:
            if (mIsBeingDragged) {
                mActivePointerId = INVALID_POINTER;
                mIsBeingDragged = false;
                mScrolling = false;

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                handled = true;
            }
            break;
        case MotionEvent.ACTION_POINTER_DOWN: {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            final int index = ev.getActionIndex();
            mLastMotionX = (int) ev.getX(index);
            mLastMotionY = (int) ev.getY(index);
            mActivePointerId = ev.getPointerId(index);
            mIsBeingDragged = true;
            break;
        }
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            if (mIsBeingDragged) {
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
            }
            break;
        }
        return handled;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);

            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private Runnable mFlingRunnable = new Runnable() {
        @Override
        public void run() {
            computeScroll();
        }
    };

    private void computeScroll() {
        final OverScroller scroller = mScroller;
        if (scroller.computeScrollOffset()) {
            int scrollX = scroller.getCurrX();
            int scrollY = scroller.getCurrY();
            final int maxX = mContentWidth - mWidth + 2 * mPadding;
            final int maxY = mContentHeight - mHeight + 2 * mPadding;
            if (mMode == Mode.X) {
                if (scrollX < 0)
                    scrollX = 0;
                else if (scrollX > maxX)
                    scrollX = maxX;

                mScrollX = scrollX;
                if (mPositionListener != null) {
                    mPositionListener.onScrollChanged(getScrollX(), getScrollY());
                }
                if (scrollX != mScroller.getFinalX() && !((scrollX == 0 && mScroller.getFinalX() < scrollX) || (scrollX == maxX && mScroller.getFinalX() > scrollX))) {
                    mHandler.removeCallbacks(mFlingRunnable);
                    mHandler.postDelayed(mFlingRunnable, FLING_HANDLER_INTERVEL);
                } else {
                    if (!mIsBeingDragged && mPositionListener != null) {
                        mPositionListener.onScrollFinished();

                        int [] feedback = isNeedRebounding((mMode == Mode.X)?mScrollX:mScrollY);
                        if (feedback[FEEDBACK_WHAT] != NO_NEED_REBOUNDING) {
                            rebounding(feedback);
                        }
                    }
                }
            } else {
                if (scrollY < 0)
                    scrollY = 0;
                else if (scrollY > maxY)
                    scrollY = maxY;

                mScrollY = scrollY;
                if (mPositionListener != null) {
                    mPositionListener.onScrollChanged(getScrollX(), getScrollY());
                }
                if (scrollY != mScroller.getFinalY() && !((scrollY == 0 && mScroller.getFinalY() < scrollY) || (scrollY == maxY && mScroller.getFinalY() > scrollY))) {
                    mHandler.removeCallbacks(mFlingRunnable);
                    mHandler.postDelayed(mFlingRunnable, FLING_HANDLER_INTERVEL);
                } else {
                    if (!mIsBeingDragged && mPositionListener != null) {
                        mPositionListener.onScrollFinished();

                        int [] feedback = isNeedRebounding((mMode == Mode.X)?mScrollX:mScrollY);
                        if (feedback[FEEDBACK_WHAT] != NO_NEED_REBOUNDING) {
                            rebounding(feedback);
                        }
                    }
                }
            }
        } else {
            mHandler.removeCallbacks(mFlingRunnable);
        }
    }

    private void rebounding(int [] feedback) {
        int scrollToWhere = feedback[FEEDBACK_SCROLL_TO_WHERE];
        if (mMode == Mode.X) {
            mScroller.startScroll(mScrollX, 0, scrollToWhere - mScrollX, 0, DURATION_REBOUNDING);
        } else {
            mScroller.startScroll(0, mScrollY, 0, scrollToWhere - mScrollY, DURATION_REBOUNDING);
        }
        computeScroll();
        if (DEBUG) {
            Log.d(TAG, "Rebounding finish, and you will go to " + scrollToWhere);
        }
    }

    public boolean isScrolling() {
        return mScrolling;
    }

    // Used in rebounding function to check if needs rebounding.
    public int[] isNeedRebounding(int scrollToWhere) {
        final int maxX = mContentWidth - mWidth + 2 * mPadding;;
        final int maxY = mContentHeight - mHeight + 2 * mPadding;;
        int forwardLimit = ((mMode == Mode.X) ? maxX : maxY) - mPadding;
        int backwardLimit = mPadding;
        int [] feedback = {NO_NEED_REBOUNDING, forwardLimit};

        if (scrollToWhere < backwardLimit) {
            feedback[FEEDBACK_WHAT] = NEED_REBOUNDING_LOWER;
            feedback[FEEDBACK_SCROLL_TO_WHERE]  = backwardLimit;
            if (DEBUG) {
                Log.d(TAG, "Too upper! you will go to " + feedback[1]);
            }
        } else if (scrollToWhere > forwardLimit) {
            feedback[FEEDBACK_WHAT] = NEED_REBOUNDING_UPPER;
            feedback[FEEDBACK_SCROLL_TO_WHERE] = forwardLimit;
            if (DEBUG) {
                Log.d(TAG, "Too lower! you will go to " + feedback[1]);
            }
        } else {
            feedback[FEEDBACK_WHAT] = NO_NEED_REBOUNDING;
            feedback[FEEDBACK_SCROLL_TO_WHERE] = scrollToWhere;
            if (DEBUG) {
                Log.d(TAG, "No need to rebounding, so just return it. " + scrollToWhere + " " + forwardLimit);
            }
        }
        return feedback;
    }

    public interface ScrollerListener {
        void onScrollChanged(int scrollX, int scrollY);
        void onScrollFinished();
    }
}