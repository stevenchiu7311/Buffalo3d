package cm.buffalo3d.engine.objectPrimitives;

import cm.buffalo3d.engine.core.GContext;
import cm.buffalo3d.engine.core.Object3dContainer;
import cm.buffalo3d.engine.vos.Color4;

public class Rectangle extends Object3dContainer {
    int mSegNumW, mSegNumH;
    Color4 mColor4;

    public Rectangle(GContext context, float width, float height, int segNumW, int segNumH) {
        this(context, width, height, segNumW, segNumH, new Color4());
    }

    public Rectangle(GContext context, float width, float height, int segNumW, int segNumH,
            Color4 color) {
        super(context, 4 * segNumW * segNumH, 2 * segNumW * segNumH);

        mSegNumW = segNumW;
        mSegNumH = segNumH;
        mColor4 = color;
        layout(width, height);

        int row, col;

        // Add faces
        getFaces().clear();
        int colspan = mSegNumW + 1;
        for (row = 1; row <= mSegNumH; row++) {
            for (col = 1; col <= mSegNumW; col++) {
                int lr = row * colspan + col;
                int ll = lr - 1;
                int ur = lr - colspan;
                int ul = ur - 1;
                getFaces().add((short) ul, (short) lr, (short) ur);
                getFaces().add((short) ul, (short) ll, (short) lr);
            }
        }
    }

    public void defaultColor(Color4 color) {
        super.defaultColor(color);
        mColor4 = color;
    }

    public void layout(float w, float h) {
        super.layout(w, h);
        float oldW = getWidth();
        float oldH = getHeight();
        if (w != oldW || h != oldH) {
            setWidth(w);
            setHeight(h);

            int row, col;

            float segW = w / mSegNumW;
            float segH = h / mSegNumH;

            float halfW = w / 2f;
            float halfH = h / 2f;

            // Add vertices
            getVertices().clear();
            for (row = 0; row <= mSegNumH; row++) {
                for (col = 0; col <= mSegNumW; col++) {
                    getVertices().addVertex(
                            (float) col * segW - halfW, (float) row * segH - halfH, 0f,
                            (float) col / (float) mSegNumW, 1 - (float) row / (float) mSegNumH,
                            0, 0, 1f,
                            mColor4.r, mColor4.g, mColor4.b, mColor4.a);
                }
            }
        }
    }
}
