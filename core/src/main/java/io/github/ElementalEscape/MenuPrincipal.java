package io.github.ElementalEscape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** Menu: 1=Servidor, 2=Cliente, 3=Local. Solo SpriteBatch. */
public class MenuPrincipal implements Screen {
    final Juego juego;
    OrthographicCamera camara;
    SpriteBatch lote;
    BitmapFont fuente;
    GestorRed redMuestra = new GestorRed();
    String ipLocal = "?";

    public MenuPrincipal(Juego juego) { this.juego = juego; }

    @Override public void show() {
        camara = new OrthographicCamera(960, 540);
        camara.position.set(480, 270, 0);
        lote = new SpriteBatch(); fuente = new BitmapFont();
        fuente.getData().setScale(1.5f);
        Dibujo.iniciar();
        ipLocal = redMuestra.miIp();
    }

    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            GestorRed red = new GestorRed();
            red.iniciarServidor();
            juego.setScreen(new PantallaJuego(juego, red));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override public void input(String texto) {
                    GestorRed red = new GestorRed();
                    red.iniciarCliente(texto.isEmpty() ? "127.0.0.1" : texto);
                    juego.setScreen(new PantallaJuego(juego, red));
                }
                @Override public void canceled() {}
            }, "IP del SERVIDOR (ej 192.168.1.5)", "", "192.168.1.5");
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            juego.setScreen(new PantallaJuego(juego, new GestorRed()));
            return;
        }

        ScreenUtils.clear(0.08f, 0.1f, 0.16f, 1);
        camara.update();
        lote.setProjectionMatrix(camara.combined);
        lote.begin();
        Dibujo.circulo(lote, 300, 330, 80, new Color(1f, 0.35f, 0.15f, 1));
        Dibujo.circulo(lote, 600, 330, 80, new Color(0.2f, 0.6f, 1f, 1));
        fuente.setColor(Color.WHITE);
        fuente.draw(lote, "ELEMENTAL ESCAPE - cooperativo 2 jugadores", 220, 480);
        fuente.draw(lote, "Fuego (rojo) + Agua (azul). Estilo Fireboy & Watergirl.", 190, 450);
        fuente.getData().setScale(1.2f);
        fuente.draw(lote, "Tu IP local: " + ipLocal + "  Puerto: " + GestorRed.PUERTO, 220, 300);
        fuente.draw(lote, "[1/H] SER SERVIDOR (Fuego) - crea partida, pasa tu IP al otro", 170, 260);
        fuente.draw(lote, "[2/C] UNIRSE (Agua) - te pide IP del servidor", 170, 230);
        fuente.draw(lote, "[3/L] JUGAR LOCAL 2 en mismo teclado (sin red)", 170, 200);
        fuente.draw(lote, "Controles LOCAL: Fuego A/D + W | Agua Flechas", 170, 160);
        fuente.draw(lote, "Controles RED: tu personaje con A/D o Flechas + W/ESPACIO", 170, 135);
        fuente.draw(lote, "Reglas: lava mata Agua, agua mata Fuego. Boton = abre puerta.", 170, 110);
        fuente.draw(lote, "Ambos a sus salidas para ganar. Gemas = 100pts. R = reiniciar.", 170, 85);
        fuente.draw(lote, "Deshabilita Parsec virtual display y actualiza driver AMD.", 170, 55);
        lote.end();
    }

    @Override public void resize(int ancho, int alto) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() { lote.dispose(); fuente.dispose(); }
}
