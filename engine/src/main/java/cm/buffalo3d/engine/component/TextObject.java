package cm.buffalo3d.engine.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView.BufferType;

import cm.buffalo3d.engine.core.FacesBufferedList;
import cm.buffalo3d.engine.core.GContext;
import cm.buffalo3d.engine.core.Object3d;
import cm.buffalo3d.engine.core.Vertices;
import cm.buffalo3d.engine.util.Utils;
import cm.buffalo3d.engine.vos.Color4;
import cm.buffalo3d.engine.vos.TextureVo;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

/**
 * Note how each 'face' (quad) of the box uses its own set of 4 vertices each,
 * rather than sharing with adjacent faces. This allows for each face to be
 * texture mapped, normal'ed, and colored independently of the others.
 *
 * Object origin is center of box.
 */
public class TextObject extends ComponentBase {
    private static final String TAG = "TextObject";

    private static final String PREFIX_TEXT = "text_";

    float mDepth;

    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private boolean mHasShape;
    private TextView mTextView;
    private float mRatio;
    private float mMaxWidth = Integer.MAX_VALUE;
    private StringBuilder mTextTexId;

    public TextObject(GContext context, float width, float height, float depth,
            Color4[] sixColor4s, Boolean useUvs, Boolean useNormals,
            Boolean useVertexColors) {
        super(context, 4, 2);

        setWidth(width);
        setHeight(height);
        mDepth = depth;

        mTextView = new TextView(context.getContext());
        // Force text view to configure its size.
        mTextView.setTextSize(mTextView.getTextSize());
    }

    public TextObject(GContext context, float width, float height, float depth,
            Color4[] sixColor4s) {
        this(context, width, height, depth, sixColor4s, true, true, true);
    }

    public TextObject(GContext context, float width, float height, float depth,
            Color4 color) {
        this(context, width, height, depth, new Color4[] { color, color, color,
                color, color, color }, true, true, true);
    }

    public TextObject(GContext context, float width, float height, float depth) {
        this(context, width, height, depth, null, true, true, false);
    }

    public void setRatio(float ratio) {
        if (ratio != mRatio) {
            mRatio = ratio;
            invalidate();
        }
    }

    public void setText(CharSequence text) {
        if (!text.equals(mTextView.getText())) {
            mTextView.setText(text);
            invalidate();
        }
    }

    public void setShape(Vertices vertices, FacesBufferedList faces) {
        super.setShape(vertices, faces);
        mHasShape = true;
    }

    public void setShape(Object3d object3d) {
        super.setShape(object3d);
        mHasShape = true;
    }

    public float getMeasuredWidth() {
        return mMeasuredWidth / mRatio;
    }

    public float getMeasuredHeight() {
        return mMeasuredHeight / mRatio;
    }

    @Override
    protected void onManageLayerTexture() {
        super.onManageLayerTexture();
        float x = getGContext().getRenderer().getWorldPlaneSize(position().z).x;
        mRatio = getGContext().getRenderer().getWidth() / x;

        int layoutWidth = (int) ((getWidth() > 0) ? (int) (getWidth() * mRatio) : getWidth());
        int layoutHeight = (int) ((getHeight() > 0) ? (int) (getHeight() * mRatio) : getHeight());
        LayoutParams layoutParams = new LayoutParams(layoutWidth, layoutHeight);
        mTextView.setLayoutParams(layoutParams);
        mTextView.setMaxWidth((int) (mMaxWidth * mRatio));
        mTextView.updateMeasuredDimension();

        int measuredWidth = mTextView.getMeasuredWidth();
        int measuredHeight = mTextView.getMeasuredHeight();
        if (!mHasShape && (mMeasuredWidth != measuredWidth || mMeasuredHeight != measuredHeight)) {
            layout(measuredWidth / mRatio, measuredHeight / mRatio);
        }

        if (mTextTexId == null) {
            mTextTexId = new StringBuilder(PREFIX_TEXT);
        }
        mTextTexId.append(mTextView.toString());

        String textTexId = mTextTexId.toString();
        mTextTexId.delete(PREFIX_TEXT.length(), mTextTexId.capacity());
        destroyLastTextRes();
        if (mTextView != null) {
            if (measuredWidth > 0 && measuredHeight > 0) {
                Bitmap bitmap = loadBitmapFromView(mTextView, measuredWidth, measuredHeight);
                mGContext.getTexureManager().addTextureId(bitmap, textTexId, false);
                recycleTextBitmap(bitmap);
            } else {
                Log.e(TAG, "Cannot allocate invalid bitmap for text("
                        + mTextView.getText() + ") with measured width("
                        + measuredWidth + ") and measured height("
                        + measuredHeight + ").");
            }

            TextureVo textureText = new TextureVo(textTexId);
            // Texture env should be not used if text object background is transparent.
            // On the contrary, texture env should be applied when it already have any background texture.
            if (getTextures().size() > 0) {
                textureText.textureEnvs.get(0).setAll(GL10.GL_TEXTURE_ENV_MODE,
                        GL10.GL_DECAL);
            }
            textureText.repeatU = false;
            textureText.repeatV = false;
            getTextures().add(textureText);
        }

        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;
    }

