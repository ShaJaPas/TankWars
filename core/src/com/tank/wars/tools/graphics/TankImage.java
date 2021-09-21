package com.tank.wars.tools.graphics;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.tank.wars.MainGame;
import com.tank.wars.player.TanksInfo;

public class TankImage extends Group {
    public TanksInfo info;
    private Image body, gun;

    public TankImage(TanksInfo info, MainGame game){
        this.info = info;
        body = new Image(game.bodies.findRegion(info.graphicsInfo.tankBodyName));
        gun = new Image(game.guns.findRegion(info.graphicsInfo.tankGunName));
        setSize(body.getWidth(), body.getHeight());
        setOrigin(body.getWidth() / 2, body.getHeight() / 2);
        body.setOrigin(body.getWidth() / 2, body.getHeight() / 2);
        gun.setPosition(info.graphicsInfo.gunX, body.getHeight() - info.graphicsInfo.gunY - gun.getHeight());
        gun.setOrigin(info.graphicsInfo.gunOriginX, gun.getHeight() - info.graphicsInfo.GunOriginY);
        rotateBy(180);
        addActor(body);
        addActor(gun);
    }

    @Override
    public float getHeight() {
        return (body.getHeight() + (gun.getY() < 0 ? -gun.getY() : 0)) * body.getScaleY();
    }

    public float getBodyHeight(){
        return body.getHeight();
    }

    public void setBodyRotation(float angle){
        setRotation(angle);
    }

    public void setGunRotation(float angle){
        gun.setRotation(angle);
    }
}
