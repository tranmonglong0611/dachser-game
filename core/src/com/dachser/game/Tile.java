package com.dachser.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by taprosoft on 9/11/15.
 */
public class Tile {

    TileType type;

    int variant;
    int direction;

    static int tileSize;
    static Color highlighted = new Color(0x7d / 255.f, 0x7d / 255.f, 0x7d / 255.f, 1);

    private int height;

    Animation[] animations;
    TextureRegion[][] textureRegions;
    TextureRegion currentFrame;
    Texture texture;

    float time;

    public Tile()
    {
    }

    public Tile(TileType type, java.util.Map<String, Texture> tile_textures) {
        switch (type)
        {
            case VOID:
                setTile(1, tile_textures.get("void"), TileType.VOID, 1.f, new int[] {1, 1});
                break;
            case TREE:
                setTile(3, tile_textures.get("tree"), TileType.TREE, 1.f, new int[] {1});
                break;
            case GRASS:
                setTile(1, tile_textures.get("grass"), TileType.GRASS, 1.f, new int[] {1});
                break;
            case WATER:
                setTile(1, tile_textures.get("water"), TileType.WATER, 0.08f, new int[] {16});
                break;
            case CONVEYOR:
                setTile(2, tile_textures.get("conveyor"), TileType.CONVEYOR, 0.04f, new int[] {8, 8, 8, 8,
                                                                                            1, 1, 1, 1,
                                                                                            1, 1, 1, 1,
                                                                                            1, 1,
                                                                                            1, 1, 1, 1});
                break;
            case START:
                setTile(2, tile_textures.get("start"), TileType.START, 1.f, new int[] {1});
                break;
            case END:
                setTile(2, tile_textures.get("end"), TileType.END, 1.f, new int[]{1, 1 ,1 ,1});
                break;
        }
    }

    private void setTile(int height, Texture texture, TileType type, float animationTime, int[] frame_count) {
        this.type = type;
        this.variant = 0;
        this.direction = 0;
        this.texture = texture;
        this.height = height;

        //textureRegions = TextureRegion.split(texture, tileSize * 2, tileSize * height);
        textureRegions = new TextureRegion[frame_count.length][];
        animations = new Animation[textureRegions.length];

        for (int i = 0; i < textureRegions.length; i++)
        {
            textureRegions[i] = new TextureRegion[frame_count[i]];
            for (int j = 0; j < frame_count[i]; j++) {
                textureRegions[i][j] = new TextureRegion(texture, j * 2 * tileSize, i * height * tileSize, 2 * tileSize, height * tileSize);
            }
            animations[i] = new Animation(animationTime, textureRegions[i]);
        }
        time = 0.f;
    }

    public void draw(Batch batch, float dt, int x, int y, int selected) {
        time += dt;
        float duration = animations[variant].getAnimationDuration();
        while (time > duration) time -= duration;
        currentFrame = animations[variant].getKeyFrame(time, true);
        if (selected > 0) batch.setColor(highlighted);
            else batch.setColor(Color.WHITE);
        batch.draw(currentFrame, x, y);
    }

    public void dispose () {
        texture.dispose();
    }
}
