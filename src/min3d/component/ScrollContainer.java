package min3d.component;

import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import min3d.CustomScroller;
import min3d.CustomScroller.Mode;
import min3d.CustomScroller.ScrollerListener;
import min3d.core.GContext;
import min3d.core.Object3d;
import min3d.core.Object3dContainer;
import min3d.listeners.OnTouchListener;
import min3d.vos.Number3d;
import min3d.vos.Ray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrollContainer extends Object3dContainer {
    private static final String TAG = "ScrollView";

    class ScrollItemInfo {
        int mVisibility;
        Number3d mPosition;
    }

    private CustomScroller mScroller;
    private CustomScroller.Mode mMode;
    private float mRatio = 0;
    private Number3d mSize = null;
    private Ray[] mBound = null;
    private Map<Object3d, ScrollItemInfo> mMap = new HashMap<Object3d, ScrollItemInfo>();
    private float mScrollTemp = -1;
    private ScrollContainerListener mScrollViewListener = null;

    public ScrollContainer(GContext context, Mode mode) {
        super(context);
        mMode = mode;

        mScroller = new CustomScroller(context.getContext(), mMode,
                new DecelerateInterpolator(CustomScroller.FLING_DECELERATION_INTERPOLATOR));
        mScroller.setPositionListener(mScrollerListener);
    }

    public void setOverScrollRange(int range) {
        if (mScroller != null) {
            mScroller.setPadding(range);
        }
    }

    public void setScrollRange(int range) {
        if (mMode == CustomScroller.Mode.X) {
            mScroller.setContentWidth(range);
        } else {
            mScroller.setContentHeight(range);
        }
    }

    public int getScroll() {
        return (mScroller != null)?mScroller.getScroll():0;
    }

    public void addMotionEvent(MotionEvent event) {
        if (mScroller != null) {
            mScroller.processScroll(event);
        }
    }

    public void setScrollViewListener(ScrollContainerListener listener) {
        mScrollViewListener = listener;
    }

    public void scrollTo(int x, int y, int duration) {
        if (mScroller != null) {
            mScroller.scrollTo(x, y, duration);
        }
    }

    private ScrollerListener mScrollerListener = new ScrollerListener() {
        @Override
        public void onScrollChanged(int scrollX, int scrollY) {
            if (mScrollViewListener != null) {
                mScrollViewListener.onScrollChanged(scrollX, scrollY);
            }
        }

        @Override
        public void onScrollFinished() {
            if (mScrollViewListener != null) {
                mScrollViewListener.onScrollFinished();
            }
        }
    };

    protected void onRender() {
        super.onRender();

        if (mRatio == 0) {
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

        for (int i = 0; i < numChildren(); i++) {
            Number3d position = mMap.get(getChildAt(i)).mPosition;
            if ((mMode == CustomScroller.Mode.X)) {
                getChildAt(i).position().x = position.x + (-mScroller.getScroll() * mRatio);
            } else {
                getChildAt(i).position().y = position.y + (mScroller.getScroll() * mRatio);
            }
        }

        if (mScrollTemp != mScroller.getScroll()) {
            float bound[] = new float[2];
            for (int i = 0; i < numChildren(); i++) {
                Object3d obj = getChildAt(i);
                float pos = (mMode == CustomScroller.Mode.X)?obj.position().x:obj.position().y;
                bound[0] = (mMode == CustomScroller.Mode.X)?-mSize.x:-mSize.y;
                bound[1] = (mMode == CustomScroller.Mode.X)?mSize.x:mSize.y;
                if (pos > bound[0] && pos < bound[1]){
                    obj.setVisibility(Object3d.INVISIBLE);
                    obj.setOnTouchListener(mTouchListener);
                }
            }

            getGContext().getRenderer().updateAABBCoord();

            List<Object3d> visibilityChanged = new ArrayList<Object3d>();
            for (int i = 0; i < numChildren(); i++) {
                Object3d obj = getChildAt(i);
                float pos = (mMode == CustomScroller.Mode.X)?obj.position().x:obj.position().y;
                int orig = mMap.get(obj).mVisibility;
                if (pos > bound[0] / 2 && pos < bound[1] / 2) {
                    obj.setVisibility(Object3d.VISIBLE);
                    mMap.get(obj).mVisibility = Object3d.VISIBLE;
                } else if (obj.intersects(mBound[0]) || obj.intersects(mBound[1])) {
                    obj.setVisibility(Object3d.VISIBLE);
                    mMap.get(obj).mVisibility = Object3d.VISIBLE;
                } else {
                    obj.setVisibility(Object3d.GONE);
                    mMap.get(obj).mVisibility = Object3d.GONE;
                    obj.setOnTouchListener(null);
                }
                int after = mMap.get(obj).mVisibility;
                if (after != orig && (after == Object3d.VISIBLE || after == Object3d.GONE)) {
                    visibilityChanged.add(obj);
                }
            }
            if (!visibilityChanged.isEmpty() && mScrollViewListener != null) {
                mScrollViewListener.onItemVisibilityChanged(visibilityChanged);
            }

        }
        mScrollTemp = mScroller.getScroll();
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(Object3d obj, MotionEvent event,
                List<Object3d> list, Number3d coordinate) {
            addMotionEvent(event);
            return true;
        }
    };

    /**
     * {@inheritDoc}
     */
    public void addChild(Object3d $o) {
        super.addChild($o);
        ScrollItemInfo info = new ScrollItemInfo();
        info.mVisibility = Object3d.GONE;
        info.mPosition = $o.position().clone();
        mMap.put($o, info);
    }

    /**
     * {@inheritDoc}
     */
    public void addChildAt(Object3d $o, int $index) {
        super.addChildAt($o, $index);
        ScrollItemInfo info = new ScrollItemInfo();
        info.mVisibility = Object3d.GONE;
        info.mPosition = $o.position().clone();
        mMap.put($o, info);
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeChild(Object3d $o) {
        mMap.remove($o);
        return super.removeChild($o);
    }

    /**
     * {@inheritDoc}
     */
    public Object3d removeChildAt(int $index) {
        Object3d $o = super.removeChildAt($index);
        mMap.remove($o);
        return $o;
    }

    public interface ScrollContainerListener {
        void onItemVisibilityChanged(List<Object3d> visibilityChanged);
        void onScrollChanged(int scrollX, int scrollY);
        void onScrollFinished();
    }
}