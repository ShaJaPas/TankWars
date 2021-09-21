package com.tank.wars.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.tank.wars.tools.graphics.DrawableText;
import com.tank.wars.tools.graphics.Toast;
import com.tank.wars.tools.network.ChannelsConstants;
import com.tank.wars.tools.network.Client;
import com.tank.wars.tools.network.PacketConstants;
import com.tank.wars.player.SignInData;
import com.tank.wars.player.SignUpData;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.protocol.Reliability;

public class LoginScreen implements Screen {

    private MainGame game;
    private Stage stage;
    public Toast.ToastFactory toastFactory;
    public Toast toast;
    public Input.TextInputListener listener;
    private ImageButton rememberMe;

    public LoginScreen(MainGame game) throws Exception {
        this.game = game;
        stage = new Stage(new FitViewport(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT, game.camera), game.batch);
        game.currentStage = stage;

        Image loadingImage = new Image(game.loading);
        loadingImage.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);
        stage.addActor(loadingImage);

        Group group = new Group();
        group.setSize(MainGame.VIRTUAL_WIDTH, MainGame.VIRTUAL_HEIGHT);

        Image loginImage = new Image(game.UIAtlas.findRegion("Login"));
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

        TextButton.TextButtonStyle signInStyle = new TextButton.TextButtonStyle();
        signInStyle.font = game.gameFont.font;
        signInStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("PlayBtn"));
        signInStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("PlayBtnPressed"));
        signInStyle.pressedOffsetY = 2;
        signInStyle.pressedOffsetX = 2;
        TextButton signIn = new TextButton("Sign In", signInStyle);
        signIn.setTransform(true);
        signIn.setScale(0.7f);
        signIn.setPosition(380, 190);
        group.addActor(signIn);

        String[] login = game.preferenceManager.getLogin();

        TextField.TextFieldStyle emailStyle = new TextField.TextFieldStyle();
        emailStyle.font = game.gameFont.font;
        emailStyle.fontColor = Color.WHITE;
        emailStyle.messageFont = game.gameFont.font;
        emailStyle.background = new TextureRegionDrawable(game.UIAtlas.findRegion("BtnPressed"));
        emailStyle.cursor = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
        emailStyle.background.setLeftWidth(emailStyle.background.getLeftWidth() + 10);
        emailStyle.background.setRightWidth(emailStyle.background.getRightWidth() + 10);
        emailStyle.background.setTopHeight(emailStyle.background.getTopHeight() + 5);
        emailStyle.background.setBottomHeight(emailStyle.background.getBottomHeight() + 5);
        emailStyle.selection = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
        TextField email = new TextField(login.length > 0 ? login[0] : "", emailStyle);
        email.setPosition(605, 450);
        email.setWidth(310);
        group.addActor(email);

        TextField.TextFieldStyle passwordStyle = new TextField.TextFieldStyle();
        passwordStyle.font = game.gameFont.font;
        passwordStyle.fontColor = Color.WHITE;
        passwordStyle.messageFont = game.gameFont.font;
        passwordStyle.background = new TextureRegionDrawable(game.UIAtlas.findRegion("BtnPressed"));
        passwordStyle.cursor = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
        passwordStyle.selection = new TextureRegionDrawable(game.UIAtlas.findRegion("VolumeBarEdge"));
        passwordStyle.background.setLeftWidth(passwordStyle.background.getLeftWidth() + 10);
        passwordStyle.background.setRightWidth(passwordStyle.background.getRightWidth() + 10);
        passwordStyle.background.setTopHeight(passwordStyle.background.getTopHeight() + 5);
        passwordStyle.background.setBottomHeight(passwordStyle.background.getBottomHeight() + 5);
        TextField password = new TextField(login.length > 0 ? login[1] : "", passwordStyle);
        password.setPosition(605, 345);
        password.setPasswordMode(true);
        password.setPasswordCharacter('*');
        password.setWidth(310);
        signIn.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(signIn.getX(), signIn.getY(),
                        signIn.getWidth() * signIn.getScaleX(), signIn.getHeight() * signIn.getScaleY());
                if(rectangle.contains(event.toCoordinates(group, new Vector2(group.getX(), group.getY())).x, event.toCoordinates(group, new Vector2(group.getX(), group.getY())).y)) {
                    try {
                        SignInData signInData = new SignInData(email.getText(), password.getText());
                        byte[] data = Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(signInData));
                        data[data.length - 1] = PacketConstants.SIGN_IN_PACKET;
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, ChannelsConstants.SignInUp, Client.newPacket(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
        group.addActor(password);

        listener = new Input.TextInputListener() {
            @Override
            public void input(String text) {
                try {
                    SignUpData signUpData = new SignUpData(email.getText(), password.getText(), text);
                    byte[] data = Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(signUpData));
                    data[data.length - 1] = PacketConstants.SIGN_UP_PACKET;
                    Client.client.sendMessage(Reliability.RELIABLE_ORDERED, ChannelsConstants.SignInUp, Client.newPacket(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void canceled() {
                Gdx.app.postRunnable(() -> toast = toastFactory.create("Destroying code", Toast.Length.LONG));
                try {
                    SignInData signInData = new SignInData(email.getText(), password.getText());
                    byte[] data = Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(signInData));
                    data[data.length - 1] = PacketConstants.SIGN_UP_PACKET_REMOVE_KEY;
                    Client.client.sendMessage(Reliability.RELIABLE_ORDERED, ChannelsConstants.SignInUp, Client.newPacket(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        TextButton.TextButtonStyle signUpStyle = new TextButton.TextButtonStyle();
        signUpStyle.font = game.gameFont.font;
        signUpStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtn"));
        signUpStyle.down = new TextureRegionDrawable(game.UIAtlas.findRegion("GreenBtnPressed"));
        signUpStyle.pressedOffsetX = -2;
        signUpStyle.pressedOffsetY = -2;
        TextButton signUp = new TextButton("Sign Up", signUpStyle);
        signUp.setTransform(true);
        signUp.setScale(0.7f);
        signUp.setPosition(670, 190);
        signUp.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Rectangle rectangle = new Rectangle(signUp.getX(), signUp.getY(),
                        signUp.getWidth() * signUp.getScaleX(), signUp.getHeight() * signUp.getScaleY());
                if(rectangle.contains(event.toCoordinates(group, new Vector2(group.getX(), group.getY())).x, event.toCoordinates(group, new Vector2(group.getX(), group.getY())).y)) {
                    try {
                        SignInData signInData = new SignInData(email.getText(), password.getText());
                        byte[] data = Client.handler.secureData.makeDataSecure(Client.handler.secureData.serialize(signInData));
                        data[data.length - 1] = PacketConstants.SIGN_UP_PACKET_REQUEST;
                        Client.client.sendMessage(Reliability.RELIABLE_ORDERED, ChannelsConstants.SignInUp, Client.newPacket(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
        group.addActor(signUp);

        ImageButton.ImageButtonStyle remMeStyle = new ImageButton.ImageButtonStyle();
        remMeStyle.up = new TextureRegionDrawable(game.UIAtlas.findRegion("CheckBoxUnChecked"));
        remMeStyle.checked = new TextureRegionDrawable(game.UIAtlas.findRegion("CheckBoxChecked"));
        rememberMe = new ImageButton(remMeStyle);
        rememberMe.setPosition(530, 280);
        rememberMe.setSize(44, 44);
        rememberMe.setChecked(game.preferenceManager.getRememberMe());
        rememberMe.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.preferenceManager.saveRememberMe(rememberMe.isChecked());
                super.touchUp(event, x, y, pointer, button);
            }
        });
        group.addActor(rememberMe);

        toastFactory = new Toast.ToastFactory.Builder()
                .font(game.gameFont.generateFont(12, Color.WHITE))
                .fadingDuration(0.8f)
                .positionY(60)
                .build();

        DrawableText remMeText = new DrawableText(game.gameFont, "Remember me");
        remMeText.setPosition(605, 315);
        remMeText.setScale(0.9f);
        group.addActor(remMeText);

        DrawableText loginText = new DrawableText(game.gameFont, "Login");
        loginText.setPosition(620 - game.gameFont.getWidth("Login") / 2, 590);
        group.addActor(loginText);

        DrawableText emailText = new DrawableText(game.gameFont, "Email");
        emailText.setPosition(430 - game.gameFont.getWidth("Email") / 2, 490);
        group.addActor(emailText);

        DrawableText passText = new DrawableText(game.gameFont, "Password");
        passText.setScale(0.7f);
        passText.setPosition(430 - game.gameFont.getWidth("Password", passText.getScaleX()) / 2, 385);
        group.addActor(passText);

        group.setPosition(0, -50);
        stage.addActor(group);
    }

    public void ShowConfirmation(){
        Gdx.input.getTextInput(listener, "Confirm your email(Check your mailbox)", "", "Code from your email");
    }

    public void saveLogin(String email, String password) throws Exception{
        if(rememberMe.isChecked())
            game.preferenceManager.saveLogin(email, password);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
