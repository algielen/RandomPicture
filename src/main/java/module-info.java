module randompicture {
	requires java.base;
	requires java.desktop;
	requires javafx.graphics;
	requires javafx.controls;

	opens be.algielen.randompicture.gui to javafx.graphics; // javafx needs reflection
}