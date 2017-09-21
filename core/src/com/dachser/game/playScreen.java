package com.dachser.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by taprosoft on 28/10/2015.
 */
public class playScreen implements Screen {
    private enum Action {
        PANNING, NONE
    }

    private Map map;
    private SpriteBatch batch;

    private Stage UI;
    private Table menu_bar;
    private Skin skin;

    private GestureDetector.GestureListener gesture;
    private InputMultiplexer inputMultiplexer;

    private OrthographicCamera camera;
    private OrthographicCamera bg_camera;
    private Texture background;
    private Music bg_music;
    Dachser game;

    private Vector2 panningAnchor;
    private Action action;

    private int start_x;
    private int start_y;

    private ArrayList<Integer> color_variants;
    private GoodHandler goodHandler;

    private final int variant_count = 3;
    private pauseScreen pause;

    /* constructor */
    public playScreen(final Dachser game, String map_name) {
        /*init camera*/
        camera = new OrthographicCamera(1000, 600);  //map camera
        bg_camera = new OrthographicCamera(1000, 600);   // background camera

        background = new Texture("background.jpg");
        panningAnchor = new Vector2();
        action = Action.NONE;

        /* start background music */
        bg_music = Gdx.audio.newMusic(Gdx.files.internal("main_theme.wav"));
        bg_music.setVolume(0.5f);
        bg_music.setLooping(true);
        bg_music.play();

        /* sprite batch for drawing */
        batch = new SpriteBatch();

        /*init game UI */
        initUI();

        /* load map */
        map = new Map();
        if (!map_name.isEmpty()) {
            map.load(map_name);
            Vector2 start_pos = new Vector2();
            map.getStart(start_pos);
            start_x = (int) start_pos.x;
            start_y = (int) start_pos.y;
        }
        else map.load_blank(15, 15);

        /* ready the map for playing */
        updateStart();
        initSwitch();
        changeBlankVariant();
        initEnd();
        goodHandler = new GoodHandler(map, color_variants, start_x, start_y, batch);

        this.game = game;

        /* init pause screen */
        pause = new pauseScreen(game, this);

        /* setup input */
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(UI);
        inputMultiplexer.addProcessor(new GestureDetector(gesture));

        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(true);

        /* animate UI at load up */
        UI.getRoot().getColor().a = 0;
        UI.getRoot().addAction(Actions.fadeIn(0.5f));
    }

    /* create game UI */
    private void initUI() {
         /* init UI and handle input */

        final TextButton[] menu_bar_items = new TextButton[4];

        /* --------------------------------------------------------------- */
        /* --------------------- create menu style ----------------------- */
        skin = new Skin();
        Texture menu_theme = new Texture("menu.png");

        TextureRegion[][] menu_state = TextureRegion.split(menu_theme, menu_theme.getWidth(), menu_theme.getHeight() / 3);
        skin.add("menu_up", menu_state[0][0]);
        skin.add("menu_highlight", menu_state[1][0]);
        skin.add("menu_down", menu_state[2][0]);

        BitmapFont bfont = new BitmapFont();
        bfont.setColor(Color.BLACK);

        skin.add("default", bfont);

        TextButton.TextButtonStyle menu_style = new TextButton.TextButtonStyle();
        menu_style.up = skin.newDrawable("menu_up");
        menu_style.over = skin.newDrawable("menu_highlight");
        menu_style.down = skin.newDrawable("menu_down");
        menu_style.checked = skin.newDrawable("menu_down");

        menu_style.font = skin.getFont("default");
        skin.add("menu", menu_style);

        /* --------------------- main menu bar ---------------------------*/

        menu_bar_items[0] = new TextButton("Score: ", skin, "menu");
        menu_bar_items[1] = new TextButton("Time: ", skin, "menu");
        menu_bar_items[2] = new TextButton("Menu ", skin, "menu");
        menu_bar_items[3] = new TextButton("Menu ", skin, "menu");

        /* Create and set UI */
        UI = new Stage(new ScreenViewport(bg_camera)) {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.MIDDLE) {
                    if (action != Action.PANNING) {
                        action = Action.PANNING;
                        panningAnchor.set(screenX, screenY);
                    }
                }
                else if (button == Input.Buttons.LEFT) {
                    Vector3 pos = new Vector3(screenX, screenY, 0);
                    camera.unproject(pos);
                    Vector2 index = new Vector2();
                    map.posToTile(index, pos.x, pos.y);

                    map.changeSwitch((int) index.x, (int) index.y);
                }

                if (pointer == 2) {
                    panningAnchor.set(screenX, screenY);
                }
                return super.touchDown(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.MIDDLE) {
                    action = Action.NONE;
                }

                return super.touchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                /* moving the map around */
                if (action == Action.PANNING) {
                    int dx = (int) (screenX - panningAnchor.x);
                    int dy = (int) (screenY - panningAnchor.y);
                    camera.translate(-dx, dy);
                    panningAnchor.set(screenX, screenY);
                }

                /* move the map if the third finger detected */
                if (pointer == 2) {
                    int dx = (int) (screenX - panningAnchor.x);
                    int dy = (int) (screenY - panningAnchor.y);
                    camera.translate(-dx, dy);
                    panningAnchor.set(screenX, screenY);
                    return true;
                }

                return super.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean keyUp(int keyCode) {
                if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK) {
                    pause.setBackground(ScreenUtils.getFrameBufferTexture());
                    game.setScreen(pause);
                }

                return super.keyUp(keyCode);
            }

