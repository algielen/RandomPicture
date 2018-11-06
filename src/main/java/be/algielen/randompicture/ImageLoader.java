package be.algielen.randompicture;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.scene.image.Image;

class ImageLoader extends Thread {
	boolean running = true;
	private final ConcurrentLinkedQueue<Image> nextPictures;
	private final ConcurrentLinkedQueue<Path> nextPicturesPaths;
	private List<Path> filepaths;
	private final int size;
	private final PathMatcher acceptedExtensions;
	private RandomSelector randomSelector;

	ImageLoader(ConcurrentLinkedQueue<Image> nextPictures, List<Path> filepaths,
				ConcurrentLinkedQueue<Path> nextPicturesPaths, int size) {
		this.nextPictures = nextPictures;
		this.filepaths = filepaths;
		this.nextPicturesPaths = nextPicturesPaths;
		this.size = size;
		this.acceptedExtensions = FileSystems.getDefault().getPathMatcher("glob:*." + "{jpg,jpeg,png,gif,bmp,tiff,avi}");
		setName(ImageLoader.class.getSimpleName());
	}

	@Override
	public void run() {
		this.randomSelector = new RandomSelector();
		while (running) {
			int i = 0;
			while (nextPictures.size() < size) {
				fetchImage();
				i++;
			}
			System.out.println("fetched " + i + " pictures");
			try {
				synchronized (nextPictures) {
					nextPictures.wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void finish() {
		running = false;
	}

	private void fetchImage() {
		Path picture = null;
		do {
			try {
				Optional<Path> optional = randomSelector.getRandomFileIn(filepaths);
				if (optional.isPresent()) {
					picture = optional.get();
				}
			} catch (Exception e) { // TODO check if necessary
				e.printStackTrace();
			}
			if (picture != null) {
				System.out.println("Attempting to load : " + picture);
			}
		} while (!isAPicture(picture));
		String uri = picture.toUri().toString();
		Image image = new Image(uri);
		nextPictures.offer(image);
		nextPicturesPaths.offer(picture);
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
