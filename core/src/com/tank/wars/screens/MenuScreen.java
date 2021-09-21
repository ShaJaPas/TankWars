package com.tank.wars.screens;

import com.annimon.stream.Stream;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tank.wars.MainGame;
import com.tank.wars.player.Player;
import com.tank.wars.player.SignInData;
import com.tank.wars.player.Tank;
import com.tank.wars.player.TanksInfo;
import com.tank.wars.player.chest.Chest;
import com.tank.wars.player.chest.ChestName;
import com.tank.wars.player.chest.DailyItem;
import com.tank.wars.tools.graphics.DrawableText;
import com.tank.wars.tools.graphics.InternalWindow;
import com.tank.wars.tools.graphics.TankImage;
import com.tank.wars.tools.network.ChannelsConstants;
import com.tank.wars.tools.network.Client;
import com.tank.wars.tools.network.PacketConstants;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.peer.RakNetPeer;
import com.whirvis.jraknet.protocol.Reliability;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class MenuScreen implements Screen {
    public static class AdvancedCell {
        public boolean visible;
        public Cell cell;

        public AdvancedCell(Cell cell, boolean visible)
        {
            this.cell = cell;
            this.visible = visible;
        }
    }
    public MainGame game;
    private Stage stage;
    private Timer friendsTimer;
    private Array<AdvancedCell> friendsTableCells;
    private boolean friendsSearching;

    public InternalWindow createWindow(Group page, DailyItem item){
        Image closeIcon = new Image(game.UIAtlas.findRegion("Close"));
        Image windowBg = new Image(game.UIAtlas.findRegion("FeaturedItem2"));
        windowBg.setSize(windowBg.getWidth() * 1.2f, windowBg.getHeight() * 1.2f);
        InternalWindow rareDailyWindow = new InternalWindow();
        rareDailyWindow.init(closeIcon, windowBg);
        rareDailyWindow.setPosition(page.getWidth() / 2 - rareDailyWindow.getWidth() / 2, page.getHeight() / 2 - rareDailyWindow.getHeight() / 2);
        rareDailyWindow.setVisible(false);
        final TankImage[] rareTank = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == item.tankId).single(), game)};
        Image rareTankNameBg = new Image(game.UIAtlas.findRegion("PlayerNameBackground"));
        rareTankNameBg.setPosition(60, rareDailyWindow.getHeight() - 20 - rareTankNameBg.getHeight());
        rareTankNameBg.setHeight(30);
        DrawableText rareTankNameText = new DrawableText(game.gameFontBold, rareTank[0].info.characteristics.name);
        rareTankNameText.setScale(15 / game.gameFontBold.getHeight("1", 1));
        rareTankNameText.setPosition(rareTankNameBg.getX() + 5, rareTankNameBg.getY() + rareTankNameBg.getHeight() / 2 + rareTankNameText.getHeight() / 2);
        rareTankNameBg.setWidth(rareTankNameText.getWidth() * 1.3f);
        rareDailyWindow.addActor(rareTankNameBg);
        rareDailyWindow.addActor(rareTankNameText);
        String[] rareTankCharacteristics = new String[8];
        DecimalFormat format = new DecimalFormat("#.##");
        rareTankCharacteristics[0] = "RARITY - " + rareTank[0].info.characteristics.rarity.name();
        rareTankCharacteristics[1] = "HP - " + format.format(rareTank[0].info.characteristics.hp);
        rareTankCharacteristics[2] = "DAMAGE - " + format.format(rareTank[0].info.characteristics.damage);
        rareTankCharacteristics[3] = "RELOADING - " + format.format(rareTank[0].info.characteristics.reloading);
        rareTankCharacteristics[4] = "VELOCITY - " + format.format(rareTank[0].info.characteristics.velocity);
        rareTankCharacteristics[5] = "BULLET SPEED - " + format.format(rareTank[0].info.characteristics.bulletSpeed);
        rareTankCharacteristics[6] = "BODY ROTATE SPEED - " + format.format(rareTank[0].info.characteristics.bodyRotateDegrees);
        rareTankCharacteristics[7] = "GUN ROTATE SPEED - " + format.format(rareTank[0].info.characteristics.gunRotateDegrees);
        for (int i = 0; i < rareTankCharacteristics.length; i++) {
            DrawableText text = new DrawableText(game.gameFontBold, rareTankCharacteristics[i]);
            text.setScale(Math.min(rareDailyWindow.getWidth() / 2.5f / game.gameFontBold.getWidth(text.getText().toString(), 1),
                    30 / game.gameFontBold.getHeight(text.getText().toString(), 1)));
            text.setPosition(rareTankNameBg.getX(), rareTankNameBg.getY() - 20 - i * 40);
            rareDailyWindow.addActor(text);
        }
        final TankImage[] rareTankWindow = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == item.tankId).single(), game)};
        rareTankWindow[0].setScale(180 / rareTankWindow[0].getHeight());
        rareTankWindow[0].setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareTankWindow[0].getWidth() / 2,
                rareDailyWindow.getHeight() / 2 + rareDailyWindow.getHeight() / 4 - rareTankWindow[0].getHeight());
        rareDailyWindow.addActor(rareTankWindow[0]);

        DrawableText rareTankCount = new DrawableText(game.gameFontBold, item.count != 0 ? "x" + item.count : "New vehicle!");
        rareTankCount.setScale(30 / game.gameFontBold.getHeight(rareTankCount.getText().toString(), 1));
        rareTankCount.setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareTankCount.getWidth() / 2,
                rareDailyWindow.getY() + 170);
        rareDailyWindow.addActor(rareTankCount);

        TextButton.TextButtonStyle rareTankBuyStyle = new TextButton.TextButtonStyle();
        rareTankBuyStyle.font = game.gameFont.font;
        rareTankBuyStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
        rareTankBuyStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
        rareTankBuyStyle.pressedOffsetX = -2;
        rareTankBuyStyle.pressedOffsetY = -2;
        TextButton rareTankBuy = new TextButton("Buy", rareTankBuyStyle);
        rareTankBuy.setTransform(true);
        rareTankBuy.setScale(0.7f);
        rareTankBuy.setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareTankBuy.getWidth() * rareTankBuy.getScaleX() / 2,
                rareDailyWindow.getY() + 10);
        rareTankBuy.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(rareTankBuy.getX(), rareTankBuy.getY(),
                        rareTankBuy.getWidth() * rareTankBuy.getScaleX(), rareTankBuy.getHeight() * rareTankBuy.getScaleY());
                if(rectangle.contains(event.toCoordinates(rareDailyWindow, new Vector2(rareDailyWindow.getX(), rareDailyWindow.getY())).x, event.toCoordinates(rareDailyWindow, new Vector2(rareDailyWindow.getX(), rareDailyWindow.getY())).y)) {
                    Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.serialize(item), PacketConstants.BUY_DAILY_ITEM)));
                    boolean bought = (Boolean) Client.handler.getValue("daily_item_buy");
                    if(bought) {
                        Gdx.app.postRunnable(() -> {
                            game.menuScreen = new MenuScreen(game, 2);
                            game.setScreen(game.menuScreen);
                        });
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
        rareDailyWindow.addActor(rareTankBuy);

        Image rareCostBuy = new Image(game.UIAtlas.findRegion("Coins"));
        rareCostBuy.setSize(40, 40);
        DrawableText rareCostTextBuy = new DrawableText(game.gameFontBold, String.valueOf(item.price));
        rareCostTextBuy.setScale(rareCostBuy.getHeight() * 0.7f / game.gameFontBold.getHeight(rareCostTextBuy.getText().toString(), 1));
        rareCostTextBuy.setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareCostTextBuy.getWidth() / 2 - 5 - rareCostBuy.getWidth() / 2,
                rareTankCount.getY() - rareTankCount.getHeight() - 10 - rareCostBuy.getHeight() / 2 + rareCostTextBuy.getHeight() / 2);
        rareCostBuy.setPosition(rareCostTextBuy.getX() + rareCostTextBuy.getWidth() + 5, rareTankCount.getY() - rareTankCount.getHeight() - 10 - rareCostBuy.getHeight());
        rareDailyWindow.addActor(rareCostBuy);
        rareCostTextBuy.setColor(game.player.coins - item.price >= 0 ? Color.WHITE : Color.RED);
        rareDailyWindow.addActor(rareCostTextBuy);
        return rareDailyWindow;
    }

    public void updateUpgradeWindow(InternalWindow rareDailyWindow, Group page, Tank tank, int tankNumber){
        Image closeIcon = new Image(game.UIAtlas.findRegion("Close"));
        Image windowBg = new Image(game.UIAtlas.findRegion("FeaturedItem2"));
        windowBg.setSize(windowBg.getWidth() * 1.2f, windowBg.getHeight() * 1.2f);
        rareDailyWindow.clearChildren();
        rareDailyWindow.init(closeIcon, windowBg);
        rareDailyWindow.setPosition(page.getWidth() / 2 - rareDailyWindow.getWidth() / 2, -page.getHeight() / 2 - rareDailyWindow.getHeight() / 2);
        rareDailyWindow.setVisible(false);
        final TankImage[] rareTank = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == tank.id).single(), game)};
        Image rareTankNameBg = new Image(game.UIAtlas.findRegion("PlayerNameBackground"));
        rareTankNameBg.setPosition(60, rareDailyWindow.getHeight() - 20 - rareTankNameBg.getHeight());
        rareTankNameBg.setHeight(30);
        DrawableText rareTankNameText = new DrawableText(game.gameFontBold, rareTank[0].info.characteristics.name);
        rareTankNameText.setScale(15 / game.gameFontBold.getHeight("1", 1));
        rareTankNameText.setPosition(rareTankNameBg.getX() + 5, rareTankNameBg.getY() + rareTankNameBg.getHeight() / 2 + rareTankNameText.getHeight() / 2);
        rareTankNameBg.setWidth(rareTankNameText.getWidth() * 1.3f);
        rareDailyWindow.addActor(rareTankNameBg);
        rareDailyWindow.addActor(rareTankNameText);
        String[] rareTankCharacteristics = new String[8];
        DecimalFormat format = new DecimalFormat("#.##");
        rareTankCharacteristics[0] = "RARITY - " + rareTank[0].info.characteristics.rarity.name();
        rareTankCharacteristics[1] = "HP - " + format.format(rareTank[0].info.characteristics.hp + (int)(rareTank[0].info.characteristics.hp * 0.25f * (tank.level - 1))) + "+" + format.format((int)(rareTank[0].info.characteristics.hp * 0.25f));
        rareTankCharacteristics[2] = "DAMAGE - " + format.format(rareTank[0].info.characteristics.damage + (int)(rareTank[0].info.characteristics.damage * 0.2f * (tank.level - 1))) + "+" + format.format((int)(rareTank[0].info.characteristics.damage * 0.2f));
        rareTankCharacteristics[3] = "RELOADING - " + format.format(rareTank[0].info.characteristics.reloading);
        rareTankCharacteristics[4] = "VELOCITY - " + format.format(rareTank[0].info.characteristics.velocity);
        rareTankCharacteristics[5] = "BULLET SPEED - " + format.format(rareTank[0].info.characteristics.bulletSpeed);
        rareTankCharacteristics[6] = "BODY ROTATE SPEED - " + format.format(rareTank[0].info.characteristics.bodyRotateDegrees);
        rareTankCharacteristics[7] = "GUN ROTATE SPEED - " + format.format(rareTank[0].info.characteristics.gunRotateDegrees);
        for (int i = 0; i < rareTankCharacteristics.length; i++) {
            DrawableText text = new DrawableText(game.gameFontBold, rareTankCharacteristics[i]);
            text.setScale(Math.min(rareDailyWindow.getWidth() / 2.5f / game.gameFontBold.getWidth(text.getText().toString(), 1),
                    30 / game.gameFontBold.getHeight(text.getText().toString(), 1)));
            text.setPosition(rareTankNameBg.getX(), rareTankNameBg.getY() - 20 - i * 40);
            rareDailyWindow.addActor(text);
        }
        final TankImage[] rareTankWindow = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == tank.id).single(), game)};
        rareTankWindow[0].setScale(180 / rareTankWindow[0].getHeight());
        rareTankWindow[0].setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareTankWindow[0].getWidth() / 2,
                rareDailyWindow.getHeight() / 2 + rareDailyWindow.getHeight() / 4 - rareTankWindow[0].getHeight());
        rareDailyWindow.addActor(rareTankWindow[0]);

        DrawableText hpForUpgradeText = new DrawableText(game.gameFontBold, "+" + (tank.level * 50) + "xp");
        hpForUpgradeText.setScale(30 / game.gameFontBold.getHeight(hpForUpgradeText.getText().toString(), 1));
        hpForUpgradeText.setColor(new Color(0.26274f, 0.48235f, 0.11764f, 1));
        hpForUpgradeText.setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - hpForUpgradeText.getWidth() / 2,
                (page.getHeight() - rareDailyWindow.getHeight()) / 2 + 170);
        rareDailyWindow.addActor(hpForUpgradeText);

        TextButton.TextButtonStyle rareTankBuyStyle = new TextButton.TextButtonStyle();
        rareTankBuyStyle.font = game.gameFont.font;
        rareTankBuyStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
        rareTankBuyStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
        rareTankBuyStyle.pressedOffsetX = -2;
        rareTankBuyStyle.pressedOffsetY = -2;
        TextButton rareTankBuy = new TextButton("Upgrade", rareTankBuyStyle);
        rareTankBuy.setTransform(true);
        rareTankBuy.setScale(0.7f);
        rareTankBuy.setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareTankBuy.getWidth() * rareTankBuy.getScaleX() / 2,
                (page.getHeight() - rareDailyWindow.getHeight()) / 2 + 10);
        rareTankBuy.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(rareTankBuy.getX(), rareTankBuy.getY(),
                        rareTankBuy.getWidth() * rareTankBuy.getScaleX(), rareTankBuy.getHeight() * rareTankBuy.getScaleY());
                if(rectangle.contains(event.toCoordinates(rareDailyWindow, new Vector2(rareDailyWindow.getX(), rareDailyWindow.getY())).x, event.toCoordinates(rareDailyWindow, new Vector2(rareDailyWindow.getX(), rareDailyWindow.getY())).y)) {
                    try {
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(tank.id)), PacketConstants.UPGRADE_CARD)));
                    } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }

                    boolean upgraded = (Boolean) Client.handler.getValue("upgrade_card");
                    if(upgraded){
                        rareDailyWindow.setVisible(false);
                        Gdx.app.postRunnable(() -> {
                            game.menuScreen = new MenuScreen(game, 1, tankNumber);
                            game.setScreen(game.menuScreen);
                        });
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
        rareDailyWindow.addActor(rareTankBuy);

        Image rareCostBuy = new Image(game.UIAtlas.findRegion("Coins"));
        rareCostBuy.setSize(40, 40);
        DrawableText rareCostTextBuy = new DrawableText(game.gameFontBold, String.valueOf(ChestOpenScreen.upgradeCards[tank.level - 1]));
        rareCostTextBuy.setScale(rareCostBuy.getHeight() * 0.7f / game.gameFontBold.getHeight(rareCostTextBuy.getText().toString(), 1));
        rareCostTextBuy.setPosition(rareDailyWindow.getWidth() / 2 + rareDailyWindow.getWidth() / 4.5f - rareCostTextBuy.getWidth() / 2 - 5 - rareCostBuy.getWidth() / 2,
                hpForUpgradeText.getY() - hpForUpgradeText.getHeight() - 10 - rareCostBuy.getHeight() / 2 + rareCostTextBuy.getHeight() / 2);
        rareCostBuy.setPosition(rareCostTextBuy.getX() + rareCostTextBuy.getWidth() + 5, hpForUpgradeText.getY() - hpForUpgradeText.getHeight() - 10 - rareCostBuy.getHeight());
        rareDailyWindow.addActor(rareCostBuy);
        rareCostTextBuy.setColor(game.player.coins - ChestOpenScreen.upgradeCards[tank.level - 1] >= 0 ? Color.WHITE : Color.RED);
        rareDailyWindow.addActor(rareCostTextBuy);
    }


    public MenuScreen(MainGame game, int... selectedPageNumber) {
        try {
            this.game = game;
            stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
            game.currentStage = stage;

            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.GET_NICKNAME}));
            String nick = (String) Client.handler.getValue("my_nickname");
            try {
                byte[] data = Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(nick));
                data[data.length - 1] = PacketConstants.GET_PLAYER_PROFILE;
                Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(data));
            } catch (Exception e) {
                e.printStackTrace();
            }

            game.player = (Player) Client.handler.getValue("player_profile");

            Image bg = new Image(game.UIAtlas.findRegion("Bg"));
            bg.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
            stage.addActor(bg);

            Table table = new Table();
            table.setWidth(840);
            table.setHeight(60);
            table.setPosition(11, 658, Align.bottomLeft);

            //region Home Page
            Group homePage = new Group();
            homePage.setPosition(11, 658);
            homePage.setSize(1024, 566);
            Image hbg = new Image(game.manager.get("Player/TrainingMap.png", Texture.class));
            hbg.setSize(1024, 566);
            hbg.setPosition(0, -566);
            ScrollPane.ScrollPaneStyle tanksStyle = new ScrollPane.ScrollPaneStyle();
            tanksStyle.background = new TextureRegionDrawable(game.UIAtlas.findRegion("UIContainer"));
            final ImageButton[] selectedImg = {null};
            Table tanks = new Table();
            tanks.align(Align.topLeft);
            Table tanksCharacteristics = new Table();
            tanksCharacteristics.align(Align.topRight);
            Arrays.sort(game.player.tanks, (a, b) -> Stream.of(game.tanks).filter(c -> c.id == a.id).single().characteristics.rarity.compareTo(Stream.of(game.tanks).filter(c -> c.id == b.id).single().characteristics.rarity));
            DrawableText tankLvlText = new DrawableText(game.gameFontBold, game.player.tanks[0].level + " LVL");

            TextButton.TextButtonStyle upgradeButtonStyle = new TextButton.TextButtonStyle();
            upgradeButtonStyle.font = game.gameFont.font;
            upgradeButtonStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
            upgradeButtonStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
            upgradeButtonStyle.pressedOffsetX = -2;
            upgradeButtonStyle.pressedOffsetY = -2;
            TextButton upgradeButton = new TextButton("Upgrade", upgradeButtonStyle);
            upgradeButton.setTransform(true);
            upgradeButton.setScale(0.5f);
            upgradeButton.setPosition(homePage.getWidth() / 2 - upgradeButton.getWidth() * upgradeButton.getScaleX() / 2,
                    -homePage.getHeight() + 190);
            for (int i = 0; i < game.player.tanks.length; i++) {
                ImageButton.ImageButtonStyle imgStyle = new ImageButton.ImageButtonStyle();
                imgStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("Btn"));
                imgStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("Btn"));
                ImageButton img = new ImageButton(imgStyle);
                int finalI1 = i;
                img.addListener(new ClickListener(){
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            if (selectedImg[0] != img) {
                                selectedImg[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("Btn"));
                                selectedImg[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("Btn"));
                                imgStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("BgSelected"));
                                imgStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("BgSelected"));
                                selectedImg[0] = img;
                                TankImage tankImage = (TankImage) selectedImg[0].getChild(1);
                                TankImage tankImageBg = new TankImage(tankImage.info, game);
                                tankImageBg.setScale(0.7f);
                                tankImageBg.setPosition(homePage.getWidth() / 2 - tankImageBg.getWidth() / 2, -(homePage.getHeight() - 180) / 2 - tankImageBg.getHeight() / 2);
                                tankImageBg.setName("tankImageBg");
                                Actor a = homePage.findActor("tankImageBg");
                                a.remove();
                                tankLvlText.setText(Stream.of(game.player.tanks).filter(c -> c.id == tankImageBg.info.id).single().level + " LVL");
                                tankLvlText.setScale(25 / game.gameFontBold.getHeight(tankLvlText.getText().toString(), 1));
                                tankLvlText.setPosition(homePage.getWidth() / 2 - tankLvlText.getWidth() / 2, tankImageBg.getY() + tankImageBg.getHeight() + 5 + tankLvlText.getHeight());
                                upgradeButton.setVisible(Stream.of(game.player.tanks).filter(c -> c.id == tankImageBg.info.id).single().count >= ChestOpenScreen.cardFull[Stream.of(game.player.tanks).filter(c -> c.id == tankImageBg.info.id).single().level - 1]);
                                String[] ch = new String[8];
                                DecimalFormat format = new DecimalFormat("#.##");
                                ch[0] = "RARITY - " + tankImageBg.info.characteristics.rarity.name();
                                ch[1] = "HP - " + format.format(tankImageBg.info.characteristics.hp + (int)(tankImageBg.info.characteristics.hp * 0.25f * (game.player.tanks[finalI1].level - 1)));
                                ch[2] = "DAMAGE - " + format.format(tankImageBg.info.characteristics.damage + (int)(tankImageBg.info.characteristics.damage * 0.2f * (game.player.tanks[finalI1].level - 1)));
                                ch[3] = "RELOADING - " + format.format(tankImageBg.info.characteristics.reloading);
                                ch[4] = "VELOCITY - " + format.format(tankImageBg.info.characteristics.velocity);
                                ch[5] = "BULLET SPEED - " + format.format(tankImageBg.info.characteristics.bulletSpeed);
                                ch[6] = "BODY ROTATE SPEED - " + format.format(tankImageBg.info.characteristics.bodyRotateDegrees);
                                ch[7] = "GUN ROTATE SPEED - " + format.format(tankImageBg.info.characteristics.gunRotateDegrees);
                                tanksCharacteristics.clearChildren();
                                for (int i = 0; i < ch.length; i++) {
                                    Group group = new Group();
                                    Image image = new Image(game.UIAtlas.findRegion("StatsBg"));
                                    DrawableText text = new DrawableText(game.gameFontBold, ch[i]);
                                    text.setScale(Math.min(240 / game.gameFontBold.getWidth(text.getText().toString(), 1), 1));
                                    image.setSize(260, 40);
                                    text.setPosition(image.getWidth() - text.getWidth() - 10, image.getHeight() / 2 + text.getHeight() / 2);
                                    group.setSize(image.getWidth(), image.getHeight());
                                    group.addActor(image);
                                    group.addActor(text);
                                    tanksCharacteristics.add(group).align(Align.right);
                                    tanksCharacteristics.row();
                                }
                                homePage.addActor(tankImageBg);
                            }
                        return super.touchDown(event, x, y, pointer, button);
                    }
                });
                int finalI = i;
                TanksInfo tanksInfo = Stream.of(game.tanks).filter(c -> c.id == game.player.tanks[finalI].id).single();
                TankImage tankImage = new TankImage(tanksInfo, game);
                tankImage.setScale(0.8f);
                img.add(tankImage).padTop(img.getHeight() / 2 - tankImage.getHeight() * tankImage.getScaleY() / 2 + 40);

                Image levelContainer = new Image(game.UIAtlas.findRegion("LevelBarContainer"));
                levelContainer.setSize(120, 15);
                levelContainer.setPosition(85 - levelContainer.getWidth() / 2, 10);
                img.addActor(levelContainer);

                final Tank tank = Stream.of(game.player.tanks).filter(c -> c.id == tanksInfo.id).single();
                Image level = new Image(game.UIAtlas.findRegion("LevelBar"));
                level.setPosition(levelContainer.getX(), levelContainer.getY());
                level.setSize(Math.min((float) tank.count / (float) ChestOpenScreen.cardFull[tank.level - 1], 1) * levelContainer.getWidth(), 15);
                img.addActor(level);

                DrawableText tankCount = new DrawableText(game.gameFontBold, tank.count + "/" + ChestOpenScreen.cardFull[tank.level - 1]);
                tankCount.setScale(levelContainer.getHeight() * 0.6f / game.gameFontBold.getHeight(tankCount.getText().toString(), 1));
                tankCount.setPosition(levelContainer.getX() + levelContainer.getWidth() / 2 - game.gameFontBold.getWidth(tankCount.getText().toString(), tankCount.getScale()) / 2,
                        levelContainer.getY() + levelContainer.getHeight() / 2 + game.gameFontBold.getHeight(tankCount.getText().toString(), tankCount.getScale()) / 2);
                img.addActor(tankCount);

                DrawableText tankName = new DrawableText(game.gameFontBold, tanksInfo.characteristics.name);
                tankName.setScale(Math.min(img.getWidth() * 0.8f / game.gameFontBold.getWidth(tankName.getText().toString()), 1));
                tankName.setPosition(85 - tankName.getWidth() / 2, 157);
                img.addActor(tankName);

                tanks.add(img).width(170).height(170).pad(5, 2.5f, 5, 2.5f);
            }
            selectedImg[0] = (ImageButton) tanks.getCells().get(selectedPageNumber.length > 1 ? selectedPageNumber[1] : 0).getActor();
            selectedImg[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("BgSelected"));
            selectedImg[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("BgSelected"));
            TankImage tankImage = (TankImage) selectedImg[0].getChild(1);
            TankImage tankImageBg = new TankImage(tankImage.info, game);
            tankImageBg.setScale(0.7f);
            tankImageBg.setName("tankImageBg");
            tankImageBg.setPosition(homePage.getWidth() / 2 - tankImageBg.getWidth() / 2, -(homePage.getHeight() - 180) / 2 - tankImageBg.getHeight() / 2);
            tankLvlText.setText(Stream.of(game.player.tanks).filter(c -> c.id == tankImageBg.info.id).single().level + " LVL");
            tankLvlText.setScale(25 / game.gameFontBold.getHeight(tankLvlText.getText().toString(), 1));
            tankLvlText.setPosition(homePage.getWidth() / 2 - tankLvlText.getWidth() / 2, tankImageBg.getY() + tankImageBg.getHeight() + 5 + tankLvlText.getHeight());
            String[] ch = new String[8];
            DecimalFormat format = new DecimalFormat("#.##");
            ch[0] = "RARITY - " + tankImageBg.info.characteristics.rarity.name();
            ch[1] = "HP - " + format.format(tankImageBg.info.characteristics.hp + (int)(tankImageBg.info.characteristics.hp * 0.25f * game.player.tanks[0].level));
            ch[2] = "DAMAGE - " + format.format(tankImageBg.info.characteristics.damage + (int)(tankImageBg.info.characteristics.damage * 0.2f * game.player.tanks[0].level));
            ch[3] = "RELOADING - " + format.format(tankImageBg.info.characteristics.reloading);
            ch[4] = "VELOCITY - " + format.format(tankImageBg.info.characteristics.velocity);
            ch[5] = "BULLET SPEED - " + format.format(tankImageBg.info.characteristics.bulletSpeed);
            ch[6] = "BODY ROTATE SPEED - " + format.format(tankImageBg.info.characteristics.bodyRotateDegrees);
            ch[7] = "GUN ROTATE SPEED - " + format.format(tankImageBg.info.characteristics.gunRotateDegrees);
            tanksCharacteristics.clearChildren();
            for (int i = 0; i < ch.length; i++) {
                Group group = new Group();
                Image image = new Image(game.UIAtlas.findRegion("StatsBg"));
                DrawableText text = new DrawableText(game.gameFontBold, ch[i]);
                text.setScale(Math.min(240 / game.gameFontBold.getWidth(text.getText().toString(), 1), 1));
                image.setSize(260, 40);
                text.setPosition(image.getWidth() - text.getWidth() - 10, image.getHeight() / 2 + text.getHeight() / 2);
                group.setSize(image.getWidth(), image.getHeight());
                group.addActor(image);
                group.addActor(text);
                tanksCharacteristics.add(group).align(Align.right);
                tanksCharacteristics.row();
            }
            InternalWindow upgradeWindow = new InternalWindow();
            upgradeWindow.setVisible(false);

            upgradeButton.addListener(new ClickListener(){
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Rectangle rectangle = new Rectangle(upgradeButton.getX(), upgradeButton.getY(),
                            upgradeButton.getWidth() * upgradeButton.getScaleX(), upgradeButton.getHeight() * upgradeButton.getScaleY());
                    if(rectangle.contains(event.toCoordinates(homePage, new Vector2(homePage.getX(), homePage.getY())).x, event.toCoordinates(homePage, new Vector2(homePage.getX(), homePage.getY())).y)) {
                        TankImage tankImageCurrent = homePage.findActor("tankImageBg");
                        tankImageCurrent.setVisible(false);
                        tanks.setTouchable(Touchable.disabled);
                        updateUpgradeWindow(upgradeWindow, homePage, Stream.of(game.player.tanks).filter(c -> c.id == tankImageCurrent.info.id).single(), tanks.getCells().indexOf(tanks.getCell(selectedImg[0]), false));
                        upgradeWindow.addOnClose(() -> {
                            tankImageCurrent.setVisible(true);
                            tanks.setTouchable(Touchable.enabled);
                        });
                        upgradeWindow.setVisible(true);
                    }
                    super.touchUp(event, x, y, pointer, button);
                }
            });
            upgradeButton.setVisible(game.player.tanks[selectedPageNumber.length > 1 ? selectedPageNumber[1] : 0].count >= ChestOpenScreen.cardFull[game.player.tanks[selectedPageNumber.length > 1 ? selectedPageNumber[1] : 0].level - 1]);
            ScrollPane tanksPane = new ScrollPane(tanks, tanksStyle);
            tanksPane.setSize(1024, 180);
            tanksPane.setPosition(0, -566);
            homePage.addActor(hbg);
            tanksCharacteristics.setPosition(homePage.getWidth() - 20, -30);
            homePage.addActor(tanksCharacteristics);
            Image coins = new Image(game.UIAtlas.findRegion("Coins"));
            coins.setSize(64,64);
            coins.setPosition(20, -20 - coins.getHeight());
            homePage.addActor(tanksPane);
            homePage.addActor(coins);
            DrawableText coinsCount = new DrawableText(game.gameFontBold, String.valueOf(game.player.coins));
            coinsCount.setScale(coins.getHeight() / game.gameFontBold.getHeight(coinsCount.getText().toString(), 1) * 0.5f);
            coinsCount.setPosition(30 + coins.getWidth(), -20 - coins.getHeight() / 2 + game.gameFontBold.getHeight(coinsCount.getText().toString(), coinsCount.getScale()) / 2);
            homePage.addActor(coinsCount);
            Image diamonds = new Image(game.UIAtlas.findRegion("Diamond"));
            diamonds.setSize(60,60);
            diamonds.setPosition(15, -40 - coins.getHeight() - diamonds.getHeight());
            homePage.addActor(diamonds);
            DrawableText diamondsCount = new DrawableText(game.gameFontBold, String.valueOf(game.player.diamonds));
            diamondsCount.setScale(coinsCount.getScale());
            diamondsCount.setPosition(30 + coins.getWidth(), -40 - coins.getHeight() - diamonds.getHeight() / 2 + game.gameFontBold.getHeight(diamondsCount.getText().toString(), diamondsCount.getScale()) / 2);
            homePage.addActor(diamondsCount);
            homePage.addActor(tankImageBg);
            homePage.addActor(tankLvlText);
            homePage.addActor(upgradeButton);
            homePage.addActor(upgradeWindow);
            stage.addActor(homePage);
            //endregion

            //region Shop Page
            Group shopPage = new Group();
            shopPage.setPosition(11, 92);
            shopPage.setSize(1024, 566);
            Image dailyItemsBg = new Image(game.UIAtlas.findRegion("FeaturedItemsBackgroundLarge"));
            dailyItemsBg.setSize(480, 40);
            dailyItemsBg.setPosition(15, 566 - 40 - dailyItemsBg.getHeight());
            Image dailyItemsTextBg = new Image(game.UIAtlas.findRegion("PlayerNameBackground"));
            DrawableText dailyItemsText = new DrawableText(game.gameFontBold, "DAILY ITEMS");
            dailyItemsText.setScale(Math.min(dailyItemsBg.getHeight() * 0.65f / game.gameFontBold.getHeight(dailyItemsText.getText().toString(), 1), 1));
            dailyItemsText.setPosition(dailyItemsBg.getX() + 10, dailyItemsBg.getY()  + dailyItemsBg.getHeight() / 2 + dailyItemsText.getHeight() / 2);
            dailyItemsTextBg.setSize(dailyItemsText.getWidth() * 1.2f, dailyItemsBg.getHeight());
            dailyItemsTextBg.setPosition(dailyItemsBg.getX(), dailyItemsBg.getY());
            shopPage.addActor(dailyItemsBg);
            shopPage.addActor(dailyItemsTextBg);
            shopPage.addActor(dailyItemsText);

            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[] {PacketConstants.GET_DAILY_ITEMS}));
            final DailyItem[][] dailyItems = {(DailyItem[]) Client.handler.getValue("daily_items")};

            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[] {PacketConstants.GET_DAILY_TIME}));
            final long[] dailyTime = {(Long) Client.handler.getValue("daily_items_time")};

            final InternalWindow[] commonDailyWindow = {createWindow(shopPage, dailyItems[0][0])};
            final InternalWindow[] epicDailyWindow = {createWindow(shopPage, dailyItems[0][2])};
            final InternalWindow[] mythicalDailyWindow = {createWindow(shopPage, dailyItems[0][3])};
            final InternalWindow[] rareDailyWindow = {createWindow(shopPage, dailyItems[0][1])};

            ImageButton.ImageButtonStyle commonBgStyle = new ImageButton.ImageButtonStyle();
            commonBgStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SmallFeaturedItem1"));
            commonBgStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SmallFeaturedItem1"));
            ImageButton commonBg = new ImageButton(commonBgStyle);
            commonBg.setSize(dailyItemsBg.getWidth() / 2 * 0.9f, dailyItemsBg.getWidth() / 2 * 0.87f);
            commonBg.setPosition(dailyItemsBg.getWidth() / 2 * 0.1f, dailyItemsBg.getY() - commonBg.getHeight() - dailyItemsBg.getWidth() / 2 * 0.1f);
            commonBg.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if(!dailyItems[0][0].bought && !rareDailyWindow[0].isVisible() && !epicDailyWindow[0].isVisible() && !mythicalDailyWindow[0].isVisible())
                        commonDailyWindow[0].setVisible(true);
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            shopPage.addActor(commonBg);

            final TankImage[] commonTank = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][0].tankId).single(), game)};
            commonTank[0].setScale(Math.min(commonBg.getHeight() * 0.4f / commonTank[0].getHeight(), 1));
            commonTank[0].setPosition(commonBg.getWidth() / 2 - commonTank[0].getWidth() / 2, commonBg.getHeight() / 2 - commonTank[0].getHeight() / 2);
            commonBg.addActor(commonTank[0]);

            final DrawableText[] commonTankText = {new DrawableText(game.gameFontBold, dailyItems[0][0].count != 0 ? "x" + dailyItems[0][0].count : "New vehicle!")};
            commonTankText[0].setScale(Math.min(commonBg.getHeight() * 0.7f / game.gameFontBold.getWidth(commonTankText[0].getText().toString(), 1), 0.55f));
            commonTankText[0].setPosition(commonBg.getWidth() / 2 - commonTankText[0].getWidth() / 2, 40 + commonTankText[0].getHeight());
            commonBg.addActor(commonTankText[0]);

            final DrawableText[] commonTankName = {new DrawableText(game.gameFontBold, commonTank[0].info.characteristics.name)};
            commonTankName[0].setScale(Math.min(commonBg.getWidth() * 0.7f / game.gameFontBold.getWidth(commonTankName[0].getText().toString(), 1), 0.8f));
            commonTankName[0].setPosition(commonBg.getWidth() / 2 - commonTankName[0].getWidth() / 2, commonBg.getHeight() - 25);
            commonBg.addActor(commonTankName[0]);

            final Image[] commonCost = {new Image(game.UIAtlas.findRegion("Coins"))};
            commonCost[0].setSize(20, 20);
            final DrawableText[] commonCostText = {new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][0].price))};
            commonCostText[0].setScale(commonCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(commonCostText[0].getText().toString(), 1));
            commonCostText[0].setPosition(commonBg.getWidth() / 2 - commonCost[0].getWidth() / 2 - commonCostText[0].getWidth() / 2,
                    commonTankText[0].getY() - commonTankText[0].getHeight() - commonCost[0].getHeight() / 2 + commonCostText[0].getHeight() / 2);
            commonCost[0].setPosition(commonCostText[0].getX() + commonCostText[0].getWidth() + 5,
                    commonTankText[0].getY() - commonTankText[0].getHeight() - commonCost[0].getHeight());
            commonBg.addActor(commonCost[0]);
            commonBg.addActor(commonCostText[0]);

            if(dailyItems[0][0].bought){
                Image commonBoughtImage = new Image(game.UIAtlas.findRegion("Bought"));
                commonBoughtImage.setSize(commonBoughtImage.getWidth() * 0.7f, commonBoughtImage.getHeight() * 0.7f);
                commonBoughtImage.setPosition(commonBg.getWidth() / 2 - commonBoughtImage.getWidth() / 2,
                        commonBg.getHeight() / 2 - commonBoughtImage.getHeight() / 2);
                commonBg.addActor(commonBoughtImage);
            }

            ImageButton.ImageButtonStyle rareBgStyle = new ImageButton.ImageButtonStyle();
            rareBgStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("FrameBrown"));
            rareBgStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("FrameBrown"));
            ImageButton rareBg = new ImageButton(rareBgStyle);
            rareBg.setSize(dailyItemsBg.getWidth() / 2 * 0.9f, dailyItemsBg.getWidth() / 2 * 0.87f);
            rareBg.setPosition(commonBg.getX() + commonBg.getWidth() + dailyItemsBg.getWidth() / 2 * 0.1f, dailyItemsBg.getY() - rareBg.getHeight() - dailyItemsBg.getWidth() / 2 * 0.1f);
            rareBg.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if(!dailyItems[0][1].bought && !commonDailyWindow[0].isVisible() && !epicDailyWindow[0].isVisible() && !mythicalDailyWindow[0].isVisible())
                        rareDailyWindow[0].setVisible(true);
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            shopPage.addActor(rareBg);

            final TankImage[] rareTank = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][1].tankId).single(), game)};
            rareTank[0].setScale(Math.min(rareBg.getHeight() * 0.4f / rareTank[0].getHeight(), 1));
            rareTank[0].setPosition(rareBg.getWidth() / 2 - rareTank[0].getWidth() / 2, rareBg.getHeight() / 2 - rareTank[0].getHeight() / 2);
            rareBg.addActor(rareTank[0]);

            final DrawableText[] rareTankText = {new DrawableText(game.gameFontBold, dailyItems[0][1].count != 0 ? "x" + dailyItems[0][1].count : "New vehicle!")};
            rareTankText[0].setScale(Math.min(rareBg.getHeight() * 0.7f / game.gameFontBold.getWidth(rareTankText[0].getText().toString(), 1), 0.55f));
            rareTankText[0].setPosition(rareBg.getWidth() / 2 - rareTankText[0].getWidth() / 2, 40 + rareTankText[0].getHeight());
            rareBg.addActor(rareTankText[0]);

            final DrawableText[] rareTankName = {new DrawableText(game.gameFontBold, rareTank[0].info.characteristics.name)};
            rareTankName[0].setScale(Math.min(rareBg.getWidth() * 0.7f / game.gameFontBold.getWidth(rareTankName[0].getText().toString(), 1), 0.8f));
            rareTankName[0].setPosition(rareBg.getWidth() / 2 - rareTankName[0].getWidth() / 2, rareBg.getHeight() - 25);
            rareBg.addActor(rareTankName[0]);

            final Image[] rareCost = {new Image(game.UIAtlas.findRegion("Coins"))};
            rareCost[0].setSize(20, 20);
            final DrawableText[] rareCostText = {new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][1].price))};
            rareCostText[0].setScale(rareCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(rareCostText[0].getText().toString(), 1));
            rareCostText[0].setPosition(rareBg.getWidth() / 2 - rareCost[0].getWidth() / 2 - rareCostText[0].getWidth() / 2,
                    rareTankText[0].getY() - rareTankText[0].getHeight() - rareCost[0].getHeight() / 2 + rareCostText[0].getHeight() / 2);
            rareCost[0].setPosition(rareCostText[0].getX() + rareCostText[0].getWidth() + 5,
                    rareTankText[0].getY() - rareTankText[0].getHeight() - rareCost[0].getHeight());
            rareBg.addActor(rareCost[0]);
            rareBg.addActor(rareCostText[0]);

            if(dailyItems[0][1].bought){
                Image rareBoughtImage = new Image(game.UIAtlas.findRegion("Bought"));
                rareBoughtImage.setSize(rareBoughtImage.getWidth() * 0.7f, rareBoughtImage.getHeight() * 0.7f);
                rareBoughtImage.setPosition(rareBg.getWidth() / 2 - rareBoughtImage.getWidth() / 2,
                        rareBg.getHeight() / 2 - rareBoughtImage.getHeight() / 2);
                rareBg.addActor(rareBoughtImage);
            }

            ImageButton.ImageButtonStyle epicBgStyle = new ImageButton.ImageButtonStyle();
            epicBgStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("FrameBlue"));
            epicBgStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("FrameBlue"));
            ImageButton epicBg = new ImageButton(epicBgStyle);
            epicBg.setSize(dailyItemsBg.getWidth() / 2 * 0.9f, dailyItemsBg.getWidth() / 2 * 0.87f);
            epicBg.setPosition(dailyItemsBg.getWidth() / 2 * 0.1f, dailyItemsBg.getY() - epicBg.getHeight() - dailyItemsBg.getWidth() / 2 * 0.2f - rareBg.getHeight());
            epicBg.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if(!dailyItems[0][2].bought && !rareDailyWindow[0].isVisible() && !commonDailyWindow[0].isVisible() && !mythicalDailyWindow[0].isVisible())
                        epicDailyWindow[0].setVisible(true);
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            shopPage.addActor(epicBg);

            final TankImage[] epicTank = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][2].tankId).single(), game)};
            epicTank[0].setScale(Math.min(epicBg.getHeight() * 0.4f / epicTank[0].getHeight(), 1));
            epicTank[0].setPosition(epicBg.getWidth() / 2 - epicTank[0].getWidth() / 2, epicBg.getHeight() / 2 - epicTank[0].getHeight() / 2);
            epicBg.addActor(epicTank[0]);

            final DrawableText[] epicTankText = {new DrawableText(game.gameFontBold, dailyItems[0][2].count != 0 ? "x" + dailyItems[0][2].count : "New vehicle!")};
            epicTankText[0].setScale(Math.min(epicBg.getHeight() * 0.7f / game.gameFontBold.getWidth(epicTankText[0].getText().toString(), 1), 0.55f));
            epicTankText[0].setPosition(epicBg.getWidth() / 2 - epicTankText[0].getWidth() / 2, 40 + epicTankText[0].getHeight());
            epicBg.addActor(epicTankText[0]);

            final DrawableText[] epicTankName = {new DrawableText(game.gameFontBold, epicTank[0].info.characteristics.name)};
            epicTankName[0].setScale(Math.min(epicBg.getWidth() * 0.7f / game.gameFontBold.getWidth(epicTankName[0].getText().toString(), 1), 0.8f));
            epicTankName[0].setPosition(epicBg.getWidth() / 2 - epicTankName[0].getWidth() / 2, epicBg.getHeight() - 25);
            epicBg.addActor(epicTankName[0]);

            final Image[] epicCost = {new Image(game.UIAtlas.findRegion("Coins"))};
            epicCost[0].setSize(20, 20);
            final DrawableText[] epicCostText = {new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][2].price))};
            epicCostText[0].setScale(epicCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(epicCostText[0].getText().toString(), 1));
            epicCostText[0].setPosition(epicBg.getWidth() / 2 - epicCost[0].getWidth() / 2 - epicCostText[0].getWidth() / 2,
                    epicTankText[0].getY() - epicTankText[0].getHeight() - epicCost[0].getHeight() / 2 + epicCostText[0].getHeight() / 2);
            epicCost[0].setPosition(epicCostText[0].getX() + epicCostText[0].getWidth() + 5,
                    epicTankText[0].getY() - epicTankText[0].getHeight() - epicCost[0].getHeight());
            epicBg.addActor(epicCost[0]);
            epicBg.addActor(epicCostText[0]);

            if(dailyItems[0][2].bought){
                Image epicBoughtImage = new Image(game.UIAtlas.findRegion("Bought"));
                epicBoughtImage.setSize(epicBoughtImage.getWidth() * 0.7f, epicBoughtImage.getHeight() * 0.7f);
                epicBoughtImage.setPosition(epicBg.getWidth() / 2 - epicBoughtImage.getWidth() / 2,
                        epicBg.getHeight() / 2 - epicBoughtImage.getHeight() / 2);
                epicBg.addActor(epicBoughtImage);
            }

            ImageButton.ImageButtonStyle mythicalBgStyle = new ImageButton.ImageButtonStyle();
            mythicalBgStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("FrameGreen"));
            mythicalBgStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("FrameGreen"));
            ImageButton mythicalBg = new ImageButton(mythicalBgStyle);
            mythicalBg.setSize(dailyItemsBg.getWidth() / 2 * 0.9f, dailyItemsBg.getWidth() / 2 * 0.87f);
            mythicalBg.setPosition(commonBg.getX() + commonBg.getWidth() + dailyItemsBg.getWidth() / 2 * 0.1f, dailyItemsBg.getY() - mythicalBg.getHeight() - dailyItemsBg.getWidth() / 2 * 0.2f - rareBg.getHeight());
            mythicalBg.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if(!dailyItems[0][3].bought && !rareDailyWindow[0].isVisible() && !epicDailyWindow[0].isVisible() && !commonDailyWindow[0].isVisible())
                        mythicalDailyWindow[0].setVisible(true);
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            shopPage.addActor(mythicalBg);

            final TankImage[] mythicalTank = {new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][3].tankId).single(), game)};
            mythicalTank[0].setScale(Math.min(mythicalBg.getHeight() * 0.4f / mythicalTank[0].getHeight(), 1));
            mythicalTank[0].setPosition(mythicalBg.getWidth() / 2 - mythicalTank[0].getWidth() / 2, mythicalBg.getHeight() / 2 - mythicalTank[0].getHeight() / 2);
            mythicalBg.addActor(mythicalTank[0]);

            final DrawableText[] mythicalTankText = {new DrawableText(game.gameFontBold, dailyItems[0][3].count != 0 ? "x" + dailyItems[0][3].count : "New vehicle!")};
            mythicalTankText[0].setScale(Math.min(mythicalBg.getHeight() * 0.7f / game.gameFontBold.getWidth(mythicalTankText[0].getText().toString(), 1), 0.55f));
            mythicalTankText[0].setPosition(mythicalBg.getWidth() / 2 - mythicalTankText[0].getWidth() / 2, 40 + mythicalTankText[0].getHeight());
            mythicalBg.addActor(mythicalTankText[0]);

            final DrawableText[] mythicalTankName = {new DrawableText(game.gameFontBold, mythicalTank[0].info.characteristics.name)};
            mythicalTankName[0].setScale(Math.min(mythicalBg.getWidth() * 0.7f / game.gameFontBold.getWidth(mythicalTankName[0].getText().toString(), 1), 0.8f));
            mythicalTankName[0].setPosition(mythicalBg.getWidth() / 2 - mythicalTankName[0].getWidth() / 2, mythicalBg.getHeight() - 25);
            mythicalBg.addActor(mythicalTankName[0]);

            final Image[] mythicalCost = {new Image(game.UIAtlas.findRegion("Coins"))};
            mythicalCost[0].setSize(20, 20);
            final DrawableText[] mythicalCostText = {new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][3].price))};
            mythicalCostText[0].setScale(mythicalCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(mythicalCostText[0].getText().toString(), 1));
            mythicalCostText[0].setPosition(mythicalBg.getWidth() / 2 - mythicalCost[0].getWidth() / 2 - mythicalCostText[0].getWidth() / 2,
                    mythicalTankText[0].getY() - mythicalTankText[0].getHeight() - mythicalCost[0].getHeight() / 2 + mythicalCostText[0].getHeight() / 2);
            mythicalCost[0].setPosition(mythicalCostText[0].getX() + mythicalCostText[0].getWidth() + 5,
                    mythicalTankText[0].getY() - mythicalTankText[0].getHeight() - mythicalCost[0].getHeight());
            mythicalBg.addActor(mythicalCost[0]);
            mythicalBg.addActor(mythicalCostText[0]);

            if(dailyItems[0][3].bought){
                Image mythicalBoughtImage = new Image(game.UIAtlas.findRegion("Bought"));
                mythicalBoughtImage.setSize(mythicalBoughtImage.getWidth() * 0.7f, mythicalBoughtImage.getHeight() * 0.7f);
                mythicalBoughtImage.setPosition(mythicalBg.getWidth() / 2 - mythicalBoughtImage.getWidth() / 2,
                        mythicalBg.getHeight() / 2 - mythicalBoughtImage.getHeight() / 2);
                mythicalBg.addActor(mythicalBoughtImage);
            }

            DrawableText dailyTimeText = new DrawableText(game.gameFontBold, "00:00:00");
            dailyTimeText.setScale(dailyItemsText.getScale() * 0.8f);
            dailyTimeText.setPosition(dailyItemsBg.getX() + dailyItemsBg.getWidth() - 10 - dailyTimeText.getWidth(),
                    dailyItemsBg.getY() + dailyItemsBg.getHeight() / 2 + dailyTimeText.getHeight() / 2);
            shopPage.addActor(dailyTimeText);

            Image anytimeItemsBg = new Image(game.UIAtlas.findRegion("FeaturedItemsBackgroundLarge"));
            anytimeItemsBg.setSize(480, 40);
            anytimeItemsBg.setPosition(homePage.getWidth() - 15 - anytimeItemsBg.getWidth(), 566 - 40 - dailyItemsBg.getHeight());
            Image anytimeItemsTextBg = new Image(game.UIAtlas.findRegion("PlayerNameBackground"));
            DrawableText anytimeItemsText = new DrawableText(game.gameFontBold, "ANYTIME ITEMS");
            anytimeItemsText.setScale(Math.min(dailyItemsBg.getHeight() * 0.65f / game.gameFontBold.getHeight(anytimeItemsText.getText().toString(), 1), 1));
            anytimeItemsText.setPosition(anytimeItemsBg.getX() + 10, anytimeItemsBg.getY()  + anytimeItemsBg.getHeight() / 2 + anytimeItemsText.getHeight() / 2);
            anytimeItemsTextBg.setSize(anytimeItemsText.getWidth() * 1.2f, anytimeItemsBg.getHeight());
            anytimeItemsTextBg.setPosition(anytimeItemsBg.getX(), anytimeItemsBg.getY());
            shopPage.addActor(anytimeItemsBg);
            shopPage.addActor(anytimeItemsTextBg);
            shopPage.addActor(anytimeItemsText);

            ImageButton.ImageButtonStyle commonChestFrameStyle = new ImageButton.ImageButtonStyle();
            commonChestFrameStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SmallFrame3"));
            commonChestFrameStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SmallFrame3"));
            ImageButton commonChestFrame = new ImageButton(commonChestFrameStyle);
            commonChestFrame.setTransform(true);
            commonChestFrame.setSize(anytimeItemsBg.getWidth() / 2 * 0.9f, anytimeItemsBg.getWidth() / 2 * 0.87f);
            commonChestFrame.setPosition(anytimeItemsBg.getX(), anytimeItemsBg.getY() - commonChestFrame.getHeight() - anytimeItemsBg.getWidth() / 2 * 0.07f);
            commonChestFrame.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    commonChestFrame.setScale(0.98f);
                    commonChestFrame.setPosition(commonChestFrame.getX() + commonChestFrame.getWidth() * 0.01f, commonChestFrame.getY() + commonChestFrame.getHeight() * 0.01f);
                    return super.touchDown(event, x, y, pointer, button);
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    commonChestFrame.setScale(1);
                    commonChestFrame.setPosition(commonChestFrame.getX() - commonChestFrame.getWidth() * 0.01f, commonChestFrame.getY() - commonChestFrame.getHeight() * 0.01f);
                    Rectangle rectangle = new Rectangle(commonChestFrame.getX(), commonChestFrame.getY(),
                            commonChestFrame.getWidth() * commonChestFrame.getScaleX(), commonChestFrame.getHeight() * commonChestFrame.getScaleY());
                    if (rectangle.contains(event.toCoordinates(shopPage, new Vector2(shopPage.getX(), shopPage.getY())).x, event.toCoordinates(shopPage, new Vector2(shopPage.getX(), shopPage.getY())).y)) {
                        try {
                            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(ChestName.COMMON)), PacketConstants.BUY_CHEST)));
                        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                            e.printStackTrace();
                        }
                        Chest bought = (Chest) Client.handler.getValue("buy_chest");
                        if(bought.loot != null){
                            Gdx.app.postRunnable(() -> {
                                ChestOpenScreen chestOpenScreen = new ChestOpenScreen(game, bought, 2);
                                game.setScreen(chestOpenScreen);
                            });
                        }
                    }
                    super.touchUp(event, x, y, pointer, button);
                }
            });
            shopPage.addActor(commonChestFrame);

            Image commonChestImage = new Image(game.commonChestAtlas.findRegion("0001"));
            commonChestImage.setSize(commonChestImage.getWidth() * 0.6f, commonChestImage.getHeight() * 0.6f);
            commonChestImage.setPosition(commonChestFrame.getWidth() / 2 - commonChestImage.getWidth() / 2,
                     commonChestFrame.getHeight() / 2 - commonChestImage.getHeight() / 2.5f);
            commonChestFrame.addActor(commonChestImage);

            DrawableText commonChestName = new DrawableText(game.gameFontBold, "Common Chest");
            commonChestName.setScale(Math.min(commonChestFrame.getWidth() * 0.6f / game.gameFontBold.getWidth(commonChestName.getText().toString(), 1), 1));
            commonChestName.setPosition(commonChestFrame.getWidth() / 2 - commonChestName.getWidth() / 2,
                    commonChestFrame.getHeight() - 35);
            commonChestFrame.addActor(commonChestName);

            Image commonChestCost = new Image(game.UIAtlas.findRegion("Coins"));
            commonChestCost.setSize(30, 30);
            DrawableText commonChestCostText = new DrawableText(game.gameFontBold, String.valueOf(ChestName.COMMON.value));
            commonChestCostText.setColor(game.player.coins - 50 >= 0 ? Color.WHITE : Color.RED);
            commonChestCostText.setScale(Math.min(commonChestCost.getHeight() * 0.6f / game.gameFontBold.getHeight(commonChestCostText.getText().toString(), 1), 1));
            commonChestCostText.setPosition(commonChestFrame.getWidth() / 2 - commonChestCost.getWidth() / 2 - commonChestCostText.getWidth() / 2,
                    commonChestCost.getHeight() / 2 + 20 + commonChestCostText.getHeight() / 2);
            commonChestCost.setPosition(commonChestCostText.getX() + commonChestCostText.getWidth() + 5,
                    20);
            commonChestFrame.addActor(commonChestCost);
            commonChestFrame.addActor(commonChestCostText);

            shopPage.setVisible(false);
            shopPage.addActor(commonDailyWindow[0]);
            shopPage.addActor(rareDailyWindow[0]);
            shopPage.addActor(epicDailyWindow[0]);
            shopPage.addActor(mythicalDailyWindow[0]);
            stage.addActor(shopPage);
            //endregion

            //region Profile Page
            Group profilePage = new Group();
            profilePage.setPosition(11, 92);
            profilePage.setSize(1024, 566);
            Image playerBg = new Image(game.UIAtlas.findRegion("PlayerNameBackground"));
            playerBg.setScale(0.7f);
            playerBg.setPosition(15, 540 - playerBg.getHeight() * playerBg.getScaleY());
            DrawableText nickname = new DrawableText(game.gameFontBold, game.player.nickName);
            nickname.setScale(playerBg.getHeight() * playerBg.getScaleY() * 0.6f / game.gameFontBold.getHeight(game.player.nickName));
            nickname.setScale(Math.min(playerBg.getWidth() * playerBg.getScaleX() * 0.8f / game.gameFontBold.getWidth(game.player.nickName), nickname.getScale()));
            nickname.setPosition(20, 540 - (playerBg.getHeight() * playerBg.getScaleY() - game.gameFontBold.getHeight(game.player.nickName, nickname.getScale())) / 2);
            profilePage.addActor(playerBg);
            profilePage.addActor(nickname);

            Image playerPic = new Image(game.UIAtlas.findRegion("LargPlayerProfilePic"));
            playerPic.setScale(0.7f);
            playerPic.setPosition(15, 520 - playerBg.getHeight() * playerBg.getScaleY() - playerPic.getHeight() * playerPic.getScaleY());
            profilePage.addActor(playerPic);

            Image statsPic = new Image(game.UIAtlas.findRegion("StatsContainerLarge"));
            statsPic.setScale(0.68f);
            statsPic.setPosition(15, 510 - playerBg.getHeight() * playerBg.getScaleY() - playerPic.getHeight() * playerPic.getScaleY() - statsPic.getHeight() * statsPic.getScaleY());
            profilePage.addActor(statsPic);

            DrawableText lvl = new DrawableText(game.gameFontBold, "LVL" + game.player.rankLevel);
            lvl.setScale(0.8f);
            lvl.setPosition(35 + playerPic.getWidth() * playerPic.getScaleX(), 518 - playerBg.getHeight() * playerBg.getScaleY());
            profilePage.addActor(lvl);

            Image levelContainer = new Image(game.UIAtlas.findRegion("LevelBarContainer"));
            levelContainer.setRotation(270);
            levelContainer.setPosition(lvl.getX(), lvl.getY() - game.gameFontBold.getHeight(lvl.getText().toString(), lvl.getScale()) - 10);
            levelContainer.setSize(20, statsPic.getX() + statsPic.getWidth() * statsPic.getScaleX() - levelContainer.getX());
            profilePage.addActor(levelContainer);

            Image level = new Image(game.UIAtlas.findRegion("LevelBar"));
            level.setRotation(270);
            level.setPosition(lvl.getX(), lvl.getY() - game.gameFontBold.getHeight(lvl.getText().toString(), lvl.getScale()) - 10);
            level.setSize(20, (float) game.player.xp / (float) (game.player.rankLevel * 50) * (statsPic.getX() + statsPic.getWidth() * statsPic.getScaleX() - level.getX()));
            profilePage.addActor(level);

            DrawableText xp = new DrawableText(game.gameFontBold, game.player.xp + "/" + (game.player.rankLevel * 50));
            xp.setScale(0.4f);
            xp.setPosition(lvl.getX(), level.getY() - level.getWidth() - 5);
            profilePage.addActor(xp);

            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault());

            DrawableText registrationDate = new DrawableText(game.gameFontBold, "Registered at " + dateFormat.format(game.player.registrationDate));
            registrationDate.setScale(0.5f);
            registrationDate.setPosition(lvl.getX(), level.getY() - level.getWidth() - game.gameFontBold.getHeight(xp.getText().toString(), xp.getScale()) - 20);
            profilePage.addActor(registrationDate);

            DrawableText lastOnlineDate = new DrawableText(game.gameFontBold, "Was online at " + dateFormat.format(game.player.lastOnlineDate));
            lastOnlineDate.setScale(0.5f);
            lastOnlineDate.setPosition(lvl.getX(), registrationDate.getY() - game.gameFontBold.getHeight(registrationDate.getText().toString(), registrationDate.getScale()) - 20);
            profilePage.addActor(lastOnlineDate);

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String[] stats = new String[12];
            stats[0] = "BATTLES";
            stats[1] = "EFFICIENCY";
            stats[2] = "WIN RATE";
            stats[3] = "ACCURACY";
            stats[4] = "DAMAGE DEALT";
            stats[5] = "DAMAGE TAKEN";
            stats[6] = String.valueOf(game.player.battleCount);
            stats[7] = decimalFormat.format(game.player.getEfficiency() * 100) + "%";
            stats[8] = game.player.battleCount > 0 ? decimalFormat.format((float) game.player.victoriesCount / game.player.battleCount * 100) + "%" : "0%";
            stats[9] = decimalFormat.format(game.player.accuracy * 100) + "%";
            stats[10] = String.valueOf(game.player.damageDealt);
            stats[11] = String.valueOf(game.player.damageTaken);
            final float count = (float) stats.length / 2;
            final float h1 = (statsPic.getHeight() * statsPic.getScaleY() - 50 - 5 * (count - 1)) / count;
            for (int i = 0; i < 6; i++) {
                Image statBg = new Image(game.UIAtlas.findRegion("StatsContainer"));
                statBg.setRotation(270);
                statBg.setPosition(statsPic.getX() + 15, statsPic.getY() + 65 + i * (h1 + 5));
                statBg.setSize(h1, statsPic.getWidth() / 3);
                DrawableText text = new DrawableText(game.gameFontBold, stats[5 - i]);
                text.setScale(h1 * 0.5f / game.gameFontBold.getHeight(text.getText().toString()));
                text.setScale(game.gameFontBold.getWidth(text.getText().toString(), text.getScale()) > statBg.getHeight() * 0.5f ? (statBg.getHeight() * 0.5f) / game.gameFontBold.getWidth(text.getText().toString()) : text.getScale());
                text.setPosition(statBg.getX() + 10, statBg.getY() - (h1 - game.gameFontBold.getHeight(text.getText().toString(), text.getScale())) / 2);
                DrawableText valueText = new DrawableText(game.gameFontBold, stats[11 - i]);
                valueText.setScale(h1 * 0.5f / game.gameFontBold.getHeight(valueText.getText().toString()));
                valueText.setPosition(statBg.getX() + 10 + statBg.getHeight() * 0.6f, statBg.getY() - (h1 - game.gameFontBold.getHeight(valueText.getText().toString(), valueText.getScale())) / 2);
                profilePage.addActor(statBg);
                profilePage.addActor(text);
                profilePage.addActor(valueText);
            }

            Image trophiesContainer = new Image(game.UIAtlas.findRegion("VolumeBarContainer"));
            trophiesContainer.setPosition(statsPic.getX() + statsPic.getWidth() / 3 + 45, statsPic.getY() + 65 + 5 * (h1 + 5));
            trophiesContainer.setRotation(270);
            trophiesContainer.setSize(105, 195);
            profilePage.addActor(trophiesContainer);

            DrawableText trophiesText = new DrawableText(game.gameFontBold, "TROPHIES");
            trophiesText.setScale(trophiesContainer.getHeight() * 0.5f / game.gameFontBold.getWidth(trophiesText.getText().toString()));
            trophiesText.setPosition(trophiesContainer.getX() + trophiesContainer.getHeight() * 0.25f, trophiesContainer.getY() - 15);
            profilePage.addActor(trophiesText);

            DrawableText trophiesCount = new DrawableText(game.gameFontBold, String.valueOf(game.player.trophies));
            trophiesCount.setScale(1.2f);
            trophiesCount.setPosition(trophiesContainer.getX() + trophiesContainer.getHeight() * 0.5f -
                            (game.gameFontBold.getHeight(trophiesCount.getText().toString(), trophiesCount.getScale()) + game.gameFontBold.getWidth(trophiesCount.getText().toString(), trophiesCount.getScale())) / 2,
                    trophiesContainer.getY() - 55);
            profilePage.addActor(trophiesCount);

            Image trophyImage = new Image(game.UIAtlas.findRegion("Trophie"));
            trophyImage.setSize(game.gameFontBold.getHeight(trophiesCount.getText().toString(), trophiesCount.getScale()), game.gameFontBold.getHeight(trophiesCount.getText().toString(), trophiesCount.getScale()));
            trophyImage.setPosition(trophiesCount.getX() + game.gameFontBold.getWidth(trophiesCount.getText().toString(), trophiesCount.getScale()), trophiesContainer.getY() - 55 - trophyImage.getHeight());
            profilePage.addActor(trophyImage);

            Image rankContainer = new Image(game.UIAtlas.findRegion("VolumeBarContainer"));
            rankContainer.setPosition(statsPic.getX() + statsPic.getWidth() / 3 + 45, statsPic.getY() + 165);
            rankContainer.setRotation(270);
            rankContainer.setSize(139, 195);
            profilePage.addActor(rankContainer);

            DrawableText rankText = new DrawableText(game.gameFontBold, "TROPHIES RANK");
            rankText.setScale(rankContainer.getHeight() * 0.8f / game.gameFontBold.getWidth(rankText.getText().toString()));
            rankText.setPosition(rankContainer.getX() + rankContainer.getHeight() * 0.1f, rankContainer.getY() - 15);
            profilePage.addActor(rankText);

            DrawableText rankLevel = new DrawableText(game.gameFontBold, String.valueOf(game.player.trophies / 100 + 1));
            rankLevel.setScale(rankText.getScale());
            rankLevel.setPosition(rankContainer.getX() + rankContainer.getHeight() * 0.5f - game.gameFontBold.getWidth(rankLevel.getText().toString(), rankText.getScale()) / 2, rankContainer.getY() - 20 - game.gameFontBold.getHeight(rankText.getText().toString(), rankText.getScale()));
            profilePage.addActor(rankLevel);

            Image rnkBg = new Image(game.UIAtlas.findRegion("StatsContainer"));
            rnkBg.setSize(75, 75);
            Image rankImage = new Image(game.ranksAtlas.findRegion("rank" + (game.player.trophies / 100 > 10 ? "0" + (game.player.trophies / 100) : "00" + (game.player.trophies / 100))));
            rankImage.setSize(65, 65);
            rankImage.setPosition(rankContainer.getX() + rankContainer.getHeight() * 0.5f - rnkBg.getWidth() / 2 + 5, rankLevel.getY() - game.gameFontBold.getHeight(rankText.getText().toString(), rankText.getScale()) - 10 - rankImage.getHeight());
            rnkBg.setPosition(rankContainer.getX() + rankContainer.getHeight() * 0.5f - rnkBg.getWidth() / 2, rankLevel.getY() - game.gameFontBold.getHeight(rankText.getText().toString(), rankText.getScale()) - 15 - rankImage.getHeight());
            profilePage.addActor(rnkBg);
            profilePage.addActor(rankImage);

            profilePage.setVisible(false);
            stage.addActor(profilePage);
            //endregion

            //region PingInfo
            final Image[] bars = new Image[3];
            for (int i = 0; i < 3; i++) {
                bars[i] = new Image(game.UIAtlas.findRegion("VolumeBarEdge"));
                bars[i].setSize(5, 10 + 5 * i);
                bars[i].setPosition(890 + 2 * (i - 1) + 5 * i, 678);
                stage.addActor(bars[i]);
            }

            DrawableText pingText = new DrawableText(game.gameFontBold, "");
            pingText.setPosition(922, 698);
            pingText.setScale(20 / game.gameFontBold.getHeight("1"));
            stage.addActor(pingText);

            Timer pingTimer = new Timer();
            pingTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(Client.client.getServer() != null) {
                        long ping = Client.client.getServer().getLastLatency();
                        if (ping > 0) {
                            pingText.setText(ping + " ms");
                            bars[2].setVisible(!(ping >= 150));
                            bars[1].setVisible(!(ping >= 300));
                        }
                    }
                }
            }, 0, RakNetPeer.PING_SEND_INTERVAL);

            //endregion

            //region Settings Page
            Group settingsPage = new Group();
            settingsPage.setPosition(11, 92);
            settingsPage.setSize(1024, 566);
            settingsPage.setVisible(false);
            stage.addActor(settingsPage);
            //endregion

            //region Pages Buttons
            final float cellsCount = 4;
            TextButton.TextButtonStyle homeStyle = new TextButton.TextButtonStyle();
            homeStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
            homeStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
            homeStyle.font = game.gameFontBold.font;
            homeStyle.fontColor = Color.WHITE;
            TextButton home = new TextButton("HOME", homeStyle);
            final TextButton[] selected = {home};
            final Actor[] selectedPage = {homePage};
            home.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (selected[0] != home) {
                        homeStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        homeStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0] = home;
                        selectedPage[0].setVisible(false);
                        selectedPage[0] = homePage;
                        selectedPage[0].setVisible(true);
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            home.setTransform(true);
            home.getLabel().setFontScale(game.gameFont.getWidth(home.getText().toString()) > table.getWidth() / cellsCount * 0.6f ? table.getWidth() / cellsCount * 0.6f / game.gameFont.getWidth(home.getText().toString()) : 1f);
            table.add(home).height(table.getHeight()).width(table.getWidth() / cellsCount);

            TextButton.TextButtonStyle shopStyle = new TextButton.TextButtonStyle();
            shopStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
            shopStyle.focused = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
            shopStyle.font = game.gameFontBold.font;
            shopStyle.fontColor = Color.WHITE;
            TextButton shop = new TextButton("SHOP", shopStyle);
            shop.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (selected[0] != shop) {
                        shopStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        shopStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0] = shop;
                        selectedPage[0].setVisible(false);
                        selectedPage[0] = shopPage;
                        selectedPage[0].setVisible(true);
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            shop.setTransform(true);
            shop.getLabel().setFontScale(game.gameFont.getWidth(shop.getText().toString()) > table.getWidth() / cellsCount * 0.6f ? table.getWidth() / cellsCount * 0.6f / game.gameFont.getWidth(shop.getText().toString()) : 1f);
            table.add(shop).height(table.getHeight()).width(table.getWidth() / cellsCount);

            TextButton.TextButtonStyle profileStyle = new TextButton.TextButtonStyle();
            profileStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
            profileStyle.focused = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
            profileStyle.font = game.gameFontBold.font;
            profileStyle.fontColor = Color.WHITE;
            TextButton profile = new TextButton("PROFILE", profileStyle);
            profile.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (selected[0] != profile) {
                        profileStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        profileStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0] = profile;
                        selectedPage[0].setVisible(false);
                        selectedPage[0] = profilePage;
                        selectedPage[0].setVisible(true);
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            profile.setTransform(true);
            profile.getLabel().setFontScale(game.gameFont.getWidth(profile.getText().toString()) > table.getWidth() / cellsCount * 0.6f ? table.getWidth() / cellsCount * 0.6f / game.gameFont.getWidth(profile.getText().toString()) : 1f);
            table.add(profile).height(table.getHeight()).width(table.getWidth() / cellsCount);

            TextButton.TextButtonStyle settingsStyle = new TextButton.TextButtonStyle();
            settingsStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
            settingsStyle.focused = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
            settingsStyle.font = game.gameFontBold.font;
            settingsStyle.fontColor = Color.WHITE;
            TextButton settings = new TextButton("SETTINGS", settingsStyle);
            settings.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (selected[0] != settings) {
                        settingsStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        settingsStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                        selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                        selected[0] = settings;
                        selectedPage[0].setVisible(false);
                        selectedPage[0] = settingsPage;
                        selectedPage[0].setVisible(true);
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
            settings.setTransform(true);
            settings.getLabel().setFontScale(game.gameFont.getWidth(settings.getText().toString()) > table.getWidth() / cellsCount * 0.6f ? table.getWidth() / cellsCount * 0.6f / game.gameFont.getWidth(settings.getText().toString()) : 1f);
            table.add(settings).height(table.getHeight()).width(table.getWidth() / cellsCount);

            if(selectedPageNumber.length > 0){
                switch (selectedPageNumber[0]){
                    case 1:
                        if (selected[0] != home) {
                            homeStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            homeStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0] = home;
                            selectedPage[0].setVisible(false);
                            selectedPage[0] = homePage;
                            selectedPage[0].setVisible(true);
                        }
                        break;
                    case 2:
                        if (selected[0] != shop) {
                            shopStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            shopStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0] = shop;
                            selectedPage[0].setVisible(false);
                            selectedPage[0] = shopPage;
                            selectedPage[0].setVisible(true);
                        }
                        break;
                    case 3:
                        if (selected[0] != profile) {
                            profileStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            profileStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0] = profile;
                            selectedPage[0].setVisible(false);
                            selectedPage[0] = profilePage;
                            selectedPage[0].setVisible(true);
                        }
                        break;
                    case 4:
                        if (selected[0] != settings) {
                            settingsStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            settingsStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("SelectedTab"));
                            selected[0].getStyle().up = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0].getStyle().down = new TextureRegionDrawable(game.UIAtlas.findRegion("UnselectedTab"));
                            selected[0] = settings;
                            selectedPage[0].setVisible(false);
                            selectedPage[0] = settingsPage;
                            selectedPage[0].setVisible(true);
                        }
                        break;
                }
            }
            //endregion

            stage.addActor(table);

            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.GET_VERSION}));
            String version = (String) Client.handler.getValue("version");
            DrawableText ver = new DrawableText(game.gameFontBold, "V" + version);
            ver.setScale(70 / game.gameFontBold.getWidth("V" + version));
            ver.setPosition(1202, 109 + game.gameFontBold.getHeight("V" + version, ver.getScaleX()) / 2);
            stage.addActor(ver);

            Image notificationBell = new Image(game.UIAtlas.findRegion("BellIcon"));
            notificationBell.setPosition(1148, 88);
            notificationBell.setSize(42, 42);
            stage.addActor(notificationBell);

            TextButton.TextButtonStyle playStyle = new TextButton.TextButtonStyle();
            playStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("PlayBtn"));
            playStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("PlayBtnPressed"));
            playStyle.font = game.gameFont.font;
            playStyle.pressedOffsetX = 2;
            playStyle.pressedOffsetY = 2;
            TextButton play = new TextButton("Play", playStyle);
            play.setTransform(true);
            play.setScale(0.6f);
            play.setPosition(640 - play.getWidth() * play.getScaleX() / 2, 40 - play.getHeight() * play.getScaleY() / 2);
            play.addListener(new ClickListener(){
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Rectangle rectangle = new Rectangle(play.getX(), play.getY(),
                            play.getWidth() * play.getScaleX(), play.getHeight() * play.getScaleY());
                    if(rectangle.contains(event.getStageX(), event.getStageY())) {
                        try {
                            byte[] data = Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(Stream.of(game.player.tanks).filter(c -> c.id == ((TankImage) tanks.getCell(selectedImg[0]).getActor().getChild(1)).info.id).single().id));
                            data[data.length - 1] = PacketConstants.JOIN_BALANCER;
                            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(data));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        Gdx.app.postRunnable(() -> {
                            GameQueueScreen gameQueueScreen = new GameQueueScreen(game, Stream.of(game.player.tanks).filter(c -> c.id == ((TankImage) tanks.getCell(selectedImg[0]).getActor().getChild(1)).info.id).single());
                            game.setScreen(gameQueueScreen);
                        });
                    }
                    super.touchUp(event, x, y, pointer, button);
                }
            });
            stage.addActor(play);

            //region Friends ScrollPane
            ScrollPane.ScrollPaneStyle friendsStyle = new ScrollPane.ScrollPaneStyle();
            Table friendsTable = new Table();
            ScrollPane friends = new ScrollPane(friendsTable, friendsStyle);
            friends.setPosition(1048, 139);
            friends.setSize(232, 520);
            friends.setScrollingDisabled(true, false);
            friends.setupOverscroll(30, 30, 100);
            friendsTable.align(Align.topLeft);
            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.GET_FRIENDS}));
            String[] fr = (String[]) Client.handler.getValue("friends");
            friendsTableCells = new Array<>();
            if (fr.length > 0) {
                Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.serialize(fr), PacketConstants.GET_ONLINE)));
                boolean[] onl = (boolean[]) Client.handler.getValue("online");
                for (int i = 0; i < fr.length; i++) {
                    ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
                    st.unpressedOffsetX = 40;
                    st.unpressedOffsetY = 12;
                    st.pressedOffsetY = 12;
                    st.pressedOffsetX = 40;
                    st.checkedOffsetX = 40;
                    st.checkedOffsetY = 12;
                    if (i % 2 == 0) {
                        st.down = new TextureRegionDrawable(game.UIAtlas.findRegion("FriendsBg"));
                        st.up = new TextureRegionDrawable(game.UIAtlas.findRegion("FriendsBg"));
                    }
                    ImageButton bt = new ImageButton(st);
                    Image ic = new Image(game.UIAtlas.findRegion("GreenPlayerProfilePic"));
                    final String txt = fr[i];
                    DrawableText text = new DrawableText(game.gameFont, txt);
                    final float h2 = 19 / game.gameFont.getHeight(txt);
                    text.setScale(game.gameFont.getWidth(txt, h2) > 135 ? 135 / game.gameFont.getWidth(txt, h2) * h2 : h2);
                    text.setPosition(45, 42);
                    bt.addActor(text);
                    ic.setSize(40, 40);
                    ic.setPosition(-15, 3);
                    bt.addActor(ic);
                    Image onlineImg = new Image(game.UIAtlas.findRegion(onl[i] ? "OnlineIcon" : "OfflineIcon"));
                    onlineImg.setSize(20, 20);
                    onlineImg.setPosition(45, 0);
                    bt.addActor(onlineImg);
                    DrawableText onlineText = new DrawableText(game.gameFont, onl[i] ? "ONLINE" : "OFFLINE");
                    onlineText.setPosition(65, 16);
                    onlineText.setScale((float) 12 / game.gameFont.getHeight("O"));
                    bt.addActor(onlineText);
                    friendsTable.add(bt).width(232).height(70);
                    friendsTable.row();
                    friendsTableCells.add(new AdvancedCell(friendsTable.getCells().get(friendsTable.getCells().size - 1), true));
                }
            }
            //endregion

            friendsTimer = new Timer();
            friendsTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (friendsTable.getCells().size > 0) {
                        String[] nicks = Stream.of(friendsTable.getCells()).map(a -> ((DrawableText) ((ImageButton) a.getActor()).getChild(1)).getText()).toArray(String[]::new);
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.serialize(nicks), PacketConstants.GET_ONLINE)));
                        boolean[] online = (boolean[]) Client.handler.getValue("online");
                        for (int i = 0; i < online.length; i++) {
                            Cell item = friendsTable.getCells().get(i);
                            DrawableText child2 = ((DrawableText) ((ImageButton) item.getActor()).getChild(4));
                            Image child = ((Image) ((ImageButton) item.getActor()).getChild(3));
                            child.setDrawable(new SpriteDrawable(new Sprite(game.UIAtlas.findRegion(online[i] ? "OnlineIcon" : "OfflineIcon"))));
                            child2.setText(online[i] ? "ONLINE" : "OFFLINE");
                        }
                    }
                    if (dailyTime[0] <= 0) {
                        commonDailyWindow[0].setTouchable(Touchable.disabled);
                        rareDailyWindow[0].setTouchable(Touchable.disabled);
                        epicDailyWindow[0].setTouchable(Touchable.disabled);
                        mythicalDailyWindow[0].setTouchable(Touchable.disabled);
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.GET_DAILY_ITEMS}));
                        dailyItems[0] = (DailyItem[]) Client.handler.getValue("daily_items");

                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.GET_DAILY_TIME}));
                        dailyTime[0] = (Long) Client.handler.getValue("daily_items_time");

                        Gdx.app.postRunnable(()->{
                            commonBg.clearChildren();

                            commonDailyWindow[0].remove();
                            rareDailyWindow[0].remove();
                            epicDailyWindow[0].remove();
                            mythicalDailyWindow[0].remove();
                            commonDailyWindow[0] = createWindow(shopPage, dailyItems[0][0]);
                            rareDailyWindow[0] = createWindow(shopPage, dailyItems[0][1]);
                            epicDailyWindow[0] = createWindow(shopPage, dailyItems[0][2]);
                            mythicalDailyWindow[0] = createWindow(shopPage, dailyItems[0][3]);
                            shopPage.addActor(mythicalDailyWindow[0]);
                            shopPage.addActor(commonDailyWindow[0]);
                            shopPage.addActor(rareDailyWindow[0]);
                            shopPage.addActor(epicDailyWindow[0]);

                            commonTank[0] = new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][0].tankId).single(), game);
                            commonTank[0].setScale(Math.min(commonBg.getHeight() * 0.4f / commonTank[0].getHeight(), 1));
                            commonTank[0].setPosition(commonBg.getWidth() / 2 - commonTank[0].getWidth() / 2, commonBg.getHeight() / 2 - commonTank[0].getHeight() / 2);
                            commonBg.addActor(commonTank[0]);

                            commonTankText[0] = new DrawableText(game.gameFontBold, dailyItems[0][0].count != 0 ? "x" + dailyItems[0][0].count : "New vehicle!");
                            commonTankText[0].setScale(Math.min(commonBg.getHeight() * 0.7f / game.gameFontBold.getWidth(commonTankText[0].getText().toString(), 1), 0.55f));
                            commonTankText[0].setPosition(commonBg.getWidth() / 2 - commonTankText[0].getWidth() / 2, 40 + commonTankText[0].getHeight());
                            commonBg.addActor(commonTankText[0]);

                            commonTankName[0] = new DrawableText(game.gameFontBold, commonTank[0].info.characteristics.name);
                            commonTankName[0].setScale(Math.min(commonBg.getWidth() * 0.7f / game.gameFontBold.getWidth(commonTankName[0].getText().toString(), 1), 0.8f));
                            commonTankName[0].setPosition(commonBg.getWidth() / 2 - commonTankName[0].getWidth() / 2, commonBg.getHeight() - 25);
                            commonBg.addActor(commonTankName[0]);

                            commonCost[0] = new Image(game.UIAtlas.findRegion("Coins"));
                            commonCost[0].setSize(20, 20);
                            commonCostText[0] = new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][0].price));
                            commonCostText[0].setScale(commonCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(commonCostText[0].getText().toString(), 1));
                            commonCostText[0].setPosition(commonBg.getWidth() / 2 - commonCost[0].getWidth() / 2 - commonCostText[0].getWidth() / 2,
                                    commonTankText[0].getY() - commonTankText[0].getHeight() - commonCost[0].getHeight() / 2 + commonCostText[0].getHeight() / 2);
                            commonCost[0].setPosition(commonCostText[0].getX() + commonCostText[0].getWidth() + 5,
                                    commonTankText[0].getY() - commonTankText[0].getHeight() - commonCost[0].getHeight());
                            commonBg.addActor(commonCost[0]);
                            commonBg.addActor(commonCostText[0]);

                            rareBg.clearChildren();
                            rareTank[0] = new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][1].tankId).single(), game);
                            rareTank[0].setScale(Math.min(rareBg.getHeight() * 0.4f / rareTank[0].getHeight(), 1));
                            rareTank[0].setPosition(rareBg.getWidth() / 2 - rareTank[0].getWidth() / 2, rareBg.getHeight() / 2 - rareTank[0].getHeight() / 2);
                            rareBg.addActor(rareTank[0]);

                            rareTankText[0] = new DrawableText(game.gameFontBold, dailyItems[0][1].count != 0 ? "x" + dailyItems[0][1].count : "New vehicle!");
                            rareTankText[0].setScale(Math.min(rareBg.getHeight() * 0.7f / game.gameFontBold.getWidth(rareTankText[0].getText().toString(), 1), 0.55f));
                            rareTankText[0].setPosition(rareBg.getWidth() / 2 - rareTankText[0].getWidth() / 2, 40 + rareTankText[0].getHeight());
                            rareBg.addActor(rareTankText[0]);

                            rareTankName[0] = new DrawableText(game.gameFontBold, rareTank[0].info.characteristics.name);
                            rareTankName[0].setScale(Math.min(rareBg.getWidth() * 0.7f / game.gameFontBold.getWidth(rareTankName[0].getText().toString(), 1), 0.8f));
                            rareTankName[0].setPosition(rareBg.getWidth() / 2 - rareTankName[0].getWidth() / 2, rareBg.getHeight() - 25);
                            rareBg.addActor(rareTankName[0]);

                            rareCost[0] = new Image(game.UIAtlas.findRegion("Coins"));
                            rareCost[0].setSize(20, 20);
                            rareCostText[0] = new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][1].price));
                            rareCostText[0].setScale(rareCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(rareCostText[0].getText().toString(), 1));
                            rareCostText[0].setPosition(rareBg.getWidth() / 2 - rareCost[0].getWidth() / 2 - rareCostText[0].getWidth() / 2,
                                    rareTankText[0].getY() - rareTankText[0].getHeight() - rareCost[0].getHeight() / 2 + rareCostText[0].getHeight() / 2);
                            rareCost[0].setPosition(rareCostText[0].getX() + rareCostText[0].getWidth() + 5,
                                    rareTankText[0].getY() - rareTankText[0].getHeight() - rareCost[0].getHeight());
                            rareBg.addActor(rareCost[0]);
                            rareBg.addActor(rareCostText[0]);

                            epicBg.clearChildren();
                            epicTank[0] = new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][2].tankId).single(), game);
                            epicTank[0].setScale(Math.min(epicBg.getHeight() * 0.4f / epicTank[0].getHeight(), 1));
                            epicTank[0].setPosition(epicBg.getWidth() / 2 - epicTank[0].getWidth() / 2, epicBg.getHeight() / 2 - epicTank[0].getHeight() / 2);
                            epicBg.addActor(epicTank[0]);

                            epicTankText[0] = new DrawableText(game.gameFontBold, dailyItems[0][2].count != 0 ? "x" + dailyItems[0][2].count : "New vehicle!");
                            epicTankText[0].setScale(Math.min(epicBg.getHeight() * 0.7f / game.gameFontBold.getWidth(epicTankText[0].getText().toString(), 1), 0.55f));
                            epicTankText[0].setPosition(epicBg.getWidth() / 2 - epicTankText[0].getWidth() / 2, 40 + epicTankText[0].getHeight());
                            epicBg.addActor(epicTankText[0]);

                            epicTankName[0] = new DrawableText(game.gameFontBold, epicTank[0].info.characteristics.name);
                            epicTankName[0].setScale(Math.min(epicBg.getWidth() * 0.7f / game.gameFontBold.getWidth(epicTankName[0].getText().toString(), 1), 0.8f));
                            epicTankName[0].setPosition(epicBg.getWidth() / 2 - epicTankName[0].getWidth() / 2, epicBg.getHeight() - 25);
                            epicBg.addActor(epicTankName[0]);

                            epicCost[0] = new Image(game.UIAtlas.findRegion("Coins"));
                            epicCost[0].setSize(20, 20);
                            epicCostText[0] = new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][2].price));
                            epicCostText[0].setScale(epicCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(epicCostText[0].getText().toString(), 1));
                            epicCostText[0].setPosition(epicBg.getWidth() / 2 - epicCost[0].getWidth() / 2 - epicCostText[0].getWidth() / 2,
                                    epicTankText[0].getY() - epicTankText[0].getHeight() - epicCost[0].getHeight() / 2 + epicCostText[0].getHeight() / 2);
                            epicCost[0].setPosition(epicCostText[0].getX() + epicCostText[0].getWidth() + 5,
                                    epicTankText[0].getY() - epicTankText[0].getHeight() - epicCost[0].getHeight());
                            epicBg.addActor(epicCost[0]);
                            epicBg.addActor(epicCostText[0]);

                            mythicalBg.clearChildren();
                            mythicalTank[0] = new TankImage(Stream.of(game.tanks).filter(c -> c.id == dailyItems[0][3].tankId).single(), game);
                            mythicalTank[0].setScale(Math.min(mythicalBg.getHeight() * 0.4f / mythicalTank[0].getHeight(), 1));
                            mythicalTank[0].setPosition(mythicalBg.getWidth() / 2 - mythicalTank[0].getWidth() / 2, mythicalBg.getHeight() / 2 - mythicalTank[0].getHeight() / 2);
                            mythicalBg.addActor(mythicalTank[0]);

                            mythicalTankText[0] = new DrawableText(game.gameFontBold, dailyItems[0][3].count != 0 ? "x" + dailyItems[0][3].count : "New vehicle!");
                            mythicalTankText[0].setScale(Math.min(mythicalBg.getHeight() * 0.7f / game.gameFontBold.getWidth(mythicalTankText[0].getText().toString(), 1), 0.55f));
                            mythicalTankText[0].setPosition(mythicalBg.getWidth() / 2 - mythicalTankText[0].getWidth() / 2, 40 + mythicalTankText[0].getHeight());
                            mythicalBg.addActor(mythicalTankText[0]);

                            mythicalTankName[0] = new DrawableText(game.gameFontBold, mythicalTank[0].info.characteristics.name);
                            mythicalTankName[0].setScale(Math.min(mythicalBg.getWidth() * 0.7f / game.gameFontBold.getWidth(mythicalTankName[0].getText().toString(), 1), 0.8f));
                            mythicalTankName[0].setPosition(mythicalBg.getWidth() / 2 - mythicalTankName[0].getWidth() / 2, mythicalBg.getHeight() - 25);
                            mythicalBg.addActor(mythicalTankName[0]);

                            mythicalCost[0] = new Image(game.UIAtlas.findRegion("Coins"));
                            mythicalCost[0].setSize(20, 20);
                            mythicalCostText[0] = new DrawableText(game.gameFontBold, String.valueOf(dailyItems[0][3].price));
                            mythicalCostText[0].setScale(mythicalCost[0].getHeight() * 0.6f / game.gameFontBold.getHeight(mythicalCostText[0].getText().toString(), 1));
                            mythicalCostText[0].setPosition(mythicalBg.getWidth() / 2 - mythicalCost[0].getWidth() / 2 - mythicalCostText[0].getWidth() / 2,
                                    mythicalTankText[0].getY() - mythicalTankText[0].getHeight() - mythicalCost[0].getHeight() / 2 + mythicalCostText[0].getHeight() / 2);
                            mythicalCost[0].setPosition(mythicalCostText[0].getX() + mythicalCostText[0].getWidth() + 5,
                                    mythicalTankText[0].getY() - mythicalTankText[0].getHeight() - mythicalCost[0].getHeight());
                            mythicalBg.addActor(mythicalCost[0]);
                            mythicalBg.addActor(mythicalCostText[0]);
                        });
                        commonDailyWindow[0].setTouchable(Touchable.enabled);
                        rareDailyWindow[0].setTouchable(Touchable.enabled);
                        epicDailyWindow[0].setTouchable(Touchable.enabled);
                        mythicalDailyWindow[0].setTouchable(Touchable.enabled);
                    }
                    dailyTime[0] -= TimeUnit.SECONDS.toMillis(1);
                    long time = dailyTime[0];
                    long hours = TimeUnit.MILLISECONDS.toHours(time);
                    time -= TimeUnit.HOURS.toMillis(hours);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
                    time -= TimeUnit.MINUTES.toMillis(minutes);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
                    dailyTimeText.setText((String.valueOf(hours).length() == 1 ? "0" + hours : hours) + ":" +
                            (String.valueOf(minutes).length() == 1 ? "0" + minutes : minutes) + ":" +
                            (String.valueOf(seconds).length() == 1 ? "0" + seconds : seconds));
                }
            }, 0, 1000);
            stage.addActor(friends);

            //region Search Friend Button
            TextField.TextFieldStyle friendsSearchStyle = new TextField.TextFieldStyle();
            friendsSearchStyle.font = game.gameFont.font;
            friendsSearchStyle.fontColor = Color.WHITE;
            friendsSearchStyle.messageFont = game.gameFont.font;
            friendsSearchStyle.background = new TextureRegionDrawable(game.UIAtlas.findRegion("BtnPressed"));
            friendsSearchStyle.cursor = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
            friendsSearchStyle.background.setLeftWidth(friendsSearchStyle.background.getLeftWidth() + 10);
            friendsSearchStyle.background.setRightWidth(friendsSearchStyle.background.getRightWidth() + 10);
            friendsSearchStyle.background.setTopHeight(friendsSearchStyle.background.getTopHeight() + 5);
            friendsSearchStyle.background.setBottomHeight(friendsSearchStyle.background.getBottomHeight() + 5);
            friendsSearchStyle.selection = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
            TextField friendsSearch = new TextField("", friendsSearchStyle);
            friendsSearch.setPosition(1058, 664);
            friendsSearch.setSize(212, 52);
            DrawableText noMatchesText = new DrawableText(game.gameFont, "No matches found");
            noMatchesText.setScale(game.gameFont.getWidth(noMatchesText.getText().toString()) > 212 ? 212 / game.gameFont.getWidth(noMatchesText.getText().toString()) : 1f);
            noMatchesText.setPosition(1058, 640);
            noMatchesText.setVisible(false);
            stage.addActor(noMatchesText);
            friendsSearch.setTextFieldListener((textField, c) -> {
                if (friendsTableCells.size > 0 && !friendsSearching) {
                    for (int i = 0; i < friendsTableCells.size; i++) {
                        AdvancedCell cell = friendsTableCells.get(i);
                        Cell y = cell.cell;
                        DrawableText child = ((DrawableText) ((ImageButton) y.getActor()).getChild(1));
                        cell.visible = ((String) child.getText()).toLowerCase().contains(friendsSearch.getText().toLowerCase());
                    }
                    friendsTable.getCells().clear();
                    friendsTable.clearChildren();
                    for (AdvancedCell friendsTableCell : friendsTableCells) {
                        if (friendsTableCell.visible) {
                            friendsTable.add((ImageButton) friendsTableCell.cell.getActor()).width(232).height(70);
                            friendsTable.row();
                        }
                    }
                    friendsTable.invalidate();
                } else if (friendsSearching) {
                    if ((c == '\r' || c == '\n') && friendsSearch.getText().length() > 0) {
                        noMatchesText.setVisible(false);
                        friendsTable.getCells().clear();
                        friendsTable.clearChildren();
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.serialize(friendsSearch.getText()), PacketConstants.GET_NICKNAME_SEARCH)));
                        String[] nicks = (String[]) Client.handler.getValue("nickSearch");
                        if (nicks.length > 0) {
                            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(addId(Client.handler.secureData.serialize(nicks), PacketConstants.GET_ONLINE)));
                            boolean[] onl = (boolean[]) Client.handler.getValue("online");
                            for (int i = 0; i < nicks.length; i++) {
                                ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
                                st.unpressedOffsetX = 40;
                                st.unpressedOffsetY = 12;
                                st.pressedOffsetY = 12;
                                st.pressedOffsetX = 40;
                                st.checkedOffsetX = 40;
                                st.checkedOffsetY = 12;
                                if (i % 2 == 0) {
                                    st.down = new TextureRegionDrawable(game.UIAtlas.findRegion("FriendsBg"));
                                    st.up = new TextureRegionDrawable(game.UIAtlas.findRegion("FriendsBg"));
                                }
                                ImageButton bt = new ImageButton(st);
                                Image ic = new Image(game.UIAtlas.findRegion("GreenPlayerProfilePic"));
                                final String txt = nicks[i];
                                DrawableText text = new DrawableText(game.gameFont, txt);
                                final float h = 19 / game.gameFont.getHeight(txt);
                                text.setScale(game.gameFont.getWidth(txt, h) > 135 ? 135 / game.gameFont.getWidth(txt, h) * h : h);
                                text.setPosition(45, 42);
                                bt.addActor(text);
                                ic.setSize(40, 40);
                                ic.setPosition(-15, 3);
                                bt.addActor(ic);
                                Image onlineImg = new Image(game.UIAtlas.findRegion(onl[i] ? "OnlineIcon" : "OfflineIcon"));
                                onlineImg.setSize(20, 20);
                                onlineImg.setPosition(45, 0);
                                bt.addActor(onlineImg);
                                DrawableText onlineText = new DrawableText(game.gameFont, onl[i] ? "ONLINE" : "OFFLINE");
                                onlineText.setPosition(65, 16);
                                onlineText.setScale((float) 12 / game.gameFont.getHeight("O"));
                                bt.addActor(onlineText);
                                friendsTable.add(bt).width(232).height(70);
                                friendsTable.row();
                            }
                        } else {
                            noMatchesText.setVisible(true);
                        }
                    }
                }
            });
            //endregion

            stage.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    Rectangle rectangle = new Rectangle(friendsSearch.getX(), friendsSearch.getY(),
                            friendsSearch.getWidth() * friendsSearch.getScaleX(), friendsSearch.getHeight() * friendsSearch.getScaleY());
                    if (!rectangle.contains(event.getStageX(), event.getStageY())) {
                        Gdx.input.setOnscreenKeyboardVisible(false);
                        stage.unfocus(friendsSearch);
                        game.gameFont.setColor(Color.WHITE);
                    }
                    return super.touchDown(event, x, y, pointer, button);
                }
            });

            //region Cancel Friends Search
            TextButton.TextButtonStyle friendSearchCancelStyle = new TextButton.TextButtonStyle();
            friendSearchCancelStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
            friendSearchCancelStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
            friendSearchCancelStyle.font = game.gameFont.font;
            friendSearchCancelStyle.pressedOffsetX = 1;
            friendSearchCancelStyle.pressedOffsetY = 1;
            TextButton friendSearchCancel = new TextButton("Cancel Search", friendSearchCancelStyle);
            friendSearchCancel.setTransform(true);
            friendSearchCancel.setSize(232 * 0.7f, 70 * 0.7f);
            friendSearchCancel.setPosition(1164 - friendSearchCancel.getWidth() * friendSearchCancel.getScaleX() / 2,
                    174 - friendSearchCancel.getHeight() * friendSearchCancel.getScaleY() / 2);
            friendSearchCancel.setVisible(false);
            friendSearchCancel.getLabel().setFontScale(friendSearchCancel.getWidth() * 0.8f / game.gameFont.getWidth(friendSearchCancel.getText().toString()));
            friendSearchCancel.addListener(new ClickListener() {
                @Override
                public void touchUp(InputEvent event, float x, float yy, int pointer, int button) {
                    Rectangle rectangle = new Rectangle(friendSearchCancel.getX(), friendSearchCancel.getY(),
                            friendSearchCancel.getWidth() * friendSearchCancel.getScaleX(), friendSearchCancel.getHeight() * friendSearchCancel.getScaleY());
                    if (rectangle.contains(event.getStageX(), event.getStageY())) {
                        noMatchesText.setVisible(false);
                        friendSearchCancel.setVisible(false);
                        friendsSearching = false;
                        friends.setPosition(1048, 139);
                        friends.setSize(232, 520);
                        friendsSearch.setMessageText(null);
                        friendsSearch.setText("");
                    }
                    friendsTable.getCells().clear();
                    friendsTable.clearChildren();
                    if (friendsTableCells.size > 0) {
                        for (AdvancedCell friendsTableCell : friendsTableCells) {
                            if (friendsTableCell.visible) {
                                friendsTable.add((ImageButton) friendsTableCell.cell.getActor()).width(232).height(70);
                                friendsTable.row();
                            }
                        }
                    }
                    friendsTable.invalidate();
                    super.touchUp(event, x, yy, pointer, button);
                }
            });
            stage.addActor(friendSearchCancel);
            //endregion

            ImageButton.ImageButtonStyle addFriendStyle = new ImageButton.ImageButtonStyle();
            addFriendStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("AddFriendIcon"));
            addFriendStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("AddFriendIcon"));
            ImageButton addFriend = new ImageButton(addFriendStyle);
            addFriend.setPosition(1055, 90);
            addFriend.setSize(40, 40);
            addFriend.addListener(new ClickListener() {
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Rectangle rectangle = new Rectangle(addFriend.getX(), addFriend.getY(),
                            addFriend.getWidth() * addFriend.getScaleX(), addFriend.getHeight() * addFriend.getScaleY());
                    if (rectangle.contains(event.getStageX(), event.getStageY())) {
                        friendsSearch.setMessageText("Search...");
                        friendsTable.getCells().clear();
                        friendsTable.clearChildren();
                        friends.setSize(232, 450);
                        friends.setPosition(1048, 209);
                        friendSearchCancel.setVisible(true);
                        friendsSearching = true;
                    }
                    super.touchUp(event, x, y, pointer, button);
                }
            });

            stage.addActor(addFriend);
            stage.addActor(friendsSearch);

            Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(new byte[]{PacketConstants.BATTLE_EXISTS}));

        } catch (Exception e){

        }
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
        friendsTimer.cancel();
    }
}
