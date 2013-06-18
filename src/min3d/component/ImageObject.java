package min3d.component;

import java.io.FileNotFoundException;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView.ScaleType;
import min3d.Utils;
import min3d.core.GContext;
import min3d.vos.Color4;
import min3d.vos.TextureVo;

public class ImageObject extends ComponentBase {

    private static final String TAG = "ImageObject";

    private static final String PREFIX_IMAGE = "image_";

    private Uri mUri;
    private int mResource = 0;
    private Matrix mMatrix;
    private ScaleType mScaleType;
    private static final float MAPPING_PIXEL = 256.0f;

    // these are applied to the drawable
    private ColorFilter mColorFilter;
    private int mAlpha = 255;
    private int mViewAlphaScale = 256;
    private boolean mColorMod = false;

    private Drawable mDrawable = null;
    private int[] mState = null;
    private boolean mMergeState = false;
    private int mLevel = 0;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private Matrix mDrawMatrix = null;

    // Avoid allocations...
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();

    private boolean mCropToPadding;

    private float mWidth;
    private float mHeight;
    private float mDepth;
    private int mPaddingLeft = 0;
    private int mPaddingRight = 0;
    private int mPaddingTop = 0;
    private int mPaddingBottom = 0;
    private int mScrollX = 0;
    private int mScrollY = 0;
    private int mRight = 0;
    private int mLeft = 0;
    private int mBottom = 0;
    private int mTop = 0;

    public ImageObject(GContext context, float width, float height,
            float depth, Color4[] sixColor4s, Boolean useUvs,
            Boolean useNormals, Boolean useVertexColors) {
        super(context, 4, 2);

        mWidth = width;
        mHeight = height;
        mDepth = depth;
        initImageView();
    }

    public ImageObject(GContext context, float width, float height,
            float depth, Color4[] sixColor4s) {
        this(context, width, height, depth, sixColor4s, true, true, true);
    }

    public ImageObject(GContext context, float width, float height,
            float depth, Color4 color) {
        this(context, width, height, depth, new Color4[] { color, color, color,
                color, color, color }, true, true, true);
    }

    public ImageObject(GContext context, float width, float height, float depth) {
        this(context, width, height, depth, null, true, true, false);
    }

    private void initImageView() {
        mMatrix = new Matrix();
        mScaleType = ScaleType.FIT_CENTER;
        createVertices();
    }

