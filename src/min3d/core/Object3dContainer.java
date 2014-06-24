package min3d.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import min3d.GLConfiguration;
import min3d.interfaces.IObject3dContainer;
import min3d.interfaces.IObject3dParent;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.Face;
import min3d.vos.Number3d;
import min3d.vos.Ray;
import min3d.vos.TextureVo;
import min3d.vos.Uv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A Object3dContainer is a special object3d that can contain other object3d
 * (called children.) The object3d container is the base class for layouts and objects
 * containers.
 */
public class Object3dContainer extends Object3d implements IObject3dContainer, IObject3dParent
{
    private final static String TAG = "Object3dContainer";

    private static final boolean DBG = false;

    private static final String PREFIX_BACKGROUND = "container_background_";

    /**
     * When set, this group will go through its list of children to notify them of
     * any drawable state change.
     */
    private static final int FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE = 0x10000;

    private static final int FLAG_MASK_FOCUSABILITY = 0x60000;

    /**
     * This view will get focus before any of its descendants.
     */
    public static final int FOCUS_BEFORE_DESCENDANTS = 0x20000;

    /**
     * This view will get focus only if none of its descendants want it.
     */
    public static final int FOCUS_AFTER_DESCENDANTS = 0x40000;

    /**
     * This view will block any of its descendants from getting focus, even
     * if they are focusable.
     */
    public static final int FOCUS_BLOCK_DESCENDANTS = 0x60000;

    /**
     * When set, this ViewGroup should not intercept touch events.
     * {@hide}
     */
    protected static final int FLAG_DISALLOW_INTERCEPT = 0x80000;

    /**
     * Used to map between enum in attrubutes and flag values.
     */
    private static final int[] DESCENDANT_FOCUSABILITY_FLAGS =
            {FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS,
                    FOCUS_BLOCK_DESCENDANTS};

    /**
     * When set, this ViewGroup's drawable states also include those
     * of its children.
     */
    private static final int FLAG_ADD_STATES_FROM_CHILDREN = 0x2000;

    /**
     * When set, this ViewGroup will not dispatch onAttachedToWindow calls
     * to children when adding new views. This is used to prevent multiple
     * onAttached calls when a ViewGroup adds children in its own onAttached method.
     */
    private static final int FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW = 0x400000;

    protected int mGroupFlags;
    // The view contained within this ViewGroup that has or contains focus.
    private Object3d mFocused;

	protected ArrayList<Object3d> mChildren = new ArrayList<Object3d>();

    private Object3d mMotionTarget = null;
    private ArrayList<Object3d> mDepthTestList = new ArrayList<Object3d>();
    private DepthSort mDepthSort = new DepthSort(DepthSort.UP);

	public Object3dContainer(GContext context)
	{
		super(context, 0, 0, false, false, false);
        initObject3dContainer();
	}

	/**
	 * Adds container functionality to Object3d.
	 * 
	 * Subclass Object3dContainer instead of Object3d if you
	 * believe you may want to add children to that object. 
	 */
	public Object3dContainer(GContext context, int $maxVerts, int $maxFaces)
	{
		super(context, $maxVerts, $maxFaces,true,true, true);
        initObject3dContainer();
	}

