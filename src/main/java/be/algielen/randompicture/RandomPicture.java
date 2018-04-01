package be.algielen.randompicture;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class RandomPicture {

	private static Random random = new Random();

	public static File getRandomFileIn(File f) {
		if (f == null) {
			return null;
		} else if (f.isFile()) // || f.list().length == 0
			return f;
		else if (f.isDirectory()) {

			Path toPath = f.toPath();
			try (DirectoryStream<Path> paths = Files.newDirectoryStream(toPath)) {
				List<Path> subDirectories = new ArrayList<>();
				paths.forEach(subDirectories::add);

				int size = subDirectories.size();
				for (int counter = 0; counter < size; counter++) {
					int randomIndex = random.nextInt(size);
					Path chosenSubDirectory = subDirectories.get(randomIndex);
					File rndSubFile = getRandomFileIn(chosenSubDirectory.toFile());
					if (rndSubFile != null) {
						return rndSubFile;
					}
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}

	//public static void main(String[] args) {
	//   File rndFile = getRandomFileIn(new File("D:/Libraries/Pictures"));
//
	//   System.out.println(rndFile);
	//  }
}