package com.dachser.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.dachser.game.Dachser;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Dachser";
		config.width = 1000;
		config.height = 600;
		new LwjglApplication(new Dachser(), config);
	}
}
