package com.dachser.game;

import com.badlogic.gdx.Game;

public class Dachser extends Game {
	
	@Override
	public void create () {

		setScreen(new startScreen(this));
	}
}
