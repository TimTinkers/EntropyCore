package us.rockhopper.entropy.network;

import us.rockhopper.entropy.network.Packet.*;

import com.esotericsoftware.kryo.Kryo;

public class Network {
	public static void register(Kryo kryo) {
		kryo.register(Packet0Player.class);
		kryo.register(Packet1Ship.class);
	}
}