    public void destroyLastTextRes() {
        for (String id:getTextures().getIds()) {
            if (id.contains(PREFIX_TEXT)) {
                if (mGContext.getTexureManager().contains(id)) {
                    mGContext.getTexureManager().deleteTexture(id);
                }
                getTextures().removeById(id);
            }
        }
    }

    public void layout(float width, float height) {
        super.layout(width, height);

        float w = width / 2;
        float h = height / 2;
        float d = mDepth / 2;
        short ul, ur, lr, ll;
        float u = 1f, v = 1f;
        if (getVertices().size() == 0) {
            ul = getVertices().addVertex(-w, +h, d, 0f, 0f, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            ur = getVertices().addVertex(+w, +h, d, u, 0f, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            lr = getVertices().addVertex(+w, -h, d, u, v, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            ll = getVertices().addVertex(-w, -h, d, 0f, v, 0, 0, 1, (short) 0,
                    (short) 0, (short) 0, (short) 255);
            Utils.addQuad(this, ul, ur, lr, ll);
        } else {
            getVertices().overwriteVerts(
                    new float[] { -w, +h, d, +w, +h, d, +w, -h, d, -w, -h, d });
            getVertices().setUv(0, 0f, 0f);
            getVertices().setUv(1, u, 0f);
            getVertices().setUv(2, u, v);
            getVertices().setUv(3, 0f, v);
        }
    }

    private Bitmap loadBitmapFromView(TextView view, int width, int height) {
        Bitmap bitmap = createTextureBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, width, height);
        view.draw(canvas);
        return bitmap;
    }

    protected Bitmap createTextureBitmap(int width, int height, Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, config);
    }

    protected void recycleTextBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    // for debug.
    public static boolean saveBitmap(Bitmap bitmap, String fileName,
            String dateString) {
        FileOutputStream fOut = null;
        boolean result = true;
        try {
            fOut = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "FileNotFoundException for: " + fileName);
        }

        if (fOut == null) {
            return false;
        }

        if (bitmap != null) {
            result &= bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        }

        try {
            fOut.flush();
        } catch (IOException e) {
            result &= false;
            e.printStackTrace();
        }
        try {
            fOut.close();

        } catch (IOException e) {
            result &= false;
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Sets the typeface and style in which the text should be displayed, and
     * turns on the fake bold and italic bits in the Paint if the Typeface that
     * you provided does not have all the bits in the style that you specified.
     *
     */
    public void setTypeface(Typeface tf, int style) {
        mTextView.setTypeface(tf, style);
        invalidate();
    }

    /**
     * Return the text the TextView is displaying. If setText() was called with
     * an argument of BufferType.SPANNABLE or BufferType.EDITABLE, you can cast
     * the return value from this method to Spannable or Editable, respectively.
     *
     * Note: The content of the return value should not be modified. If you want
     * a modifiable one, you should make your own copy first.
     */
    public CharSequence getText() {
        return mTextView.getText();
    }

    /**
     * Returns the length, in characters, of the text managed by this TextObject
     */
    public int length() {
        return mTextView.length();
    }

    /**
     * @return the height of one standard line in pixels. Note that markup
     *         within the text can cause individual lines to be taller or
     *         shorter than this height, and the layout may contain additional
     *         first- or last-line padding.
     */
    public int getLineHeight() {
        return mTextView.getLineHeight();
    }

    /**
     * @return the size (in pixels) of the default text size in this TextObject.
     */
    public float getTextSize() {
        return mTextView.getTextSize();
    }

    /**
     * Set the default text size to the given value, interpreted as "scaled
     * pixel" units. This size is adjusted based on the current density and user
     * font size preference.
     *
     * @param size The scaled pixel size.
     */
    public void setTextSize(float size) {
        if (size != getTextSize()) {
            mTextView.setTextSize(size);
            invalidate();
        }
    }

    /**
     * Set the default text size to a given unit and value.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     *
     */
    public void setTextSize(int unit, float size) {
        mTextView.setTextSize(unit, size);
        invalidate();
    }

    /**
     * Sets the typeface and style in which the text should be displayed. Note
     * that not all Typeface families actually have bold and italic variants.
     *
     */
    public void setTypeface(Typeface tf) {
        mTextView.setTypeface(tf);
        invalidate();
    }

    /**
     * @return the current typeface and style in which the text is being
     *         displayed.
     */
    public Typeface getTypeface() {
        return mTextView.getTypeface();
    }

    /**
     * Sets the text color for all the states (normal, selected, focused) to be
     * this color.
     *
     */
    public void setTextColor(int color) {
        if (color != getCurrentTextColor()) {
            mTextView.setTextColor(color);
            invalidate();
        }
    }

    /**
     * Sets the text color.
     *
     */
    public void setTextColor(ColorStateList colors) {
        if (colors != getTextColors()) {
            mTextView.setTextColor(colors);
            invalidate();
        }
    }

    /**
     * Return the set of text colors.
     *
     * @return Returns the set of text colors.
     */
    public final ColorStateList getTextColors() {
        return mTextView.getTextColors();
    }

    /**
     * Return the current color selected for normal text.
     *
     * @return Returns the current text color.
     */
    public final int getCurrentTextColor() {
        return mTextView.getCurrentTextColor();
    }

    /**
     * Gives the text a shadow of the specified radius and color, the specified
     * distance from its normal position.
     *
     */
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        mTextView.setShadowLayer(radius, dx, dy, color);
        invalidate();
    }

    /**
     * Sets the horizontal alignment of the text and the vertical gravity that
     * will be used when there is extra space in the TextObject beyond what is
     * required for the text itself.
     *
     * @see android.view.Gravity
     */
    public void setGravity(int gravity) {
        if (gravity != getGravity()) {
            mTextView.setGravity(gravity);
            invalidate();
        }
    }

    /**
     * Returns the horizontal and vertical alignment of this TextObject.
     *
     * @see android.view.Gravity
     */
    public int getGravity() {
        return mTextView.getGravity();
    }

    /**
     * Makes the TextView at most this many lines tall.
     *
     * Setting this value overrides any other (maximum) height setting.
     */
    public void setMaxLines(int maxlines) {
        mTextView.setMaxLines(maxlines);
        invalidate();
    }

    /**
     * Makes the TextView at most this GL wide
     */
    public void setMaxWidth(float width) {
        mMaxWidth = width;
        invalidate();
    }

    /**
     * Sets the text that this TextObject is to display (see
     * {@link #setText(CharSequence)}) and also sets whether it is stored in a
     * styleable/spannable buffer and whether it is editable.
     *
     */
    public void setText(CharSequence text, BufferType type) {
        mTextView.setText(text, type);
        invalidate();
    }

    public final void setText(char[] text, int start, int len) {
        mTextView.setText(text, start, len);
        invalidate();
    }

    /**
     * Return the number of lines of text, or 0 if the internal Layout has not
     * been built.
     */
    public int getLineCount() {
        return mTextView.getLineCount();
    }

    /**
     * Sets the properties of this field (lines, horizontally scrolling,
     * transformation method) to be for a single-line input.
     *
     * @attr ref android.R.styleable#TextView_singleLine
     */
    public void setSingleLine() {
        mTextView.setSingleLine();
        invalidate();
    }

    /**
     * Causes words in the text that are longer than the view is wide to be
     * ellipsized instead of broken in the middle. You may also want to
     * {@link #setSingleLine} or {@link #setHorizontallyScrolling} to constrain
     * the text to a single line. Use <code>null</code> to turn off ellipsizing.
     *
     */
    public void setEllipsize(TextUtils.TruncateAt where) {
        if (where != getEllipsize()) {
            mTextView.setEllipsize(where);
            invalidate();
        }
    }

    /**
     * Returns where, if anywhere, words that are longer than the view is wide
     * should be ellipsized.
     */
    public TextUtils.TruncateAt getEllipsize() {
        return mTextView.getEllipsize();
    }

    public class TextView extends android.widget.TextView {
        public TextView(Context context) {
            super(context);
        }

        public int getMeasureSpec(int spec, int padding, int dimension) {
            int resultSize = 0;
            int resultMode = 0;
            // In engine3d text view, parent asked to see how big we want to be
            if (dimension >= 0) {
                // Child wants a specific size... let him have it
                resultSize = dimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (dimension == LayoutParams.MATCH_PARENT) {
                // Child wants to be our size... find out how big it should
                // be
                resultSize = 0;
                resultMode = MeasureSpec.UNSPECIFIED;
            } else if (dimension == LayoutParams.WRAP_CONTENT) {
                // Child wants to determine its own size.... find out how
                // big it should be
                resultSize = 0;
                resultMode = MeasureSpec.UNSPECIFIED;
            }
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }

        public void updateMeasuredDimension() {
            final LayoutParams lp = (LayoutParams) getLayoutParams();
            final int childWidthMeasureSpec = getMeasureSpec(MeasureSpec.UNSPECIFIED, 0, lp.width);
            final int childHeightMeasureSpec = getMeasureSpec(MeasureSpec.UNSPECIFIED, 0, lp.height);
            measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }
}
