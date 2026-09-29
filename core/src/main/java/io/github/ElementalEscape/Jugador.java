package io.github.ElementalEscape;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;

/** Jugador con fisica de plataformas AABB simple. */
public class Jugador {
    public enum Tipo { FUEGO, AGUA }

    public Tipo tipo;
    public float x, y, ancho = 30, alto = 46;
    public float velX = 0, velY = 0;
    public boolean enSuelo = false;
    public boolean vivo = true;
    public boolean mirandoDerecha = true;
    public float tiempoAnim = 0;
    public float aplastado = 0; // 1 = aplastado al caer, negativo = estirado al saltar
    public float tiempoReaparicion = 0;
    public float origenX, origenY;

    public static final float VELOCIDAD = 300f;
    public static final float SALTO = 820f;
    public static final float GRAVEDAD = 2200f;

    public Jugador(Tipo tipo, float origenX, float origenY) {
        this.tipo = tipo;
        this.origenX = origenX; this.origenY = origenY;
        this.x = origenX; this.y = origenY;
    }

    public Rectangle cuerpo() { return new Rectangle(x, y, ancho, alto); }

    public void reaparecer() {
        x = origenX; y = origenY; velX = 0; velY = 0;
        vivo = true; tiempoReaparicion = 0; aplastado = 0;
    }

    public void morir() {
        if (!vivo) return;
        vivo = false;
        tiempoReaparicion = 1.2f;
    }

    /** Fisica: mover en X, resolver, mover en Y, resolver. */
    public void actualizar(float dt, boolean izquierda, boolean derecha, boolean quiereSaltar, List<Rectangle> solidos) {
        tiempoAnim += dt;
        if (aplastado > 0) aplastado -= dt * 4f;
        if (aplastado < 0) aplastado += dt * 4f;

        if (!vivo) {
            tiempoReaparicion -= dt;
            return;
        }

        float direccion = 0;
        if (izquierda) direccion -= 1;
        if (derecha) direccion += 1;
        velX = direccion * VELOCIDAD;
        if (direccion != 0) mirandoDerecha = direccion > 0;

        velY -= GRAVEDAD * dt;
        if (velY < -900) velY = -900;

        // Eje X
        x += velX * dt;
        Rectangle r = cuerpo();
        for (Rectangle s : solidos) {
            if (r.overlaps(s)) {
                if (velX > 0) x = s.x - ancho;
                else if (velX < 0) x = s.x + s.width;
                velX = 0;
                r = cuerpo();
            }
        }
        // Eje Y
        float velYPrevia = velY;
        y += velY * dt;
        enSuelo = false;
        r = cuerpo();
        for (Rectangle s : solidos) {
            if (r.overlaps(s)) {
                if (velY <= 0 && y >= s.y + s.height - 20) {
                    if (velYPrevia < -500) aplastado = 1f;
                    y = s.y + s.height;
                    velY = 0;
                    enSuelo = true;
                } else if (velY > 0) {
                    y = s.y - alto;
                    velY = 0;
                } else {
                    if (y + alto / 2 < s.y + s.height / 2) y = s.y - alto;
                    else { y = s.y + s.height; enSuelo = true; }
                    velY = 0;
                }
                r = cuerpo();
            }
        }
        if (quiereSaltar && enSuelo) {
            velY = SALTO;
            enSuelo = false;
            aplastado = -0.7f;
        }
        if (x < 0) x = 0;
        if (x > Nivel.MUNDO_ANCHO - ancho) x = Nivel.MUNDO_ANCHO - ancho;
        if (y < -80) morir();
    }
}
