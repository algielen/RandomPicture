package be.algielen.randompicture.logic;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;


public class Utils {
	public static String getFileExtension(File file) {
		String extension = "";
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}

	public static String getFileExtension(Path file) {
		if (!Files.isRegularFile(file)) {
			return "";
		}
		String extension = "";
		String fileName = file.getFileName().toString();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}

}
