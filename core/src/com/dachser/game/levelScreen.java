package com.dachser.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.awt.*;

/**
 * Created by taprosoft on 05/11/2015.
 */
public class levelScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Texture background;
    private OrthographicCamera camera;

    /* Define game UI Style */
    Skin skin;

    private Dachser game;
    private final int level_count = 6;

    /* constructor */
    public levelScreen(Dachser game) {

        this.game = game;
        initUI();
    }

    private void initUI() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(1000, 600);

        /* stage for creating menu and handling input */
        stage = new Stage(new ScreenViewport()) {
            @Override
            public boolean keyUp(int keyCode) {
                if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK) {
                    game.setScreen(new startScreen(game));
                    dispose();
                    System.gc();
                }

                return super.keyUp(keyCode);
            }
        };

        Gdx.input.setInputProcessor(stage);

        /* generate skin for menu */
        skin = new Skin();

        Texture buttons = new Texture("buttons.png");
        background = new Texture("background.jpg");

        TextureRegion[][] button_state = TextureRegion.split(buttons, buttons.getWidth(), buttons.getHeight() / 3);

        skin.add("button_up", button_state[0][0]);
        skin.add("button_highlight", button_state[1][0]);
        skin.add("button_down", button_state[2][0]);

        // Store the default libgdx font under the name "default".
        BitmapFont bfont = new BitmapFont();
        skin.add("default", bfont);

        // Configure a TextButtonStyle and name it "button". Skin resources are stored by type, so this doesn't overwrite the font.
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("button_up");
        textButtonStyle.down = skin.newDrawable("button_down");
        textButtonStyle.checked = skin.newDrawable("button_down");
        textButtonStyle.over = skin.newDrawable("button_highlight");

        textButtonStyle.font = skin.getFont("default");

        skin.add("button", textButtonStyle);

        /* init level menu */
        Table levelMenu = new Table();

        final TextButton[] textButtons = new TextButton[2 * level_count];
        for (int i = 0; i < 2 * level_count; i++) {

            final int index = i + 1;
            textButtons[i] = new TextButton("LEVEL " + String.format("%02d", index), skin, "button");

            final TextButton button = textButtons[i];

            textButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    button.setText("Loading level...");
                    game.setScreen(new playScreen(game, "map_Slot " + String.format("%02d", index)));
                    dispose();
                    System.gc();
                }
            });
        }

        for (int i = level_count ; i < 2 * level_count; i++) {
            textButtons[i] = new TextButton("Custom", skin, "button");
            final TextButton button = textButtons[i];
        }

        for (int i = 0; i < level_count; i++) {
            levelMenu.add(textButtons[i]).pad(4).padRight(80);
            levelMenu.add(textButtons[i + level_count]).pad(4).padLeft(80);
            levelMenu.row();
        }

        levelMenu.setPosition(stage.getWidth() / 2, stage.getHeight() / 2);
        stage.addActor(levelMenu);

        Label title = new Label("SELECT A  LEVEL", new Label.LabelStyle(bfont, Color.WHITE));
        title.setPosition(stage.getWidth() / 2 - title.getWidth() / 2, stage.getHeight() / 2 + textButtons[0].getHeight() * level_count / 2 + 80 );
        stage.addActor(title);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        camera.update();

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;

        camera.position.set(width/2.f, height/2.f, 0);

        stage.getViewport().update(width, height, false);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
    }
}
