package be.algielen.randompicture.logic;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageLoader extends Thread {
    private Logger logger = LoggerFactory.getLogger(ImageLoader.class);
	private boolean running = true;
	private final ConcurrentLinkedQueue<Image> nextPictures;
	private final ConcurrentLinkedQueue<Path> nextPicturesPaths;
	private Set<Path> excluded;
	private List<Path> filepaths;
	private final int size;
	private final PathMatcher acceptedExtensions;
	private RandomSelector randomSelector;

	public ImageLoader(ConcurrentLinkedQueue<Image> nextPictures, List<Path> filepaths,
					   ConcurrentLinkedQueue<Path> nextPicturesPaths, int size) {
		this.nextPictures = nextPictures;
		this.filepaths = filepaths;
		this.nextPicturesPaths = nextPicturesPaths;
		this.excluded = new HashSet<>();
		this.size = size;
		this.acceptedExtensions = FileSystems.getDefault().getPathMatcher("glob:*." + "{jpg,jpeg,png,gif,bmp,tiff,avi}");
		setName(ImageLoader.class.getSimpleName());
	}

	@Override
	public void run() {
		this.randomSelector = new RandomSelector();
		while (running) {
			int i = 0;
			while (nextPictures.size() < size && running) {
				fetchImage();
				i++;
			}
            logger.debug("fetched " + i + " pictures");
			try {
				synchronized (nextPictures) {
					nextPictures.wait(1000);
				}
			} catch (InterruptedException e) {
                logger.error("Error in run ", e);
			}
		}
	}

	public void finish() {
		running = false;
	}

	private void fetchImage() {
		Path picture = null;
		while (picture == null && running) {
			try {
				Optional<Path> optional = randomSelector.getRandomFileIn(filepaths);

				if (optional.isPresent()) {
					Path newFile = optional.get();

					if (!excluded.contains(newFile)) {
						if (isAPicture(newFile)) {
							picture = newFile;
						} else {
							excluded.add(newFile);
                            logger.info("Excluded " + newFile);
						}
					}
				}
			} catch (Exception e) {
                logger.error("Error in fetchImage ", e);
			}

		}
		if (picture != null) {
            logger.debug("Attempting to load : " + picture);

			String uri = picture.toUri().toString();
			Image image = new Image(uri);
			nextPictures.offer(image);
			nextPicturesPaths.offer(picture);
		}
	}

	private boolean isAPicture(Path picture) {
		if (picture == null) {
			return false;
		} else if (Files.isDirectory(picture)) {
			return false;
		}
		return acceptedExtensions.matches(picture.getFileName());
	}
}
