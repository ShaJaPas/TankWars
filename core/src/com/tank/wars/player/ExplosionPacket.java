package com.tank.wars.player;

import org.msgpack.annotation.Index;

public class ExplosionPacket {
    @Index(0)
    public float x;
    @Index(1)
    public float y;
    @Index(2)
    public boolean hit;
}

