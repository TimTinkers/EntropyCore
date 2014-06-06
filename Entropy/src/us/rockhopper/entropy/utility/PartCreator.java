package us.rockhopper.entropy.utility;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.common.primitives.Bytes;

public class PartCreator {
	public static void main(String[] args) throws UnsupportedEncodingException {
		Scanner scanner = new Scanner(System.in);
		// byte[] payload = new byte[256];
		System.out.println("Please enter a string");
		String shipJSON = scanner.nextLine();
		byte[] data = shipJSON.getBytes("UTF-8");
		List<Byte> originalList = Bytes.asList(data);
		System.out.println("Length " + data.length);
		// for (int i = 1; (256 * i) < data.length; ++i) {
		// System.out.println(i + " " + (i * 256));
		// if ((i * 256) < (data.length - 1)) {
		// System.out.println(data.length);
		// System.out.println(256 * (i - 1));
		// System.out.println(256 * i);
		// payload = Arrays.copyOfRange(data, 256 * (i - 1), 256 * i);
		// } else {
		// payload = Arrays.copyOfRange(data, 256 * (i - 1), data.length);
		// }
		// Packet1Ship packet = new Packet1Ship();
		// packet.ship = new String(payload);
		// System.out.println("[CLIENT] Sending " + packet.ship);
		// }
		int partitionSize = 256;
		List<List<Byte>> partitions = new LinkedList<List<Byte>>();
		for (int i = 0; i < originalList.size(); i += partitionSize) {
			List<Byte> subList = originalList.subList(i, i + Math.min(partitionSize, originalList.size() - i));
			byte[] byteArray = Bytes.toArray(subList);
			System.out.println(new String(byteArray));
			partitions.add(subList);
		}
	}
}
