package be.algielen.randompicture;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;


public class Utils {

	public static BufferedImage scale(BufferedImage sbi, double maxWidth,
									  double maxHeight) {
		BufferedImage dbi = null;
		if (sbi != null) {
			double fWidth = 1;
			double fHeight = 1;
			if (sbi.getWidth() > maxWidth) {
				fWidth = maxWidth / sbi.getWidth();
			}
			if (sbi.getHeight() > maxHeight) {
				fHeight = maxHeight / sbi.getHeight();
			}
			if ((fHeight < fWidth && fHeight != 0) || fWidth == 0) {
				fWidth = fHeight;
			} else {
				fHeight = fWidth;
			}
			int dWidth = (int) (sbi.getWidth() * fWidth);
			int dHeight = (int) (sbi.getHeight() * fHeight);

			dbi = new BufferedImage(dWidth, dHeight, sbi.getType());
			Graphics2D g = dbi.createGraphics();
			AffineTransform at = AffineTransform.getScaleInstance(fWidth,
					fHeight);
			g.drawRenderedImage(sbi, at);
			System.out.println(String.format("Converted from %dx%d to %dx%d",
					sbi.getWidth(), sbi.getHeight(), dWidth, dHeight));
		}
		return dbi;
	}

	public static String getExt(File file) {
		String extension = "";
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}
}
