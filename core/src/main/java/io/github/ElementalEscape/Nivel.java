package io.github.ElementalEscape;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

/** Define plataformas, peligros, boton, puerta, salidas, gemas y origenes. */
public class Nivel {
    public static final float MUNDO_ANCHO = 960f;
    public static final float MUNDO_ALTO = 540f;

    public static class Peligro {
        public Rectangle rect;
        public Jugador.Tipo tipo; // FUEGO = lava (mata AGUA), AGUA = charco (mata FUEGO)
        public Peligro(float x, float y, float ancho, float alto, Jugador.Tipo tipo) {
            rect = new Rectangle(x, y, ancho, alto); this.tipo = tipo;
        }
    }
    public static class Gema {
        public Rectangle rect;
        public boolean tomada = false;
        public float fase;
        public Gema(float x, float y) { rect = new Rectangle(x, y, 22, 22); fase = (float)(Math.random()*6); }
    }
    public static class PlataformaMovil {
        public Rectangle rect;
        public float x0, x1, velocidad, t;
        public PlataformaMovil(float x, float y, float ancho, float alto, float x1, float velocidad) {
            rect = new Rectangle(x, y, ancho, alto); x0 = x; this.x1 = x1; this.velocidad = velocidad;
        }
        public void actualizar(float dt, float tiempo) {
            float k = (float)((Math.sin(tiempo * velocidad) + 1) / 2.0);
            rect.x = x0 + (x1 - x0) * k;
        }
    }

    public List<Rectangle> solidos = new ArrayList<>();
    public List<Peligro> peligros = new ArrayList<>();
    public List<Gema> gemas = new ArrayList<>();
    public List<PlataformaMovil> moviles = new ArrayList<>();
    public Rectangle boton;
    public Rectangle puerta;
    public boolean puertaAbierta = false;
    public boolean botonPresionado = false;
    public Rectangle salidaFuego, salidaAgua;
    public Vector2 origenFuego = new Vector2(60, 120);
    public Vector2 origenAgua = new Vector2(110, 120);

    public static Nivel crearNivel(int indice) {
        Nivel n = new Nivel();
        n.solidos.add(new Rectangle(0, 0, MUNDO_ANCHO, 30));
        n.solidos.add(new Rectangle(0, 0, 20, MUNDO_ALTO));
        n.solidos.add(new Rectangle(MUNDO_ANCHO - 20, 0, 20, MUNDO_ALTO));

        if (indice == 0) {
            // NIVEL 1: movimiento y salto + puerta/boton simple
            n.origenFuego.set(60, 60); n.origenAgua.set(120, 60);
            n.solidos.add(new Rectangle(150, 90, 180, 22));
            n.solidos.add(new Rectangle(420, 170, 180, 22));
            n.solidos.add(new Rectangle(650, 90, 150, 22));
            n.puerta = new Rectangle(470, 30, 24, 160);
            n.boton = new Rectangle(220, 112, 46, 14);
            n.salidaFuego = new Rectangle(830, 30, 50, 70);
            n.salidaAgua = new Rectangle(890, 30, 50, 70);
            n.gemas.add(new Gema(500, 230));
            n.gemas.add(new Gema(700, 140));
        } else if (indice == 1) {
            // NIVEL 2: lava y agua
            n.origenFuego.set(60, 60); n.origenAgua.set(120, 60);
            n.peligros.add(new Peligro(320, 30, 120, 22, Jugador.Tipo.FUEGO));
            n.peligros.add(new Peligro(520, 30, 120, 22, Jugador.Tipo.AGUA));
            n.solidos.add(new Rectangle(150, 90, 160, 22));
            n.solidos.add(new Rectangle(400, 170, 160, 22));
            n.solidos.add(new Rectangle(650, 90, 160, 22));
            n.puerta = new Rectangle(470, 30, 24, 160);
            n.boton = new Rectangle(200, 112, 46, 14);
            n.salidaFuego = new Rectangle(830, 30, 50, 70);
            n.salidaAgua = new Rectangle(890, 30, 50, 70);
            n.gemas.add(new Gema(460, 210));
            n.gemas.add(new Gema(700, 140));
            n.gemas.add(new Gema(380, 60));
        } else {
            // NIVEL 3: plataforma movil + cooperacion
            n.origenFuego.set(60, 60); n.origenAgua.set(120, 60);
            n.solidos.add(new Rectangle(100, 90, 150, 22));
            n.solidos.add(new Rectangle(700, 90, 150, 22));
            n.solidos.add(new Rectangle(380, 190, 200, 22));
            n.moviles.add(new PlataformaMovil(300, 140, 110, 20, 550, 1.2f));
            n.peligros.add(new Peligro(260, 30, 440, 22, Jugador.Tipo.FUEGO));
            n.solidos.add(new Rectangle(440, 30, 80, 24));
            n.puerta = new Rectangle(600, 30, 24, 160);
            n.boton = new Rectangle(130, 112, 46, 14);
            n.salidaFuego = new Rectangle(830, 30, 50, 70);
            n.salidaAgua = new Rectangle(890, 30, 50, 70);
            n.gemas.add(new Gema(460, 230));
            n.gemas.add(new Gema(750, 140));
            n.gemas.add(new Gema(480, 60));
        }
        return n;
    }

    /** Solidos efectivos segun puerta abierta/cerrada + moviles. */
    public List<Rectangle> solidosEfectivos() {
        List<Rectangle> salida = new ArrayList<>(solidos);
        if (!puertaAbierta && puerta != null) salida.add(new Rectangle(puerta));
        for (PlataformaMovil m : moviles) salida.add(new Rectangle(m.rect));
        return salida;
    }

    public int mascaraGemas() {
        int m = 0;
        for (int i = 0; i < gemas.size(); i++) if (gemas.get(i).tomada) m |= (1 << i);
        return m;
    }
    public void aplicarMascaraGemas(int mascara) {
        for (int i = 0; i < gemas.size(); i++) if ((mascara & (1 << i)) != 0) gemas.get(i).tomada = true;
    }
}
