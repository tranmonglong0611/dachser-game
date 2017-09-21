package com.dachser.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


/**
 * Created by taprosoft on 9/12/15.
 */
public class editorScreen implements Screen {
    private enum Action {
        SELECTING, PANNING, NONE
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
    Dachser game;

    private Vector2 panningAnchor;
    private Vector2 selectionStart;
    private Vector2 selectionEnd;

    private Action action;
    private TileType currentTile;
    private int currentVariant;
    private boolean haveStart;

    /* constructor */
    public editorScreen(final Dachser game) {
        /* init map */
        map = new Map();
        map.load_blank(15, 15);
        haveStart = false;

        currentTile = TileType.CONVEYOR;

        /*init camera*/
        camera = new OrthographicCamera(1000, 600);  //map camera
        bg_camera = new OrthographicCamera(1000, 600);   // background camera

        background = new Texture("background.jpg");

        /* sprite batch for drawing */
        batch = new SpriteBatch();

        panningAnchor = new Vector2();
        selectionStart = new Vector2(0, 0);
        selectionEnd = new Vector2(0, 0);

        action = Action.NONE;
        initUI();

        this.game = game;

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(UI);
        inputMultiplexer.addProcessor(new GestureDetector(gesture));

        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(true);

        UI.getRoot().getColor().a = 0;
        UI.getRoot().addAction(Actions.fadeIn(0.5f));
    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }

    public void render(float delta) {

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawUI();
        drawMap(delta);
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

        batch.end();

        UI.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
        UI.draw();
    }

    private void clearChecked(TextButton[] menu)
    {
           for (TextButton item : menu) {
               item.setChecked(false);
           }
    }
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

        /* ----------------------- map size menu ----------------------- */
        final VerticalGroup size_menu = new VerticalGroup();
        final TextButton size_menu_items[] = new TextButton[] {

            new TextButton("9 x 9", skin, "menu"),
            new TextButton("15 x 15", skin, "menu"),
            new TextButton("20 x 20", skin, "menu"),
            new TextButton("20 x 30", skin, "menu"),
            new TextButton("30 x 20", skin, "menu"),
            new TextButton("30 x 30", skin, "menu")

        };

        for (final TextButton item : size_menu_items) {
            size_menu.addActor(item);
            item.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    menu_bar_items[0].setText("Map size: " + item.getText());
                    clearChecked(size_menu_items);
                    clearChecked(menu_bar_items);
                    size_menu.addAction(Actions.sequence(Actions.alpha(1), Actions.fadeOut(0.1f), Actions.hide()));
                    load_size(item.getText().toString());

                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }

        size_menu.setVisible(false);

        /* --------------------- open menu -------------------------------*/

        final  VerticalGroup open_menu = new VerticalGroup();

        final TextButton open_menu_items[] = new TextButton[] {

                new TextButton("Slot 01", skin, "menu"),
                new TextButton("Slot 02", skin, "menu"),
                new TextButton("Slot 03", skin, "menu"),
                new TextButton("Slot 04", skin, "menu"),
                new TextButton("Slot 05", skin, "menu"),
                new TextButton("Slot 06", skin, "menu")

        };

        for (final TextButton item : open_menu_items) {
            open_menu.addActor(item);
            item.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    clearChecked(open_menu_items);
                    clearChecked(menu_bar_items);
                    open_menu.addAction(Actions.sequence(Actions.alpha(1), Actions.fadeOut(0.1f), Actions.hide()));

                    map.load("map_" + item.getText());
                    haveStart = true;
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }

        open_menu.setVisible(false);

        /* --------------------- save menu -------------------------------*/
        final  VerticalGroup save_menu = new VerticalGroup();

        final TextButton save_menu_items[] = new TextButton[] {

                new TextButton("Slot 01", skin, "menu"),
                new TextButton("Slot 02", skin, "menu"),
                new TextButton("Slot 03", skin, "menu"),
                new TextButton("Slot 04", skin, "menu"),
                new TextButton("Slot 05", skin, "menu"),
                new TextButton("Slot 06", skin, "menu")

        };

