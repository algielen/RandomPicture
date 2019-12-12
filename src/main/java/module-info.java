module randompicture {
	requires java.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires java.desktop;

	opens be.algielen.randompicture.gui to javafx.graphics; // javafx needs reflection
}