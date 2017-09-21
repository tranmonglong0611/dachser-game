package com.dachser.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;

/**
 * Created by taprosoft on 9/11/15.
 */
public class Map {
    int width;
    int height;

    Tile[][] tiles;

    private int num_selected;
    private int[][] selected;

    private java.util.Map<String, Texture> tile_textures;

    int tileSize;

    private int start_x;
    private int start_y;

    public Map() {
        height = width = 0;
        num_selected = 0;
        tileSize = Tile.tileSize = 32;
        load_tile();
    }

    /* open a map preference*/
    public void load(String file_name) {
        Preferences map = Gdx.app.getPreferences(file_name);
        width = map.getInteger("width");
        height = map.getInteger("height");
        tiles = new Tile[height][width];
        selected = new int[height][width];

        for (int y = 0; y < height; y++ )
            for(int x = 0; x < width; x++)
            {
                tiles[y][x] = new Tile( TileType.values()[map.getInteger( "(" + y + "," + x + ")type")], tile_textures );
                tiles[y][x].variant = map.getInteger("(" + y + "," + x + ")variant");
                tiles[y][x].direction = map.getInteger( "(" + y + "," + x + ")direction");
                if (getType(x, y) == TileType.START) {
                    start_x = x;
                    start_y = y;
                }
            }
    }

    /* save the map into preference with the name "file_name" */
    public void save(String file_name) {
        Preferences map = Gdx.app.getPreferences(file_name);
        map.putInteger("width", width);
        map.putInteger("height", height);

        for (int y = 0; y < height; y++ )
            for(int x = 0; x < width; x++) {
                map.putInteger("(" + y + "," + x + ")type", tiles[y][x].type.ordinal());
                map.putInteger("(" + y + "," + x + ")variant", tiles[y][x].variant);
                map.putInteger("(" + y + "," + x + ")direction", tiles[y][x].direction);
            }
        map.flush();
    }