    private void createVertices() {
        float w = mWidth / 2;
        float h = mHeight / 2;
        float d = mDepth / 2;
        short ul, ur, lr, ll;
        float uvX = 1.0f, uvY = 1.0f;

        if (vertices().size() == 0) {
            ul = vertices().addVertex(-w, +h, d, 0f, 0f, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            ur = vertices().addVertex(+w, +h, d, uvX, 0f, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            lr = vertices().addVertex(+w, -h, d, uvX, uvY, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            ll = vertices().addVertex(-w, -h, d, 0f, uvY, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            Utils.addQuad(this, ul, ur, lr, ll);
        } else {
            vertices().overwriteVerts(
                    new float[] { -w, +h, d, +w, +h, d, +w, -h, d, -w, -h, d });
            vertices().setUv(0, 0f, 0f);
            vertices().setUv(1, uvX, 0f);
            vertices().setUv(2, uvX, uvY);
            vertices().setUv(3, 0f, uvY);
        }
    }

    /**
     * Sets a drawable as the content of this ImageView.
     *
     * <p class="note">
     * This does Bitmap reading and decoding on the UI thread, which can cause a
     * latency hiccup. If that's a concern, consider using
     * {@link #setImageDrawable} or {@link #setImageBitmap} and
     * {@link android.graphics.BitmapFactory} instead.
     * </p>
     *
     * @param resId the resource identifier of the the drawable
     *
     * @attr ref android.R.styleable#ImageView_src
     */
    public void setImageResource(int resId) {
        if (mUri != null || mResource != resId) {
            updateDrawable(null);
            mResource = resId;
            mUri = null;
            resolveUri();
            invalidate();
        }
    }

    @Override
    protected void onManageLayerTexture() {
        super.onManageLayerTexture();

        String imageTexId = (mDrawable != null)?PREFIX_IMAGE + mDrawable.toString() + mDrawable.getState():PREFIX_IMAGE;
        String replaced = null;
        for (String id:textures().getIds()) {
            if (id.contains(PREFIX_IMAGE) && !id.equals(PREFIX_IMAGE)) {
                if (id.equals(imageTexId)) {
                    return;
                } else {
                    replaced = id;
                }
                break;
            }
        }

        if (replaced != null) {
            getGContext().getTexureManager().deleteTexture(replaced);
            textures().removeById(replaced);
        }

        if (mDrawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            onDraw(canvas);

            getGContext().getTexureManager().addTextureId(bitmap, imageTexId, false);
            bitmap.recycle();
            TextureVo textureVo = new TextureVo(imageTexId);
            textureVo.textureEnvs.get(0).setAll(GL10.GL_TEXTURE_ENV_MODE,
                    GL10.GL_DECAL);
            textureVo.repeatU = false;
            textureVo.repeatV = false;
            textures().add(textureVo);
        }
    }

    /**
     * Sets the content of this ImageView to the specified Uri.
     *
     * <p class="note">
     * This does Bitmap reading and decoding on the UI thread, which can cause a
     * latency hiccup. If that's a concern, consider using
     * {@link #setImageDrawable} or {@link #setImageBitmap} and
     * {@link android.graphics.BitmapFactory} instead.
     * </p>
     *
     * @param uri The Uri of an image
     */
    public void setImageURI(Uri uri) {
        if (mResource != 0
                || (mUri != uri && (uri == null || mUri == null || !uri
                        .equals(mUri)))) {
            updateDrawable(null);
            mResource = 0;
            mUri = uri;
            resolveUri();
            invalidate();
        }
    }

    /**
     * Sets a Bitmap as the content of this ImageView.
     *
     * @param bm The bitmap to set
     */
    public void setImageBitmap(Bitmap bm) {
        // if this is used frequently, may handle bitmaps explicitly
        // to reduce the intermediate drawable object
        setImageDrawable(new BitmapDrawable(getGContext().getContext()
                .getResources(), bm));
    }

    /**
     * Sets a drawable as the content of this ImageView.
     *
     * @param drawable The drawable to set
     */
    public void setImageDrawable(Drawable drawable) {
        if (mDrawable != drawable) {
            mResource = 0;
            mUri = null;
            updateDrawable(drawable);
            invalidate();
        }
    }

    /**
     * Controls how the image should be resized or moved to match the size of
     * this ImageView.
     *
     * @param scaleType The desired scaling mode.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }

        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            invalidate();
        }
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @see ImageView.ScaleType
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    public ScaleType getScaleType() {
        return mScaleType;
    }

    private void updateDrawable(Drawable d) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
        }
        mDrawable = d;
        if (d != null) {
            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setLevel(mLevel);
            mDrawableWidth = d.getIntrinsicWidth();
            mDrawableHeight = d.getIntrinsicHeight();
            applyColorMod();
            configureBounds();
        } else {
            mDrawableWidth = mDrawableHeight = -1;
        }
    }

    private void applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable.mutate();
            mDrawable.setColorFilter(mColorFilter);
            mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private void configureBounds() {
        if (mDrawable == null) {
            return;
        }

        int dwidth = mDrawableWidth;
        int dheight = mDrawableHeight;

        int vwidth = getWidth() - mPaddingLeft - mPaddingRight;
        int vheight = getHeight() - mPaddingTop - mPaddingBottom;

        boolean fits = (dwidth < 0 || vwidth == dwidth)
                && (dheight < 0 || vheight == dheight);

        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
            /*
             * If the drawable has no intrinsic size, or we're told to
             * scaletofit, then we just fill our entire view.
             */
            mDrawable.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            mDrawable.setBounds(0, 0, dwidth, dheight);

            if (ScaleType.MATRIX == mScaleType) {
                // Use the specified matrix as-is.
                if (mMatrix.isIdentity()) {
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
                mDrawMatrix = null;
            } else if (ScaleType.CENTER == mScaleType) {
                // Center bitmap in view, no scaling.
                mDrawMatrix = mMatrix;
                mDrawMatrix.setTranslate(
                        (int) ((vwidth - dwidth) * 0.5f + 0.5f),
                        (int) ((vheight - dheight) * 0.5f + 0.5f));
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = mMatrix;

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            } else if (ScaleType.CENTER_INSIDE == mScaleType) {
                mDrawMatrix = mMatrix;
                float scale;
                float dx;
                float dy;

                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth,
                            (float) vheight / (float) dheight);
                }

                dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
                dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else {
                // Generate the required transform.
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);

                mDrawMatrix = mMatrix;
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst,
                        scaleTypeToScaleToFit(mScaleType));
            }
        }
    }

    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        if (st == ScaleType.FIT_XY) {
            return Matrix.ScaleToFit.FILL;
        } else if (st == ScaleType.FIT_START) {
            return Matrix.ScaleToFit.START;
        } else if (st == ScaleType.CENTER) {
            return Matrix.ScaleToFit.CENTER;
        } else {
            return Matrix.ScaleToFit.END;
        }
    }

    private int getHeight() {
        // TODO Auto-generated method stub
        int height = (int) (mHeight * MAPPING_PIXEL);
        if (height % 2 == 1) {
            height--;
        }
        return height;
    }

    private int getWidth() {
        // TODO Auto-generated method stub
        int width = (int) (mWidth * this.MAPPING_PIXEL);
        if (width % 2 == 1) {
            width--;
        }
        return width;
    }

    protected void onDraw(Canvas canvas) {
        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return; // nothing to draw (empty bounds)
        }

        if (mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0) {
            mDrawable.draw(canvas);
        } else {
            if (mCropToPadding) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
                        scrollX + mRight - mLeft - mPaddingRight, scrollY
                                + mBottom - mTop - mPaddingBottom);
            }

            canvas.translate(mPaddingLeft, mPaddingTop);

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
        }
    }

    private void resolveUri() {
        if (mDrawable != null) {
            return;
        }

        Resources rsrc = getGContext().getContext().getResources();
        if (rsrc == null) {
            return;
        }

        Drawable d = null;

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Exception e) {
                Log.w(TAG, "Unable to find resource: " + mResource, e);
                // Don't try again.
                mUri = null;
            }
        } else if (mUri != null) {
            String scheme = mUri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                try {
                    // Load drawable through Resources, to get the source
                    // density information
                    OpenResourceIdResult r = getResourceId(mUri);
                    d = r.r.getDrawable(r.id);
                } catch (Exception e) {
                    Log.w(TAG, "Unable to open content: " + mUri, e);
                }
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                    || ContentResolver.SCHEME_FILE.equals(scheme)) {
                try {
                    d = Drawable.createFromStream(getGContext().getContext()
                            .getContentResolver().openInputStream(mUri), null);
                } catch (Exception e) {
                    Log.w(TAG, "Unable to open content: " + mUri, e);
                }
            } else {
                d = Drawable.createFromPath(mUri.toString());
            }

            if (d == null) {
                Log.w(TAG,"resolveUri failed on bad bitmap uri: "
                        + mUri);
                // Don't try again.
                mUri = null;
            }
        } else {
            return;
        }

        updateDrawable(d);
    }

    public OpenResourceIdResult getResourceId(Uri uri)
            throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = getGContext().getContext().getPackageManager()
                        .getResourcesForApplication(authority);
            } catch (NameNotFoundException ex) {
                throw new FileNotFoundException(
                        "No package found for authority: " + uri);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException(
                        "Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: "
                    + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        OpenResourceIdResult res = new OpenResourceIdResult();
        res.r = r;
        res.id = id;
        return res;
    }

    public class OpenResourceIdResult {
        public Resources r;
        public int id;
    }
}
