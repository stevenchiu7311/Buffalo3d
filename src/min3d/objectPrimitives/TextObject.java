package min3d.objectPrimitives;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;

/**
 * Note how each 'face' (quad) of the box uses its own set of 4 vertices each,
 * rather than sharing with adjacent faces. This allows for each face to be
 * texture mapped, normal'ed, and colored independently of the others.
 *
 * Object origin is center of box.
 */
public class TextObject extends Object3dContainer {
    private static final String TAG = "TextObject";
    private static final float MAPPING_PIXEL = 256.0f;

    private Color4[] mColors;
    private float mWidth;
    private float mHeight;
    private float mDepth;
    private boolean mWrapContent = false;
    private float mStringWidth;
    private float mStringHeight;
    private int mFontSize = 14;
    private Color4 mFontColor = new Color4(0, 0, 0, 255);
    private Color4 mBackgroundColor = new Color4(0, 0, 0, 0);
    private int mTextBitmapWidth;
    private int mTextBitmapHeight;

    public TextObject(float width, float height, float depth,
            Color4[] sixColor4s, Boolean useUvs, Boolean useNormals,
            Boolean useVertexColors) {
        super(4, 2, useUvs, useNormals, useVertexColors);

        mWidth = width;
        mHeight = height;
        mDepth = depth;

        if (sixColor4s != null) {
            mColors = sixColor4s;
        } else {
            mColors = new Color4[6];
            mColors[0] = new Color4(255, 0, 0, 255);
            mColors[1] = new Color4(0, 255, 0, 255);
            mColors[2] = new Color4(0, 0, 255, 255);
            mColors[3] = new Color4(255, 255, 0, 255);
            mColors[4] = new Color4(0, 255, 255, 255);
            mColors[5] = new Color4(255, 0, 255, 255);
        }
    }

    public void setText(String text) {

        Paint textPaint = new Paint();
        textPaint.setTextSize(mFontSize);
        textPaint.setAntiAlias(true);
        textPaint.setARGB(mFontColor.a, mFontColor.r, mFontColor.g,
                mFontColor.b);
        mStringHeight = textPaint.descent() - textPaint.ascent();
        mStringWidth = textPaint.measureText(text);
        if (mWrapContent) {
            this.mHeight = mStringHeight / MAPPING_PIXEL;
            this.mWidth = mStringWidth / MAPPING_PIXEL;
        }
        float tempTextBitmapWidth = mWidth * MAPPING_PIXEL;
        float tempTextBitmapHeight = mHeight * MAPPING_PIXEL;
        mTextBitmapWidth = ((tempTextBitmapWidth) - (int) (tempTextBitmapWidth)) > 0 ? (int) (tempTextBitmapWidth) + 1
                : (int) (tempTextBitmapWidth);
        mTextBitmapHeight = ((tempTextBitmapHeight) - (int) (tempTextBitmapHeight)) > 0 ? (int) (tempTextBitmapHeight) + 1
                : (int) (tempTextBitmapHeight);
        if (mTextBitmapWidth % 2 == 1) {
            mTextBitmapWidth += 1;
        }
        if (mTextBitmapHeight % 2 == 1) {
            mTextBitmapHeight += 1;
        }

        Bitmap bitmap = Bitmap.createBitmap(mTextBitmapWidth, mTextBitmapHeight, Bitmap.Config.ARGB_4444);;
        // get a canvas to paint over the bitmap
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.argb(mBackgroundColor.a, mBackgroundColor.r,
                mBackgroundColor.g, mBackgroundColor.b));
        canvas.drawText(text, 0, mStringHeight, textPaint);
        if (Shared.textureManager().contains(this.toString())) {
            Shared.textureManager().deleteTexture(this.toString());
        }
        Shared.textureManager().addTextureId(bitmap, this.toString(), true);
        this.textures().removeAll();
        this.textures().addById(this.toString());
        //saveBitmap(bitmap, "/data/data/min3d.sampleProject1/test.png", "test");
        bitmap.recycle();
        createVertices();
    }

    // for debug.
    private boolean saveBitmap(Bitmap bitmap, String fileName, String dateString) {
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

    public TextObject(float width, float height, float depth,
            Color4[] sixColor4s) {
        this(width, height, depth, sixColor4s, true, true, true);
    }

    public TextObject(float width, float height, float depth,
            Color4 color) {
        this(width, height, depth, new Color4[] { color, color, color,
                color, color, color }, true, true, true);
    }

    public TextObject(float width, float height, float depth) {
        this(width, height, depth, null, true, true, true);
    }

    public void setFontSize(int size) {
        mFontSize = size;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public void setFontColor(Color4 color) {
        mFontColor = color;
    }

    public Color4 getFontColor() {
        return mFontColor;
    }

    public void setBackgroundColor(Color4 color) {
        mBackgroundColor = color;
    }

    public Color4 getBackgroundColor() {
        return mBackgroundColor;
    }

    public void destory() {
        Shared.textureManager().deleteTexture(this.toString());
    }

    public void setWrapContent(Boolean wrapContent) {
        this.mWrapContent = wrapContent;
    }

    private void createVertices() {
        float w = mWidth / 2;
        float h = mHeight / 2;
        float d = mDepth / 2;
        short ul, ur, lr, ll;
        float uvX, uvY;
        if (mWrapContent) {
            uvX = mStringWidth / mTextBitmapWidth;
            uvY = mStringHeight / mTextBitmapHeight;
        } else {
            uvX = (mWidth * 256.0f) / mTextBitmapWidth;
            uvY = (mHeight * 256.0f) / mTextBitmapHeight;
        }
        if (vertices().size() == 0) {
            ul = this.vertices().addVertex(-w, +h, d, 0f, 0f, 0, 0, 1,
                    mColors[0].r, mColors[0].g, mColors[0].b, mColors[0].a);
            ur = this.vertices().addVertex(+w, +h, d, uvX, 0f, 0, 0, 1,
                    mColors[0].r, mColors[0].g, mColors[0].b, mColors[0].a);
            lr = this.vertices().addVertex(+w, -h, d, uvX, uvY, 0, 0, 1,
                    mColors[0].r, mColors[0].g, mColors[0].b, mColors[0].a);
            ll = this.vertices().addVertex(-w, -h, d, 0f, uvY, 0, 0, 1,
                    mColors[0].r, mColors[0].g, mColors[0].b, mColors[0].a);
            Utils.addQuad(this, ul, ur, lr, ll);
        } else {
            this.vertices().overwriteVerts(
                    new float[] { -w, +h, d, +w, +h, d, +w, -h, d, -w, -h, d });
            this.vertices().setUv(0, 0f, 0f);
            this.vertices().setUv(1, uvX, 0f);
            this.vertices().setUv(2, uvX, uvY);
            this.vertices().setUv(3, 0f, uvY);
        }
    }
}
