package com.tank.wars.tools.graphics;

import com.annimon.stream.Stream;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.tank.wars.MainGame;
import com.tank.wars.player.GamePlayerData;
import com.tank.wars.player.map.Map;
import com.tank.wars.player.map.MapConstants;
import com.tank.wars.player.map.MapObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapActor extends Actor {
    private final Sprite mapImage;
    private final Texture mapTexture;
    private final List<Sprite> objects = new ArrayList<>();
    private final List<Sprite> bushObjects = new ArrayList<>();
    public static final int SCREEN_MAP_WIDTH = 1280;
    public static final int SCREEN_MAP_HEIGHT = 720;
    private final Map map;
    private final TextureAtlas mapObjects;
    private final TankImage myImage;
    private final TankImage foeImage;
    private Rectangle region;
    private final List<Sprite> bulletsImages = new ArrayList<>();
    private final List<AnimatedImage> explosionsToDraw = new ArrayList<>();
    private final TextureAtlas bullets;

    public MapActor(Texture mapTexture, Map map, TextureAtlas mapObjects, TextureAtlas bullets, TankImage myImage, TankImage foeImage){
        this.foeImage = foeImage;
        this.myImage = myImage;
        this.bullets = bullets;
        mapImage = new Sprite(new TextureRegion(mapTexture, 0, 0, SCREEN_MAP_WIDTH, SCREEN_MAP_HEIGHT));
        this.mapTexture = mapTexture;
        this.map = map;
        this.mapObjects = mapObjects;
        setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
    }

    public void lateInit(){
        for (MapObjects object : map.objects) {
            Sprite image = new Sprite(mapObjects.findRegion(String.valueOf(object.id)));
            image.setPosition(object.x, object.y);
            image.setSize(image.getWidth() * object.scale, image.getHeight() * object.scale);
            image.setOriginCenter();
            image.setRotation(object.rotation);
            if(object.id != MapConstants.SMALL_BUSH && object.id != MapConstants.LARGE_BUSH)
                objects.add(image);
            else bushObjects.add(image);
        }
    }

    @Override
    public void setSize(float width, float height) {
        mapImage.setSize(width, height);
        super.setSize(width, height);
    }

    @Override
    public void setPosition(float x, float y) {
        region = new Rectangle(x, y, x + SCREEN_MAP_WIDTH > mapTexture.getWidth() ?  mapTexture.getWidth() - x : SCREEN_MAP_WIDTH,(y + SCREEN_MAP_HEIGHT > mapTexture.getHeight() ?  mapTexture.getHeight() - y : SCREEN_MAP_HEIGHT));
        mapImage.setPosition(x < 0 ? -x : 0, y < 0 ? -y : 0);
        mapImage.setRegion((int)(x < 0 ? 0 : x), (int) (y < 0 ? 0 : y),
                (int) (x + SCREEN_MAP_WIDTH > mapTexture.getWidth() ?  mapTexture.getWidth() - x : SCREEN_MAP_WIDTH), (int) (y + SCREEN_MAP_HEIGHT > mapTexture.getHeight() ?  mapTexture.getHeight() - y : SCREEN_MAP_HEIGHT));
        mapImage.setSize(mapImage.getRegionWidth() * (MainGame.VIRTUAL_WIDTH / SCREEN_MAP_WIDTH), mapImage.getRegionHeight() * (MainGame.VIRTUAL_HEIGHT / SCREEN_MAP_HEIGHT));
        mapImage.setFlip(false, true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        mapImage.draw(batch, parentAlpha);
        for (Sprite object : objects) {
            float x = object.getX();
            float y = object.getY();
            object.setPosition(mapImage.getX() + x - mapImage.getRegionX(), y - region.y);
            object.draw(batch, parentAlpha);
            object.setPosition(x, y);
        }
        foeImage.draw(batch, parentAlpha);
        myImage.draw(batch, parentAlpha);

        for (Sprite object : bulletsImages) {
            object.draw(batch, parentAlpha);
        }

        for(Sprite object : bushObjects){
            float x = object.getX();
            float y = object.getY();
            object.setPosition(mapImage.getX() + x - mapImage.getRegionX(), y - region.y);
            object.draw(batch, parentAlpha);
            object.setPosition(x, y);
        }
        for (AnimatedImage object : explosionsToDraw) {
            float x = object.getX();
            float y = object.getY();
            object.setPosition(x - this.getX(), y - this.getY());
            object.draw(batch, parentAlpha);
            object.setPosition(x, y);
        }
    }

    public void setBullets(GamePlayerData.Bullet[] arr){
        bulletsImages.clear();
        for (GamePlayerData.Bullet bullet : arr) {
            Sprite image = new Sprite(bullets.findRegion(String.valueOf(bullet.name)));
            image.setPosition(bullet.x - image.getWidth() / 2 - this.getX(), bullet.y - image.getHeight() / 2 - this.getY());
            image.setOriginCenter();
            image.setRotation(bullet.rotation);
            bulletsImages.add(image);
        }
    }

    public void addExplosion(TextureAtlas explosionAtlas, Vector2 pos){
        Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(1f / 13f, explosionAtlas.getRegions());
        AnimatedImage image = new AnimatedImage(animation, false);
        image.setPosition(pos.x - image.getWidth() / 2, pos.y - image.getHeight() / 2);
        image.addListener(() -> Stream.of(explosionsToDraw).filter(c -> c == image).single().setVisible(false));
        image.start();
        explosionsToDraw.add(image);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        for (AnimatedImage explosion : explosionsToDraw) {
            explosion.act(delta);
        }
    }

    @Override
    public float getX() {
        return super.getX() + region.x;
    }

    @Override
    public float getY() {
        return super.getY() + region.y;
    }
}
