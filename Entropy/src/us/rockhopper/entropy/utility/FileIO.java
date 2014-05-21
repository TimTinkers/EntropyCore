package us.rockhopper.entropy.utility;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class FileIO {

	public static void write(String filePath, String jsonString) {
		try {
			Files.write(jsonString, new File(filePath), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String read(String filePath) {
		String jsonString = "";
		try {
			List<String> lines = Files.readLines(new File(filePath),
					Charsets.UTF_8);
			for (String line : lines) {
				System.out.println(line);
				jsonString += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}
