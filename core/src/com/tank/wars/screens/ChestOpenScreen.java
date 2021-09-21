package com.tank.wars.screens;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tank.wars.MainGame;
import com.tank.wars.player.Tank;
import com.tank.wars.player.TanksInfo;
import com.tank.wars.player.chest.Chest;
import com.tank.wars.tools.graphics.AnimatedImage;
import com.tank.wars.tools.graphics.DrawableText;
import com.tank.wars.tools.graphics.TankImage;

public class ChestOpenScreen implements Screen {

    private Stage stage;
    private AnimatedImage chestAnimation;
    private Group currentLoot;
    public static final int[] cardFull = {10, 50, 100, 300, 500, 1000, 2000, 3000, 5000, 10000};
    public static final int[] upgradeCards = {20, 70, 150, 400, 700, 1500, 3000, 5000, 8000, 13000};

    private int touchId = -1;

    public ChestOpenScreen(MainGame game, Chest chest, int... params) {
        try {
            stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
            game.currentStage = stage;

            Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(1f / 15f, game.commonChestAtlas.getRegions());
            chestAnimation = new AnimatedImage(animation, false);

            Image bg = new Image(game.manager.get("Player/Chest/ChestOpenBg.png", Texture.class));
            bg.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
            stage.addActor(bg);

            chestAnimation.setPosition(MainGame.VIRTUAL_WIDTH / 2 - chestAnimation.getWidth() / 2, 70);
            stage.addActor(chestAnimation);

            Image card = new Image(game.UIAtlas.findRegion("Card"));
            card.setScale(0.4f);
            card.setPosition(chestAnimation.getX() + chestAnimation.getWidth() * 0.81f - card.getWidth() * card.getScaleX() / 3, chestAnimation.getY() + card.getHeight() * card.getScaleY() / 1.7f);
            stage.addActor(card);

            DrawableText cardCount = new DrawableText(game.gameFontBold, String.valueOf(chest.diamonds > 0 ? chest.loot.length + 1 : chest.loot.length));
            cardCount.setScale(card.getWidth() * 0.4f * card.getScaleX() / game.gameFontBold.getWidth(cardCount.getText().toString()));
            cardCount.setPosition(card.getX() + card.getWidth() * card.getScaleX() / 2 - game.gameFontBold.getWidth(cardCount.getText().toString(), cardCount.getScale()) / 2,
                    card.getY() + card.getHeight() * card.getScaleY() / 2 + game.gameFontBold.getHeight(cardCount.getText().toString(), cardCount.getScale()) / 2);
            card.setVisible(false);
            cardCount.setVisible(false);
            stage.addActor(cardCount);
            currentLoot = new Group();
            Image moneyImage = new Image(game.UIAtlas.findRegion("FeaturedItem2"));
            moneyImage.setScale(0.3f);
            currentLoot.setSize(moneyImage.getWidth() * moneyImage.getScaleX(), moneyImage.getHeight() * moneyImage.getScaleY());
            currentLoot.addActor(moneyImage);
            Image coins = new Image(game.UIAtlas.findRegion("Coins"));
            coins.setSize(64,64);
            DrawableText coinsCount = new DrawableText(game.gameFontBold, String.valueOf(chest.coins));
            coinsCount.setScale(coins.getHeight() / game.gameFontBold.getHeight(coinsCount.getText().toString()) * 0.5f);
            coins.setPosition(moneyImage.getX() + moneyImage.getWidth() * moneyImage.getScaleX() / 2 - (coins.getWidth() + game.gameFontBold.getWidth(coinsCount.getText().toString(), coinsCount.getScale()) + 10) / 2 ,
                    coins.getY() + 10 + game.gameFontBold.getHeight(coinsCount.getText().toString(), coinsCount.getScale()) - moneyImage.getHeight() * moneyImage.getScaleY() * 0.25f);
            coinsCount.setPosition(coins.getX() + coins.getWidth() + 10, coins.getY() + coins.getHeight() / 2 + game.gameFontBold.getHeight(coinsCount.getText().toString(), coinsCount.getScale()) / 2);
            currentLoot.addActor(coins);
            currentLoot.addActor(coinsCount);
            DrawableText goldText = new DrawableText(game.gameFontBold, "Gold");
            goldText.setScale(Math.min(moneyImage.getWidth() * moneyImage.getScaleX() * 0.7f / game.gameFontBold.getWidth(goldText.getText().toString(), goldText.getScale()), 1));
            goldText.setPosition(moneyImage.getX() + moneyImage.getWidth() * moneyImage.getScaleX() / 2 - game.gameFontBold.getWidth(goldText.getText().toString(), goldText.getScale()) / 2,
                    coins.getY() + coins.getHeight() + game.gameFontBold.getHeight(goldText.getText().toString(), goldText.getScale()));
            currentLoot.addActor(goldText);
            stage.addActor(currentLoot);
            currentLoot.setVisible(false);
            DrawableText newTankText = new DrawableText(game.gameFontBold, "");
            newTankText.setVisible(false);
            chestAnimation.addListener(() -> {
                currentLoot.setVisible(true);
                newTankText.setVisible(true);
                currentLoot.setScale(0.01f);
            });
            Image chestNameImage = new Image(game.UIAtlas.findRegion("LargeFrame"));
            chestNameImage.setScale(0.2f);
            chestNameImage.setPosition(MainGame.VIRTUAL_WIDTH / 2 - chestNameImage.getWidth() * chestNameImage.getScaleX() / 2,
                    MainGame.VIRTUAL_HEIGHT - 30 - chestNameImage.getHeight() * chestNameImage.getScaleY());
            DrawableText chestName = new DrawableText(game.gameFontBold, chest.name.name() + " CHEST");
            chestName.setScale(Math.min(chestNameImage.getWidth() * chestNameImage.getScaleX() * 0.7f / game.gameFontBold.getWidth(chestName.getText().toString(), chestName.getScale()), 1));
            chestName.setPosition(chestNameImage.getX() + chestNameImage.getWidth() * chestNameImage.getScaleX() / 2 - game.gameFontBold.getWidth(chestName.getText().toString(), chestName.getScale()) / 2,
                    chestNameImage.getY() + chestNameImage.getHeight() * chestNameImage.getScaleY() / 2 + game.gameFontBold.getHeight(chestName.getText().toString(), chestName.getScale()) / 2);
            stage.addActor(chestNameImage);
            stage.addActor(chestName);
            stage.addActor(newTankText);
            stage.addListener(new ClickListener() {
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if(touchId != pointer)
                        return;
                    touchId = -1;
                    if(currentLoot.isVisible() && currentLoot.getScaleX() == 1){
                        int cardCnt = Integer.parseInt(cardCount.getText().toString());
                        if(cardCnt == 0){
                            Gdx.app.postRunnable(() -> {
                                game.menuScreen = new MenuScreen(game, params);
                                game.setScreen(game.menuScreen);
                            });
                            return;
                        }
                        currentLoot.clearChildren();
                        if(cardCnt > chest.loot.length){
                            cardCnt--;
                            cardCount.setText(String.valueOf(cardCnt));
                            Image diamondsImage = new Image(game.UIAtlas.findRegion("FeaturedItem1"));
                            diamondsImage.setScale(0.3f);
                            currentLoot.setSize(diamondsImage.getWidth() * diamondsImage.getScaleX(), diamondsImage.getHeight() * diamondsImage.getScaleY());
                            currentLoot.addActor(diamondsImage);
                            Image coins = new Image(game.UIAtlas.findRegion("Diamond"));
                            coins.setSize(64,64);
                            DrawableText coinsCount = new DrawableText(game.gameFontBold, String.valueOf(chest.diamonds));
                            coinsCount.setScale(coins.getHeight() / game.gameFontBold.getHeight(coinsCount.getText().toString()) * 0.5f);
                            coins.setPosition(moneyImage.getX() + moneyImage.getWidth() * moneyImage.getScaleX() / 2 - (coins.getWidth() + game.gameFontBold.getWidth(coinsCount.getText().toString(), coinsCount.getScale()) + 10) / 2 ,
                                    coins.getY() + 10 + game.gameFontBold.getHeight(coinsCount.getText().toString(), coinsCount.getScale()) - moneyImage.getHeight() * moneyImage.getScaleY() * 0.25f);
                            coinsCount.setPosition(coins.getX() + coins.getWidth() + 10, coins.getY() + coins.getHeight() / 2 + game.gameFontBold.getHeight(coinsCount.getText().toString(), coinsCount.getScale()) / 2);
                            currentLoot.addActor(coins);
                            currentLoot.addActor(coinsCount);
                            DrawableText goldText = new DrawableText(game.gameFontBold, "Diamonds");
                            goldText.setScale(Math.min(moneyImage.getWidth() * moneyImage.getScaleX() * 0.7f / game.gameFontBold.getWidth(goldText.getText().toString(), goldText.getScale()), 1));
                            goldText.setPosition(moneyImage.getX() + moneyImage.getWidth() * moneyImage.getScaleX() / 2 - game.gameFontBold.getWidth(goldText.getText().toString(), goldText.getScale()) / 2,
                                    coins.getY() + coins.getHeight() + game.gameFontBold.getHeight(goldText.getText().toString(), goldText.getScale()));
                            currentLoot.addActor(goldText);
                            currentLoot.setScale(0.01f);
                        } else {
                            cardCnt--;
                            cardCount.setText(String.valueOf(cardCnt));
                            int finalCardCnt = cardCnt;
                            TanksInfo info = Stream.of(game.tanks).filter(c -> c.id == chest.loot[finalCardCnt].id).single();
                            Image bg = null;
                            switch (info.characteristics.rarity){
                                case COMMON:
                                    bg = new Image(game.UIAtlas.findRegion("SmallFeaturedItem1"));
                                    break;
                                case RARE:
                                    bg = new Image(game.UIAtlas.findRegion("FrameBrown"));
                                    break;
                                case EPIC:
                                    bg = new Image(game.UIAtlas.findRegion("FrameBlue"));
                                    break;
                                case MYTHICAL:
                                    bg = new Image(game.UIAtlas.findRegion("FrameGreen"));
                                    break;
                                case LEGENDARY:
                                    bg = new Image(game.UIAtlas.findRegion("FramePink"));
                                    break;
                            }
                            bg.setSize(200, 207);
                            currentLoot.addActor(bg);
                            currentLoot.setSize(bg.getWidth(), bg.getHeight());
                            TankImage tankImage = new TankImage(info, game);
                            tankImage.setPosition(bg.getWidth() / 2 - tankImage.getWidth() / 2, 50);
                            currentLoot.addActor(tankImage);
                            DrawableText tankName = new DrawableText(game.gameFontBold, info.characteristics.name);
                            tankName.setScale(Math.min(currentLoot.getWidth() * 0.7f / game.gameFontBold.getWidth(tankName.getText().toString()), 0.8f));
                            tankName.setPosition(bg.getWidth() / 2 - game.gameFontBold.getWidth(tankName.getText().toString(), tankName.getScale()) / 2,
                                    bg.getY() + bg.getHeight() - 25);
                            currentLoot.addActor(tankName);

                            Image levelContainer = new Image(game.UIAtlas.findRegion("LevelBarContainer"));
                            levelContainer.setSize(bg.getWidth() * 0.7f, 20);
                            levelContainer.setPosition(bg.getX() + bg.getWidth() / 2 - levelContainer.getWidth() / 2, 25);
                            currentLoot.addActor(levelContainer);

                            Optional<Tank> stream = Stream.of(game.player.tanks).filter(c -> c.id == chest.loot[finalCardCnt].id).findFirst();
                            final Tank tank = !stream.isEmpty() ? stream.get() : new Tank(chest.loot[finalCardCnt].id, 1, 0);
                            Image level = new Image(game.UIAtlas.findRegion("LevelBar"));
                            level.setSize(Math.min((float) (tank.count + chest.loot[finalCardCnt].count) / (float) cardFull[tank.level - 1], 1) * levelContainer.getWidth(), 20);
                            level.setPosition(levelContainer.getX(), levelContainer.getY());
                            currentLoot.addActor(level);

                            if (chest.loot[finalCardCnt].count == 0){
                                newTankText.setText("New vehicle!");
                            } else {
                                newTankText.setText("+" + chest.loot[finalCardCnt].count);
                            }
                            newTankText.setScale(Math.min(30 / game.gameFontBold.getHeight(newTankText.getText().toString(), 1), 1));
                            newTankText.setPosition(currentLoot.getX() + currentLoot.getWidth() / 2 - newTankText.getWidth() / 2, 563.5f);

                            DrawableText tankCount = new DrawableText(game.gameFontBold, (tank.count + chest.loot[finalCardCnt].count) + "/" + cardFull[tank.level - 1]);
                            tankCount.setScale(Math.min(levelContainer.getHeight() * 0.6f / game.gameFontBold.getHeight(tankCount.getText().toString()), 1));
                            tankCount.setPosition(levelContainer.getX() + levelContainer.getWidth() / 2 - game.gameFontBold.getWidth(tankCount.getText().toString(), tankCount.getScale()) / 2,
                                    levelContainer.getY() + levelContainer.getHeight() / 2 + game.gameFontBold.getHeight(tankCount.getText().toString(), tankCount.getScale()) / 2);
                            currentLoot.addActor(tankCount);
                            currentLoot.setScale(0.01f);
                        }
                    }
                    chestAnimation.setScale(1);
                    chestAnimation.setPosition(chestAnimation.getX() - chestAnimation.getWidth() * 0.01f, chestAnimation.getY() - chestAnimation.getHeight() * 0.01f);
                    chestAnimation.start();
                    cardCount.setVisible(true);
                    card.setVisible(true);
                    card.setScale(card.getScaleX() / 0.98f);
                    card.setPosition(chestAnimation.getX() + chestAnimation.getWidth() * chestAnimation.getScaleX() * 0.81f - card.getWidth() * card.getScaleX() / 3, chestAnimation.getY() + card.getHeight() * card.getScaleY() / 1.7f);
                    //cardCount.setScale(card.getWidth() * 0.4f * card.getScaleX() / game.gameFontBold.getWidth(cardCount.getText().toString()));
                    cardCount.setPosition(card.getX() + card.getWidth() * card.getScaleX() / 2 - game.gameFontBold.getWidth(cardCount.getText().toString(), cardCount.getScale()) / 2,
                            card.getY() + card.getHeight() * card.getScaleY() / 2 + game.gameFontBold.getHeight(cardCount.getText().toString(), cardCount.getScale()) / 2);
                    super.touchUp(event, x, y, pointer, button);
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (touchId == -1)
                        touchId = pointer;
                    else return super.touchDown(event, x, y, pointer, button);
                    chestAnimation.setScale(0.98f);
                    chestAnimation.setPosition(chestAnimation.getX() + chestAnimation.getWidth() * 0.01f, chestAnimation.getY() + chestAnimation.getHeight() * 0.01f);
                    card.setScale(card.getScaleX() * 0.98f);
                    card.setPosition(chestAnimation.getX() + chestAnimation.getWidth() * chestAnimation.getScaleX() * 0.81f - card.getWidth() * card.getScaleX() / 3, chestAnimation.getY() + card.getHeight() * card.getScaleY() / 1.7f);
                    //cardCount.setScale(card.getWidth() * 0.4f * card.getScaleX() / game.gameFontBold.getWidth(cardCount.getText().toString()));
                    cardCount.setPosition(card.getX() + card.getWidth() * card.getScaleX() / 2 - game.gameFontBold.getWidth(cardCount.getText().toString(), cardCount.getScale()) / 2,
                            card.getY() + card.getHeight() * card.getScaleY() / 2 + game.gameFontBold.getHeight(cardCount.getText().toString(), cardCount.getScale()) / 2);
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(currentLoot.isVisible() && currentLoot.getScaleX() < 1){
            float appearTime = 3;
            currentLoot.setScale(currentLoot.getScaleX() + appearTime * delta);
            if(currentLoot.getScaleY() > 1){
                currentLoot.setScale(1);
            }
            currentLoot.setPosition(MainGame.VIRTUAL_WIDTH / 2 - currentLoot.getWidth() * currentLoot.getScaleX() / 2,
                    MainGame.VIRTUAL_HEIGHT / 2 + 60 - currentLoot.getHeight() * currentLoot.getScaleY() / 2);
        }
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
        stage.clear();
    }
}
