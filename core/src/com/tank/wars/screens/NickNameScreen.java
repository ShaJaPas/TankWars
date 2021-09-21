package com.tank.wars.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tank.wars.MainGame;
import com.tank.wars.player.Player;
import com.tank.wars.tools.graphics.DrawableText;
import com.tank.wars.tools.graphics.Toast;
import com.tank.wars.tools.network.Client;
import com.tank.wars.tools.network.PacketConstants;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.protocol.Reliability;

public class NickNameScreen implements Screen {

    private MainGame game;
    private Stage stage;
    public Toast.ToastFactory toastFactory;
    public Toast toast;

    public NickNameScreen(MainGame game){
        this.game = game;
        stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
        game.currentStage = stage;

        Image loadingImage = new Image(game.loading);
        loadingImage.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        stage.addActor(loadingImage);


        Group group = new Group();
        group.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);

        Image loginImage = new Image(game.UIAtlas.findRegion("MessageBox"));
        loginImage.setPosition(640 - loginImage.getWidth() / 2, 360 - loginImage.getHeight() / 2);
        group.addActor(loginImage);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = null;
        style.down = null;
        ImageButton invbtn = new ImageButton(style);
        invbtn.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        invbtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.input.setOnscreenKeyboardVisible(false);
                stage.unfocusAll();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        group.addActor(invbtn);

        DrawableText loginText = new DrawableText(game.gameFont, "Nickname");
        loginText.setPosition(620 - game.gameFont.getWidth("Nickname") / 2, 590);
        group.addActor(loginText);

        DrawableText emailText = new DrawableText(game.gameFont, "Create your nickname");
        emailText.setPosition(640 - game.gameFont.getWidth("Create your nickname") / 2, 490);
        group.addActor(emailText);

        TextField.TextFieldStyle nicknameStyle = new TextField.TextFieldStyle();
        nicknameStyle.font = game.gameFont.font;
        nicknameStyle.fontColor = Color.WHITE;
        nicknameStyle.messageFont = game.gameFont.font;
        nicknameStyle.background = new TextureRegionDrawable(game.UIAtlas.findRegion("BtnPressed"));
        nicknameStyle.cursor = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
        nicknameStyle.background.setLeftWidth(nicknameStyle.background.getLeftWidth() + 10);
        nicknameStyle.background.setRightWidth(nicknameStyle.background.getRightWidth() + 10);
        nicknameStyle.background.setTopHeight(nicknameStyle.background.getTopHeight() + 5);
        nicknameStyle.background.setBottomHeight(nicknameStyle.background.getBottomHeight() + 5);
        nicknameStyle.selection = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
        TextField nickname = new TextField("", nicknameStyle);
        nickname.setPosition(485, 330);
        nickname.setWidth(310);
        group.addActor(nickname);

        TextButton.TextButtonStyle createStyle = new TextButton.TextButtonStyle();
        createStyle.font = game.gameFont.font;
        createStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
        createStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
        createStyle.pressedOffsetX = -2;
        createStyle.pressedOffsetY = -2;
        TextButton create = new TextButton("Create", createStyle);
        create.setTransform(true);
        create.setScale(0.7f);
        create.setPosition(640 - create.getWidth() * create.getScaleX() / 2, 200);
        create.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(create.getX(), create.getY(),
                        create.getWidth() * create.getScaleX(), create.getHeight() * create.getScaleY());
                if(rectangle.contains(event.toCoordinates(group, new Vector2(group.getX(), group.getY())).x, event.toCoordinates(group, new Vector2(group.getX(), group.getY())).y)) {
                    try {
                        byte[] data = addId(Client.handler.secureData.serialize(nickname.getText()), PacketConstants.FIRST_NICKNAME_REQUEST);
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, Client.newPacket(data));
                        if((Boolean) Client.handler.getValue("nick")){
                            Gdx.app.postRunnable(() -> {
                                game.menuScreen = new MenuScreen(game);
                                if (!game.getScreen().getClass().equals(ChestOpenScreen.class)) {
                                    game.setScreen(game.menuScreen);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
        group.addActor(create);

        group.setPosition(0, -50);
        stage.addActor(group);

        toastFactory = new Toast.ToastFactory.Builder()
                .font(game.gameFont.generateFont(12, Color.WHITE))
                .fadingDuration(0.8f)
                .positionY(60)
                .build();
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

        if(toast != null)
            toast.render(delta);
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
        stage.dispose();
    }
}
