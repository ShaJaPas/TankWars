package com.tank.wars.player.chest;

import com.tank.wars.player.Tank;

import org.msgpack.annotation.Index;

public class Chest {
    @Index(0)
    public ChestName name;
    @Index(1)
    public Tank[] loot;
    @Index(2)
    public int coins;
    @Index(3)
    public int diamonds;

    public Chest(){

    }
}
