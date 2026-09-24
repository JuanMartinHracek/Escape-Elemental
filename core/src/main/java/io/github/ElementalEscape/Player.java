package io.github.ElementalEscape;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;

/** Jugador con fisica plataforma AABB simple. */
public class Player {
    public enum Tipo { FUEGO, AGUA }

    public Tipo tipo;
    public float x, y, w = 30, h = 46;
    public float vx = 0, vy = 0;
    public boolean onGround = false;
    public boolean alive = true;
    public boolean facingRight = true;
    public float animTime = 0;
    public float squash = 0; // 1 = aplastado al caer, - = estirado al saltar
    public float respawnTimer = 0;
    public float spawnX, spawnY;

    public static final float SPEED = 300f;
    public static final float JUMP = 820f;
    public static final float GRAV = 2200f;

    public Player(Tipo tipo, float sx, float sy) {
        this.tipo = tipo;
        this.spawnX = sx; this.spawnY = sy;
        this.x = sx; this.y = sy;
    }

    public Rectangle rect() { return new Rectangle(x, y, w, h); }

    public void respawn() {
        x = spawnX; y = spawnY; vx = 0; vy = 0;
        alive = true; respawnTimer = 0; squash = 0;
    }

    public void kill() {
        if (!alive) return;
        alive = false;
        respawnTimer = 1.2f;
    }

    /** Fisica: mover X, resolver, mover Y, resolver. */
    public void update(float dt, boolean left, boolean right, boolean jumpPressed, List<Rectangle> solids) {
        animTime += dt;
        if (squash > 0) squash -= dt * 4f;
        if (squash < 0) squash += dt * 4f;

        if (!alive) {
            respawnTimer -= dt;
            return;
        }

        float dir = 0;
        if (left) dir -= 1;
        if (right) dir += 1;
        vx = dir * SPEED;
        if (dir != 0) facingRight = dir > 0;

        vy -= GRAV * dt;
        if (vy < -900) vy = -900;

        // Eje X
        x += vx * dt;
        Rectangle r = rect();
        for (Rectangle s : solids) {
            if (r.overlaps(s)) {
                if (vx > 0) x = s.x - w;
                else if (vx < 0) x = s.x + s.width;
                vx = 0;
                r = rect();
            }
        }
        // Eje Y
        float prevVy = vy;
        y += vy * dt;
        onGround = false;
        r = rect();
        for (Rectangle s : solids) {
            if (r.overlaps(s)) {
                if (vy <= 0 && y >= s.y + s.height - 20) {
                    if (prevVy < -500) squash = 1f;
                    y = s.y + s.height;
                    vy = 0;
                    onGround = true;
                } else if (vy > 0) {
                    y = s.y - h;
                    vy = 0;
                } else {
                    if (y + h / 2 < s.y + s.height / 2) y = s.y - h;
                    else { y = s.y + s.height; onGround = true; }
                    vy = 0;
                }
                r = rect();
            }
        }
        if (jumpPressed && onGround) {
            vy = JUMP;
            onGround = false;
            squash = -0.7f;
        }
        if (x < 0) x = 0;
        if (x > Level.WORLD_W - w) x = Level.WORLD_W - w;
        if (y < -80) kill();
    }
}
