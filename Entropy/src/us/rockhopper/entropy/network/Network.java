package us.rockhopper.entropy.network;

import us.rockhopper.entropy.network.Packet.Packet0Chat;

import com.esotericsoftware.kryo.Kryo;

public class Network {
	public static void register(Kryo kryo) {
		kryo.register(Packet0Chat.class);
	}
}
