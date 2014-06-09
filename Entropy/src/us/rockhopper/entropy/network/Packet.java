package us.rockhopper.entropy.network;

public class Packet {
	public static class Packet0Player {
		public String name;
	}

	public static class Packet1Ship {
		public String shipName;
		public String name;
		public String ship;
	}

	public static class Packet2InboundSize {
		public String shipName;
		public String name;
		public int size;
	}

	public static class Packet3ShipCompleted {
		public String shipName;
		public String name;
		public boolean signal;
	}

	public static class Packet4Ready {
		public String name;
		public boolean ready;
	}

	// TODO could probably fix this with a screenSwitch packet determining which screen to switch to
	public static class Packet5GameStart {
	}

	public static class Packet6Key {
		public String name;
		public int keyPress;
		public boolean isDown;
	}

	public static class Packet7PositionUpdate {
		public String name;
		public int partNumber;
		public float x;
		public float y;
		public float angle;
	}

	// TODO combine with the other useless packet for a signal
	public static class Packet8DuelStart {
	}
	// TODO add accounts with unique names to prevent any sort of name overlap from ever becoming problematic
}
