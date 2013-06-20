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
    private List<Object3d> mMightInBoundList = new ArrayList<Object3d>();

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
                getGContext().getRenderer().updateAABBCoord();
                calculateObjectVisibility(mMightInBoundList, visibilityChanged);

                if (!visibilityChanged.isEmpty() && mScrollViewListener != null) {
                    mScrollViewListener.onItemVisibilityChanged(visibilityChanged);
                }
            }
        }

        mScrollTemp = mScroller.getScroll();
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
                if (pos > bound[0] && pos < bound[1]) {
                    obj.setVisibility(Object3d.INVISIBLE);
                    obj.setOnTouchListener(mTouchListener);
                    mightInBound.add(obj);
                }
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
        for (Object3d obj:mightInBound) {
            float pos = (mMode == CustomScroller.Mode.X)?mMap.get(obj).mPosition.x:mMap.get(obj).mPosition.y;

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
        mightInBound.clear();
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
        void onScrollChanged(int scrollX, int scrollY);
        void onScrollFinished();
    }
}