	public Object3dContainer(GContext context, int $maxVerts,  int $maxFaces, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		super(context, $maxVerts, $maxFaces,$useUvs,$useNormals, $useVertexColors);
        initObject3dContainer();
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3dContainer(GContext context, Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
		super(context, $vertices, $faces, $textures);
        initObject3dContainer();
	}

    private void initObject3dContainer() {
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
    }

    /**
     * {@inheritDoc}
     */
	public void addChild(Object3d $o)
	{
		mChildren.add($o);
        addObjectInner($o);
		
		$o.parent(this);
		$o.scene(mGContext.getRenderer().getScene());
	}

    /**
     * {@inheritDoc}
     */
	public void addChildAt(Object3d $o, int $index) 
	{
		mChildren.add($index, $o);
        addObjectInner($o);
		
		$o.parent(this);
		$o.scene(mGContext.getRenderer().getScene());
	}

    /**
     * {@inheritDoc}
     */
    public boolean removeChild(Object3d $o) {
        boolean b = mChildren.remove($o);

        if (b) {
            $o.parent(null);
            $o.scene(null);
        }
        return b;
    }

    /**
     * {@inheritDoc}
     */
	public Object3d removeChildAt(int $index) 
	{
		Object3d o = mChildren.remove($index);
		if (o != null) {
			o.parent(null);
			o.scene(null);
		}
		return o;
	}

    /**
     * {@inheritDoc}
     */
    public void removeAllChildren() {
        for (Object3d o : mChildren) {
            o.parent(null);
            o.scene(null);
        }
        mChildren.clear();
    }

    /**
     * {@inheritDoc}
     */
	public Object3d getChildAt(int $index) 
	{
		return mChildren.get($index);
	}

    /**
     * {@inheritDoc}
     */
    public Object3d getChildByName(String $name) {
        for (int i = 0; i < mChildren.size(); i++) {
            if (mChildren.get(i).name() != null
                    && mChildren.get(i).name().equals($name)) {
                return mChildren.get(i);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
	public int getChildIndexOf(Object3d $o) 
	{
		return mChildren.indexOf($o);
	}

    /**
     * {@inheritDoc}
     */
	public int numChildren() 
	{
		return mChildren.size();
	}
	
	/*package-private*/ 
	ArrayList<Object3d> children()
	{
		return mChildren;
	}
	
	public Object3dContainer clone()
	{
		Vertices v = mVertices.clone();
		FacesBufferedList f = mFaces.clone();

		Object3dContainer clone = new Object3dContainer(mGContext, v, f, mTextures);
		
		clone.position().x = position().x;
		clone.position().y = position().y;
		clone.position().z = position().z;
		
		clone.rotation().x = rotation().x;
		clone.rotation().y = rotation().y;
		clone.rotation().z = rotation().z;
		
		clone.scale().x = scale().x;
		clone.scale().y = scale().y;
		clone.scale().z = scale().z;
		
		for(int i = 0; i< this.numChildren();i++)
		{
			 clone.addChild(getChildAt(i).clone());
		}
		 
		return clone;
	}

    private void addObjectInner(Object3d child) {
        if (child.parent() != null) {
            throw new IllegalStateException("The specified child already has a parent. " +
                    "You must call removeView() on the child's parent first. ("+ child.name() +")");
        }

        if (child.hasFocus()) {
            requestChildFocus(child, child.findFocus());
        }

        AttachInfo ai = mAttachInfo;
        if (ai != null && (mGroupFlags & FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW) == 0) {
            child.dispatchAttachedToWindow(mAttachInfo, (mViewFlags&VISIBILITY_MASK));
        }
    }

    /**
     * Implement this method to intercept all touch screen motion events.  This
     * allows you to watch events as they are dispatched to your children, and
     * take ownership of the current gesture at any point.
     *
     * <p>Using this function takes some care, as it has a fairly complicated
     * interaction with {@link Object3d#onTouchEvent(Ray, MotionEvent, ArrayList<Object3d>)
     * Object3d.onTouchEvent(MotionEvent)}, and using it requires implementing
     * that method as well as this one in the correct way.  Events will be
     * received in the following order:
     *
     * <ol>
     * <li> You will receive the down event here.
     * <li> The down event will be handled either by a child of this object3d
     * container, or given to your own onTouchEvent() method to handle; this means
     * you should implement onTouchEvent() to return true, so you will
     * continue to see the rest of the gesture (instead of looking for
     * a parent object3d to handle it).  Also, by returning true from
     * onTouchEvent(), you will not receive any following
     * events in onInterceptTouchEvent() and all touch processing must
     * happen in onTouchEvent() like normal.
     * <li> For as long as you return false from this function, each following
     * event (up to and including the final up) will be delivered first here
     * and then to the target's onTouchEvent().
     * <li> If you return true from here, you will not receive any
     * following events: the target object3d will receive the same event but
     * with the action {@link MotionEvent#ACTION_CANCEL}, and all further
     * events will be delivered to your onTouchEvent() method and no longer
     * appear here.
     * </ol>
     *
     * @param event The motion event being dispatched down the hierarchy.
     * @return Return true to steal motion events from the children and have
     * them dispatched to this Object3dContainer through onTouchEvent().
     * The current target will receive an ACTION_CANCEL event, and no further
     * messages will be delivered here.
     */
    public boolean onInterceptTouchEvent(Ray ray, MotionEvent event, ArrayList<Object3d> list) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean dispatchTouchEvent(Ray ray, MotionEvent ev, ArrayList<Object3d> list) {
        boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mMotionTarget != null) {
                // this is weird, we got a pen down, but we thought it was
                // already down!
                // XXX: We should probably send an ACTION_UP to the current
                // target.
                mMotionTarget = null;
            }
            // If we're disallowing intercept or if we're allowing and we didn't
            // intercept
            if (disallowIntercept || !onInterceptTouchEvent(ray, ev, list)) {
                // reset this event's action (just to protect ourselves)
                ev.setAction(MotionEvent.ACTION_DOWN);

                Object3dContainer container = (Object3dContainer) this;
                mDepthTestList.addAll(container.children());
                ArrayList<Object3d> children = mDepthTestList;
                Collections.sort(children, mDepthSort);
                for (int i = children.size() - 1; i >= 0; i--) {
                    if ((children.get(i).getVisibility() & VISIBILITY_MASK) == GONE) {
                        continue;
                    }

                    Object3d child = null;
                    for (int j = 0; j < container.numChildren(); j++) {
                        if (children.get(i).equals(container.getChildAt(j))) {
                            child = container.getChildAt(j);
                        }
                    }

                    boolean find = false;
                    for (int j = 0; j < list.size(); j++) {
                        Object3d obj = list.get(j);
                        find = contain(child,obj);
                    }

                    if (child != null && find) {
                        if (child.dispatchTouchEvent(ray, ev, list)) {
                            mMotionTarget = child;
                            mDepthTestList.clear();
                            return true;
                        }
                    }
                }
                mDepthTestList.clear();
            }
        }

        boolean isUpOrCancel = (action == MotionEvent.ACTION_UP)
                || (action == MotionEvent.ACTION_CANCEL);

        if (isUpOrCancel) {
            // Note, we've already copied the previous state to our local
            // variable, so this takes effect on the next event
            mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
        }

        // The event wasn't an ACTION_DOWN, dispatch it to our target if
        // we have one.
        final Object3d target = mMotionTarget;
        if (target == null) {
            // We don't have a target, this means we're handling the
            // event as a regular view.
/*            ev.setLocation(xf, yf);*/
            if ((mPrivateFlags & CANCEL_NEXT_UP_EVENT) != 0) {
                ev.setAction(MotionEvent.ACTION_CANCEL);
                mPrivateFlags &= ~CANCEL_NEXT_UP_EVENT;
            }
            return super.dispatchTouchEvent(ray, ev, list);
        }

        // if have a target, see if we're allowed to and want to intercept its
        // events
        if (!disallowIntercept && onInterceptTouchEvent(ray, ev, list)) {
/*            final float xc = scrolledXFloat - (float) target.mLeft;
            final float yc = scrolledYFloat - (float) target.mTop;*/
            mPrivateFlags &= ~CANCEL_NEXT_UP_EVENT;
            ev.setAction(MotionEvent.ACTION_CANCEL);
/*            ev.setLocation(xc, yc);*/
            if (!target.dispatchTouchEvent(ray, ev, list)) {
                // target didn't handle ACTION_CANCEL. not much we can do
                // but they should have.
            }

            // clear the target
            mMotionTarget = null;
            // Don't dispatch this event to our own view, because we already
            // saw it when intercepting; we just want to give the following
            // event to the normal onTouchEvent().
            return true;
        }

        if (isUpOrCancel) {
            mMotionTarget = null;
        }

        return target.dispatchTouchEvent(ray, ev, list);
    }

    /**
     * Check whether there is group-contained relationship between specified two objects.
     *
     * @param ancestors object to be the ancestors container
     * @param descendants object to be the descendants containee of ancestors object
     * @return true if there is group-contained relationship
     */
    public boolean contain(Object3d ancestors, Object3d descendants) {
        if (descendants.equals(ancestors)) {
            return true;
        }
        if (ancestors instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer) ancestors;
            for (int i = 0; i < container.children().size(); i++) {
                Object3d child = container.children().get(i);
                boolean find = contain(child,descendants);
                if (find) {
                    return true;
                }
            }
        }
        return false;
    }

    private class DepthSort implements Comparator<Object3d> {
        private final static int UP = 1;
        private final static int DOWM = -1;

        private int state;

        public DepthSort(int state) {
            this.state = state;
        }

        public int compare(Object3d o1, Object3d o2) {
            if (state == DepthSort.DOWM) {
                return sortDown(o1, o2);
            }
            return sortUp(o1, o2);
        }

        private int sortUp(Object3d o1, Object3d o2) {
            if (o1.center().z < o2.center().z) {
                return -1;
            } else if (o1.center().z > o2.center().z) {
                return 1;
            } else {
                return 0;
            }
        }

        private int sortDown(Object3d o1, Object3d o2) {
            if (o1.center().z > o2.center().z) {
                return -1;
            } else if (o1.center().z < o2.center().z) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearChildFocus(Object3d child) {
        if (DBG) {
            System.out.println(this + " clearChildFocus()");
        }

        mFocused = null;
        if (mParent != null) {
            mParent.clearChildFocus(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFocus() {
        super.clearFocus();

        // clear any child focus if it exists
        if (mFocused != null) {
            mFocused.clearFocus();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void unFocus() {
        if (DBG) {
            System.out.println(this + " unFocus()");
        }

        super.unFocus();
        if (mFocused != null) {
            mFocused.unFocus();
        }
        mFocused = null;
    }

    /**
     * Returns the focused child of this view, if any. The child may have focus
     * or contain focus.
     *
     * @return the focused child or null.
     */
    public Object3d getFocusedChild() {
        return mFocused;
    }

    /**
     * Returns true if this view has or contains focus
     *
     * @return true if this view has or contains focus
     */
    @Override
    public boolean hasFocus() {
        return (mPrivateFlags & FOCUSED) != 0 || mFocused != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#findFocus()
     */
    @Override
    public Object3d findFocus() {
        if (DBG) {
            System.out.println("Find focus in " + this + ": flags="
                    + isFocused() + ", child=" + mFocused);
        }

        if (isFocused()) {
            return this;
        }

        if (mFocused != null) {
            return mFocused.findFocus();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFocusable() {
        if ((mViewFlags & VISIBILITY_MASK) != VISIBLE) {
            return false;
        }

        if (isFocusable()) {
            return true;
        }

        final int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
            final int count = numChildren();
            final Object3d[] children = (Object3d[]) mChildren.toArray();

            for (int i = 0; i < count; i++) {
                final Object3d child = children[i];
                if (child.hasFocusable()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFocusables(ArrayList<Object3d> views, int direction) {
        addFocusables(views, direction, FOCUSABLES_TOUCH_MODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFocusables(ArrayList<Object3d> views, int direction, int focusableMode) {
        final int focusableCount = views.size();

        final int descendantFocusability = getDescendantFocusability();

        if (descendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
            final int count = numChildren();
            final Object3d[] children = (Object3d[]) mChildren.toArray();

            for (int i = 0; i < count; i++) {
                final Object3d child = children[i];
                if ((child.mViewFlags & VISIBILITY_MASK) == VISIBLE) {
                    child.addFocusables(views, direction, focusableMode);
                }
            }
        }

        // we add ourselves (if focusable) in all cases except for when we are
        // FOCUS_AFTER_DESCENDANTS and there are some descendants focusable.  this is
        // to avoid the focus search finding layouts when a more precise search
        // among the focusable children would be more interesting.
        if (
            descendantFocusability != FOCUS_AFTER_DESCENDANTS ||
                // No focusable descendants
                (focusableCount == views.size())) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    /**
     * Gets the descendant focusability of this view group.  The descendant
     * focusability defines the relationship between this view group and its
     * descendants when looking for a view to take focus in
     * {@link #requestFocus(int, android.graphics.Rect)}.
     *
     * @return one of {@link #FOCUS_BEFORE_DESCENDANTS}, {@link #FOCUS_AFTER_DESCENDANTS},
     *   {@link #FOCUS_BLOCK_DESCENDANTS}.
     */
    public int getDescendantFocusability() {
        return mGroupFlags & FLAG_MASK_FOCUSABILITY;
    }

    /**
     * Set the descendant focusability of this view group. This defines the relationship
     * between this view group and its descendants when looking for a view to
     * take focus in {@link #requestFocus(int, android.graphics.Rect)}.
     *
     * @param focusability one of {@link #FOCUS_BEFORE_DESCENDANTS}, {@link #FOCUS_AFTER_DESCENDANTS},
     *   {@link #FOCUS_BLOCK_DESCENDANTS}.
     */
    public void setDescendantFocusability(int focusability) {
        switch (focusability) {
            case FOCUS_BEFORE_DESCENDANTS:
            case FOCUS_AFTER_DESCENDANTS:
            case FOCUS_BLOCK_DESCENDANTS:
                break;
            default:
                throw new IllegalArgumentException("must be one of FOCUS_BEFORE_DESCENDANTS, "
                        + "FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS");
        }
        mGroupFlags &= ~FLAG_MASK_FOCUSABILITY;
        mGroupFlags |= (focusability & FLAG_MASK_FOCUSABILITY);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if ((mGroupFlags & FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE) != 0) {
            if ((mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) != 0) {
                throw new IllegalStateException("addStateFromChildren cannot be enabled if a"
                        + " child has duplicateParentState set to true");
            }

            final Object3d[] children = mChildren.toArray(new Object3d[1]);
            final int count = numChildren();

            for (int i = 0; i < count; i++) {
                final Object3d child = children[i];
                if ((child.mViewFlags & DUPLICATE_PARENT_STATE) != 0) {
                    child.refreshDrawableState();
                }
            }
        }
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        final Object3d[] children = mChildren.toArray(new Object3d[1]);
        final int count = numChildren();
        for (int i = 0; i < count; i++) {
            children[i].jumpDrawablesToCurrentState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if ((mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) == 0) {
            return super.onCreateDrawableState(extraSpace);
        }

        int need = 0;
        int n = numChildren();
        for (int i = 0; i < n; i++) {
            int[] childState = getChildAt(i).getDrawableState();

            if (childState != null) {
                need += childState.length;
            }
        }

        int[] state = super.onCreateDrawableState(extraSpace + need);

        for (int i = 0; i < n; i++) {
            int[] childState = getChildAt(i).getDrawableState();

            if (childState != null) {
                state = mergeDrawableStates(state, childState);
            }
        }

        return state;
    }

    /**
     * Sets whether this ViewGroup's drawable states also include
     * its children's drawable states.  This is used, for example, to
     * make a group appear to be focused when its child EditText or button
     * is focused.
     */
    public void setAddStatesFromChildren(boolean addsStates) {
        if (addsStates) {
            mGroupFlags |= FLAG_ADD_STATES_FROM_CHILDREN;
        } else {
            mGroupFlags &= ~FLAG_ADD_STATES_FROM_CHILDREN;
        }

        refreshDrawableState();
    }

    /**
     * Returns whether this ViewGroup's drawable states also include
     * its children's drawable states.  This is used, for example, to
     * make a group appear to be focused when its child EditText or button
     * is focused.
     */
    public boolean addStatesFromChildren() {
        return (mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) != 0;
    }

    /**
     * If {link #addStatesFromChildren} is true, refreshes this group's
     * drawable state (to include the states from its children).
     */
    public void childDrawableStateChanged(Object3d child) {
        if ((mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) != 0) {
            refreshDrawableState();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if (mFocused != null) {
            mFocused.unFocus();
            mFocused = null;
        }
        super.handleFocusGainInternal(direction, previouslyFocusedRect);
    }

    /**
     * {@inheritDoc}
     */
    public void requestChildFocus(Object3d child, Object3d focused) {
        if (DBG) {
            System.out.println(this + " requestChildFocus()");
        }
        if (getDescendantFocusability() == FOCUS_BLOCK_DESCENDANTS) {
            return;
        }

        // Unfocus us, if necessary
        super.unFocus();

        // We had a previous notion of who had focus. Clear it.
        if (mFocused != child) {
            if (mFocused != null) {
                mFocused.unFocus();
            }

            mFocused = child;
        }
        if (mParent != null) {
            mParent.requestChildFocus(this, focused);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void focusableObjectAvailable(Object3d obj) {
        if (DBG) {
            System.out.println(this + " focus root's child available:" + obj);
        }
        if (mParent != null
                // shortcut: don't report a new focusable view if we block our descendants from
                // getting focus
                && (getDescendantFocusability() != FOCUS_BLOCK_DESCENDANTS)
                // shortcut: don't report a new focusable view if we already are focused
                // (and we don't prefer our descendants)
                //
                // note: knowing that mFocused is non-null is not a good enough reason
                // to break the traversal since in that case we'd actually have to find
                // the focused view and make sure it wasn't FOCUS_AFTER_DESCENDANTS and
                // an ancestor of v; this will get checked for at ViewAncestor
                && !(isFocused() && getDescendantFocusability() != FOCUS_AFTER_DESCENDANTS)) {
            mParent.focusableObjectAvailable(obj);
        }
    }

    @Override
    public IObject3dParent getParent() {
        return parent();
    }

    @Override
    public Object3d focusSearch(Object3d focused, int direction) {
        if (isRootNamespace()) {
            // root namespace means we should consider ourselves the top of the
            // tree for focus searching; otherwise we could be focus searching
            // into other tabs.  see LocalActivityManager and TabHost for more info
            /*return FocusFinder.getInstance().findNextFocus(this, focused, direction);*/
        } else if (mParent != null) {
            return mParent.focusSearch(focused, direction);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Looks for a view to give focus to respecting the setting specified by
     * {@link #getDescendantFocusability()}.
     *
     * Uses {@link #onRequestFocusInDescendants(int, android.graphics.Rect)} to
     * find focus within the children of this group when appropriate.
     *
     * @see #FOCUS_BEFORE_DESCENDANTS
     * @see #FOCUS_AFTER_DESCENDANTS
     * @see #FOCUS_BLOCK_DESCENDANTS
     * @see #onRequestFocusInDescendants(int, android.graphics.Rect)
     */
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (DBG) {
            System.out.println(this + " ViewGroup.requestFocus direction="
                    + direction + " Name:" + name());
        }
        int descendantFocusability = getDescendantFocusability();

        switch (descendantFocusability) {
            case FOCUS_BLOCK_DESCENDANTS:
                return super.requestFocus(direction, previouslyFocusedRect);
            case FOCUS_BEFORE_DESCENDANTS: {
                final boolean took = super.requestFocus(direction, previouslyFocusedRect);
                return took ? took : onRequestFocusInDescendants(direction, previouslyFocusedRect);
            }
            case FOCUS_AFTER_DESCENDANTS: {
                final boolean took = onRequestFocusInDescendants(direction, previouslyFocusedRect);
                return took ? took : super.requestFocus(direction, previouslyFocusedRect);
            }
            default:
                throw new IllegalStateException("descendant focusability must be "
                        + "one of FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS "
                        + "but is " + descendantFocusability);
        }
    }

    /**
     * Look for a descendant to call {@link View#requestFocus} on.
     * Called by {@link ViewGroup#requestFocus(int, android.graphics.Rect)}
     * when it wants to request focus within its children.  Override this to
     * customize how your {@link ViewGroup} requests focus within its children.
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     * @param previouslyFocusedRect The rectangle (in this View's coordinate system)
     *        to give a finer grained hint about where focus is coming from.  May be null
     *        if there is no hint.
     * @return Whether focus was taken.
     */
    protected boolean onRequestFocusInDescendants(int direction,
            Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = numChildren();
        if ((direction & FOCUS_FORWARD) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
        final Object3d[] children = (Object3d[]) mChildren.toArray(new Object3d[1]);
        for (int i = index; i != end; i += increment) {
            Object3d child = children[i];
            if ((child.mViewFlags & VISIBILITY_MASK) == VISIBLE) {
                if (child.requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchConfigurationChanged(GLConfiguration newConfig) {
        super.dispatchConfigurationChanged(newConfig);
        final int count = numChildren();
        final Object3d[] children = (Object3d[]) mChildren.toArray(new Object3d[1]);
        for (int i = 0; i < count; i++) {
            children[i].dispatchConfigurationChanged(newConfig);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        mGroupFlags |= FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW;
        super.dispatchAttachedToWindow(info, visibility);
        mGroupFlags &= ~FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW;

        visibility |= mViewFlags & VISIBILITY_MASK;

        final int count = numChildren();
        final Object3d[] children = (Object3d[]) mChildren.toArray(new Object3d[1]);
        for (int i = 0; i < count; i++) {
            if (info != null) {
                children[i].dispatchAttachedToWindow(info, visibility);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchSetSelected(boolean selected) {
        final Object3d[] children = (Object3d[]) mChildren.toArray(new Object3d[1]);
        final int count = numChildren();
        for (int i = 0; i < count; i++) {
            children[i].setSelected(selected);
        }
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        final Object3d[] children = (Object3d[]) mChildren.toArray(new Object3d[1]);
        final int count = numChildren();
        for (int i = 0; i < count; i++) {
            children[i].setPressed(pressed);
        }
    }

    public void buildRenderingCache(HashMap<Object3d, Integer> map, Rect rect) {
        Vertices vertices = getVertices();
        vertices.clear();
        FacesBufferedList faces = getFaces();
        faces.clear();

        buildRenderingCache(vertices, faces, this, new Number3d(0f, 0f, 0f), map, rect);
    }

    void buildRenderingCache(Vertices vertices, FacesBufferedList faces, Object3d obj, Number3d offset, HashMap<Object3d, Integer> map, Rect rect) {
        if (obj.isRenderCacheEnabled()) {
            for (int j = 0; j < obj.getVertices().size(); j++) {
                Number3d point = obj.getVertices().getPoints().getAsNumber3d(j);
                Number3d.add(point, offset, point);
                Uv uv = obj.getVertices().getUvs().getAsUv(j);
                Bitmap bitmap = obj.getRenderingCache();
                if (bitmap != null) {
                    uv.v = uv.v / map.size() + (float)map.get(obj) / rect.height();
                }
                Number3d normal = obj.getVertices().getNormals().getAsNumber3d(j);
                Color4 color = null;
                if (obj.getVertices().hasColors()) {
                    color = obj.getVertices().getColors().getAsColor4(j);
                    color.a = 255;
                } else {
                    color = new Color4();
                }

                vertices.addVertex(point, uv, normal, color);
            }
            for (int j = 0; j < obj.getFaces().size(); j++) {
                int index = vertices.size() / obj.getVertices().size() - 1;
                Face face = obj.getFaces().get(j);
                face.a = (short) (face.a + index * obj.getVertices().size());
                face.b = (short) (face.b + index * obj.getVertices().size());
                face.c = (short) (face.c + index * obj.getVertices().size());
                faces.add(face);
            }
        }
        if (obj instanceof Object3dContainer) {
            Number3d newOffset = new Number3d();
            Object3dContainer container = (Object3dContainer)obj;
            for (int i = 0; i < container.numChildren(); i++) {
                Object3d child = container.getChildAt(i);
                Number3d.add(newOffset, offset, child.position());
                buildRenderingCache(vertices, faces, child, newOffset, map, rect);
            }
        }
    }

    void computeTextureSize(Object3d obj, Rect rect) {
        if (obj.isRenderCacheEnabled()) {
            Bitmap bitmap = obj.getRenderingCache();
            if (bitmap != null) {
                rect.right = bitmap.getWidth();
                rect.bottom += bitmap.getHeight();
            }
        }
        if (obj instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer)obj;
            for (int i = 0; i < container.numChildren(); i++) {
                Object3d child = container.getChildAt(i);
                computeTextureSize(child, rect);
            }
        }
    }

    void drawTextureMap(Object3d obj, Canvas canvas, Paint paint, int[] offset, HashMap<Object3d, Integer> map) {
        if (obj.isRenderCacheEnabled()) {
            Bitmap bitmap = obj.getRenderingCache();
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0 + offset[0], paint);
                map.put(obj, new Integer(offset[0]));
                offset[0] += bitmap.getHeight();
            }
        }
        if (obj instanceof Object3dContainer) {
            Object3dContainer container = (Object3dContainer)obj;
            for (int i = 0; i < container.numChildren(); i++) {
                Object3d child = container.getChildAt(i);
                drawTextureMap(child, canvas, paint, offset, map);
            }
        }
    }

    protected void onManageLayerTexture() {
        super.onManageLayerTexture();

        // Following are only for render cache.
        // Condition on visibility for this have already handled in Object3d render
        // onManageLayerTexture entrance.
        if (numChildren() == 0 || !checkRenderCacheBuilder()) {
            return;
        }

        for (int i = 0; i < numChildren(); i++) {
            Object3d obj = getChildAt(i);
            obj.onManageLayerTexture();
        }

        String backgroundTexId = PREFIX_BACKGROUND+this;
        String replaced = null;
        for (String id:getTextures().getIds()) {
            if (id.contains(PREFIX_BACKGROUND) && !id.equals(PREFIX_BACKGROUND)) {
                if (id.equals(backgroundTexId)) {
                    return;
                } else {
                    replaced = id;
                }
                break;
            }
        }

        if (replaced != null) {
            getGContext().getTexureManager().deleteTexture(backgroundTexId);
            getTextures().removeById(backgroundTexId);
        }

        Rect rect = new Rect();
        computeTextureSize(this, rect);

        Bitmap container = null;
        HashMap<Object3d, Integer> map = new HashMap<Object3d, Integer>();
        if (rect.width() != 0 && rect.height() != 0) {
            container = createTextureBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(container);
            drawTextureMap((Object3dContainer) this, canvas, new Paint(), new int[1], map);
        }

        buildRenderingCache(map, rect);
        map.clear();

        if (!getGContext().getTexureManager().contains(backgroundTexId)) {
            if (container != null) {
                getGContext().getTexureManager().addTextureId(container, backgroundTexId, false);

                TextureVo textureVo = new TextureVo(backgroundTexId);
                textureVo.repeatU = false;
                textureVo.repeatV = false;
                getTextures().add(0, textureVo);
            }
        }

        releaseTextureBitmap(container);
    }

    protected Bitmap createTextureBitmap(int width, int height, Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    protected void releaseTextureBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    protected void prepareRenderingShader(CameraVo camera) {
        if (getVertices() != null && getVertices().size() > 0) {
            super.prepareRenderingShader(camera);
        }
    }

    protected void prepareRenderingBuffer() {
        if (getVertices() != null && getVertices().size() > 0) {
            super.prepareRenderingBuffer();
        }
    }

    protected void doRenderingTask(float[] vMatrix) {
        if (getVertices() != null && getVertices().size() > 0) {
            super.doRenderingTask(vMatrix);
        }
    }
}
