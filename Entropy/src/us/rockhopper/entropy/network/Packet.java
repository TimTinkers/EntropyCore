package us.rockhopper.entropy.network;

public class Packet {
	public static class Packet0Player {
		public String name;
	}

	public static class Packet1Ship {
		public String ship;
		public String name;
	}
	// TODO add accounts with unique names to prevent any sort of name overlap from ever becoming problematic
}
