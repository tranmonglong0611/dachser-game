package com.dachser.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by taprosoft on 05/11/2015.
 */
public class pauseScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private TextureRegion background;
    private OrthographicCamera camera;

    /* Define game UI Style */
    Skin skin;

    private Dachser game;
    private playScreen prevScreen;

    /* constructor */
    public pauseScreen(Dachser game, playScreen prevScreen) {
        this.game = game;
        this.prevScreen = prevScreen;

        initUI();
    }

    public void setBackground(TextureRegion background) {
        this.background = background;
    }

    private void initUI() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(1000, 600);

        /* stage for creating menu and handling input */
        stage = new Stage(new ScreenViewport()) {
            @Override
            public boolean keyUp(int keyCode) {
               /* if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK) {
                    game.setScreen(prevScreen);
                    Gdx.input.setInputProcessor(prevScreen.getStage());
                    dispose();
                    System.gc();
                }*/

                return super.keyUp(keyCode);
            }
        };

        /* generate skin for menu */
        skin = new Skin();

        Texture buttons = new Texture("buttons.png");

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

        /* init pause menu */
        final TextButton[] textButton = new TextButton[] {
                new TextButton("RESUME", skin, "button"),
                new TextButton("OPTION", skin, "button"),
                new TextButton("RETURN TO LEVEL SELECT", skin, "button"),
                new TextButton("EXIT TO MAIN MENU", skin, "button")
        };

        VerticalGroup mainMenu = new VerticalGroup();

        for (TextButton button : textButton) {
            mainMenu.addActor(button);
        }

        mainMenu.setPosition(stage.getWidth() / 2, (stage.getHeight() + textButton[0].getHeight() * textButton.length) / 2);
        mainMenu.space(10);
        stage.addActor(mainMenu);

        /* resume */
        textButton[0].addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                textButton[0].setChecked(false);
                game.setScreen(prevScreen);
                Gdx.input.setInputProcessor(prevScreen.getStage());
                background.getTexture().dispose();
            }
        });

        /* level select */
        textButton[2].addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                textButton[2].setChecked(false);
                game.setScreen(new levelScreen(game));
                prevScreen.dispose();
                dispose();
                System.gc();

                return super.touchDown(event, x, y, pointer, button);
            }
        });

        /* return to main menu */
        textButton[3].addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                textButton[3].setChecked(false);
                game.setScreen(new startScreen(game));
                prevScreen.dispose();
                dispose();
                System.gc();

                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    public void render (float delta) {
        camera.update();

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(100 / 255f, 100 / 255f, 100 /255f, 1);
        batch.draw(background, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize (int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;

        camera.position.set(width/2.f, height/2.f, 0);

        stage.getViewport().update(width, height, false);
    }

    private boolean disposed = false;

    @Override
    public void dispose () {
        if (!disposed) {
            stage.dispose();
            skin.dispose();
            disposed = true;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.getRoot().getColor().a = 0;
        stage.getRoot().addAction(Actions.fadeIn(0.2f));
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }
}
