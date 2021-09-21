package com.tank.wars.tools.network;
import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.RakNet;
import com.whirvis.jraknet.RakNetException;
import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.client.RakNetClient;

import java.net.UnknownHostException;

public class Client {
    public static RakNetClient client;
    public static ClientHandler handler;

    public static void init(){
        client = new RakNetClient();
        RakNet.setMaxPacketsPerSecond(Long.MAX_VALUE);
        handler = new ClientHandler();
        client.addListener(handler);
    }

    public static void connect(String host, int port) throws RakNetException, UnknownHostException {
        client.connect(host, port);
    }

    public static Packet newPacket(byte[] content){
        Packet packet = new Packet();
        packet.writeUnsignedByte(RakNetPacket.ID_USER_PACKET_ENUM);
        packet.write(content);
        return packet;
    }
}
