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
public class PantallaJuego implements Screen {
    final Juego juego;
    final GestorRed red;
    OrthographicCamera camara;
    SpriteBatch lote;
    BitmapFont fuente;

    Jugador fuego, agua;
    Nivel nivel;
    int indiceNivel = 0;
    public static final int MAX_NIVEL = 3;
    int puntos = 0;
    int vidasFuego = 3, vidasAgua = 3;
    float tiempo = 0, tiempoSalida = 0, tiempoEnvio = 0;
    boolean nivelGanado = false, derrota = false, victoriaTotal = false;
    String mensaje = "";
    boolean wPrevio = false, arribaPrevio = false;

    final Color COLOR_FUEGO = new Color(1f, 0.3f, 0.15f, 1);
    final Color COLOR_AGUA = new Color(0.2f, 0.6f, 1f, 1);

    public PantallaJuego(Juego juego, GestorRed red) {
        this.juego = juego; this.red = red;
    }

    @Override public void show() {
        camara = new OrthographicCamera(Nivel.MUNDO_ANCHO, Nivel.MUNDO_ALTO);
        camara.position.set(Nivel.MUNDO_ANCHO/2, Nivel.MUNDO_ALTO/2, 0);
        lote = new SpriteBatch(); fuente = new BitmapFont();
        fuente.getData().setScale(1.4f);
        Dibujo.iniciar();
        cargarNivel(0);
    }

    void cargarNivel(int indice) {
        indiceNivel = indice;
        nivel = Nivel.crearNivel(indice);
        fuego = new Jugador(Jugador.Tipo.FUEGO, nivel.origenFuego.x, nivel.origenFuego.y);
        agua = new Jugador(Jugador.Tipo.AGUA, nivel.origenAgua.x, nivel.origenAgua.y);
        nivelGanado = false; tiempoSalida = 0; mensaje = "Nivel " + (indice+1);
    }

    Jugador jugadorLocal() {
        if (red.modo == GestorRed.Modo.CLIENTE) return agua;
        return fuego;
    }
    Jugador jugadorRemoto() {
        if (red.modo == GestorRed.Modo.CLIENTE) return fuego;
        return agua;
    }

