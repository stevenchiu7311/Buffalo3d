package min3d.component;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import min3d.FloatOverScroller;
import min3d.GLConfiguration;
import min3d.core.GContext;
import min3d.core.Object3d;
import min3d.core.Object3dContainer;
import min3d.vos.Number3d;
import min3d.vos.Ray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrollContainer extends Object3dContainer {
    private static final String TAG = "ScrollView";

    private final static boolean DEBUG = false;
    private static final boolean BOUND_RAY_DETECTION = false;

    public enum Mode{X,Y};

    private final static int FLING_HANDLER_INTERVEL = 10; // Should be shorter than 1000 / 60
    public final static float FLING_DECELERATION_INTERPOLATOR = 1.5f;

    private final static int CURRENT_VELOCITY_UNIT = 30;
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
    private FloatOverScroller mScroller;
    private Handler mHandler;
    private float mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    public float mScrollX = 0;
    public float mScrollY = 0;

    private float mItemSize = 0;
    private float mContentWidth;
    private float mWidth;
    private float mContentHeight;
    private float mHeight;
    private float mPadding = 0;
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId = INVALID_POINTER;

    private boolean mScrolling = false;
    private ScrollContainerListener mScrollContainerListener;

    class ScrollItemInfo {
        int mVisibility;
        boolean mFirst;
        Number3d mPosition;
    }

    private Mode mMode;

    private Ray[] mBoundRay = null;
    private Map<Object3d, ScrollItemInfo> mMap = new HashMap<Object3d, ScrollItemInfo>();
    private boolean mInit;
    private float mRatio = 0f;

    protected Number3d mSize = null;
    protected float mBound = 1f;
    protected float mScrollTemp = -1;
    private int mOrientation;

    public ScrollContainer(GContext context, Mode mode) {
        super(context);

        mContext = context.getContext();
        mMode = mode;
        mScroller = new FloatOverScroller(mContext, new DecelerateInterpolator(FLING_DECELERATION_INTERPOLATOR), 1, 1, true);
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = 0.1f;
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setHandler(getHandler(), getGContext().getGLSurfaceView());
    }

    public void setOverScrollRange(float range) {
        if (range != getPadding()) {
            setPadding(range);
        }
    }

    public void setScrollRange(float range) {
        if (mMode == Mode.X) {
            if (range != getContentWidth()) {
                setContentWidth(range);
            }
        } else {
            if (range != getContentHeight()) {
                setContentHeight(range);
            }
        }
    }

    public void setAlignment(float size) {
        if (size != getItemSize()) {
            setItemSize(size);
        }
    }

    public float getScrollRange() {
        return (mMode == Mode.X) ? getContentWidth() : getContentHeight();
    }

    public void addMotionEvent(MotionEvent event) {
        if (mScroller != null) {
            processScroll(event);
        }
    }

    public boolean onInterceptTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        if (isEnabled()) {
            processScroll(event);

            if (isScrolling()) {
                return true;
            }
            return false;
        }

        return super.onInterceptTouchEvent(ray, event, list);
    }

    public boolean onTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        if (isEnabled()) {
            processScroll(event);
            return true;
        }
        return super.onTouchEvent(ray, event, list);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            processScroll(event);
            event.recycle();
        }
        invalidate();
    }

    public void invalidate() {
        super.invalidate();
        mScrollTemp = Integer.MIN_VALUE;
    }

    public void setRenderBound(float bound) {
        mBound = bound;
    }

    protected void onRender() {
        super.onRender();

        if (!mInit) {
            mSize = getGContext().getRenderer().getWorldPlaneSize(position().z);
            mRatio = (mMode == Mode.X) ? mSize.x
                    / getGContext().getRenderer().getWidth() : mSize.y
                    / getGContext().getRenderer().getHeight();

            if (BOUND_RAY_DETECTION) {
                mBoundRay = new Ray[2];
                Number3d source[] = new Number3d[2];
                Number3d direction[] = new Number3d[2];
                source[0] = new Number3d(-mSize.x / 2, mSize.y / 2, 0);
                direction[0] = (mMode == Mode.X)?new Number3d(0, -mSize.y / 2 - mSize.y / 2, 0):new Number3d(mSize.x / 2 - -mSize.x / 2, 0, 0);
                direction[0].normalize();
                mBoundRay[0] = new Ray(source[0], direction[0]);

                source[1] = (mMode == Mode.X)?new Number3d(mSize.x / 2, mSize.y / 2, 0):new Number3d(-mSize.x / 2, -mSize.y / 2, 0);
                direction[1] = (mMode == Mode.X)?new Number3d(0, -mSize.y / 2 - mSize.y / 2, 0):new Number3d(mSize.x / 2 - -mSize.x / 2, 0, 0);
                direction[1].normalize();
                mBoundRay[1] = new Ray(source[1], direction[1]);
            }
            mInit = true;
        }

        if (numChildren() > 0) {
            if (mScrollTemp != getScroll()) {
                Object3dContainer directChild = (Object3dContainer) getChildAt(0);
                Number3d position = mMap.get(directChild).mPosition;
                if ((mMode == Mode.X)) {
                    directChild.position().x = -getScroll();
                    position.x = -getScroll();
                } else {
                    directChild.position().y = getScroll();
                    position.y = getScroll();
                }
                computeScroll();
            }
        }
        mScrollTemp = getScroll();
    }

    public void computeScroll() {
        List<Object3d> mightInBound = new ArrayList<Object3d>();
        List<Object3d> visibilityChanged = new ArrayList<Object3d>();
        Object3dContainer directChild = (Object3dContainer) getChildAt(0);
        calculateObjectCoodinate(directChild, mightInBound);
        if (BOUND_RAY_DETECTION) getGContext().getRenderer().updateAABBCoord();
        calculateObjectVisibility(mightInBound, visibilityChanged);
        mightInBound.clear();
        if (!visibilityChanged.isEmpty() && mScrollContainerListener != null) {
            mScrollContainerListener.onItemVisibilityChanged(visibilityChanged);
        }
    }

    void calculateObjectCoodinate(Object3dContainer parent, List<Object3d> mightInBound) {
        for (int i = 0; i < parent.numChildren(); i++) {
            Object3d obj = parent.getChildAt(i);
            addInnerChild(obj);
            Number3d.add(mMap.get(obj).mPosition, obj.position(), mMap.get(parent).mPosition);
            if (obj.getVertices().capacity() > 0) {
                obj.setVisibility(Object3d.GONE);
                mightInBound.add(obj);
            }

            if (obj instanceof Object3dContainer && !(obj instanceof ScrollContainer)) {
                calculateObjectCoodinate((Object3dContainer)obj, mightInBound);
            }
        }
    }

    void calculateObjectVisibility(List<Object3d> mightInBound, List<Object3d> visibilityChanged) {
        float bound[] = new float[2];
        bound[0] = (mMode == Mode.X) ? -mSize.x * mBound : -mSize.y * mBound;
        bound[1] = (mMode == Mode.X) ? mSize.x * mBound : mSize.y * mBound;

        if ((BOUND_RAY_DETECTION)) {
            bound[0] /= 2;
            bound[1] /= 2;
        }

        for (Object3d obj:mightInBound) {
            float pos = (mMode == Mode.X)?mMap.get(obj).mPosition.x:mMap.get(obj).mPosition.y;

            int orig = mMap.get(obj).mVisibility;
            if (pos > bound[0] && pos < bound[1]) {
                obj.setVisibility(Object3d.VISIBLE);
                mMap.get(obj).mVisibility = Object3d.VISIBLE;
            } else if (BOUND_RAY_DETECTION && (obj.intersects(mBoundRay[0]) || obj.intersects(mBoundRay[1]))) {
                obj.setVisibility(Object3d.VISIBLE);
                mMap.get(obj).mVisibility = Object3d.VISIBLE;
            } else {
                obj.setVisibility(Object3d.GONE);
                mMap.get(obj).mVisibility = Object3d.GONE;
            }

            int after = mMap.get(obj).mVisibility;
            if (mMap.get(obj).mFirst) {
                mMap.get(obj).mFirst = false;
                visibilityChanged.add(obj);
            } else if (after != orig && (after == Object3d.VISIBLE || after == Object3d.GONE)) {
                visibilityChanged.add(obj);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addChild(Object3d $o) {
        if (numChildren() > 0) {
            throw new IllegalStateException("ScrollContainer can host only one direct child");
        }
        super.addChild($o);
        addInnerChild($o);
    }

    /**
     * {@inheritDoc}
     */
    public void addChildAt(Object3d $o, int $index) {
        if (numChildren() > 0) {
            throw new IllegalStateException("ScrollContainer can host only one direct child");
        }
        super.addChildAt($o, $index);
        addInnerChild($o);
    }

    public void addInnerChild(Object3d $o) {
        if (!mMap.containsKey($o)) {
            ScrollItemInfo info = new ScrollItemInfo();
            info.mFirst = true;
            info.mVisibility = Object3d.GONE;
            info.mPosition = $o.position().clone();
            mMap.put($o, info);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeChild(Object3d $o) {
        mMap.clear();
        return super.removeChild($o);
    }

    /**
     * {@inheritDoc}
     */
    public Object3d removeChildAt(int $index) {
        Object3d $o = super.removeChildAt($index);
        mMap.clear();
        return $o;
    }

    public interface ScrollContainerListener {
        void onItemVisibilityChanged(List<Object3d> visibilityChanged);
        void onScrollChanged(float scrollX, float scrollY);
        void onScrollFinished();
    }

    public void setHandler(Handler handler, GLSurfaceView view) {
        mHandler = handler;
    }

    public void setContentWidth(float width) {
        mContentWidth = width;
    }

    public float getContentWidth() {
        return mContentWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public float setWidth() {
        return mWidth;
    }

    public void setContentHeight(float height) {
        mContentHeight = height;
    }

    public float getContentHeight() {
        return mContentHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setItemSize(float size) {
        mItemSize = size;
    }

    public float getItemSize() {
        return mItemSize;
    }

    public void setScrollContainerListener(ScrollContainerListener listener) {
        mScrollContainerListener = listener;
    }

    public void setPadding(float value) {
        if (mMode == Mode.X) {
            mScrollX = mScrollX - mPadding + value;
        } else {
            mScrollY = mScrollY - mPadding + value;
        }

        mPadding = value;
        scrollTo(getScrollX(), getScrollY(), 0);
    }

    public float getPadding() {
        return mPadding;
    }

    public float getScrollX() {
        return mScrollX - mPadding;
    }

    public float getScrollY() {
        return mScrollY - mPadding;
    }

    public float getScroll() {
        return (mMode == Mode.X)?getScrollX():getScrollY();
    }

    public void fling(float velocity) {
        final float maxX = mContentWidth - mWidth + 2 * mPadding;;
        final float maxY = mContentHeight - mHeight + 2 * mPadding;;
        float scroll = ((mMode == Mode.X)?mScroller.getCurrX():mScroller.getCurrY());
        if (mMode == Mode.X) {
            mScroller.fling(scroll, 0, velocity, 0, 0, Math.max(0, maxX), 0, 0);
        } else {
            mScroller.fling(0, scroll, 0, velocity, 0, 0, 0, Math.max(0, maxY));
        }

        computeScrollOffest();
    }

    public void flingWithAlign(float velocity, int duration) {
        final float maxX = mContentWidth - mWidth + 2 * mPadding;
        final float maxY = mContentHeight - mHeight + 2 * mPadding;
        float forwardLimit = (mMode == Mode.X) ? maxX : maxY;
        float backwardLimit = 0;
        float ry = (10f * mItemSize) * (float) (((float) velocity / ((float) mMaximumVelocity)));
        float delta;
        float scroll = ((mMode == Mode.X)?mScroller.getCurrX():mScroller.getCurrY());
        if (velocity > 0) {
            float dyForward = (ry - ry % mItemSize) - (scroll - mPadding) % mItemSize;
            if (scroll + dyForward > forwardLimit) {
                dyForward -= dyForward - forwardLimit;
            }
            delta = dyForward;
        } else {
            float dyBackward = -(mItemSize - (scroll - mPadding) % mItemSize)
                    - (ry - ry % mItemSize);
            if (scroll + dyBackward < backwardLimit) {
                dyBackward -= backwardLimit - dyBackward;
            }
            delta = -dyBackward;
        }
        if (mMode == Mode.X) {
            mScroller.startScroll(scroll, 0, delta, 0, duration);
        } else {
            mScroller.startScroll(0, scroll, 0, delta, duration);
        }
        computeScroll();
    }

    public void scrollWithAlign(float velocity, int duration) {
        float scroll = ((mMode == Mode.X)?mScroller.getCurrX():mScroller.getCurrY());
        float delta = ((scroll - mPadding) % mItemSize > mItemSize / 2) ? mItemSize - (scroll - mPadding) % mItemSize
                : -((scroll - mPadding) % mItemSize);

        if (mMode == Mode.X) {
            mScroller.startScroll(scroll, 0, delta, 0, duration);
        } else {
            mScroller.startScroll(0, scroll, 0, delta, duration);
        }
        computeScrollOffest();
    }

    protected void onConfigurationChanged(GLConfiguration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mOrientation != newConfig.mOrientation) {
            mInit = false;
            invalidate();
            mOrientation = newConfig.mOrientation;
        }
    }

    public void scrollTo(float x, float y, int duration) {
        float scrollX = x + mPadding;
        float scrollY = y + mPadding;
        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), scrollX - mScroller.getCurrX(), scrollY - mScroller.getCurrY(), duration);
        computeScrollOffest();
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
                    float deltaX = (mLastMotionX - x) * mRatio;

                    if (mScrolling || Math.abs(deltaX) > mTouchSlop) {
                        mLastMotionX = x;

                        mScroller.startScroll(mScrollX, 0, deltaX, 0, 0);
                        computeScrollOffest();
                        mScrolling = true;
                        handled = true;
                    }
                } else {
                    float deltaY = (mLastMotionY - y) * mRatio;

                    if (mScrolling || Math.abs(deltaY) > mTouchSlop) {
                        mLastMotionY = y;

                        mScroller.startScroll(0, mScrollY, 0, deltaY, 0);
                        computeScrollOffest();
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


                float initialVelocity = (mMode == Mode.X) ? velocityTracker
                        .getXVelocity(pointerId) : velocityTracker
                        .getYVelocity(pointerId);
                float scroll = ((mMode == Mode.X) ? mScroller.getCurrX() : mScroller.getCurrY());
                float length = ((mMode == Mode.X) ? mContentWidth : mContentHeight);
                if (scroll >= mPadding && scroll <= (length + mPadding)) {
                    if (mScrolling) {
                        if (mItemSize != 0) {
                            int preDuration = (int)((float)Math.abs(initialVelocity) / BASE_PARAMETER_FLING * 1000);
                            int duration = (int)(preDuration * (Math.sqrt(0.4 * (MAX_DURATION_VALUE / preDuration))));
                            flingWithAlign(-initialVelocity, duration);
                        } else {
                            fling(-initialVelocity);
                        }
                    }
                } else {
                    float [] feedback = isNeedRebounding((mMode == Mode.X)?mScrollX:mScrollY);
                    if (feedback[FEEDBACK_WHAT] == NO_NEED_REBOUNDING) {
                        if (mItemSize != 0) {
                            scrollWithAlign(-initialVelocity, DURATION_SCROLLING_AUTOALIGN);
                        } else {
                            if (!mIsBeingDragged && mScrollContainerListener != null) {
                                mScrollContainerListener.onScrollFinished();
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
            computeScrollOffest();
        }
    };

    private void computeScrollOffest() {
        final FloatOverScroller scroller = mScroller;
        if (scroller.computeScrollOffset()) {
            float scrollX = scroller.getCurrX();
            float scrollY = scroller.getCurrY();
            final float maxX = mContentWidth - mWidth + 2 * mPadding;
            final float maxY = mContentHeight - mHeight + 2 * mPadding;
            if (mMode == Mode.X) {
                if (scrollX < 0)
                    scrollX = 0;
                else if (scrollX > maxX)
                    scrollX = maxX;

                mScrollX = scrollX;
                if (mScrollContainerListener != null) {
                    mScrollContainerListener.onScrollChanged(getScrollX(), getScrollY());
                }
                if (scrollX != mScroller.getFinalX() && !((scrollX == 0 && mScroller.getFinalX() < scrollX) || (scrollX == maxX && mScroller.getFinalX() > scrollX))) {
                    mHandler.removeCallbacks(mFlingRunnable);
                    mHandler.postDelayed(mFlingRunnable, FLING_HANDLER_INTERVEL);
                } else {
                    if (!mIsBeingDragged) {
                        if (mScrollContainerListener != null) {
                            mScrollContainerListener.onScrollFinished();
                        }

                        float [] feedback = isNeedRebounding((mMode == Mode.X)?mScrollX:mScrollY);
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
                if (mScrollContainerListener != null) {
                    mScrollContainerListener.onScrollChanged(getScrollX(), getScrollY());
                }
                if (scrollY != mScroller.getFinalY() && !((scrollY == 0 && mScroller.getFinalY() < scrollY) || (scrollY == maxY && mScroller.getFinalY() > scrollY))) {
                    mHandler.removeCallbacks(mFlingRunnable);
                    mHandler.postDelayed(mFlingRunnable, FLING_HANDLER_INTERVEL);
                } else {
                    if (!mIsBeingDragged) {
                        if (mScrollContainerListener != null) {
                            mScrollContainerListener.onScrollFinished();
                        }

                        float [] feedback = isNeedRebounding((mMode == Mode.X)?mScrollX:mScrollY);
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

    private void rebounding(float [] feedback) {
        float scrollToWhere = feedback[FEEDBACK_SCROLL_TO_WHERE];
        if (mMode == Mode.X) {
            mScroller.startScroll(mScrollX, 0, scrollToWhere - mScrollX, 0, DURATION_REBOUNDING);
        } else {
            mScroller.startScroll(0, mScrollY, 0, scrollToWhere - mScrollY, DURATION_REBOUNDING);
        }
        computeScrollOffest();
        if (DEBUG) {
            Log.d(TAG, "Rebounding finish, and you will go to " + scrollToWhere);
        }
    }

    public boolean isScrolling() {
        return mScrolling;
    }

    // Used in rebounding function to check if needs rebounding.
    public float[] isNeedRebounding(float scrollToWhere) {
        final float maxX = mContentWidth - mWidth + 2 * mPadding;;
        final float maxY = mContentHeight - mHeight + 2 * mPadding;;
        float forwardLimit = ((mMode == Mode.X) ? maxX : maxY) - mPadding;
        float backwardLimit = mPadding;
        float [] feedback = {NO_NEED_REBOUNDING, forwardLimit};

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
}