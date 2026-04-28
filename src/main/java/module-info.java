module randompicture {
	requires java.base;
	requires java.desktop;
	requires javafx.graphics;
	requires javafx.controls;
	requires org.slf4j;

	opens be.algielen.randompicture.gui to javafx.graphics; // javafx needs reflection
}