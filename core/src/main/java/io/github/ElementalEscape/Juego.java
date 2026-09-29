package io.github.ElementalEscape;

import com.badlogic.gdx.Game;

/** Punto de entrada LibGDX. Todo el juego va sin assets externos. */
public class Juego extends Game {
    @Override
    public void create() {
        setScreen(new MenuPrincipal(this));
    }
}