    /* draw the map */
    public void draw(Batch batch, float dt) {
        for(int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int pos_x = (x - y) * tileSize + width * tileSize;
                int pos_y = (int) (height * tileSize - (x + y) * tileSize * 0.5);
                tiles[y][x].draw(batch, dt, pos_x, pos_y, selected[y][x]);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /* load a blank map with size width x height */
    public void load_blank(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new Tile[height][width];
        selected = new int[height][width];
        for (int y = 0; y < height; y++ )
            for (int x = 0; x < width; x++)
            {
                tiles[y][x] = new Tile(TileType.VOID, tile_textures);
                selected[y][x] = 0;
            }
        num_selected = 0;
    }

    /* convert a position on screen to a tile's index */
    public void posToTile(Vector2 tile_index, float x, float y) {
        float rev_y = height * tileSize - y;
        tile_index.x = (int) ( rev_y / tileSize + x / (2 * tileSize) - width * 0.5 + 0.5 ) ;
        tile_index.y = (int) ( rev_y / tileSize - x / (2 * tileSize) + width * 0.5 + 1.5 ) ;
    }

    /* unset all selected state */
    public void clearSelected() {
        for (int y = 0; y < height; y++ )
            for (int x = 0; x < width; x++)
            {
                selected[y][x] = 0;
            }
        num_selected = 0;
    }

    /* select tiles within start and end range */
    public void select(Vector2 start, Vector2 end, TileType[] blacklist) {
        /* Swap coordinates if necessary */
        int lo_x, hi_x, lo_y, hi_y;

        if (start.x > end.x) {
            lo_x = (int) end.x;
            hi_x = (int) start.x;
        } else {
            lo_x = (int) start.x;
            hi_x = (int) end.x;
        }

        if (start.y > end.y) {
            lo_y = (int) end.y;
            hi_y = (int) start.y;
        } else {
            lo_y = (int) start.y;
            hi_y = (int) end.y;
        }

        /* check for out of bound select */
        if (lo_x >= width) return;
        if (lo_y >= height) return;
        if (hi_x < 0) return;
        if (hi_y < 0) return;

        /* Clamp in range */
        if (hi_x >= width) hi_x = width - 1;
        else if (hi_x < 0) hi_x = 0;
        if (hi_y >= height) hi_y = height - 1;
        else if (hi_y < 0) hi_y = 0;
        if (lo_x >= width) lo_x = width - 1;
        else if (lo_x < 0) lo_x = 0;
        if (lo_y >= height) lo_y = height - 1;
        else if (lo_y < 0) lo_y = 0;

        for (int y = lo_y; y <= hi_y; y++ )
            for (int x = lo_x; x <= hi_x; x++ )
            {
                selected[y][x] = 1;
                num_selected++;
                for (TileType black_type : blacklist) {
                    if (tiles[y][x].type == black_type) {
                        selected[y][x] = 2;
                        num_selected--;
                        break;
                    }
                }
            }
    }

    /* set selected tiles to a specified type */
    public void setSelectedTo(TileType type, int variant, boolean[] haveStart) {
        for(int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x)
                if (selected[y][x] == 1) {
                    if (getType(x, y) == TileType.START && type == TileType.VOID) {
                        haveStart[0] = false;
                        clearEnd();
                    }

                    tiles[y][x] = new Tile(type, tile_textures);

                    if (type == TileType.CONVEYOR) {
                        tiles[y][x].variant = variant;
                        tiles[y][x].direction = variant;
                    }

                    if (type == TileType.START) {
                        haveStart[0] = true;
                        start_x = x;
                        start_y = y;
                        clearEnd();
                        updateStart();
                    }
                }
        }
    }

    /* load tiles textures */
    private void load_tile()
    {
        tile_textures = new HashMap();

        tile_textures.put("void", new Texture("void.png"));
        tile_textures.put("grass", new Texture("grass.png"));
        tile_textures.put("water", new Texture("water.png"));
        tile_textures.put("tree", new Texture("tree.png"));
        tile_textures.put("conveyor", new Texture("conveyor.png"));
        tile_textures.put("start", new Texture("start.png"));
        tile_textures.put("end", new Texture("end.png"));
    }

    /* dispose map to release resources */
    public void dispose() {
        for (Tile[] tile_row : tiles) {
            for (Tile cell: tile_row) {
                cell.dispose();
            }
        }
        for (String key : tile_textures.keySet()) {
            tile_textures.get(key).dispose();
        }
    }

    /* return TileType at (x,y) */
    public TileType getType(int x, int y) {
        if (y >= 0 && x >= 0 && y < height && x < width)
            return tiles[y][x].type;
        else
            return TileType.VOID;
    }

    /* get tile direction at (x,y) */
    public int getDirection(int x, int y) {
        if (y >= 0 && x >= 0 && y < height && x < width && getType(x, y) == TileType.CONVEYOR)
            return tiles[y][x].direction;
        else
            return -1;
    }

    public void getStart(Vector2 start_pos) {
        start_pos.x = start_x;
        start_pos.y = start_y;
    }

    public void setVariant(int x, int y, int variant) {
        tiles[y][x].variant = variant;
    }

    public int getVariant(int x, int y) {
        if (x >= 0 && y >= 0 && x < width && y < height)
            return tiles[y][x].variant;
        return -1;
    }

    /* check if tile at (x,y) is conveyor */
    public boolean isConveyor(int x, int y) {
        if (y >= 0 && x >= 0 && y < height && x < width)
            return (tiles[y][x].type == TileType.CONVEYOR);
        return false;
    }

    public boolean checkSquare(int x, int y, int hole)
        /* hole position
         * 0 1
         * 2 3 */
    {
        switch (hole) {
            case 0:   if ((x+1) < width && (y+1) < height &&
                    isConveyor(x + 1, y) &&
                    isConveyor(x, y + 1) &&
                    isConveyor(x + 1, y + 1) )
                        return false;
                    break;
            case 1:   if (x > 0 && (y+1) < height &&
                    isConveyor(x - 1, y) &&
                    isConveyor(x - 1, y + 1) &&
                    isConveyor(x , y + 1) )
                        return false;
                    break;
            case 2:   if ((x+1) < width && y > 0 &&
                    isConveyor(x, y - 1) &&
                    isConveyor(x + 1, y) &&
                    isConveyor(x + 1, y - 1) )
                        return false;
                    break;
            case 3:   if (x > 0 && y > 0 &&
                    isConveyor(x - 1, y - 1) &&
                    isConveyor(x , y - 1) &&
                    isConveyor(x - 1, y) )
                        return false;
                    break;
        }

        return true;
    }

    /* check and detect invalid selection */
    public boolean check(int start, int end, int column, boolean col)
    {
        /* Clamp in range */
        if (start < 0) start = 0;
        if (col && end >= height) end = height - 1;
        if (!col && end >= width) end = width - 1;

        if (column < 0) return false;
        if (col && column >= width ) return false;
        if (!col && column >= height ) return false;

        if (col)
        {
            for (int i = start; i < end; i++)
            {
                if ( ( (column + 1) < width ) && isConveyor(column + 1, i) &&
                isConveyor(column + 1, i + 1)   )
                    return false;
                if ( (column > 0) && isConveyor(column - 1, i) &&
                isConveyor(column - 1, i + 1)   )
                    return false;
            }

            if (!checkSquare(column, start, 2)) return false;
            if (!checkSquare(column, start, 3)) return false;
            if (!checkSquare(column, end, 0)) return false;
            if (!checkSquare(column, end, 1)) return false;
        }
        else /* check selection row */
        {
            int row = column;
            for (int i = start; i < end; i++)
            {
                if ( ( (row + 1) < height ) && isConveyor(i, row + 1) &&
                isConveyor(i + 1, row + 1)   )
                    return false;
                if ( (row > 0) && isConveyor(i, row - 1) &&
                isConveyor(i + 1, row - 1)   )
                return false;
            }

            if (!checkSquare( start, row, 3)) return false;
            if (!checkSquare( start, row, 1)) return false;
            if (!checkSquare( end, row, 2)) return false;
            if (!checkSquare( end, row, 0)) return false;
        }


        return true;
    }

    /* Create tiles transition */
    public void setTransition(int x, int y) {
        int numOut = 0;
        int outDirection = -1;

        /* Count the number of out going ways from current pos */
        if (x > 0 && isConveyor(x - 1, y) &&
               tiles[y][x - 1].direction == Direction.BACKWARD)
        {
            numOut ++;
            outDirection = Direction.BACKWARD;
        }
        if ((x+1) < width && isConveyor(x + 1, y) &&
                tiles[y][x + 1].direction == Direction.FORWARD)
        {
            numOut ++;
            outDirection = Direction.FORWARD;
        }
        if (y > 0 && isConveyor(x , y - 1) &&
                tiles[y-1][x].direction == Direction.RIGHT)
        {
            numOut ++;
            outDirection = Direction.RIGHT;
        }
        if ((y+1) < height &&  isConveyor(x , y + 1) &&
                tiles[y+1][x].direction == Direction.LEFT)
        {
            numOut ++;
            outDirection = Direction.LEFT;
        }

        /* Change tile transition */

        if (numOut < 2)
        {
            boolean[][] aTile = {{false, false}, {false, false}}; /* adjancent Tile */

            aTile[0][0] = (x > 0 && isConveyor(x - 1, y));
            aTile[0][1] = ((x + 1) < width && isConveyor(x + 1, y));
            aTile[1][0] = (y > 0 && isConveyor(x, y - 1));
            aTile[1][1] = ((y + 1) < height && isConveyor(x, y + 1));
            int numAd = (aTile[0][0] ? 1 : 0) + (aTile[0][1] ? 1 : 0) + (aTile[1][0] ? 1 : 0) + (aTile[1][1] ? 1 : 0);

            if (numAd == 2)
            {
                if (aTile[0][0] && aTile[1][0])
                {
                    tiles[y][x].variant = 7;
                    if (outDirection > -1)
                    {
                        tiles[y][x].direction = outDirection;
                    }
                }
                if (aTile[0][0] && aTile[1][1])
                {
                    tiles[y][x].variant = 6;
                    if (outDirection > -1)
                    {
                        tiles[y][x].direction = outDirection;
                    }
                }
                if (aTile[0][1] && aTile[1][0])
                {
                    tiles[y][x].variant = 5;
                    if (outDirection > -1)
                    {
                        tiles[y][x].direction = outDirection;
                    }
                }
                if (aTile[0][1] && aTile[1][1])
                {
                    tiles[y][x].variant = 4;
                    if (outDirection > -1)
                    {
                        tiles[y][x].direction = outDirection;
                    }
                }
            }
            else if (numAd == 3)
            {
                if (!aTile[0][0]) tiles[y][x].variant = 10;
                if (!aTile[0][1]) tiles[y][x].variant = 11;
                if (!aTile[1][0]) tiles[y][x].variant = 9;
                if (!aTile[1][1]) tiles[y][x].variant = 8;
                if (outDirection > -1)
                {
                    tiles[y][x].direction = outDirection;
                }
            }
            else if (numAd == 4)
            {
                tiles[y][x].variant = 12;
                if (outDirection > -1)
                {
                    tiles[y][x].direction = outDirection;
                }
            }
        }
        else
        {
            tiles[y][x].variant = 13;
            tiles[y][x].direction = outDirection;
        }
    }

    public void createTransition() {
        for(int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x)
                if (selected[y][x] == 1) {
                    setTransition(x, y);
                }
        }
    }

