package com.tank.wars;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.tank.wars.player.Player;
import com.tank.wars.player.TanksInfo;
import com.tank.wars.screens.GameLoadingScreen;
import com.tank.wars.screens.LoginScreen;
import com.tank.wars.screens.MapScreen;
import com.tank.wars.screens.MenuScreen;
import com.tank.wars.screens.NickNameScreen;
import com.tank.wars.tools.data.PreferenceManager;
import com.tank.wars.tools.graphics.Font;

import com.whirvis.jraknet.protocol.Reliability;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainGame extends Game {
	public SpriteBatch batch;
	public OrthographicCamera camera;

	public Stage currentStage;
	public GameLoadingScreen loadingScreen;
	public LoginScreen loginScreen;
	public MenuScreen menuScreen;
	public NickNameScreen nickNameScreen;
	public MapScreen mapScreen;
	public Font gameFont;
	public Font gameFontBold;
	public TextureAtlas UIAtlas;
	public TextureAtlas ranksAtlas;
	public TextureAtlas commonChestAtlas;
	public TextureAtlas bullets;
	public TextureAtlas bodies;
	public TextureAtlas hitExplosion;
	public TextureAtlas explosion;
	public TextureAtlas guns;
	public TextureAtlas sandClock;
	public AssetManager manager;
	public Texture loading;
	public PreferenceManager preferenceManager;
	public Player player;
	public ArrayList<TanksInfo> tanks = new ArrayList<>();

	public static final float VIRTUAL_WIDTH = 1280;
	public static final float VIRTUAL_HEIGHT = 720;
	private FPSLogger logger;

	@Override
	public void create () {
		batch = new SpriteBatch();

		initLog4j2();

		System.out.println(Gdx.app.getGraphics().getGLVersion().getDebugVersionString());
		logger = new FPSLogger();

		manager = new AssetManager();
		manager.load("UI/UI.pack", TextureAtlas.class);
		manager.load("Player/Ranks/Ranks.pack", TextureAtlas.class);
		manager.load("Player/Chest/ChestAnimationCommon.pack", TextureAtlas.class);
		manager.load("Player/TrainingMap.png", Texture.class);
		manager.load("Player/Chest/ChestOpenBg.png", Texture.class);
		manager.load("Player/Tanks/Bullets/Bullets.pack", TextureAtlas.class);
		manager.load("Player/Tanks/TankGuns/Guns.pack", TextureAtlas.class);
		manager.load("Player/Tanks/TankBodies/Bodies.pack", TextureAtlas.class);
		manager.load("UI/Sandclock.pack", TextureAtlas.class);
		manager.load("Player/Explosions/Explosion.pack", TextureAtlas.class);
		manager.load("Player/Explosions/ExplosionHit.pack", TextureAtlas.class);
		loading = new Texture(Gdx.files.internal("LoadingScreen\\Loading.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		gameFont = new Font("Fonts\\RobotoRegular.ttf", 42, Color.WHITE, "Loading textures");
		gameFontBold = new Font("Fonts\\SemiBold.ttf", 42, Color.WHITE, "Loading textures");

		loadingScreen = new GameLoadingScreen(this);
		loadingScreen.init();
		setScreen(loadingScreen);

		Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		Gdx.graphics.setResizable(false);
	}

	private void initLog4j2() {
		// Content of log4j2.xml
		String log4j2xmlFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<Configuration status=\"ERROR\">\n" +
				"\t<Appenders>\n" +
				"\t\t<Console name=\"STD_OUT\" target=\"SYSTEM_OUT\">\n" +
				"\t\t\t<PatternLayout pattern=\"%d{HH:mm:ss} [%level] %logger{1}: %msg%n\" />\n" +
				"\t\t</Console>\n" +
				"\t</Appenders>\n" +
				"\t<Loggers>\n" +
				"\t\t<Root level=\"INFO\">\n" +
				"\t\t\t<AppenderRef ref=\"STD_OUT\" />\n" +
				"\t\t</Root>\n" +
				"\t</Loggers>\n" +
				"</Configuration>";

		InputStream is = new ByteArrayInputStream(log4j2xmlFileContent.getBytes());

		try {
			ConfigurationSource source = new ConfigurationSource(is);
			Configurator.initialize(null, source);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render () {
		if(screen != null)
			screen.render(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
		///log
		logger.log();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		loadingScreen.dispose();
		if(preferenceManager != null)
			preferenceManager.save();
		super.dispose();
	}

	@Override
	public void pause() {
		if(preferenceManager != null)
			preferenceManager.save();
		super.pause();
	}
}
