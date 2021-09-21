package com.tank.wars.screens;

import com.alibaba.fastjson.JSON;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tank.wars.MainGame;
import com.tank.wars.player.TanksInfo;
import com.tank.wars.tools.data.PreferenceManager;
import com.tank.wars.tools.encryption.SecureData;
import com.tank.wars.tools.graphics.AnimatedImage;
import com.tank.wars.tools.graphics.Toast;
import com.tank.wars.tools.network.Client;
import com.tank.wars.tools.network.PacketConstants;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.RakNetException;
import com.whirvis.jraknet.client.peer.ServerOfflineException;
import com.whirvis.jraknet.protocol.Reliability;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

public class GameLoadingScreen implements Screen {

    private MainGame game;
    private TextureAtlas atlas;
    private Stage stage;
    private int step = 0;
    private String text;
    private float frameTime = 0;
    private boolean conn = false;
    public Toast.ToastFactory toastFactory;
    public Toast unreachable;

    public GameLoadingScreen(MainGame game){
        this.game = game;
    }
    
    public void init(){
        stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
        game.currentStage = stage;

        Image loadingImage = new Image(game.loading);
        loadingImage.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        stage.addActor(loadingImage);

        text = "Loading textures";

        atlas = new TextureAtlas("LoadingScreen\\IternalLoading.pack");
        Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(1f / 20f, atlas.getRegions());
        AnimatedImage animatedImage = new AnimatedImage(animation, true);
        animatedImage.start();
        animatedImage.setSize(128, 128);
        animatedImage.setPosition(MainGame.VIRTUAL_WIDTH / 2 - animatedImage.getWidth() / 2,MainGame.VIRTUAL_HEIGHT / 2 - animatedImage.getHeight() / 2);

        toastFactory = new Toast.ToastFactory.Builder()
                .font(game.gameFont.generateFont(24, Color.WHITE))
                .fadingDuration(0.8f)
                .positionY(60)
                .margin(16)
                .build();

        stage.addActor(animatedImage);
    }

    @Override
    public void show() {
        conn = false;
        new Thread(() ->{
            FileHandle[] handles = Gdx.files.internal("Player/Tanks").list(".json");
            if(game.tanks.size() != handles.length){
                for (FileHandle handle : handles) {
                    game.tanks.add(JSON.parseObject(handle.readString("UTF8"), TanksInfo.class));
                }
            }
        }).start();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        frameTime += delta;
        if (frameTime >= 0.4f) {
            if (step == 3) {
                step = 0;
                text = String.copyValueOf(text.toCharArray(), 0, text.length() - 3);
            } else {
                step++;
                text += ".";
            }
            frameTime = 0;
        }
        if (game.manager.update()) {
            if (!conn) {
                Client.init();
                new Thread(() -> {
                    try {
                        Client.handler.secureData = new SecureData();
                        game.preferenceManager = new PreferenceManager(Client.handler.secureData);
                        while (!Client.client.isLoggedIn())
                            Client.connect("192.168.0.145", 900);
                        Client.handler.game = game;
                    } catch (RakNetException | UnknownHostException | NoSuchPaddingException | NoSuchAlgorithmException e) {
                        if (e.getClass().equals(ServerOfflineException.class)) {
                            Gdx.app.postRunnable(() -> unreachable = toastFactory.create("Server seems to be offline,\n" +
                                    "but maybe there are problems with your internet connection", Toast.Length.VERY_LONG));
                        }
                    }
                }).start();
                conn = true;
                text = "Connecting to server";
                step = 0;
                frameTime = 0;
                game.gameFont.setText(text);
            }
        }
        game.batch.begin();
        game.gameFont.font.draw(game.batch, text,
                640 - game.gameFont.getWidth(text.replace(".", "")) / 2, 360 + game.gameFont.getHeight(text.replace(".", "")) / 2);
        game.batch.end();
        if (Client.client != null && Client.client.isLoggedIn()) {
            try {
                game.UIAtlas = game.manager.get("UI/UI.pack", TextureAtlas.class);
                Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.AES_KEY_PACKET}));
                game.ranksAtlas = game.manager.get("Player/Ranks/Ranks.pack", TextureAtlas.class);
                game.commonChestAtlas = game.manager.get("Player/Chest/ChestAnimationCommon.pack", TextureAtlas.class);
                game.bullets = game.manager.get("Player/Tanks/Bullets/Bullets.pack", TextureAtlas.class);
                game.guns = game.manager.get("Player/Tanks/TankGuns/Guns.pack", TextureAtlas.class);
                game.bodies = game.manager.get("Player/Tanks/TankBodies/Bodies.pack", TextureAtlas.class);
                game.sandClock = game.manager.get("UI/Sandclock.pack", TextureAtlas.class);
                game.explosion = game.manager.get("Player/Explosions/Explosion.pack", TextureAtlas.class);
                game.hitExplosion = game.manager.get("Player/Explosions/ExplosionHit.pack", TextureAtlas.class);
                if (Client.handler.keySet) {
                    game.loginScreen = new LoginScreen(game);
                    game.setScreen(game.loginScreen);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (unreachable != null)
            unreachable.render(delta);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        atlas.dispose();
        stage.dispose();
    }
}
