package min3d.component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView.BufferType;
import min3d.Utils;
import min3d.core.GContext;
import min3d.vos.Color4;
import min3d.vos.TextureVo;

/**
 * Note how each 'face' (quad) of the box uses its own set of 4 vertices each,
 * rather than sharing with adjacent faces. This allows for each face to be
 * texture mapped, normal'ed, and colored independently of the others.
 *
 * Object origin is center of box.
 */
public class TextObject extends ComponentBase {
    private static final String TAG = "TextObject";
    private static final float MAPPING_PIXEL = 256.0f;

    private float mWidth;
    private float mHeight;
    private float mDepth;
    TextView mTextView;

    public TextObject(GContext context, float width, float height, float depth,
            Color4[] sixColor4s, Boolean useUvs, Boolean useNormals,
            Boolean useVertexColors) {
        super(context, 4, 2);

        mWidth = width;
        mHeight = height;
        mDepth = depth;
        LinearLayout ll = new LinearLayout(context.getContext());

        mTextView = new TextView(context.getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                (int) (mWidth * MAPPING_PIXEL), (int) (mHeight * MAPPING_PIXEL));
        mTextView.setLayoutParams(layoutParams);
        ll.addView(mTextView);
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

    public void setText(CharSequence text) {
        mTextView.setText(text);
        int w = (int) (mWidth * MAPPING_PIXEL);
        if (w % 2 == 1) {
            w--;
        }
        int h = (int) (mHeight * MAPPING_PIXEL);
        if (h % 2 == 1) {
            h--;
        }

        Bitmap bitmap = loadBitmapFromView(mTextView, w, h);
        if (mGContext.getTexureManager().contains(toString())) {
            mGContext.getTexureManager().deleteTexture(toString());
            textures().removeById(toString());
        }
        mGContext.getTexureManager().addTextureId(bitmap, toString(), false);
        TextureVo textureText = new TextureVo(toString());
        textureText.textureEnvs.get(0).setAll(GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_DECAL);
        textureText.repeatU = false;
        textureText.repeatV = false;
        textures().add(textureText);
        bitmap.recycle();
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

    private Bitmap loadBitmapFromView(View v, int w, int h) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, w, h);
        v.draw(c);
        return b;
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
        mTextView.setTextSize(size);
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
    }

    /**
     * Sets the typeface and style in which the text should be displayed. Note
     * that not all Typeface families actually have bold and italic variants.
     *
     */
    public void setTypeface(Typeface tf) {
        mTextView.setTypeface(tf);
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
        mTextView.setTextColor(color);
    }

    /**
     * Sets the text color.
     *
     */
    public void setTextColor(ColorStateList colors) {
        mTextView.setTextColor(colors);
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
    }

    /**
     * Sets the horizontal alignment of the text and the vertical gravity that
     * will be used when there is extra space in the TextObject beyond what is
     * required for the text itself.
     *
     * @see android.view.Gravity
     */
    public void setGravity(int gravity) {
        mTextView.setGravity(gravity);
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
     * Sets the text that this TextObject is to display (see
     * {@link #setText(CharSequence)}) and also sets whether it is stored in a
     * styleable/spannable buffer and whether it is editable.
     *
     */
    public void setText(CharSequence text, BufferType type) {
        mTextView.setText(text, type);
    }

    public final void setText(char[] text, int start, int len) {
        mTextView.setText(text, start, len);
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
    }

    /**
     * Causes words in the text that are longer than the view is wide to be
     * ellipsized instead of broken in the middle. You may also want to
     * {@link #setSingleLine} or {@link #setHorizontallyScrolling} to constrain
     * the text to a single line. Use <code>null</code> to turn off ellipsizing.
     *
     */
    public void setEllipsize(TextUtils.TruncateAt where) {
        mTextView.setEllipsize(where);
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
            setMeasuredDimension((int) (mWidth * MAPPING_PIXEL),
                    (int) (mHeight * MAPPING_PIXEL));
        }
    }
}