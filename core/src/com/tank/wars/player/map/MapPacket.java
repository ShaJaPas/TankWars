package com.tank.wars.player.map;

import com.tank.wars.player.Player;
import com.tank.wars.player.PlayerDataPacket;
import com.tank.wars.player.Tank;

import org.msgpack.annotation.Index;

public class MapPacket {
    @Index(0)
    public Map map;
    @Index(1)
    public Player opponent;
    @Index(2)
    public int myTankId;
    @Index(3)
    public Tank opponentTank;
    @Index(4)
    public PlayerDataPacket dataPacket;

    public MapPacket(){

    }
}