        for (final TextButton item : save_menu_items) {
            save_menu.addActor(item);
            item.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    clearChecked(save_menu_items);
                    clearChecked(menu_bar_items);
                    save_menu.addAction(Actions.sequence(Actions.alpha(1), Actions.fadeOut(0.1f), Actions.hide()));

                    map.save("map_" + item.getText());
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }

        save_menu.setVisible(false);

        /* --------------------- tiles list menu -------------------------------*/
        final  VerticalGroup tile_menu = new VerticalGroup();

        final TextButton tile_menu_items[] = new TextButton[] {

                new TextButton("Grass", skin, "menu"),
                new TextButton("Conveyor", skin, "menu"),
                new TextButton("Blank", skin, "menu"),
                new TextButton("Tree", skin, "menu"),
                new TextButton("Water", skin, "menu"),
                new TextButton("Start point", skin, "menu")
        };

        for (final TextButton item : tile_menu_items) {
            tile_menu.addActor(item);
            item.addListener(new ClickListener() {
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    menu_bar_items[3].setText("Current tile: " + item.getText());
                    clearChecked(tile_menu_items);
                    clearChecked(menu_bar_items);
                    tile_menu.addAction(Actions.sequence(Actions.alpha(1), Actions.fadeOut(0.1f), Actions.hide()));

                    currentTile = TileType.string_toType(item.getText().toString());
                    super.touchUp(event, x, y, pointer, button);
                    event.handle();
                }
            });
        }

        tile_menu.setVisible(false);

        /* --------------------- main menu bar ---------------------------*/

        menu_bar_items[0] = new TextButton("Map size: ", skin, "menu");
        menu_bar_items[1] = new TextButton("Load ", skin, "menu");
        menu_bar_items[2] = new TextButton("Save ", skin, "menu");
        menu_bar_items[3] = new TextButton("Current Tile: ", skin, "menu");

        final VerticalGroup[] menu = new VerticalGroup[] {size_menu, open_menu, save_menu, tile_menu};

