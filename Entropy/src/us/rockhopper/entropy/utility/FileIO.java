package us.rockhopper.entropy.utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
			List<String> lines = Files.readLines(new File(filePath), Charsets.UTF_8);
			for (String line : lines) {
				jsonString += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	public static void delete(String filePath) {
		try {
			File file = new File(filePath);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<File> getFilesForFolder(final File folder) {
		ArrayList<File> files = new ArrayList<File>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				String temp = fileEntry.getName();
				if ((temp.substring(temp.lastIndexOf('.') + 1, temp.length()).toLowerCase()).equals("json"))
					files.add(fileEntry);
			}
		}
		return files;
	}

	public static boolean exists(String string) {
		return new File(string).isFile();
	}
}
