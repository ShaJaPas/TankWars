package com.tank.wars.tools.encryption;

import com.badlogic.gdx.math.Vector2;
import com.tank.wars.player.*;
import com.tank.wars.player.map.MapPacket;
import com.tank.wars.player.chest.Chest;
import com.tank.wars.player.chest.ChestName;
import com.tank.wars.player.chest.DailyItem;
import com.tank.wars.player.map.Map;
import com.tank.wars.player.map.MapObjects;
import com.tank.wars.tools.network.PacketConstants;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SecureData {
    public EncryptionAES aes;
    private byte[] key;
    private final MessagePack msgPack;

    public SecureData() throws NoSuchPaddingException, NoSuchAlgorithmException {
        aes = new EncryptionAES();
        key = EncryptionAES.getRandomKey();
        msgPack = new MessagePack();
        msgPack.register(Tank.class);
        msgPack.register(DailyItem.class);
        msgPack.register(Player.class);
        msgPack.register(SignInData.class);
        msgPack.register(SignUpData.class);
        msgPack.register(UnBalancedReason.class);
        msgPack.register(ChestName.class);
        msgPack.register(Chest.class);
        msgPack.register(MapObjects.class);
        msgPack.register(Map.class);
        msgPack.register(GamePlayerData.Bullet.class);
        msgPack.register(GamePlayerData.class);
        msgPack.register(PlayerDataPacket.class);
        msgPack.register(MapPacket.class);
        msgPack.register(BattleResultsEnum.class);
        msgPack.register(BattleResults.class);
        msgPack.register(RotationPacket.class);
        msgPack.register(ExplosionPacket.class);
    }

    public void setKey(byte[] key, byte[] iv) {
        this.key = key;
        aes.setKey(key, iv);
    }

    public byte[] getKey(){
        return key;
    }

    public byte[] makeDataSecure(byte[] array) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] iv = EncryptionAES.getRandomIV();
        aes.setIV(iv);
        array = aes.encrypt(array);
        byte[] result = new byte[array.length + 17];
        System.arraycopy(array, 0, result, 0, array.length);
        System.arraycopy(iv, 0, result, array.length, 16);
        result[result.length - 1] = PacketConstants.AES_KEY_PACKET;
        return result;
    }

    public byte[] makeDataSecureDefault(byte[] array) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] iv = EncryptionAES.getRandomIV();
        EncryptionAES aes1 = new EncryptionAES();
        aes1.setKey(EncryptionAES.defaultKey, iv);
        array = aes1.encrypt(array);
        byte[] result = new byte[array.length + 17];
        System.arraycopy(array, 0, result, 0, array.length);
        System.arraycopy(iv, 0, result, array.length, 16);
        result[result.length - 1] = PacketConstants.AES_KEY_PACKET;
        return result;
    }

    public byte[] makeDataUnSecureDefault(byte[] data) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] iv = new byte[16];
        EncryptionAES aes1 = new EncryptionAES();
        byte[] keydata = new byte[data.length - 17];
        System.arraycopy(data, data.length - 17, iv, 0,16);
        System.arraycopy(data, 0, keydata, 0, data.length - 17);
        aes1.setKey(EncryptionAES.defaultKey, iv);
        keydata = aes1.decrypt(keydata);
        return keydata;
    }

    public byte[] makeDataUnSecure(byte[] data) throws IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException {
        byte[] iv = new byte[16];
        byte[] keydata = new byte[data.length - 17];
        System.arraycopy(data, data.length - 17, iv, 0,16);
        System.arraycopy(data, 0, keydata, 0, data.length - 17);
        aes.setIV(iv);
        keydata = aes.decrypt(keydata);
        return keydata;
    }

    public <T> byte[] serialize(T o){
        try {
            return msgPack.write(o);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public <T> T deserialize(byte[] array, Class<T> tClass) {
        try {
            return msgPack.read(array, tClass);
        } catch (IOException | MessageTypeException e){
            e.printStackTrace();
            return null;
        }
    }
}

