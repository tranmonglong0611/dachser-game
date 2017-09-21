package com.dachser.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class startScreen implements Screen {
    private Stage stage;
    private SpriteBatch batch;
    private Texture background;
    private OrthographicCamera camera;
    private Music bg_music;

    /* Define game UI Style */
    Skin skin;

    Dachser game;

    public startScreen(Dachser game){
        create();
        this.game = game;
    }

    public startScreen(){
        create();
    }

    public void create(){
        batch = new SpriteBatch();
        camera = new OrthographicCamera(1000, 600);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        bg_music = Gdx.audio.newMusic(Gdx.files.internal("main_theme.wav"));
        bg_music.setVolume(0.5f);
        bg_music.setLooping(true);
        bg_music.play();

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


        final TextButton[] textButton = new TextButton[] {
                new TextButton("PLAY", skin, "button"),
                new TextButton("MAP EDITOR", skin, "button"),
                new TextButton("OPTION", skin, "button"),
                new TextButton("EXIT", skin, "button")
        };

        VerticalGroup mainMenu = new VerticalGroup();

        for (TextButton button : textButton) {
            mainMenu.addActor(button);
        }

        mainMenu.setPosition(stage.getWidth() / 2, (stage.getHeight() + textButton[0].getHeight() * 4) / 2);
        mainMenu.space(10);
        stage.addActor(mainMenu);

        textButton[0].addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                textButton[0].setText("Starting new game...");
                //game.setScreen(new playScreen(game, "map_Slot 05"));
                game.setScreen(new levelScreen(game));
                dispose();
                System.gc();
            }
        });

        textButton[1].addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                textButton[1].setText("Loading editor...");
                game.setScreen(new editorScreen(game));
                dispose();
                System.gc();
            }
        });

        textButton[3].addListener(new ChangeListener() {
            public void changed (ChangeEvent event, Actor actor) {
                textButton[3].setText("Exitting...");
                dispose();
                Gdx.app.exit();
            }
        });

    }

    public void render (float delta) {
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
    public void resize (int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;

        camera.position.set(width/2.f, height/2.f, 0);

        stage.getViewport().update(width, height, false);
    }

    @Override
    public void dispose () {
        stage.dispose();
        skin.dispose();
        background.dispose();
        bg_music.dispose();
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

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
