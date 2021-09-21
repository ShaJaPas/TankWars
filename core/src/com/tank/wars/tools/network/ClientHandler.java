package com.tank.wars.tools.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.tank.wars.MainGame;
import com.tank.wars.player.*;
import com.tank.wars.player.map.MapPacket;
import com.tank.wars.player.chest.Chest;
import com.tank.wars.player.chest.DailyItem;
import com.tank.wars.screens.ChestOpenScreen;
import com.tank.wars.screens.MapScreen;
import com.tank.wars.screens.MenuScreen;
import com.tank.wars.screens.NickNameScreen;
import com.tank.wars.tools.encryption.SecureData;
import com.tank.wars.tools.graphics.Toast;
import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.client.RakNetClient;
import com.whirvis.jraknet.client.RakNetClientListener;
import com.whirvis.jraknet.peer.RakNetServerPeer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ClientHandler implements RakNetClientListener {

    public SecureData secureData;
    public MainGame game;
    private final ConcurrentMap<String, Object> requests = new ConcurrentHashMap<>();
    public volatile boolean keySet;

    public ClientHandler(){
    }

    @Override
    public void handleUnknownMessage(RakNetClient client, RakNetServerPeer peer, RakNetPacket packet, int channel) {
        handleMessage(client, peer, packet, channel);
    }

    @Override
    public void handleMessage(RakNetClient client, RakNetServerPeer peer, RakNetPacket packet, int channel) {
        try {
            byte[] data = packet.read(packet.remaining());
            byte id = data[data.length - 1];
            switch (id) {
                case PacketConstants.AES_KEY_PACKET:
                    if(!keySet) {
                        byte[] iv = new byte[16];
                        byte[] keydata = new byte[data.length - 17];
                        System.arraycopy(data, data.length - 17, iv, 0, 16);
                        System.arraycopy(data, 0, keydata, 0, data.length - 17);
                        secureData.aes.setIV(iv);
                        keydata = secureData.aes.decrypt(keydata);
                        secureData.setKey(keydata, iv);
                        keySet = true;
                    }
                    break;
                case PacketConstants.SIGN_UP_ERROR:
                    byte[] newdata = new byte[data.length - 1];
                    System.arraycopy(data, 0, newdata, 0, data.length - 1);
                    Gdx.app.postRunnable(() -> game.loginScreen.toast = game.loginScreen.toastFactory.create(secureData.deserialize(newdata, String.class), Toast.Length.LONG));
                    break;
                case PacketConstants.SIGN_UP_OK:
                    game.loginScreen.ShowConfirmation();
                    break;
                case PacketConstants.SIGN_UP_SUCCESS:
                    SignUpData signUpData = secureData.deserialize(secureData.makeDataUnSecure(data), SignUpData.class);
                    game.loginScreen.saveLogin(signUpData.email, signUpData.password);
                    Gdx.app.postRunnable(() -> {
                        game.nickNameScreen = new NickNameScreen(game);
                        game.setScreen(game.nickNameScreen);
                    });
                    break;
                case PacketConstants.SIGN_IN_SUCCESS:
                    SignInData signInData = secureData.deserialize(secureData.makeDataUnSecure(data), SignInData.class);
                    game.loginScreen.saveLogin(signInData.email, signInData.password);
                    Gdx.app.postRunnable(() -> {
                        game.menuScreen = new MenuScreen(game);
                        game.setScreen(game.menuScreen);
                    });
                    break;
                case PacketConstants.SIGN_IN_SUCCESS_WITHOUT_NICK:
                    SignInData signInData2 = secureData.deserialize(secureData.makeDataUnSecure(data), SignInData.class);
                    game.loginScreen.saveLogin(signInData2.email, signInData2.password);
                    Gdx.app.postRunnable(() -> {
                        game.nickNameScreen = new NickNameScreen(game);
                        game.setScreen(game.nickNameScreen);
                    });
                    break;
                case PacketConstants.FIRST_NICKNAME_OK:
                    requests.put("nick", true);
                    break;
                case PacketConstants.FIRST_NICKNAME_ERROR:
                    requests.put("nick", false);
                    byte[] newdata2 = new byte[data.length - 1];
                    System.arraycopy(data, 0, newdata2, 0, data.length - 1);
                    Gdx.app.postRunnable(() -> game.nickNameScreen.toast = game.nickNameScreen.toastFactory.create(secureData.deserialize(newdata2, String.class), Toast.Length.LONG));
                    break;
                case PacketConstants.GET_VERSION:
                    byte[] datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("version", secureData.deserialize(datanew, String.class));
                    break;
                case PacketConstants.GET_FRIENDS:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("friends", secureData.deserialize(datanew, String[].class));
                    break;
                case PacketConstants.GET_ONLINE:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("online", secureData.deserialize(datanew, boolean[].class));
                    break;
                case PacketConstants.GET_NICKNAME_SEARCH:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("nickSearch", secureData.deserialize(datanew, String[].class));
                    break;
                case PacketConstants.GET_PLAYER_PROFILE:
                    datanew = secureData.makeDataUnSecure(data);
                    requests.put("player_profile", secureData.deserialize(datanew, Player.class));
                    break;
                case PacketConstants.GET_NICKNAME:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("my_nickname", secureData.deserialize(datanew, String.class));
                    break;
                case PacketConstants.GET_CHEST:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    Gdx.app.postRunnable(() -> {
                        ChestOpenScreen screen = new ChestOpenScreen(game, secureData.deserialize(datanew, Chest.class));
                        game.setScreen(screen);
                    });
                    break;
                case PacketConstants.GET_DAILY_TIME:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("daily_items_time", secureData.deserialize(datanew, Long.class));
                    break;
                case PacketConstants.GET_DAILY_ITEMS:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("daily_items", secureData.deserialize(datanew, DailyItem[].class));
                    break;
                case PacketConstants.BUY_DAILY_ITEM_OK:
                    requests.put("daily_item_buy", true);
                    break;
                case PacketConstants.BUY_DAILY_ITEM_ERROR:
                    requests.put("daily_item_buy", false);
                    break;
                case PacketConstants.UPGRADE_CARD_OK:
                    requests.put("upgrade_card", true);
                    break;
                case PacketConstants.UPGRADE_CARD_ERROR:
                    requests.put("upgrade_card", false);
                    break;
                case PacketConstants.BUY_CHEST_OK | PacketConstants.BUY_CHEST_ERROR:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    requests.put("buy_chest", secureData.deserialize(datanew, Chest.class) == null ? new Chest() : secureData.deserialize(datanew, Chest.class));
                    break;
                case PacketConstants.EXIT_BALANCER:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    UnBalancedReason reason = secureData.deserialize(secureData.makeDataUnSecure(datanew), UnBalancedReason.class);
                    switch (reason) {
                        case TIMEOUT:
                            Gdx.app.postRunnable(() -> game.setScreen(game.menuScreen));
                            break;
                    }
                    break;
                case PacketConstants.MAP_FOUND:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    MapPacket mapPacket = secureData.deserialize(datanew, MapPacket.class);
                    Gdx.app.postRunnable(() -> {
                        game.mapScreen = new MapScreen(game, mapPacket);
                        game.setScreen(game.mapScreen);
                    });
                    break;
                case PacketConstants.BATTLE_DATA:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    PlayerDataPacket dataPacket = secureData.deserialize(datanew, PlayerDataPacket.class);
                    if(game.mapScreen != null){
                        if(game.mapScreen.playerDataPacket == null)
                            game.mapScreen.startTime = dataPacket.timeLeft;
                         requests.put("battle_data", dataPacket);
                    }
                    break;
                case PacketConstants.BATTLE_STARTED:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    if(game.mapScreen != null){
                        game.mapScreen.battleStarted(secureData.deserialize(datanew, Long.class));
                    }
                    break;
                case PacketConstants.BATTLE_ENDS:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    if(game.mapScreen != null){
                        game.mapScreen.battleEnds(secureData.deserialize(datanew, BattleResults.class));
                    }
                    break;
                case PacketConstants.BULLET_EXPLOSION:
                    datanew = new byte[data.length - 1];
                    System.arraycopy(data, 0, datanew, 0, datanew.length);
                    ExplosionPacket explosionPacket = secureData.deserialize(datanew, ExplosionPacket.class);
                    if(requests.containsKey("explosions")){
                        ((List<ExplosionPacket>) requests.get("explosions")).add(explosionPacket);
                    } else {
                        List<ExplosionPacket> vector2s = new ArrayList<>();
                        vector2s.add(explosionPacket);
                        requests.put("explosions", vector2s);
                    }
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect(RakNetClient client, InetSocketAddress address, RakNetServerPeer peer, String reason) {
        Gdx.app.postRunnable(() -> {
            game.setScreen(game.loadingScreen);
            game.loadingScreen.unreachable = game.loadingScreen.toastFactory.create("Connection lost", Toast.Length.LONG);
        });
        System.out.println(reason);
    }

    public boolean containsRequest(String key){
        return requests.containsKey(key);
    }
    public Object getValue(String key) {
        Callable<Object> task = () -> {
            long a = System.currentTimeMillis();
            while (!Client.client.isDisconnected()) {
                if (requests.containsKey(key)) break;
                Thread.sleep(0, 1);
            }
            Object val = requests.get(key);
            requests.remove(key);
            return val;
        };
        try {
            return task.call();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
