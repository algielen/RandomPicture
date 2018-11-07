module randompicture {
	requires javafx.controls;
	requires java.desktop;

	opens be.algielen.randompicture.gui to javafx.graphics; // javafx needs reflection
}