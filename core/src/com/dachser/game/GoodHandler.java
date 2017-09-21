package com.dachser.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by taprosoft on 01/11/2015.
 */
public class GoodHandler {
    private LinkedList<Good> goods;
    private ArrayList<Integer> color_variants;
    public LinkedList<Label> scores;

    private Map map;
    private Batch batch;
    private Label.LabelStyle style_red;
    private Label.LabelStyle style_green;

    private float timer;
    private float spawn_interval = 5.f;
    private int speed = 4;

    private int start_x;
    private int start_y;

    /* constructor */
    public GoodHandler(Map map, ArrayList<Integer> color_variants, int start_x, int start_y, Batch batch) {
        this.map = map;

        goods = new LinkedList<Good>();
        scores = new LinkedList<Label>();
        Good.texture = new Texture("good.png");

        this.start_x = start_x;
        this.start_y = start_y;
        this.color_variants = color_variants;
        this.batch = batch;

        BitmapFont bfont = new BitmapFont();
        style_green = new Label.LabelStyle(bfont, Color.GREEN);
        style_red = new Label.LabelStyle(bfont, Color.RED);

        timer = spawn_interval - 1;
    }

    /* spawn new good after specific interval */
    private void spawn(float dt) {
        timer += dt;
        if (timer >= spawn_interval ) {
            if (color_variants.size() > 0) {
                int variant = MathUtils.random(0, color_variants.size() - 1);
                goods.add(new Good(start_x, start_y, color_variants.get(variant)));
            }
            else
                goods.add(new Good(start_x, start_y, 0));
            timer = 0;
        }
    }

    /* draw goods in list */
    private void draw(float dt) {
        for (Good good : goods) {
            int pos_x = (int) ((good.x - good.y) * map.tileSize + map.width * map.tileSize);
            int pos_y = (int) (map.height * map.tileSize - (good.x + good.y) * map.tileSize * 0.5);
            good.draw(batch, dt, pos_x, pos_y);
        }
        for (Label label : scores) {
            label.act(dt);
            label.draw(batch, 1);
        }
    }

    /* purge stopped goods from list */
    private void clearStopped() {
        Iterator<Good> it = goods.iterator();
        while (it.hasNext()) {
            Good good = it.next();
            if (good.getDirection() < 0) {

                /* add score notification */
                Label new_score;
                if (good.getDirection() == -2)
                    new_score = new Label("+100", style_green);
                else
                    new_score = new Label("-100", style_red);

                int pos_x = (int) ((good.x - good.y) * map.tileSize + map.width * map.tileSize);
                int pos_y = (int) (map.height * map.tileSize - (good.x + good.y) * map.tileSize * 0.5);

                new_score.setPosition(pos_x + 10, pos_y + 20);
                new_score.addAction(Actions.sequence(Actions.moveBy(0, 40, 2.f), Actions.fadeOut(0.2f), Actions.hide()));
                scores.add(new_score);

                it.remove();
            }
        }

        /* purge hidden labels */
        Iterator<Label> il = scores.iterator();
        while (il.hasNext()) {
            Label label = il.next();
            if (!label.isVisible()) il.remove();
        }
    }

    /* handle specific TileType logic on good */
    private void handle(Good good, TileType type, int tile_variant) {
        if (type == TileType.END) {
            if (good.getVariant() == tile_variant) {
                good.setDirection(-2);
            }
            else
                good.setDirection(-1);
        }
        else if (type != TileType.CONVEYOR) {
            good.setDirection(-1);
        }
    }

    /* handle goods collision */
    private void handleCollision() {
        for (int i = 0; i < goods.size() - 1; i++)
            for (int j = i + 1; j < goods.size(); j++) {
                    Good good = goods.get(i);
                    Good collid = goods.get(j);
                    if (Math.abs(good.x - collid.x) < 1 && Math.abs(good.y - collid.y) < 0.1) {
                        float step = 1 - Math.abs(good.x - collid.x);
                        if (good.x > collid.x) {
                            good.x += step;
                            collid.x -= step;
                        }
                        else {
                            good.x -= step;
                            collid.x += step;
                        }
                    }
                    if (Math.abs(good.y - collid.y) < 1 && Math.abs(good.x - collid.x) < 0.1) {
                        float step = 1 - Math.abs(good.y - collid.y);
                        if (good.y > collid.y) {
                            good.y += step;
                            collid.y -= step;
                        }
                        else {
                            good.y -= step;
                            collid.y += step;
                        }
                    }
                }
    }

    /* move the goods according to current tile direction */
    private void updatePosition(float dt) {
        for (Good good : goods) {
            int x = (int) good.x;
            int y = (int) good.y;

            switch (good.getDirection()) {
                case Direction.RIGHT: y = (int) Math.ceil(good.y); break;
                case Direction.BACKWARD: x = (int) Math.ceil(good.x); break;
            }

            good.setDirection(map.getDirection(x, y));

            switch (map.getDirection(x, y)) {
                case Direction.FORWARD:
                    if ( (good.x + speed * dt) < (Math.floor(good.x) + 1) )
                        good.x += speed * dt;
                    else
                        good.x = (int) Math.floor(good.x) + 1;
                    break;
                case Direction.BACKWARD:
                    if ( (good.x - speed * dt) > (Math.ceil(good.x) - 1) )
                        good.x -= speed * dt;
                    else
                        good.x = (int) Math.ceil(good.x) - 1;
                    break;
                case Direction.LEFT:
                    if ( (good.y + speed * dt) < (Math.floor(good.y) + 1) )
                        good.y += speed * dt;
                    else
                        good.y = (int) Math.floor(good.y) + 1;
                    break;
                case Direction.RIGHT:
                    if ( (good.y - speed * dt) > (Math.ceil(good.y) - 1) )
                        good.y -= speed * dt;
                    else
                        good.y = (int) Math.ceil(good.y) - 1;
                    break;
            }

            handle(good, map.getType(x, y), map.getVariant(x, y));
        }

        handleCollision();
    }

    /* main method to update and manage goods */
    public void act(float dt) {
        spawn(dt);
        updatePosition(dt);
        draw(dt);
        clearStopped();
    }

    public void dispose() {
        Good.dispose();
    }
}
