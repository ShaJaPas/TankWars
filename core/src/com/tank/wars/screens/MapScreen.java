package com.tank.wars.screens;

import com.annimon.stream.Stream;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tank.wars.MainGame;
import com.tank.wars.player.*;
import com.tank.wars.player.map.Map;
import com.tank.wars.player.map.MapPacket;
import com.tank.wars.tools.graphics.AnimatedImage;
import com.tank.wars.tools.graphics.Controller;
import com.tank.wars.tools.graphics.DrawableText;
import com.tank.wars.tools.graphics.HPBar;
import com.tank.wars.tools.graphics.InternalWindow;
import com.tank.wars.tools.graphics.MapActor;
import com.tank.wars.tools.graphics.TankImage;
import com.tank.wars.tools.network.Client;
import com.tank.wars.tools.network.PacketConstants;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.peer.RakNetPeer;
import com.whirvis.jraknet.protocol.Reliability;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MapScreen implements Screen {
    private final AssetManager manager;
    private final Stage loadingMapStage;
    private final MainGame game;

    private long waitTime = -1;
    private DrawableText battleText;
    private String text = "Loading map";
    private float frameTime = 0;
    private int step = 0;

    public volatile PlayerDataPacket playerDataPacket;
    public long startTime;
    private final Stage stage;
    private boolean loading = true;
    private MapActor mapActor;
    private final TankImage tankImage;
    private final TankImage foeImage;
    private final Controller movementController;
    private final Controller shootController;
    private final MapPacket mapPacket;
    private HPBar myHP;
    private HPBar foeHP;
    private Timer pingTimer;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private final DrawableText reloading;

    public MapScreen(MainGame game, MapPacket mapPacket) {
        this.game = game;
        manager = new AssetManager();
        manager.load("Maps/MapObjects/MapObjects.pack", TextureAtlas.class);
        manager.load("Maps/" + mapPacket.map.name + ".png", Texture.class);
        this.mapPacket = mapPacket;

        loadingMapStage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
        stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
        game.currentStage = loadingMapStage;

        TanksInfo tankInfo = Stream.of(game.tanks).filter(c -> c.id == mapPacket.myTankId).single();
        tankImage = new TankImage(tankInfo, game);
        tankImage.setScale(MainGame.VIRTUAL_WIDTH / MapActor.SCREEN_MAP_WIDTH);
        tankImage.setPosition(MainGame.VIRTUAL_WIDTH / 2 - tankImage.getWidth() / 2, MainGame.VIRTUAL_HEIGHT / 2 - tankImage.getBodyHeight() / 2);

        TanksInfo foeInfo = Stream.of(game.tanks).filter(c -> c.id == mapPacket.opponentTank.id).single();
        foeImage = new TankImage(foeInfo, game);

        Image loadingImage = new Image(game.loading);
        loadingImage.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        loadingMapStage.addActor(loadingImage);

        Sprite bodySprite = new Sprite(game.UIAtlas.findRegion("JoystickBackGround"));
        Sprite centerSprite = new Sprite(game.UIAtlas.findRegion("Joystick"));
        movementController = new Controller(bodySprite, centerSprite);
        movementController.setPosition(new Vector2(70, 70));

        Sprite shootBodySprite = new Sprite(game.UIAtlas.findRegion("JoystickBackGround"));
        Sprite shootCenterSprite = new Sprite(game.UIAtlas.findRegion("ShootButton"));
        shootController = new Controller(shootBodySprite, shootCenterSprite);
        shootController.setPosition(new Vector2(MainGame.VIRTUAL_WIDTH - shootController.getWidth() - 70, 70));
        shootController.addControllerListener(angle -> Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.SHOOT_PACKET})));

        TextureAtlas atlas = new TextureAtlas("LoadingScreen\\IternalLoading.pack");
        Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(1f / 20f, atlas.getRegions());
        AnimatedImage animatedImage = new AnimatedImage(animation, true);
        animatedImage.start();
        animatedImage.setSize(128, 128);
        animatedImage.setPosition(MainGame.VIRTUAL_WIDTH / 2 - animatedImage.getWidth() / 2,MainGame.VIRTUAL_HEIGHT / 2 - animatedImage.getHeight() / 2);
        loadingMapStage.addActor(animatedImage);

        reloading = new DrawableText(game.gameFontBold, decimalFormat.format(tankImage.info.characteristics.reloading));

        foeImage.setVisible(false);
        tankImage.setRotation(0);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    public void setUp(){
        if(Client.handler.containsRequest("explosions")) {
            List<ExplosionPacket> list = (List<ExplosionPacket>) Client.handler.getValue("explosions");
            for (ExplosionPacket packet : list) {
                mapActor.addExplosion(packet.hit ? game.hitExplosion : game.explosion, new Vector2(packet.x, packet.y));
            }
        }
        if(Client.handler.containsRequest("battle_data"))
            playerDataPacket = (PlayerDataPacket) Client.handler.getValue("battle_data");
        if(mapActor != null && playerDataPacket != null) {
            if(!foeImage.isVisible())
                foeImage.setVisible(true);
            mapActor.setBullets(playerDataPacket.myData.bullets);
            reloading.setText(decimalFormat.format(playerDataPacket.myData.coolDown));
            mapActor.setPosition(playerDataPacket.myData.x - MapActor.SCREEN_MAP_WIDTH / 2f, playerDataPacket.myData.y - MapActor.SCREEN_MAP_HEIGHT / 2f);
            tankImage.setBodyRotation(playerDataPacket.myData.bodyRotation);
            tankImage.setGunRotation(playerDataPacket.myData.gunRotation - playerDataPacket.myData.bodyRotation);
            foeImage.setBodyRotation(playerDataPacket.foeData.bodyRotation);
            foeImage.setGunRotation(playerDataPacket.foeData.gunRotation - playerDataPacket.foeData.bodyRotation);
            foeImage.setPosition(playerDataPacket.foeData.x - foeImage.getWidth() / 2 - mapActor.getX(), playerDataPacket.foeData.y - foeImage.getBodyHeight() / 2 - mapActor.getY());

            myHP.setCurrentHealth(playerDataPacket.myData.hp);
            foeHP.setCurrentHealth(playerDataPacket.foeData.hp);

            if (waitTime != - 1 && startTime - playerDataPacket.timeLeft < waitTime) {
                long timeLeft = TimeUnit.MILLISECONDS.toSeconds(waitTime - (startTime - playerDataPacket.timeLeft));
                battleText.setScale(2f);
                battleText.setText(String.valueOf(timeLeft));
                battleText.setPosition(MainGame.VIRTUAL_WIDTH / 2 - battleText.getWidth() / 2,
                        MainGame.VIRTUAL_HEIGHT / 2 + 100 + battleText.getHeight());
            } else {
                long time = playerDataPacket.timeLeft;
                long timeLeftMinutes = TimeUnit.MILLISECONDS.toMinutes(time);
                time -= TimeUnit.MINUTES.toMillis(timeLeftMinutes);
                long timeLeftSeconds = TimeUnit.MILLISECONDS.toSeconds(time);
                battleText.setText((timeLeftMinutes > 9 ? timeLeftMinutes  : "0" + timeLeftMinutes) + ":" + (timeLeftSeconds > 9 ? timeLeftSeconds  : "0" + timeLeftSeconds));
                battleText.setScale(0.6f);
                battleText.setPosition(20, MainGame.VIRTUAL_HEIGHT - 20);
            }
            RotationPacket rotationPacket = new RotationPacket();
            rotationPacket.bodyRotation = (float) movementController.getAngle();
            rotationPacket.inMove = movementController.isOutside();
            rotationPacket.gunRotation = (float) shootController.getAngle();
            byte[] data = Client.handler.secureData.serialize(rotationPacket);
            if(Client.client.isConnected())
                Client.client.sendMessage(Reliability.UNRELIABLE_SEQUENCED, Client.newPacket(addId(data, PacketConstants.ROTATION_PACKET)));
        }
    }

    private byte[] addId(byte[] arr, byte id){
        byte[] result = new byte[arr.length + 1];
        System.arraycopy(arr, 0, result, 0, arr.length);
        result[result.length - 1] = id;
        return result;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(loading) {
            loadingMapStage.act(delta);
            loadingMapStage.draw();

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
            game.batch.begin();
            game.gameFont.font.draw(game.batch, text,
                    640 - game.gameFont.getWidth(text.replace(".", ""), 1) / 2, 360 + game.gameFont.getHeight(text.replace(".", ""), 1) / 2);
            game.batch.end();

            if (manager.update()) {
                Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.MAP_READY}));
                TextureAtlas mapObjects = manager.get("Maps/MapObjects/MapObjects.pack", TextureAtlas.class);
                Texture mapTexture = manager.get("Maps/" + mapPacket.map.name + ".png", Texture.class);
                loading = false;
                loadingMapStage.dispose();
                mapActor = new MapActor(mapTexture, mapPacket.map, mapObjects, game.bullets, tankImage, foeImage);
                battleText = new DrawableText(game.gameFontBold, "Waiting for opponent");
                battleText.setPosition(MainGame.VIRTUAL_WIDTH / 2 - game.gameFontBold.getWidth("Waiting for opponent", 1) / 2,
                        MainGame.VIRTUAL_HEIGHT / 2 + 100 + game.gameFontBold.getHeight("Waiting for opponent", 1));
                mapActor.setPosition(mapPacket.dataPacket.myData.x - MapActor.SCREEN_MAP_WIDTH / 2f, mapPacket.dataPacket.myData.y - MapActor.SCREEN_MAP_HEIGHT / 2f);
                myHP = new HPBar(game, new Image(game.UIAtlas.findRegion("HPBar")), new Image(game.UIAtlas.findRegion("HPBarIndicator")), mapPacket.dataPacket.myData.hp);
                myHP.setSize(300, 60);
                myHP.setPosition(MainGame.VIRTUAL_WIDTH / 2 - myHP.getWidth() / 2, 40);
                DrawableText info = new DrawableText(game.gameFontBold, mapPacket.opponent.nickName + " (" + foeImage.info.characteristics.name + " LVL" + mapPacket.opponentTank.level + ")");
                foeHP = new HPBar(game, new Image(game.UIAtlas.findRegion("HPBar")), new Image(game.UIAtlas.findRegion("HPBarIndicator")), mapPacket.dataPacket.foeData.hp);
                foeHP.setSize(200, 40);
                info.setScale(Math.min(1, foeHP.getWidth() / game.gameFontBold.getWidth(info.getText().toString(), 1)));
                info.setPosition(20, MainGame.VIRTUAL_HEIGHT - 70 + info.getHeight());
                foeHP.setPosition(20, info.getY() - 25 - foeHP.getHeight());
                stage.addActor(mapActor);
                stage.addActor(movementController);
                stage.addActor(shootController);
                stage.addActor(battleText);
                mapActor.lateInit();
                stage.addActor(info);
                stage.addActor(myHP);
                stage.addActor(foeHP);

                reloading.setScale(1.5f);
                reloading.setPosition(shootController.getX() - reloading.getWidth() - 90, myHP.getY() + myHP.getHeight() / 2 + reloading.getHeight() / 2);
                stage.addActor(reloading);

                mapActor.setPosition(mapPacket.dataPacket.myData.x - MapActor.SCREEN_MAP_WIDTH / 2f, mapPacket.dataPacket.myData.y - MapActor.SCREEN_MAP_HEIGHT / 2f);
                tankImage.setBodyRotation(mapPacket.dataPacket.myData.bodyRotation);
                tankImage.setGunRotation(mapPacket.dataPacket.myData.gunRotation - mapPacket.dataPacket.myData.bodyRotation);
                foeImage.setBodyRotation(mapPacket.dataPacket.foeData.bodyRotation);
                foeImage.setGunRotation(mapPacket.dataPacket.foeData.gunRotation - mapPacket.dataPacket.foeData.bodyRotation);
                foeImage.setPosition(mapPacket.dataPacket.foeData.x - foeImage.getWidth() / 2 - mapActor.getX(), mapPacket.dataPacket.foeData.y - foeImage.getBodyHeight() / 2 - mapActor.getY());

                //region PingInfo
                final Image[] bars = new Image[3];
                for (int i = 0; i < 3; i++) {
                    bars[i] = new Image(game.UIAtlas.findRegion("VolumeBarEdge"));
                    bars[i].setSize(5, 10 + 5 * i);
                    bars[i].setPosition(1130 + 2 * (i - 1) + 5 * i, 678);
                    stage.addActor(bars[i]);
                }

                DrawableText pingText = new DrawableText(game.gameFontBold, "");
                pingText.setPosition(1162, 698);
                pingText.setScale(20 / game.gameFontBold.getHeight("1"));
                stage.addActor(pingText);

                DrawableText avgPing = new DrawableText(game.gameFontBold, "");
                avgPing.setPosition(1130, pingText.getY() - 20 - pingText.getHeight());
                avgPing.setScale(20 / game.gameFontBold.getHeight("1"));
                stage.addActor(avgPing);

                pingTimer = new Timer();
                pingTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(Client.client.getServer() != null) {
                            long ping = Client.client.getServer().getLastLatency();
                            if (ping > 0) {
                                pingText.setText(ping + " ms");
                                avgPing.setText("Avg: " + Client.client.getServer().getLatency() + " ms");
                                bars[2].setVisible(!(ping >= 150));
                                bars[1].setVisible(!(ping >= 300));
                            }
                        }
                    }
                }, 0, RakNetPeer.PING_SEND_INTERVAL);

                //endregion
            }
        } else {
            setUp();
            stage.draw();
            stage.act(delta);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    public void battleEnds(BattleResults results){
        pingTimer.cancel();
        reloading.setVisible(false);
        Image closeIcon = new Image(game.UIAtlas.findRegion("Close"));
        Image windowBg = new Image(game.UIAtlas.findRegion("FeaturedItem2"));
        windowBg.setSize(windowBg.getWidth() * 1.2f, windowBg.getHeight() * 1.2f);
        InternalWindow resultsWindow = new InternalWindow();
        resultsWindow.init(closeIcon, windowBg);
        resultsWindow.setPosition(MainGame.VIRTUAL_WIDTH / 2 - resultsWindow.getWidth() / 2, MainGame.VIRTUAL_HEIGHT / 2 - resultsWindow.getHeight() / 2);
        resultsWindow.addOnClose(() -> Gdx.app.postRunnable(() -> {
            game.menuScreen = new MenuScreen(game);
            game.setScreen(game.menuScreen);
        }));
        DrawableText result = new DrawableText(game.gameFont, results.results == BattleResultsEnum.DRAW ? "DRAW" : (results.winner ? "VICTORY" : "DEFEAT"));
        result.setScale(2);
        result.setPosition(resultsWindow.getWidth() / 2 - result.getWidth() / 2, resultsWindow.getHeight() - 40);
        resultsWindow.addActor(result);
        String[] battleResultsText = new String[4];
        DecimalFormat format = new DecimalFormat("#.#");
        battleResultsText[0] = "ACCURACY - " + format.format(results.accuracy * 100) + "%";
        battleResultsText[1] = "DAMAGE DEALT - " + results.damageDealt;
        battleResultsText[2] = "DAMAGE TAKEN - " + results.damageTaken;
        battleResultsText[3] = "EFFICIENCY - " +  format.format((int) (results.getEfficiency() * 100)) + "%";
        for (int i = 0; i < battleResultsText.length; i++) {
            DrawableText text = new DrawableText(game.gameFontBold, battleResultsText[i]);
            text.setScale(Math.min(resultsWindow.getWidth() / 2.3f / game.gameFontBold.getWidth(text.getText().toString(), 1),
                    30 / game.gameFontBold.getHeight(text.getText().toString(), 1)));
            text.setPosition(50, result.getY() - result.getHeight() - 25 - i * (25 + text.getHeight()));
            resultsWindow.addActor(text);
        }
        TextButton.TextButtonStyle okButtonStyle = new TextButton.TextButtonStyle();
        okButtonStyle.font = game.gameFont.font;
        okButtonStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
        okButtonStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
        okButtonStyle.pressedOffsetX = -2;
        okButtonStyle.pressedOffsetY = -2;
        TextButton okButton = new TextButton("OK", okButtonStyle);
        okButton.setTransform(true);
        okButton.setScale(0.7f);
        okButton.setPosition(resultsWindow.getWidth() / 4 - okButton.getWidth() * okButton.getScaleX() / 2, 50);
        okButton.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(okButton.getX(), okButton.getY(),
                        okButton.getWidth() * okButton.getScaleX(), okButton.getHeight() * okButton.getScaleY());
                if(rectangle.contains(event.toCoordinates(resultsWindow, new Vector2(resultsWindow.getX(), resultsWindow.getY())).x, event.toCoordinates(resultsWindow, new Vector2(resultsWindow.getX(), resultsWindow.getY())).y)) {
                    Gdx.app.postRunnable(() -> {
                        game.menuScreen = new MenuScreen(game);
                        game.setScreen(game.menuScreen);
                    });
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
        resultsWindow.addActor(okButton);
        Image trophiesImage = new Image(game.UIAtlas.findRegion("Trophie"));
        trophiesImage.setSize(90, 90);
        trophiesImage.setPosition(resultsWindow.getWidth() * 0.74f - trophiesImage.getWidth() / 2, result.getY()  - result.getHeight() - 30 - trophiesImage.getHeight());
        resultsWindow.addActor(trophiesImage);
        DrawableText trophiesCount = new DrawableText(game.gameFontBold, results.trophies > 0 ? "+" + results.trophies : String.valueOf(results.trophies));
        trophiesCount.setPosition(trophiesImage.getX() + trophiesImage.getWidth() / 2 - trophiesCount.getWidth() / 2, trophiesImage.getY() - 10);
        resultsWindow.addActor(trophiesCount);
        Image coins = new Image(game.UIAtlas.findRegion("Coins"));
        coins.setSize(60, 60);
        coins.setPosition(resultsWindow.getWidth() * 0.6f, trophiesCount.getY() - trophiesCount.getHeight() - 30 - coins.getHeight());
        DrawableText coinsCount = new DrawableText(game.gameFontBold, String.valueOf(results.coins));
        coinsCount.setScale(coins.getHeight() * 0.6f / game.gameFontBold.getHeight());
        coinsCount.setPosition(coins.getX() + coins.getWidth() + 10, coins.getY() + coins.getHeight() / 2 + coinsCount.getHeight() / 2);
        resultsWindow.addActor(coinsCount);
        resultsWindow.addActor(coins);
        DrawableText xp = new DrawableText(game.gameFontBold, "+" + results.xp + "xp");
        xp.setScale(coinsCount.getScale());
        xp.setColor(new Color(0.26274f, 0.48235f, 0.11764f, 1));
        xp.setPosition(resultsWindow.getWidth() * 0.6f, coins.getY() - 20);
        resultsWindow.addActor(xp);
        stage.addActor(resultsWindow);
        shootController.setVisible(false);
        movementController.setVisible(false);
    }

    public void battleStarted(long waitTime1){
        this.waitTime = waitTime1;
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
        manager.dispose();
        stage.dispose();
    }
}
