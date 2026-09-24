package io.github.ElementalEscape;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

/** Define plataformas, hazards, boton, puerta, salidas, gemas y spawns. */
public class Level {
    public static final float WORLD_W = 960f;
    public static final float WORLD_H = 540f;

    public static class Hazard {
        public Rectangle r;
        public Player.Tipo tipo; // FUEGO = lava (mata AGUA), AGUA = charco (mata FUEGO)
        public Hazard(float x, float y, float w, float h, Player.Tipo t) {
            r = new Rectangle(x, y, w, h); tipo = t;
        }
    }
    public static class Gem {
        public Rectangle r;
        public boolean taken = false;
        public float phase;
        public Gem(float x, float y) { r = new Rectangle(x, y, 22, 22); phase = (float)(Math.random()*6); }
    }
    public static class MovingPlat {
        public Rectangle r;
        public float x0, x1, speed, t;
        public MovingPlat(float x, float y, float w, float h, float x1, float speed) {
            r = new Rectangle(x, y, w, h); x0 = x; this.x1 = x1; this.speed = speed;
        }
        public void update(float dt, float time) {
            float k = (float)((Math.sin(time * speed) + 1) / 2.0);
            r.x = x0 + (x1 - x0) * k;
        }
    }

    public List<Rectangle> solids = new ArrayList<>();
    public List<Hazard> hazards = new ArrayList<>();
    public List<Gem> gems = new ArrayList<>();
    public List<MovingPlat> movers = new ArrayList<>();
    public Rectangle button;
    public Rectangle door;
    public boolean doorOpen = false;
    public boolean buttonPressed = false;
    public Rectangle exitFire, exitWater;
    public Vector2 spawnFire = new Vector2(60, 120);
    public Vector2 spawnWater = new Vector2(110, 120);

    public static Level crearNivel(int idx) {
        Level l = new Level();
        l.solids.add(new Rectangle(0, 0, WORLD_W, 30));
        l.solids.add(new Rectangle(0, 0, 20, WORLD_H));
        l.solids.add(new Rectangle(WORLD_W - 20, 0, 20, WORLD_H));

        if (idx == 0) {
            l.spawnFire.set(60, 60); l.spawnWater.set(120, 60);
            l.solids.add(new Rectangle(150, 90, 180, 22));
            l.solids.add(new Rectangle(420, 170, 180, 22));
            l.solids.add(new Rectangle(650, 90, 150, 22));
            l.door = new Rectangle(470, 30, 24, 160);
            l.button = new Rectangle(220, 112, 46, 14);
            l.exitFire = new Rectangle(830, 30, 50, 70);
            l.exitWater = new Rectangle(890, 30, 50, 70);
            l.gems.add(new Gem(500, 230));
            l.gems.add(new Gem(700, 140));
        } else if (idx == 1) {
            l.spawnFire.set(60, 60); l.spawnWater.set(120, 60);
            l.hazards.add(new Hazard(320, 30, 120, 22, Player.Tipo.FUEGO));
            l.hazards.add(new Hazard(520, 30, 120, 22, Player.Tipo.AGUA));
            l.solids.add(new Rectangle(150, 90, 160, 22));
            l.solids.add(new Rectangle(400, 170, 160, 22));
            l.solids.add(new Rectangle(650, 90, 160, 22));
            l.door = new Rectangle(470, 30, 24, 160);
            l.button = new Rectangle(200, 112, 46, 14);
            l.exitFire = new Rectangle(830, 30, 50, 70);
            l.exitWater = new Rectangle(890, 30, 50, 70);
            l.gems.add(new Gem(460, 210));
            l.gems.add(new Gem(700, 140));
            l.gems.add(new Gem(380, 60));
        } else {
            l.spawnFire.set(60, 60); l.spawnWater.set(120, 60);
            l.solids.add(new Rectangle(100, 90, 150, 22));
            l.solids.add(new Rectangle(700, 90, 150, 22));
            l.solids.add(new Rectangle(380, 190, 200, 22));
            l.movers.add(new MovingPlat(300, 140, 110, 20, 550, 1.2f));
            l.hazards.add(new Hazard(260, 30, 440, 22, Player.Tipo.FUEGO));
            l.solids.add(new Rectangle(440, 30, 80, 24));
            l.door = new Rectangle(600, 30, 24, 160);
            l.button = new Rectangle(130, 112, 46, 14);
            l.exitFire = new Rectangle(830, 30, 50, 70);
            l.exitWater = new Rectangle(890, 30, 50, 70);
            l.gems.add(new Gem(460, 230));
            l.gems.add(new Gem(750, 140));
            l.gems.add(new Gem(480, 60));
        }
        return l;
    }

    public List<Rectangle> solidosEfectivos() {
        List<Rectangle> out = new ArrayList<>(solids);
        if (!doorOpen && door != null) out.add(new Rectangle(door));
        for (MovingPlat m : movers) out.add(new Rectangle(m.r));
        return out;
    }

    public int gemsMask() {
        int m = 0;
        for (int i = 0; i < gems.size(); i++) if (gems.get(i).taken) m |= (1 << i);
        return m;
    }
    public void applyGemsMask(int mask) {
        for (int i = 0; i < gems.size(); i++) if ((mask & (1 << i)) != 0) gems.get(i).taken = true;
    }
}