    @Override public void render(float delta) {
        tiempo += delta;
        for (Nivel.PlataformaMovil m : nivel.moviles) m.actualizar(delta, tiempo);
        List<Rectangle> solidos = nivel.solidosEfectivos();

        boolean wAhora = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean arribaAhora = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean saltoFuego = wAhora && !wPrevio, saltoAgua = arribaAhora && !arribaPrevio;
        wPrevio = wAhora; arribaPrevio = arribaAhora;

        if (red.modo == GestorRed.Modo.LOCAL) {
            fuego.actualizar(delta, Gdx.input.isKeyPressed(Input.Keys.A), Gdx.input.isKeyPressed(Input.Keys.D), saltoFuego, solidos);
            agua.actualizar(delta, Gdx.input.isKeyPressed(Input.Keys.LEFT), Gdx.input.isKeyPressed(Input.Keys.RIGHT), saltoAgua, solidos);
        } else {
            boolean izq = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean der = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            boolean saltar = saltoFuego || saltoAgua || Gdx.input.isKeyPressed(Input.Keys.SPACE);
            Jugador yo = jugadorLocal();
            yo.actualizar(delta, izq, der, saltar, solidos);
            Jugador otro = jugadorRemoto();
            if (red.conectado) {
                float k = Math.min(1, delta * 12);
                otro.x += (red.otroX - otro.x) * k;
                otro.y += (red.otroY - otro.y) * k;
                otro.velX = red.otroVelX; otro.velY = red.otroVelY;
                otro.tiempoAnim = red.otroAnim;
                otro.mirandoDerecha = red.otroDir == 1;
                otro.vivo = red.otroVivo;
                nivel.aplicarMascaraGemas(red.otroGemas);
                if (red.otroNivel > indiceNivel && red.otroNivel < MAX_NIVEL) cargarNivel(red.otroNivel);
            }
        }

        for (Jugador p : new Jugador[]{fuego, agua}) {
            if (!p.vivo) continue;
            for (Nivel.PlataformaMovil m : nivel.moviles) {
                if (p.cuerpo().overlaps(new Rectangle(m.rect.x, m.rect.y, m.rect.width, m.rect.height + 8)) && p.velY <= 0.1f) {
                    float dx = (float)Math.cos(tiempo * m.velocidad) * m.velocidad * (m.x1 - m.x0) / 2f * delta;
                    p.x += dx;
                }
            }
        }

        boolean presionado = false;
        if (nivel.boton != null) {
            if (fuego.vivo && fuego.cuerpo().overlaps(nivel.boton)) presionado = true;
            if (agua.vivo && agua.cuerpo().overlaps(nivel.boton)) presionado = true;
        }
        nivel.botonPresionado = presionado;
        nivel.puertaAbierta = presionado;

        for (Jugador p : new Jugador[]{fuego, agua}) {
            if (!p.vivo) continue;
            for (Nivel.Peligro h : nivel.peligros) {
                if (p.cuerpo().overlaps(h.rect)) {
                    if ((h.tipo == Jugador.Tipo.FUEGO && p.tipo == Jugador.Tipo.AGUA) ||
                        (h.tipo == Jugador.Tipo.AGUA && p.tipo == Jugador.Tipo.FUEGO)) {
                        p.morir(); mensaje = (p.tipo == Jugador.Tipo.FUEGO ? "Fuego" : "Agua") + " toco zona contraria!";
                    }
                }
            }
        }

        for (Jugador p : new Jugador[]{fuego, agua}) {
            if (!p.vivo && p.tiempoReaparicion <= 0) {
                if (p.tipo == Jugador.Tipo.FUEGO) {
                    vidasFuego--;
                    if (vidasFuego <= 0) { derrota = true; mensaje = "Sin vidas (Fuego). R = reintentar"; }
                    else p.reaparecer();
                } else {
                    vidasAgua--;
                    if (vidasAgua <= 0) { derrota = true; mensaje = "Sin vidas (Agua). R = reintentar"; }
                    else p.reaparecer();
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && (derrota || victoriaTotal)) {
            vidasFuego = 3; vidasAgua = 3; puntos = 0; derrota = false; victoriaTotal = false;
            cargarNivel(0);
        }

        for (Nivel.Gema g : nivel.gemas) {
            if (g.tomada) continue;
            if (fuego.vivo && fuego.cuerpo().overlaps(g.rect)) { g.tomada = true; puntos += 100; }
            else if (agua.vivo && agua.cuerpo().overlaps(g.rect)) { g.tomada = true; puntos += 100; }
        }

        boolean fuegoDentro = fuego.vivo && fuego.cuerpo().overlaps(nivel.salidaFuego);
        boolean aguaDentro = agua.vivo && agua.cuerpo().overlaps(nivel.salidaAgua);
        if (fuegoDentro && aguaDentro && !derrota) {
            tiempoSalida += delta;
            mensaje = "Saliendo... " + (int)Math.ceil(1.5f - tiempoSalida);
            if (tiempoSalida > 1.5f && !nivelGanado) {
                nivelGanado = true;
                puntos += 500;
                if (indiceNivel + 1 >= MAX_NIVEL) { victoriaTotal = true; mensaje = "VICTORIA TOTAL! R = reiniciar"; }
                else { cargarNivel(indiceNivel + 1); mensaje = "Nivel " + (indiceNivel+1); }
            }
        } else tiempoSalida = 0;

        tiempoEnvio += delta;
        if (tiempoEnvio > 0.05f && red.modo != GestorRed.Modo.LOCAL) {
            tiempoEnvio = 0;
            Jugador yo = jugadorLocal();
            red.enviar(yo.x, yo.y, yo.velX, yo.velY, yo.mirandoDerecha?1:0, yo.tiempoAnim, yo.vivo, nivel.mascaraGemas(), indiceNivel, nivelGanado);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { red.cerrar(); juego.setScreen(new MenuPrincipal(juego)); return; }

        ScreenUtils.clear(0.07f, 0.08f, 0.12f, 1);
        camara.update();
        lote.setProjectionMatrix(camara.combined);
        lote.begin();
        Color plat = new Color(0.35f, 0.37f, 0.42f, 1);
        for (Rectangle s : nivel.solidos) Dibujo.rectangulo(lote, s.x, s.y, s.width, s.height, plat);
        for (Nivel.PlataformaMovil m : nivel.moviles) Dibujo.rectangulo(lote, m.rect.x, m.rect.y, m.rect.width, m.rect.height, new Color(0.5f, 0.45f, 0.7f, 1));
        if (nivel.puerta != null) {
            Dibujo.rectangulo(lote, nivel.puerta.x, nivel.puerta.y, nivel.puerta.width, nivel.puerta.height,
                nivel.puertaAbierta ? new Color(0.2f, 0.7f, 0.3f, 0.6f) : new Color(0.5f, 0.3f, 0.15f, 1));
        }
        if (nivel.boton != null) Dibujo.rectangulo(lote, nivel.boton.x, nivel.boton.y, nivel.boton.width, nivel.boton.height,
            nivel.botonPresionado ? Color.GREEN : Color.YELLOW);
        for (Nivel.Peligro h : nivel.peligros) {
            Color c = h.tipo == Jugador.Tipo.FUEGO ? new Color(1f, 0.45f, 0.1f, 1) : new Color(0.2f, 0.7f, 1f, 1);
            Dibujo.rectangulo(lote, h.rect.x, h.rect.y, h.rect.width, h.rect.height, c);
            float onda = (float)Math.sin(tiempo*6 + h.rect.x)*3;
            Dibujo.rectangulo(lote, h.rect.x, h.rect.y + h.rect.height - 6 + onda*0.3f, h.rect.width, 4, new Color(1,1,1,0.6f));
        }
        Dibujo.rectangulo(lote, nivel.salidaFuego.x, nivel.salidaFuego.y, nivel.salidaFuego.width, nivel.salidaFuego.height, new Color(1f, 0.35f, 0.1f, 1));
        Dibujo.rectangulo(lote, nivel.salidaAgua.x, nivel.salidaAgua.y, nivel.salidaAgua.width, nivel.salidaAgua.height, new Color(0.15f, 0.5f, 1f, 1));
        Dibujo.rectangulo(lote, nivel.salidaFuego.x+8, nivel.salidaFuego.y+50, 34, 6, Color.WHITE);
        Dibujo.rectangulo(lote, nivel.salidaAgua.x+8, nivel.salidaAgua.y+50, 34, 6, Color.WHITE);
        for (Nivel.Gema g : nivel.gemas) {
            if (g.tomada) continue;
            float flotar = (float)Math.sin(tiempo*3 + g.fase)*4;
            float cx = g.rect.x + 11, cy = g.rect.y + 11 + flotar;
            Dibujo.rectangulo(lote, cx - 7, cy - 2, 14, 4, new Color(0.3f, 1f, 0.5f, 1));
            Dibujo.rectangulo(lote, cx - 2, cy - 9, 4, 18, new Color(0.3f, 1f, 0.5f, 1));
            Dibujo.circulo(lote, cx - 5, cy - 5, 10, new Color(0.7f, 1f, 0.8f, 1));
        }
        dibujarJugador(fuego, COLOR_FUEGO);
        dibujarJugador(agua, COLOR_AGUA);

        fuente.setColor(Color.WHITE);
        fuente.draw(lote, "Nivel " + (indiceNivel+1) + "/" + MAX_NIVEL + "  Puntos:" + puntos +
            "  Vidas F:" + vidasFuego + " A:" + vidasAgua, 20, 520);
        String textoRed = red.modo == GestorRed.Modo.LOCAL ? "LOCAL 2P (A/D+W y Flechas)"
            : (red.modo == GestorRed.Modo.SERVIDOR ? "SERVIDOR " + red.estado : "CLIENTE " + red.estado);
        fuente.draw(lote, textoRed, 20, 495);
        if (!mensaje.isEmpty()) fuente.draw(lote, mensaje, 380, 270);
        if (derrota) { fuente.setColor(Color.RED); fuente.draw(lote, "DERROTA - pulsa R", 400, 300); fuente.setColor(Color.WHITE); }
        if (victoriaTotal) { fuente.setColor(Color.GREEN); fuente.draw(lote, "GANASTE TODO! " + puntos + " R=reintentar", 340, 300); fuente.setColor(Color.WHITE); }
        fuente.draw(lote, "ESC=menu", 20, 25);
        lote.end();
    }

    void dibujarJugador(Jugador p, Color c) {
        if (!p.vivo) return;
        Dibujo.rectangulo(lote, p.x, p.y - 4, p.ancho, 5, new Color(0,0,0,0.3f));
        Dibujo.rectangulo(lote, p.x - p.velX*0.03f, p.y - p.velY*0.03f, p.ancho, p.alto, new Color(c.r, c.g, c.b, 0.25f));
        float sx = 1 + p.aplastado*0.25f, sy = 1 - p.aplastado*0.25f;
        float bamboleo = (float)Math.sin(p.tiempoAnim*10)* (p.enSuelo && Math.abs(p.velX)>10 ? 0.05f : 0f);
        float ancho = p.ancho * (sx + bamboleo), alto = p.alto * (sy - bamboleo);
        float dx = p.x - (ancho - p.ancho)/2, dy = p.y;
        Dibujo.rectangulo(lote, dx, dy, ancho, alto, c);
        float salto = (float)Math.abs(Math.sin(p.tiempoAnim*8))*6;
        if (p.tipo == Jugador.Tipo.FUEGO) {
            Dibujo.rectangulo(lote, dx+4, dy+alto, ancho-8, 6+salto, new Color(1f, 0.8f, 0.2f, 1));
            Dibujo.circulo(lote, dx+ancho/2-6, dy+alto+6+salto, 12, new Color(1f, 0.8f, 0.2f, 1));
        } else {
            Dibujo.circulo(lote, dx+ancho/2-7, dy+alto+salto*0.3f, 14, new Color(0.7f, 0.95f, 1f, 1));
        }
        float ex = p.mirandoDerecha ? dx+ancho-19 : dx+5;
        Dibujo.circulo(lote, ex, dy+alto-17, 10, Color.WHITE);
        Dibujo.circulo(lote, ex+9, dy+alto-17, 10, Color.WHITE);
        float px = p.mirandoDerecha ? 2 : -2;
        Dibujo.circulo(lote, ex+2+px, dy+alto-14, 5, Color.BLACK);
        Dibujo.circulo(lote, ex+11+px, dy+alto-14, 5, Color.BLACK);
    }

    @Override public void resize(int ancho, int alto) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { lote.dispose(); fuente.dispose(); }
}