            @Override
            public boolean scrolled(int amount) {
                /* zoom the map on scroll */
                camera.zoom += amount * 0.2;
                if (camera.zoom > 2.0) camera.zoom = 2.0f;
                return super.scrolled(amount);
            }

            @Override
            public void act(float delta) {
                super.act(delta);
            }
        };

        menu_bar = new Table();
        for (TextButton item: menu_bar_items) {
            menu_bar.add(item).width(UI.getWidth() / menu_bar_items.length);
        }
        menu_bar.left().bottom();
        menu_bar.setPosition(0, UI.getHeight() - menu_bar.getHeight());
        menu_bar.setWidth(UI.getWidth());
        menu_bar.setHeight(menu_bar_items[0].getHeight());

        UI.addActor(menu_bar);

        /* handle multitouch gesture */
        gesture = new GestureDetector.GestureListener() {
            public float initialScale = 1.0f;

            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {

                initialScale = camera.zoom;

                return true;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {
                if (!Gdx.input.isTouched(2)) {
                    //Calculate pinch to zoom
                    float ratio = initialDistance / distance;

                    //Clamp range and set zoom
                    camera.zoom = MathUtils.clamp(initialScale * ratio, 0.1f, 1.0f);
                }
                return true;
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                return false;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                return false;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                return false;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
                return false;
            }

            @Override
            public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                return false;
            }

            @Override
            public boolean longPress(float x, float y) {
                return false;
            }
        };
    }

    private void drawUI() {
        bg_camera.update();
        batch.setProjectionMatrix(bg_camera.combined);

        batch.begin();

        batch.draw(background, 0, 0, bg_camera.viewportWidth, bg_camera.viewportHeight);

        batch.end();
    }

    private void drawMap(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        map.draw(batch, delta);
        goodHandler.act(delta);

        batch.end();

        UI.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
        UI.draw();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawUI();
        drawMap(delta);
    }

    /* calculate goods starting point */
    private void updateStart() {
        if (start_x > 0 && map.getDirection(start_x - 1, start_y) == Direction.BACKWARD)
            start_x = start_x - 1;
        if (start_x + 1 < map.width && map.getDirection(start_x + 1, start_y) == Direction.FORWARD)
            start_x = start_x + 1;
        if (start_y > 0 && map.getDirection(start_x, start_y - 1) == Direction.RIGHT)
            start_y = start_y - 1;
        if (start_y + 1 < map.height && map.getDirection(start_x, start_y + 1) == Direction.LEFT)
            start_y = start_y + 1;
    }

    /* create End point colors variant */
    private void initEnd() {
        color_variants = new ArrayList<Integer>();

        for (int x = 0; x < map.width; x++ )
            for (int y = 0; y < map.height; y++ ) {
                if (map.getType(x, y) == TileType.END) {
                    int variant = MathUtils.random(1, variant_count);
                    map.setVariant(x, y, variant);
                    color_variants.add(variant);
                }
            }
    }

    /* set blank tiles to no border */
    private void changeBlankVariant() {
        for (int x = 0; x < map.width; x++ )
            for (int y = 0; y < map.height; y++ ) {
                if (map.getType(x, y) == TileType.VOID)
                    map.setVariant(x, y, 1);
            }
    }

    /* Add arrow to all Switchs */
    private void initSwitch() {
        for (int x = 0; x < map.width; x++ )
            for (int y = 0; y < map.height; y++ ) {
                if (map.getType(x, y) == TileType.CONVEYOR && map.getVariant(x, y) == 13)
                    map.setVariant(x, y, 14 + map.getDirection(x, y));
            }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;

        bg_camera.viewportHeight = height;
        bg_camera.viewportWidth = width;

        camera.position.set(map.width * map.tileSize, map.height * map.tileSize / 2.f + menu_bar.getHeight() + 10, 0);
        bg_camera.position.set(width / 2.f, height / 2.f, 0);

        UI.getViewport().update(width, height, false);

        menu_bar.setPosition(0, UI.getHeight() - menu_bar.getHeight());
        menu_bar.setWidth(UI.getWidth());
        for (Cell cell : menu_bar.getCells()) {
            cell.width(UI.getWidth() / menu_bar.getCells().size);
        }

    }

    public Stage getStage() {
        return  UI;
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
        UI.dispose();
        skin.dispose();
        batch.dispose();
        background.dispose();
        map.dispose();
        goodHandler.dispose();
        bg_music.dispose();
    }
}
