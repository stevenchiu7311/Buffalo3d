package min3d.component;

import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import min3d.CustomScroller;
import min3d.CustomScroller.Mode;
import min3d.CustomScroller.ScrollerListener;
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

    private static final boolean BOUND_RAY_DETECTION = false;

    class ScrollItemInfo {
        int mVisibility;
        Number3d mPosition;
    }

    private CustomScroller mScroller;
    private CustomScroller.Mode mMode;
    private float mRatio = 0f;
    private Number3d mSize = null;
    private Ray[] mBound = null;
    private Map<Object3d, ScrollItemInfo> mMap = new HashMap<Object3d, ScrollItemInfo>();
    private float mScrollTemp = -1;
    private ScrollContainerListener mScrollContainerListener = null;
    private List<Object3d> mMightInBoundList = new ArrayList<Object3d>();
    private float mScrollRange;
    private float mOverScrollRange;
    private boolean mInit;;
    private float mScroll;

    public ScrollContainer(GContext context, Mode mode) {
        super(context);
        mMode = mode;
        mScroller = new CustomScroller(getGContext().getContext(), mMode,
                new DecelerateInterpolator(CustomScroller.FLING_DECELERATION_INTERPOLATOR));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mScroller.setHandler(getHandler());
        mScroller.setPositionListener(mScrollerListener);
    }

    public void setOverScrollRange(float range) {
        if (range != mOverScrollRange) {
            mOverScrollRange = range;
            if (mInit) {
                if (mScroller != null) {
                    mScroller.setPadding((int) (mOverScrollRange / mRatio));
                }
            } else {
                requestLayout();
            }
        }
    }

    public void setScrollRange(float range) {
        if (range != mScrollRange) {
            mScrollRange = range;
            if (mInit) {
                if (mMode == CustomScroller.Mode.X) {
                    mScroller.setContentWidth((int) (mScrollRange / mRatio));
                } else {
                    mScroller.setContentHeight((int) (mScrollRange / mRatio));
                }
            } else {
                requestLayout();
            }
        }
    }

    public float getScrollRange() {
        return mScrollRange;
    }

    public float getScroll() {
        return (mScroller != null && mRatio != 0)?((float)Math.round((float)mScroller.getScroll() * mRatio * 100) / 100) : mScroll;
    }

    public int getScrollNative() {
        return (mScroller != null)?mScroller.getScroll():0;
    }

    public void addMotionEvent(MotionEvent event) {
        if (mScroller != null && isEnabled()) {
            mScroller.processScroll(event);
        }
    }

    public void setScrollContainerListener(ScrollContainerListener listener) {
        mScrollContainerListener = listener;
    }

    public void scrollTo(float x, float y, int duration) {
        if (mScroller != null && mRatio != 0) {
            mScroller.scrollTo((int)(x / mRatio), (int)(y / mRatio), duration);
        } else {
            mScroll = (mMode == Mode.X) ? x : y;
        }
    }

    public boolean onInterceptTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        if (mScroller != null && isEnabled()) {
            mScroller.processScroll(event);

            if (mScroller.isScrolling()) {
                return true;
            }
            return false;
        }

        return super.onInterceptTouchEvent(ray, event, list);
    }

    public boolean onTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        if (mScroller != null && isEnabled()) {
            mScroller.processScroll(event);
            return true;
        }
        return super.onTouchEvent(ray, event, list);
    }

    private ScrollerListener mScrollerListener = new ScrollerListener() {
        @Override
        public void onScrollChanged(int scrollX, int scrollY) {
            if (mScrollContainerListener != null) {
                mScrollContainerListener.onScrollChanged(scrollX * mRatio, scrollY * mRatio, scrollX, scrollY);
            }
        }

        @Override
        public void onScrollFinished() {
            if (mScrollContainerListener != null) {
                mScrollContainerListener.onScrollFinished();
            }
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            if (mScroller != null) {
                mScroller.processScroll(event);
            }
            event.recycle();
        }
        invalidate();
    }

    public void invalidate() {
        super.invalidate();
        mScrollTemp = Integer.MIN_VALUE;
    }

    protected void onRender() {
        super.onRender();

        if (mScroller == null) return;

        if (!mInit) {
            mSize = getGContext().getRenderer().getWorldPlaneSize(position().z);
            mRatio = (mMode == CustomScroller.Mode.X) ? mSize.x
                    / getGContext().getRenderer().getWidth() : mSize.y
                    / getGContext().getRenderer().getHeight();

            mBound = new Ray[2];
            Number3d source[] = new Number3d[2];
            Number3d direction[] = new Number3d[2];
            source[0] = new Number3d(-mSize.x / 2, mSize.y / 2, 0);
            direction[0] = (mMode == CustomScroller.Mode.X)?new Number3d(0, -mSize.y / 2 - mSize.y / 2, 0):new Number3d(mSize.x / 2 - -mSize.x / 2, 0, 0);
            direction[0].normalize();
            mBound[0] = new Ray(source[0], direction[0]);

            source[1] = (mMode == CustomScroller.Mode.X)?new Number3d(mSize.x / 2, mSize.y / 2, 0):new Number3d(-mSize.x / 2, -mSize.y / 2, 0);
            direction[1] = (mMode == CustomScroller.Mode.X)?new Number3d(0, -mSize.y / 2 - mSize.y / 2, 0):new Number3d(mSize.x / 2 - -mSize.x / 2, 0, 0);
            direction[1].normalize();
            mBound[1] = new Ray(source[1], direction[1]);
        }

        if (isLayoutRequested()) {
            if (mMode == CustomScroller.Mode.X) {
                mScroller.setContentWidth((int) (mScrollRange / mRatio));
            } else {
                mScroller.setContentHeight((int) (mScrollRange / mRatio));
            }

            if (mScroller != null) {
                mScroller.setPadding((int) (mOverScrollRange / mRatio));
            }
            layout();
        }

        if (!mInit) {
            if (mMode == CustomScroller.Mode.X) {
                mScroller.scrollTo((int)(mScroll / mRatio), 0, 0);
            } else {
                mScroller.scrollTo(0, (int)(mScroll / mRatio), 0);
            }
        }

        if (numChildren() > 0) {
            Object3dContainer directChild = (Object3dContainer) getChildAt(0);
            Number3d position = mMap.get(directChild).mPosition;
            if ((mMode == CustomScroller.Mode.X)) {
                directChild.position().x = -mScroller.getScroll() * mRatio;
                position.x = -mScroller.getScroll() * mRatio;
            } else {
                directChild.position().y = mScroller.getScroll() * mRatio;
                position.y = mScroller.getScroll() * mRatio;
            }

            if (mScrollTemp != mScroller.getScroll()) {
                List<Object3d> visibilityChanged = new ArrayList<Object3d>();
                calculateObjectCoodinate(directChild, mMightInBoundList);
                if (BOUND_RAY_DETECTION) getGContext().getRenderer().updateAABBCoord();
                calculateObjectVisibility(mMightInBoundList, visibilityChanged);

                if (!visibilityChanged.isEmpty() && mScrollContainerListener != null) {
                    mScrollContainerListener.onItemVisibilityChanged(visibilityChanged);
                }
            }
        }

        mScrollTemp = mScroller.getScroll();

        if (!mInit) {
            if (mScrollContainerListener != null) {
                mScrollContainerListener.onScrollerReady();
            }
            mInit = true;
        }
    }

    void calculateObjectCoodinate(Object3dContainer parent, List<Object3d> mightInBound) {
        float bound[] = new float[2];
        bound[0] = (mMode == CustomScroller.Mode.X)?-mSize.x:-mSize.y;
        bound[1] = (mMode == CustomScroller.Mode.X)?mSize.x:mSize.y;
        for (int i = 0; i < parent.numChildren(); i++) {
            Object3d obj = parent.getChildAt(i);
            addInnerChild(obj);
            Number3d.add(mMap.get(obj).mPosition, obj.position(), mMap.get(parent).mPosition);
            float pos = (mMode == CustomScroller.Mode.X)?mMap.get(obj).mPosition.x:mMap.get(obj).mPosition.y;
            if (obj.vertices().size() > 0) {
                obj.setVisibility(Object3d.GONE);
                mightInBound.add(obj);
            }

            if (obj instanceof Object3dContainer) {
                calculateObjectCoodinate((Object3dContainer)obj, mightInBound);
            }
        }
    }

    void calculateObjectVisibility(List<Object3d> mightInBound, List<Object3d> visibilityChanged) {
        float bound[] = new float[2];
        bound[0] = (mMode == CustomScroller.Mode.X)?-mSize.x:-mSize.y;
        bound[1] = (mMode == CustomScroller.Mode.X)?mSize.x:mSize.y;

        if ((BOUND_RAY_DETECTION)) {
            bound[0] /= 2;
            bound[1] /= 2;
        }

        for (Object3d obj:mightInBound) {
            float pos = (mMode == CustomScroller.Mode.X)?mMap.get(obj).mPosition.x:mMap.get(obj).mPosition.y;

            int orig = mMap.get(obj).mVisibility;
            if (pos > bound[0] && pos < bound[1]) {
                obj.setVisibility(Object3d.VISIBLE);
                mMap.get(obj).mVisibility = Object3d.VISIBLE;
            } else if (BOUND_RAY_DETECTION && (obj.intersects(mBound[0]) || obj.intersects(mBound[1]))) {
                obj.setVisibility(Object3d.VISIBLE);
                mMap.get(obj).mVisibility = Object3d.VISIBLE;
            } else {
                obj.setVisibility(Object3d.GONE);
                mMap.get(obj).mVisibility = Object3d.GONE;
            }
            int after = mMap.get(obj).mVisibility;
            if (after != orig && (after == Object3d.VISIBLE || after == Object3d.GONE)) {
                visibilityChanged.add(obj);
            }
        }
        mightInBound.clear();
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
        void onScrollChanged(float scrollX, float scrollY, int nativeScrollX, int nativeScrollY);
        void onScrollFinished();
        void onScrollerReady();
    }
}