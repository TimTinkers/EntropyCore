package us.rockhopper.entropy.network;

public class Packet {
	public static class Packet0Player {
		public String name;
	}

	public static class Packet1Ship {
		public String ship;
		public String name;
	}

	public static class Packet2InboundSize {
		public String name;
		public int size;
	}

	public static class Packet3ShipCompleted {
		public String name;
		public boolean signal;
	}
	// TODO add accounts with unique names to prevent any sort of name overlap from ever becoming problematic
}
