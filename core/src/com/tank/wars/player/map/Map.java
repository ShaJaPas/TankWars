package com.tank.wars.player.map;

import org.msgpack.annotation.Index;

public class Map {
    @Index(0)
    public String name;
    @Index(1)
    public int width;
    @Index(2)
    public int height;
    @Index(3)
    public MapObjects[] objects;

    public Map(){

    }
}
