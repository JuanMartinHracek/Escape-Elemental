package io.github.ElementalEscape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** Menu: 1=Host, 2=Cliente, 3=Local. Solo SpriteBatch. */
public class MenuScreen implements Screen {
    final Main game;
    OrthographicCamera cam;
    SpriteBatch batch;
    BitmapFont font;
    NetManager netPreview = new NetManager();
    String ipLocal = "?";

    public MenuScreen(Main game) { this.game = game; }

    @Override public void show() {
        cam = new OrthographicCamera(960, 540);
        cam.position.set(480, 270, 0);
        batch = new SpriteBatch(); font = new BitmapFont();
        font.getData().setScale(1.5f);
        Draw.init();
        ipLocal = netPreview.miIp();
    }

    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            NetManager net = new NetManager();
            net.startHost();
            game.setScreen(new GameScreen(game, net));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override public void input(String text) {
                    NetManager net = new NetManager();
                    net.startClient(text.isEmpty() ? "127.0.0.1" : text);
                    game.setScreen(new GameScreen(game, net));
                }
                @Override public void canceled() {}
            }, "IP del HOST (ej 192.168.1.5)", "", "192.168.1.5");
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.setScreen(new GameScreen(game, new NetManager()));
            return;
        }

        ScreenUtils.clear(0.08f, 0.1f, 0.16f, 1);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        Draw.circ(batch, 300, 330, 80, new Color(1f, 0.35f, 0.15f, 1));
        Draw.circ(batch, 600, 330, 80, new Color(0.2f, 0.6f, 1f, 1));
        font.setColor(Color.WHITE);
        font.draw(batch, "ELEMENTAL ESCAPE - cooperativo 2 jugadores", 220, 480);
        font.draw(batch, "Fuego (rojo) + Agua (azul). Estilo Fireboy & Watergirl.", 190, 450);
        font.getData().setScale(1.2f);
        font.draw(batch, "Tu IP local: " + ipLocal + "  Puerto: " + NetManager.PUERTO, 220, 300);
        font.draw(batch, "[1/H] SER HOST (Fuego) - crea partida, pasa tu IP al otro", 170, 260);
        font.draw(batch, "[2/C] UNIRSE (Agua) - te pide IP del host", 170, 230);
        font.draw(batch, "[3/L] JUGAR LOCAL 2 en mismo teclado (sin red)", 170, 200);
        font.draw(batch, "Controles LOCAL: Fuego A/D + W | Agua Flechas", 170, 160);
        font.draw(batch, "Controles RED: tu personaje con A/D o Flechas + W/ESPACIO", 170, 135);
        font.draw(batch, "Reglas: lava mata Agua, agua mata Fuego. Boton = abre puerta.", 170, 110);
        font.draw(batch, "Ambos a sus salidas para ganar. Gemas = 100pts. R = reiniciar.", 170, 85);
        font.draw(batch, "Deshabilita Parsec virtual display y actualiza driver AMD.", 170, 55);
        batch.end();
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
