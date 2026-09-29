package io.github.ElementalEscape;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Dibujo solo con SpriteBatch (sin ShapeRenderer: crashea driver AMD atio6axx). */
public class Dibujo {
    public static Texture texturaBlanca;
    public static Texture texturaCirculo;

    public static void iniciar() {
        if (texturaBlanca != null) return;
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill();
        texturaBlanca = new Texture(p); p.dispose();
        int lado = 64;
        Pixmap c = new Pixmap(lado, lado, Pixmap.Format.RGBA8888);
        c.setColor(0, 0, 0, 0); c.fill();
        c.setColor(Color.WHITE);
        c.fillCircle(lado / 2, lado / 2, lado / 2 - 1);
        texturaCirculo = new Texture(c); c.dispose();
    }

    public static void rectangulo(SpriteBatch lote, float x, float y, float ancho, float alto, Color color) {
        lote.setColor(color);
        lote.draw(texturaBlanca, x, y, ancho, alto);
        lote.setColor(Color.WHITE);
    }

    public static void circulo(SpriteBatch lote, float x, float y, float diametro, Color color) {
        lote.setColor(color);
        lote.draw(texturaCirculo, x, y, diametro, diametro);
        lote.setColor(Color.WHITE);
    }

    public static void liberar() {
        if (texturaBlanca != null) texturaBlanca.dispose();
        if (texturaCirculo != null) texturaCirculo.dispose();
        texturaBlanca = null; texturaCirculo = null;
    }
}
