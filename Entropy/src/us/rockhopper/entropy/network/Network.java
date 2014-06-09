package us.rockhopper.entropy.network;

import us.rockhopper.entropy.network.Packet.*;

import com.esotericsoftware.kryo.Kryo;

public class Network {
	public static void register(Kryo kryo) {
		kryo.register(Packet0Player.class);
		kryo.register(Packet1Ship.class);
		kryo.register(Packet2InboundSize.class);
		kryo.register(Packet3ShipCompleted.class);
		kryo.register(Packet4Ready.class);
		kryo.register(Packet5GameStart.class);
		kryo.register(Packet6Key.class);
		kryo.register(Packet7PositionUpdate.class);
		kryo.register(Packet8DuelStart.class);
	}
}
