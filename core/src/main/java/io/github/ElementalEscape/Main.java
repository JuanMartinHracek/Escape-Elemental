package io.github.ElementalEscape;

import com.badlogic.gdx.Game;

/** Punto de entrada LibGDX. Todo el juego va sin assets externos. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
