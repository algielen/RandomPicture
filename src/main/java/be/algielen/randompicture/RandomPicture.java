package be.algielen.randompicture;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

class RandomPicture {

	private static Random random = new Random();

	// TODO non recursive
	static Optional<Path> getRandomFileIn(Path path) {
		if (path == null || !Files.exists(path)) {
			return Optional.empty();
		} else if (Files.isRegularFile(path)) {
			return Optional.of(path);
		} else if (Files.isDirectory(path)) {

			try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
				List<Path> children = new ArrayList<>();
				paths.forEach(children::add);

				int size = children.size();
				for (int counter = 0; counter < size; counter++) {
					int randomIndex = random.nextInt(size);
					Path chosenChild = children.get(randomIndex);
					Optional<Path> rndSubFile = getRandomFileIn(chosenChild);

					if (rndSubFile.isPresent()) {
						return rndSubFile;
					}
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return Optional.empty();
	}

	//public static void main(String[] args) {
	//   File rndFile = getRandomFileIn(new File("D:/Libraries/Pictures"));
//
	//   System.out.println(rndFile);
	//  }
}