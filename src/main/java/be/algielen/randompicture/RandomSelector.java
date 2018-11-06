package be.algielen.randompicture;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

class RandomSelector {

	private ThreadLocalRandom random = ThreadLocalRandom.current();

	Optional<Path> getRandomFileIn(List<Path> paths) {
		return getRandomFileIn(randomChoice(paths));
	}

	// TODO non recursive
	Optional<Path> getRandomFileIn(Path path) {
		if (path == null || !Files.exists(path)) {
			return Optional.empty();
		} else if (Files.isRegularFile(path)) {
			return Optional.of(path);
		} else if (Files.isDirectory(path)) {

			try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
				List<Path> children = new ArrayList<>();
				paths.forEach(children::add);

				// randomly choosing elements until one is valid or there are none left
				while (!children.isEmpty()) {
					Path chosenChild = randomChoice(children);
					Optional<Path> rndSubFile = getRandomFileIn(chosenChild);

					if (rndSubFile.isPresent()) {
						return rndSubFile;
					} else {
						children.remove(chosenChild);
					}
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return Optional.empty();
	}


	private <ELEMENT> ELEMENT randomChoice(List<ELEMENT> list) {
		int size = list.size();
		int index = random.nextInt(size);
		return list.get(index);
	}

	//public static void main(String[] args) {
	//   File rndFile = getRandomFileIn(new File("D:/Libraries/Pictures"));
//
	//   System.out.println(rndFile);
	//  }
}