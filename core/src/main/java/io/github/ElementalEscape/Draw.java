package io.github.ElementalEscape;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Dibujo solo con SpriteBatch (sin ShapeRenderer: crashea driver AMD atio6axx). */
public class Draw {
    public static Texture white;
    public static Texture circle;

    public static void init() {
        if (white != null) return;
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill();
        white = new Texture(p); p.dispose();
        int s = 64;
        Pixmap c = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        c.setColor(0, 0, 0, 0); c.fill();
        c.setColor(Color.WHITE);
        c.fillCircle(s / 2, s / 2, s / 2 - 1);
        circle = new Texture(c); c.dispose();
    }

    public static void rect(SpriteBatch b, float x, float y, float w, float h, Color col) {
        b.setColor(col);
        b.draw(white, x, y, w, h);
        b.setColor(Color.WHITE);
    }

    public static void circ(SpriteBatch b, float x, float y, float d, Color col) {
        b.setColor(col);
        b.draw(circle, x, y, d, d);
        b.setColor(Color.WHITE);
    }

    public static void dispose() {
        if (white != null) white.dispose();
        if (circle != null) circle.dispose();
        white = null; circle = null;
    }
}