        for (int i = 0; i < 4 ; i++) {
            final int index = i;
            menu_bar_items[i].addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (menu[index].isVisible()) {
                        menu[index].addAction(Actions.sequence(Actions.alpha(1), Actions.fadeOut(0.1f), Actions.hide()));
                    } else {
                        closeAllMenu();
                        clearChecked(menu_bar_items);
                        menu[index].setPosition(UI.getWidth() / menu_bar_items.length * (0.5f + index), size_menu_items[0].getHeight() * (menu[index].getChildren().size + 1));
                        menu[index].setVisible(true);
                        menu[index].addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }

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
                    if (action != Action.SELECTING && !Gdx.input.isTouched(2)) {
                        action = Action.SELECTING;

                        Vector3 pos = new Vector3(screenX, screenY, 0);
                        camera.unproject(pos);

                        map.posToTile(selectionStart, pos.x, pos.y);
                        map.posToTile(selectionEnd, pos.x, pos.y);
                    }
                    else
                    {
                        action = Action.NONE;
                        map.clearSelected();
                    }
                }
                else if (button == Input.Buttons.RIGHT) {
                    Vector3 pos = new Vector3(screenX, screenY, 0);
                    bg_camera.unproject(pos);

                    closeAllMenu();
                    clearChecked(menu_bar_items);

                    float menu_x = (pos.x + tile_menu_items[0].getWidth() * 1.f <= UI.getWidth()) ? pos.x + tile_menu_items[0].getWidth() * 0.5f :
                                    pos.x - tile_menu_items[0].getWidth() * 0.5f;
                    float menu_y = (pos.y - tile_menu_items[0].getHeight() * tile_menu_items.length >= 0) ? pos.y :
                                    pos.y + tile_menu_items[0].getHeight() * tile_menu_items.length;

                    tile_menu.setPosition(menu_x, menu_y);
                    tile_menu.setVisible(true);
                    tile_menu.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));

                }

                if (pointer == 2) {
                    panningAnchor.set(screenX, screenY);
                }

                boolean handled = super.touchDown(screenX, screenY, pointer, button);

                if (!handled && button != Input.Buttons.RIGHT) {
                    closeAllMenu();
                    clearChecked(menu_bar_items);
                }

                return handled;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (action == Action.PANNING) {
                    int dx = (int) (screenX - panningAnchor.x);
                    int dy = (int) (screenY - panningAnchor.y);
                    camera.translate(-dx, dy);
                    panningAnchor.set(screenX, screenY);
                }
                else if (action == Action.SELECTING) {
                    Vector3 pos = new Vector3(screenX, screenY, 0);
                    camera.unproject(pos);

                    map.clearSelected();
                    map.posToTile(selectionEnd, pos.x, pos.y);

                    switch (currentTile) {
                        case VOID:
                            map.select(selectionStart, selectionEnd, new TileType[]{});
                            break;
                        case GRASS:
                        case WATER:
                        case TREE:
                            map.select(selectionStart, selectionEnd, new TileType[]{TileType.CONVEYOR
                                                                                  , TileType.WATER
                                                                                  , TileType.START
                                                                                  , TileType.END           });
                            break;
                        case START:
                            map.select(selectionStart, selectionStart, new TileType[]{TileType.CONVEYOR});
                            break;
                        case CONVEYOR:
                            /* force selection to 1 row or 1 column only */
                            int width =  (int) ( (selectionStart.x > selectionEnd.x) ? (selectionStart.x - selectionEnd.x) :
                                    (selectionEnd.x - selectionStart.x) );
                            int height = (int) ( (selectionStart.y > selectionEnd.y) ? (selectionStart.y - selectionEnd.y) :
                                    (selectionEnd.y - selectionStart.y) );
                            boolean isValid;

                            if (height > width)
                            {
                                selectionEnd.x = selectionStart.x;
                                if (selectionEnd.y > selectionStart.y)
                                {
                                    currentVariant = Direction.LEFT;
                                /* check if selected tiles would form a 2x2 square */
                                    isValid = map.check((int) selectionStart.y, (int) selectionEnd.y, (int) selectionEnd.x, true);
                                }
                                else
                                {
                                    currentVariant = Direction.RIGHT;
                                /* check if selected tiles would form a 2x2 square */
                                    isValid = map.check((int) selectionEnd.y, (int) selectionStart.y, (int) selectionEnd.x, true);
                                }
                            }
                            else
                            {
                                selectionEnd.y = selectionStart.y;
                                if (selectionEnd.x > selectionStart.x)
                                {
                                    currentVariant = Direction.FORWARD;
                                /* check if selected tiles would form a 2x2 square */
                                    isValid = map.check((int) selectionStart.x, (int) selectionEnd.x, (int) selectionEnd.y, false);
                                }
                                else
                                {
                                    currentVariant = Direction.BACKWARD;
                                /* check if selected tiles would form a 2x2 square */
                                    isValid = map.check((int) selectionEnd.x, (int) selectionStart.x, (int) selectionEnd.y, false);
                                }
                            }

                            if (isValid)
                            {
                                map.select(selectionStart, selectionEnd, new TileType[] {TileType.START});
                            }
                            break;
                    }
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
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.MIDDLE) {
                    action = Action.NONE;
                }
                else if (button == Input.Buttons.LEFT) {
                    if (action == Action.SELECTING) {
                        action = Action.NONE;
                        /* create tile */
                        if (currentTile != TileType.START || !haveStart) {
                            /* change selected tile */
                            boolean[] start = {haveStart};
                            map.setSelectedTo(currentTile, currentVariant, start);
                            haveStart = start[0];

                            /* if new tiles is conveyor, create transition */
                            if (currentTile == TileType.CONVEYOR) {
                                    map.createTransition();
                                    if (haveStart) {
                                        map.clearEnd();
                                        map.updateStart();
                                    }
                            }

                            /* if new tile is void and haveStart, update end point */
                            if (currentTile == TileType.VOID && haveStart) {
                                map.clearEnd();
                                map.updateStart();
                            }
                        }
                        if (currentTile == TileType.START && haveStart) {
                            currentTile = TileType.CONVEYOR;
                        }
                        map.clearSelected();
                    }
                }
                return super.touchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {return super.mouseMoved(screenX, screenY);
            }

            @Override
            public boolean keyDown(int keyCode) {
                return super.keyDown(keyCode);
            }

            @Override
            public boolean keyUp(int keyCode) {
                if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACK) {
                    game.setScreen(new startScreen(game));
                    dispose();
                    System.gc();
                }
                return super.keyUp(keyCode);
            }

            @Override
            public boolean keyTyped(char character) {
                return super.keyTyped(character);
            }

            @Override
            public boolean scrolled(int amount) {
                camera.zoom += amount * 0.2;
                if (camera.zoom > 2.0) camera.zoom = 2.0f;
                return super.scrolled(amount);
            }

            @Override
            public void act(float delta) {
                menu_bar_items[0].setText("Map size: " + map.width + " x " + map.height);
                menu_bar_items[3].setText("Current tile: " + TileType.type_toString(currentTile));
                super.act(delta);
            }
        };

        menu_bar = new Table();
        for (TextButton item: menu_bar_items) {
            menu_bar.add(item).width(UI.getWidth() / menu_bar_items.length);
        }
        menu_bar.left().bottom();
        menu_bar.setPosition(0, 0);
        menu_bar.setWidth(UI.getWidth());
        menu_bar.setHeight(menu_bar_items[0].getHeight());

        UI.addActor(menu_bar);

        UI.addActor(size_menu);
        UI.addActor(open_menu);
        UI.addActor(tile_menu);
        UI.addActor(save_menu);

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
            public boolean longPress(float x, float y) {
                Vector3 pos = new Vector3(x, y, 0);
                bg_camera.unproject(pos);

                closeAllMenu();
                clearChecked(menu_bar_items);

                float menu_x = (pos.x + tile_menu_items[0].getWidth() * 1.f <= UI.getWidth()) ? pos.x + tile_menu_items[0].getWidth() * 0.5f :
                        pos.x - tile_menu_items[0].getWidth() * 0.5f;
                float menu_y = (pos.y - tile_menu_items[0].getHeight() * tile_menu_items.length >= 0) ? pos.y :
                        pos.y + tile_menu_items[0].getHeight() * tile_menu_items.length;

                tile_menu.setPosition(menu_x, menu_y);
                tile_menu.setVisible(true);
                tile_menu.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));
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
        };
    }

    private void closeAllMenu() {
        for (Actor actor : UI.getActors()) {
            if (actor.getClass() == VerticalGroup.class) {
                actor.setVisible(false);
            }
        }
    }

    private void load_size(String size) {
        String[] part = size.split(" x ");
        int x = Integer.parseInt(part[0]);
        int y = Integer.parseInt(part[1]);
        map.load_blank(x, y);
        haveStart = false;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;

        bg_camera.viewportHeight = height;
        bg_camera.viewportWidth = width;

        camera.position.set(map.width * map.tileSize, map.height * map.tileSize / 2.f, 0);
        bg_camera.position.set(width / 2.f, height / 2.f, 0);

        UI.getViewport().update(width, height, false);

        menu_bar.setPosition(0, 0);
        menu_bar.setWidth(UI.getWidth());
        for (Cell cell : menu_bar.getCells()) {
            cell.width(UI.getWidth() / menu_bar.getCells().size);
        }

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        UI.dispose();
        skin.dispose();
        batch.dispose();
        background.dispose();
        map.dispose();
    }

}
