package com.dachser.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by taprosoft on 31/10/2015.
 */
public class Good {

    private static int variant_count = 4;
    private static int frame_count = 1;
    private static float animationTime = 1.f;

    float x;
    float y;
    private int variant;
    private int direction;
    private int height;

    static int tileSize = 32;

    Animation[] animations;
    TextureRegion[][] textureRegions;
    TextureRegion currentFrame;
    static Texture texture;

    float time;

    public Good(int x, int y, int variant) {
        this.x = x;
        this.y = y;
        this.variant = variant;

        height = 2;
        direction = 0;

        textureRegions = new TextureRegion[variant_count][];
        animations = new Animation[textureRegions.length];

        for (int i = 0; i < textureRegions.length; i++)
        {
            textureRegions[i] = new TextureRegion[frame_count];
            for (int j = 0; j < frame_count; j++) {
                textureRegions[i][j] = new TextureRegion(texture, j * 2 * tileSize, i * height * tileSize, 2 * tileSize, height * tileSize);
            }
            animations[i] = new Animation(animationTime, textureRegions[i]);
        }
        time = 0.f;
    }

    public void draw(Batch batch, float dt, int x, int y) {
        time += dt;
        while (time > animationTime) time -= animationTime;
        currentFrame = animations[variant].getKeyFrame(time, true);
        batch.draw(currentFrame, x, y);
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getVariant() {
        return  variant;
    }

    public static void dispose () {
        texture.dispose();
    }
}
