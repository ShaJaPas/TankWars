package com.tank.wars.player;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;

import java.io.Serializable;
import java.util.Date;

public class Player implements Serializable {

    @Index(0)
    public Date registrationDate;

    @Index(1)
    public Date lastOnlineDate;

    @Index(2)
    public String nickName;

    @Index(3)
    public int battleCount;

    @Index(4)
    public int victoriesCount;

    @Index(5)
    public Tank[] tanks;

    @Index(6)
    public int xp;

    @Index(7)
    public int rankLevel;

    @Index(8)
    public int coins;

    @Index(9)
    public int diamonds;

    @Index(10)
    public float accuracy;

    @Index(11)
    public int damageDealt;

    @Index(12)
    public int damageTaken;

    @Index(13)
    public int trophies;

    public Player(){

    }

    public float getEfficiency(){
        float res = (float) victoriesCount / battleCount * (accuracy + 0.5f) * ((float) damageDealt / damageTaken);
        return Float.isNaN(res) ? 0 : res;
    }
}

