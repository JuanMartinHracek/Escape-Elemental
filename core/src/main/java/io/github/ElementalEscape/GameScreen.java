package io.github.ElementalEscape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.List;

/** Juego solo con SpriteBatch (evita crash AMD con ShapeRenderer). */
public class GameScreen implements Screen {
    final Main game;
    final NetManager net;
    OrthographicCamera cam;
    SpriteBatch batch;
    BitmapFont font;

    Player fuego, agua;
    Level level;
    int nivelIdx = 0;
    public static final int MAX_NIVEL = 3;
    int puntos = 0;
    int vidasFuego = 3, vidasAgua = 3;
    float time = 0, winTimer = 0, sendTimer = 0;
    boolean wonLevel = false, gameOver = false, victoryTotal = false;
    String msg = "";
    boolean wWas = false, upWas = false;

    final Color COL_FUEGO = new Color(1f, 0.3f, 0.15f, 1);
    final Color COL_AGUA = new Color(0.2f, 0.6f, 1f, 1);

    public GameScreen(Main game, NetManager net) {
        this.game = game; this.net = net;
    }

    @Override public void show() {
        cam = new OrthographicCamera(Level.WORLD_W, Level.WORLD_H);
        cam.position.set(Level.WORLD_W/2, Level.WORLD_H/2, 0);
        batch = new SpriteBatch(); font = new BitmapFont();
        font.getData().setScale(1.4f);
        Draw.init();
        cargarNivel(0);
    }

    void cargarNivel(int idx) {
        nivelIdx = idx;
        level = Level.crearNivel(idx);
        fuego = new Player(Player.Tipo.FUEGO, level.spawnFire.x, level.spawnFire.y);
        agua = new Player(Player.Tipo.AGUA, level.spawnWater.x, level.spawnWater.y);
        wonLevel = false; winTimer = 0; msg = "Nivel " + (idx+1);
    }

    Player localPlayer() {
        if (net.modo == NetManager.Modo.CLIENT) return agua;
        return fuego;
    }
    Player remotePlayer() {
        if (net.modo == NetManager.Modo.CLIENT) return fuego;
        return agua;
    }

