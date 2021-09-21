package com.tank.wars.screens;

import com.annimon.stream.Stream;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tank.wars.MainGame;
import com.tank.wars.player.Tank;
import com.tank.wars.player.TanksInfo;
import com.tank.wars.tools.graphics.AnimatedImage;
import com.tank.wars.tools.graphics.DrawableText;
import com.tank.wars.tools.graphics.TankImage;
import com.tank.wars.tools.network.Client;
import com.tank.wars.tools.network.PacketConstants;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.protocol.Reliability;

public class GameQueueScreen implements Screen {

    private MainGame game;
    private Stage stage;
    private Color bgColor;

    public GameQueueScreen(MainGame game, Tank chosenTank){
        this.game = game;

        bgColor = Color.valueOf("FFEBA6");

        stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
        game.currentStage = stage;


        TextButton.TextButtonStyle exitQueueStyle = new TextButton.TextButtonStyle();
        exitQueueStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
        exitQueueStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
        exitQueueStyle.font = game.gameFont.font;
        exitQueueStyle.pressedOffsetX = 2;
        exitQueueStyle.pressedOffsetY = 2;
        TextButton exitQueue = new TextButton("Cancel", exitQueueStyle);
        exitQueue.setTransform(true);
        exitQueue.setScale(0.6f);
        exitQueue.setPosition(640 - exitQueue.getWidth() * exitQueue.getScaleX() / 2, 60 - exitQueue.getHeight() * exitQueue.getScaleY() / 2);
        exitQueue.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(exitQueue.getX(), exitQueue.getY(),
                        exitQueue.getWidth() * exitQueue.getScaleX(), exitQueue.getHeight() * exitQueue.getScaleY());
                if (rectangle.contains(event.getStageX(), event.getStageY())) {
                    Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.EXIT_BALANCER}));
                    Gdx.app.postRunnable(() -> {
                        game.setScreen(game.menuScreen);
                    });
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });

        TanksInfo info = Stream.of(game.tanks).filter(c -> c.id == chosenTank.id).single();
        TankImage tankImage = new TankImage(info, game);
        tankImage.setScale(200 / tankImage.getHeight());
        tankImage.setPosition(stage.getWidth() / 2 - tankImage.getWidth() / 2,
                stage.getHeight() / 2 - 80 - tankImage.getHeight() / 2);

        DrawableText tankName = new DrawableText(game.gameFontBold, info.characteristics.name + " " + chosenTank.level + " LVL");
        //tankName.setColor(Color.BLACK);
        tankName.setPosition(stage.getWidth() / 2 - tankName.getWidth() / 2,
                tankImage.getY() + tankImage.getHeight() * 2  + tankName.getHeight() - 10);


        DrawableText text = new DrawableText(game.gameFontBold, "Searching for opponents");
        //tankName.setColor(Color.BLACK);
        text.setPosition(stage.getWidth() / 2 - text.getWidth() / 2,
                stage.getHeight() - 20);

        Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(1f / 20f, game.sandClock.getRegions());
        AnimatedImage sandClock = new AnimatedImage(animation, true);
        sandClock.setPosition(stage.getWidth() / 2 - sandClock.getWidth() / 2, tankName.getY() + 5);
        sandClock.start();

        stage.addActor(text);
        stage.addActor(tankName);
        stage.addActor(tankImage);
        stage.addActor(exitQueue);
        stage.addActor(sandClock);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
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

    }
}
