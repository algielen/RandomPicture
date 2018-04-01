package be.algielen.randompicture;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import darrylbu.icon.StretchIcon;

class ImageFetcher extends Thread {
	private Vector<StretchIcon> nextPictures;
	private Vector<File> nextPicturesPathes;
	private File filepath;
	private final int SIZE = 10;
	private static List<String> acceptedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff");

	ImageFetcher(Vector<StretchIcon> nextPictures, File filepath,
				 Vector<File> nextPicturesPathes) {
		this.nextPictures = nextPictures;
		this.filepath = filepath;
		this.nextPicturesPathes = nextPicturesPathes;
	}

	public void run() {
		while (true) {
			int i = 0;
			while (nextPictures.size() < SIZE) {
				fetchImage();
				i++;
			}
			System.out.println("Fetched " + i + " pictures.");
			try {
				synchronized (nextPictures) {
					nextPictures.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void fetchImage() {
		String ext = "";
		File picture;
		do {
			try {
				picture = RandomPicture.getRandomFileIn(filepath);
			} catch (Exception e) {
				picture = null;
				e.printStackTrace();
			}
			if (picture != null) {
				System.out.println(String.format("Attempting to load %s",
						picture.getPath()));
				ext = Utils.getExt(picture);
				// System.out.println(ext);
			}
		} while (picture == null || !acceptedExtensions.contains(ext));
		StretchIcon myPicture = new StretchIcon(picture.getPath());
		nextPictures.add(myPicture);
		nextPicturesPathes.addElement(picture);
	}
}
