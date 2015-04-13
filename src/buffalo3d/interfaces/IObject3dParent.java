package buffalo3d.interfaces;

import buffalo3d.core.Object3d;

/**
 * Defines the responsibilities for a class that will be a parent of a Object3d.
 * This is the API that a Object3d sees when it wants to interact with its parent.
 *
 */
public interface IObject3dParent {
    /**
     * Returns the parent if it exists, or null.
     *
     * @return a IObject3dParent or null if this IObject3dParent does not have a parent
     */
    public IObject3dParent getParent();

    /**
     * Called when a child of this parent wants focus
     *
     * @param child The child of this IObject3dParent that wants focus. This view
     *        will contain the focused view. It is not necessarily the view that
     *        actually has focus.
     * @param focused The view that is a descendant of child that actually has
     *        focus
     */
    public void requestChildFocus(Object3d child, Object3d focused);

    /**
     * Called when a child of this parent is giving up focus
     *
     * @param child The view that is giving up focus
     */
    public void clearChildFocus(Object3d child);

    /**
     * Find the nearest view in the specified direction that wants to take focus
     *
     * @param obj The view that currently has focus
     * @param direction One of FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, and FOCUS_RIGHT
     */
    public Object3d focusSearch(Object3d obj, int direction);

    /**
     * Tells the parent that a new focusable view has become available. This is
     * to handle transitions from the case where there are no focusable views to
     * the case where the first focusable view appears.
     *
     * @param obj The view that has become newly focusable
     */
    public void focusableObjectAvailable(Object3d obj);
}
