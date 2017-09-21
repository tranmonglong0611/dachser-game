package com.dachser.game;

/**
 * Created by taprosoft on 9/12/15.
 */
public enum TileType {
    VOID, GRASS, CONVEYOR, TREE, WATER, START, END;

    static final int size = values().length;

    static public TileType string_toType(String type) {
       if (type.equals("Blank")) return VOID;
        else if (type.equals("Grass")) return  GRASS;
        else if (type.equals("Water")) return  WATER;
        else if (type.equals("Tree"))  return  TREE;
        else if (type.equals("Conveyor"))  return  CONVEYOR;
        else if (type.equals("Start point"))  return  START;
       return VOID;
    }

    static public String type_toString(TileType type) {
        switch (type) {
            case VOID: return "Blank";
            case GRASS: return "Grass";
            case CONVEYOR: return  "Conveyor";
            case TREE: return "Tree";
            case WATER: return "Water";
            case START: return "Start point";
        }
        return "Blank";
    }
}