    @Override public void render(float delta) {
        time += delta;
        for (Level.MovingPlat m : level.movers) m.update(delta, time);
        List<Rectangle> solids = level.solidosEfectivos();

        boolean wNow = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean upNow = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean wFlank = wNow && !wWas, upFlank = upNow && !upWas;
        wWas = wNow; upWas = upNow;

        if (net.modo == NetManager.Modo.LOCAL) {
            fuego.update(delta, Gdx.input.isKeyPressed(Input.Keys.A), Gdx.input.isKeyPressed(Input.Keys.D), wFlank, solids);
            agua.update(delta, Gdx.input.isKeyPressed(Input.Keys.LEFT), Gdx.input.isKeyPressed(Input.Keys.RIGHT), upFlank, solids);
        } else {
            boolean L = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean R = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            boolean J = wFlank || upFlank || Gdx.input.isKeyPressed(Input.Keys.SPACE);
            Player yo = localPlayer();
            yo.update(delta, L, R, J, solids);
            Player otro = remotePlayer();
            if (net.connected) {
                float k = Math.min(1, delta * 12);
                otro.x += (net.rx - otro.x) * k;
                otro.y += (net.ry - otro.y) * k;
                otro.vx = net.rvx; otro.vy = net.rvy;
                otro.animTime = net.ranim;
                otro.facingRight = net.rdir == 1;
                otro.alive = net.rvivo;
                level.applyGemsMask(net.rgems);
                if (net.rlevel > nivelIdx && net.rlevel < MAX_NIVEL) cargarNivel(net.rlevel);
            }
        }

        for (Player p : new Player[]{fuego, agua}) {
            if (!p.alive) continue;
            for (Level.MovingPlat m : level.movers) {
                if (p.rect().overlaps(new Rectangle(m.r.x, m.r.y, m.r.width, m.r.height + 8)) && p.vy <= 0.1f) {
                    float dx = (float)Math.cos(time * m.speed) * m.speed * (m.x1 - m.x0) / 2f * delta;
                    p.x += dx;
                }
            }
        }

        boolean press = false;
        if (level.button != null) {
            if (fuego.alive && fuego.rect().overlaps(level.button)) press = true;
            if (agua.alive && agua.rect().overlaps(level.button)) press = true;
        }
        level.buttonPressed = press;
        level.doorOpen = press;

        for (Player p : new Player[]{fuego, agua}) {
            if (!p.alive) continue;
            for (Level.Hazard h : level.hazards) {
                if (p.rect().overlaps(h.r)) {
                    if ((h.tipo == Player.Tipo.FUEGO && p.tipo == Player.Tipo.AGUA) ||
                        (h.tipo == Player.Tipo.AGUA && p.tipo == Player.Tipo.FUEGO)) {
                        p.kill(); msg = (p.tipo == Player.Tipo.FUEGO ? "Fuego" : "Agua") + " toco zona contraria!";
                    }
                }
            }
        }

        for (Player p : new Player[]{fuego, agua}) {
            if (!p.alive && p.respawnTimer <= 0) {
                if (p.tipo == Player.Tipo.FUEGO) {
                    vidasFuego--;
                    if (vidasFuego <= 0) { gameOver = true; msg = "Sin vidas (Fuego). R = reintentar"; }
                    else p.respawn();
                } else {
                    vidasAgua--;
                    if (vidasAgua <= 0) { gameOver = true; msg = "Sin vidas (Agua). R = reintentar"; }
                    else p.respawn();
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && (gameOver || victoryTotal)) {
            vidasFuego = 3; vidasAgua = 3; puntos = 0; gameOver = false; victoryTotal = false;
            cargarNivel(0);
        }

        for (Level.Gem g : level.gems) {
            if (g.taken) continue;
            if (fuego.alive && fuego.rect().overlaps(g.r)) { g.taken = true; puntos += 100; }
            else if (agua.alive && agua.rect().overlaps(g.r)) { g.taken = true; puntos += 100; }
        }

        boolean fIn = fuego.alive && fuego.rect().overlaps(level.exitFire);
        boolean aIn = agua.alive && agua.rect().overlaps(level.exitWater);
        if (fIn && aIn && !gameOver) {
            winTimer += delta;
            msg = "Saliendo... " + (int)Math.ceil(1.5f - winTimer);
            if (winTimer > 1.5f && !wonLevel) {
                wonLevel = true;
                puntos += 500;
                if (nivelIdx + 1 >= MAX_NIVEL) { victoryTotal = true; msg = "VICTORIA TOTAL! R = reiniciar"; }
                else { cargarNivel(nivelIdx + 1); msg = "Nivel " + (nivelIdx+1); }
            }
        } else winTimer = 0;

        sendTimer += delta;
        if (sendTimer > 0.05f && net.modo != NetManager.Modo.LOCAL) {
            sendTimer = 0;
            Player yo = localPlayer();
            net.send(yo.x, yo.y, yo.vx, yo.vy, yo.facingRight?1:0, yo.animTime, yo.alive, level.gemsMask(), nivelIdx, wonLevel);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { net.close(); game.setScreen(new MenuScreen(game)); return; }

        ScreenUtils.clear(0.07f, 0.08f, 0.12f, 1);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        Color plat = new Color(0.35f, 0.37f, 0.42f, 1);
        for (Rectangle s : level.solids) Draw.rect(batch, s.x, s.y, s.width, s.height, plat);
        for (Level.MovingPlat m : level.movers) Draw.rect(batch, m.r.x, m.r.y, m.r.width, m.r.height, new Color(0.5f, 0.45f, 0.7f, 1));
        if (level.door != null) {
            Draw.rect(batch, level.door.x, level.door.y, level.door.width, level.door.height,
                level.doorOpen ? new Color(0.2f, 0.7f, 0.3f, 0.6f) : new Color(0.5f, 0.3f, 0.15f, 1));
        }
        if (level.button != null) Draw.rect(batch, level.button.x, level.button.y, level.button.width, level.button.height,
            level.buttonPressed ? Color.GREEN : Color.YELLOW);
        for (Level.Hazard h : level.hazards) {
            Color c = h.tipo == Player.Tipo.FUEGO ? new Color(1f, 0.45f, 0.1f, 1) : new Color(0.2f, 0.7f, 1f, 1);
            Draw.rect(batch, h.r.x, h.r.y, h.r.width, h.r.height, c);
            float wave = (float)Math.sin(time*6 + h.r.x)*3;
            Draw.rect(batch, h.r.x, h.r.y + h.r.height - 6 + wave*0.3f, h.r.width, 4, new Color(1,1,1,0.6f));
        }
        Draw.rect(batch, level.exitFire.x, level.exitFire.y, level.exitFire.width, level.exitFire.height, new Color(1f, 0.35f, 0.1f, 1));
        Draw.rect(batch, level.exitWater.x, level.exitWater.y, level.exitWater.width, level.exitWater.height, new Color(0.15f, 0.5f, 1f, 1));
        Draw.rect(batch, level.exitFire.x+8, level.exitFire.y+50, 34, 6, Color.WHITE);
        Draw.rect(batch, level.exitWater.x+8, level.exitWater.y+50, 34, 6, Color.WHITE);
        for (Level.Gem g : level.gems) {
            if (g.taken) continue;
            float bob = (float)Math.sin(time*3 + g.phase)*4;
            float cx = g.r.x + 11, cy = g.r.y + 11 + bob;
            Draw.rect(batch, cx - 7, cy - 2, 14, 4, new Color(0.3f, 1f, 0.5f, 1));
            Draw.rect(batch, cx - 2, cy - 9, 4, 18, new Color(0.3f, 1f, 0.5f, 1));
            Draw.circ(batch, cx - 5, cy - 5, 10, new Color(0.7f, 1f, 0.8f, 1));
        }
        dibujarJugador(fuego, COL_FUEGO);
        dibujarJugador(agua, COL_AGUA);

        font.setColor(Color.WHITE);
        font.draw(batch, "Nivel " + (nivelIdx+1) + "/" + MAX_NIVEL + "  Puntos:" + puntos +
            "  Vidas F:" + vidasFuego + " A:" + vidasAgua, 20, 520);
        String red = net.modo == NetManager.Modo.LOCAL ? "LOCAL 2P (A/D+W y Flechas)"
            : (net.modo == NetManager.Modo.HOST ? "HOST " + net.status : "CLIENT " + net.status);
        font.draw(batch, red, 20, 495);
        if (!msg.isEmpty()) font.draw(batch, msg, 380, 270);
        if (gameOver) { font.setColor(Color.RED); font.draw(batch, "DERROTA - pulsa R", 400, 300); font.setColor(Color.WHITE); }
        if (victoryTotal) { font.setColor(Color.GREEN); font.draw(batch, "GANASTE TODO! " + puntos + " R=reintentar", 340, 300); font.setColor(Color.WHITE); }
        font.draw(batch, "ESC=menu", 20, 25);
        batch.end();
    }

    void dibujarJugador(Player p, Color c) {
        if (!p.alive) return;
        Draw.rect(batch, p.x, p.y - 4, p.w, 5, new Color(0,0,0,0.3f));
        Draw.rect(batch, p.x - p.vx*0.03f, p.y - p.vy*0.03f, p.w, p.h, new Color(c.r, c.g, c.b, 0.25f));
        float sx = 1 + p.squash*0.25f, sy = 1 - p.squash*0.25f;
        float wob = (float)Math.sin(p.animTime*10)* (p.onGround && Math.abs(p.vx)>10 ? 0.05f : 0f);
        float w = p.w * (sx + wob), h = p.h * (sy - wob);
        float dx = p.x - (w - p.w)/2, dy = p.y;
        Draw.rect(batch, dx, dy, w, h, c);
        float bob = (float)Math.abs(Math.sin(p.animTime*8))*6;
        if (p.tipo == Player.Tipo.FUEGO) {
            Draw.rect(batch, dx+4, dy+h, w-8, 6+bob, new Color(1f, 0.8f, 0.2f, 1));
            Draw.circ(batch, dx+w/2-6, dy+h+6+bob, 12, new Color(1f, 0.8f, 0.2f, 1));
        } else {
            Draw.circ(batch, dx+w/2-7, dy+h+bob*0.3f, 14, new Color(0.7f, 0.95f, 1f, 1));
        }
        float ex = p.facingRight ? dx+w-19 : dx+5;
        Draw.circ(batch, ex, dy+h-17, 10, Color.WHITE);
        Draw.circ(batch, ex+9, dy+h-17, 10, Color.WHITE);
        float px = p.facingRight ? 2 : -2;
        Draw.circ(batch, ex+2+px, dy+h-14, 5, Color.BLACK);
        Draw.circ(batch, ex+11+px, dy+h-14, 5, Color.BLACK);
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