    /* clear all end point */
    public void clearEnd() {
        for(int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x)
                if (getType(x, y) == TileType.END) {
                    tiles[y][x] = new Tile(TileType.VOID, tile_textures);
                }
        }
    }

    /* Parse Start tile to create end points */
    public void parseStart(int x, int y, boolean[][] mark)
    {
        if (mark[y][x]) return;

        mark[y][x] = true;

        if (isConveyor(x, y))
        {
            if (tiles[y][x].variant < 13)
            {
                if (tiles[y][x].direction == Direction.FORWARD && (x + 1 < width) )
                    parseStart(x + 1, y, mark);
                if (tiles[y][x].direction == Direction.BACKWARD && (x > 0) )
                    parseStart(x - 1, y, mark);
                if (tiles[y][x].direction == Direction.LEFT && (y + 1 < height) )
                    parseStart(x, y + 1, mark);
                if (tiles[y][x].direction == Direction.RIGHT && (y > 0) )
                parseStart(x, y - 1, mark);
            }
            else
            {
                if (x > 0 && isConveyor(x - 1, y) && tiles[y][x - 1].direction == Direction.BACKWARD)
                    parseStart(x - 1, y, mark);
                if ((x+1) < width && isConveyor(x + 1, y) &&
                    tiles[y][x + 1].direction == Direction.FORWARD)
                    parseStart(x + 1, y, mark);
                if (y > 0 &&  isConveyor(x , y - 1) && tiles[y - 1][x].direction == Direction.RIGHT)
                    parseStart(x, y - 1, mark);
                if ((y+1) < height &&  isConveyor(x , y + 1) && tiles[y + 1][x].direction == Direction.LEFT)
                    parseStart(x, y + 1, mark);
            }
        }
        else
        {
            tiles[y][x] = new Tile(TileType.END, tile_textures);
        }
    }

    public void changeSwitch(int x, int y) {
        int direction = getDirection(x, y);
        if (getType(x, y) == TileType.CONVEYOR && getVariant(x, y) > 13) {
            for (int i = 0; i < 4; i++ ) {
                direction = (direction + 1) % 4;

                if (direction == Direction.BACKWARD && x > 0 && getType(x - 1, y) == TileType.CONVEYOR
                        && getDirection(x - 1, y) == Direction.BACKWARD) {
                    tiles[y][x].direction = direction;
                    break;
                }

                if (direction == Direction.FORWARD && x + 1 < width && getType(x + 1, y) == TileType.CONVEYOR
                        && getDirection(x + 1, y) == Direction.FORWARD) {
                    tiles[y][x].direction = direction;
                    break;
                }

                if (direction == Direction.RIGHT && y > 0 && getType(x, y - 1) == TileType.CONVEYOR
                        && getDirection(x, y - 1) == Direction.RIGHT) {
                    tiles[y][x].direction = direction;
                    break;
                }

                if (direction == Direction.LEFT && y + 1 < height && getType(x, y + 1) == TileType.CONVEYOR
                        && getDirection(x, y + 1) == Direction.LEFT) {
                    tiles[y][x].direction = direction;
                    break;
                }
            }

            tiles[y][x].variant = 14 + getDirection(x, y);
        }

    }

    public void updateStart() {
        boolean[][] mark = new boolean[height][width];

        if (start_x > 0 && isConveyor(start_x - 1, start_y) && getDirection(start_x - 1, start_y) == Direction.BACKWARD)
            parseStart(start_x - 1, start_y, mark);
        if (start_x + 1 < width && isConveyor(start_x + 1, start_y) && getDirection(start_x + 1, start_y) == Direction.FORWARD)
            parseStart(start_x + 1, start_y, mark);
        if (start_y > 0 && isConveyor(start_x, start_y - 1) && getDirection(start_x, start_y - 1) == Direction.RIGHT)
            parseStart(start_x, start_y - 1, mark);
        if (start_y + 1 < height && isConveyor(start_x, start_y + 1) && getDirection(start_x, start_y + 1) == Direction.LEFT)
            parseStart(start_x, start_y + 1, mark);;
    }

}



