package be.algielen.randompicture;


import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

	private Path filepath;
	private String defaultImage;
	private final ConcurrentLinkedQueue<Image> nextPictures = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<Path> nextPicturesPaths = new ConcurrentLinkedQueue<>();

	private Stage primaryStage;
	private ImageView imageView;
	private ImageFetcher imageFetcher;
	private Scene rootScene;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("RandomPicture");
		primaryStage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));

		loadProperties();
		Image defaultImage = new Image(getResourcePath(this.defaultImage));
		primaryStage.getIcons().add(defaultImage);

		imageView = new ImageView(defaultImage);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);
		imageView.setCache(true);

		// TODO status bar ?
		HBox hBox = new HBox(imageView);
		hBox.setAlignment(Pos.CENTER);
		rootScene = new Scene(hBox, 1280, 720);
		rootScene.setFill(Color.BLACK);
		rootScene.setOnMouseClicked(this::handleClick);
		rootScene.setOnKeyPressed(this::handleKeyPressed);

		primaryStage.setScene(rootScene);
		primaryStage.show();

		int size = 10;
		imageFetcher = new ImageFetcher(nextPictures, filepath, nextPicturesPaths, size);
		imageFetcher.start();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		imageFetcher.finish();
	}

	private void loadProperties() {
		Properties props = new Properties();
		try (InputStream input = Main.class
				.getResourceAsStream(getResourcePath("props.properties"))) {
			props.load(input);
			filepath = Paths.get(props.getProperty("path"));
			defaultImage = props.getProperty("defaultImage");

		} catch (Exception exception) {
			throw new RuntimeException("Couldn't properly load props.properties, verify your JAR");
		}
	}

	private String getResourcePath(String name) {
		return "/" + name;
	}

	private void handleClick(MouseEvent event) {
		switch (event.getButton()) {
			case PRIMARY:
				showNextPicture();
				break;
			case MIDDLE:
				break;
			case SECONDARY:
				showInfo();
				break;
			case NONE:
				break;
		}
	}

	private void showNextPicture() {
		synchronized (nextPictures) {
			if (nextPictures.isEmpty()) {
				setStatusMessage("No new image to show");
				return;
			}
			Image newImage = nextPictures.poll();
			imageView.setImage(newImage);
			if (newImage.getHeight() > newImage.getWidth()) {
				imageView.fitHeightProperty().bind(primaryStage.heightProperty());
			} else {
				imageView.fitWidthProperty().bind(primaryStage.widthProperty());
			}
			filepath = nextPicturesPaths.poll();
			nextPictures.notifyAll();
		}
	}

	private void showInfo() {
		TextField textField = new TextField(filepath.toString());
		textField.setEditable(false);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.getDialogPane().setContent(textField);
		ButtonType showInExplorer = new ButtonType("Open folder");
		alert.getButtonTypes().add(showInExplorer);
		ButtonType openInDefaultViewer = new ButtonType("Open file");
		alert.getButtonTypes().add(openInDefaultViewer);

		Optional<ButtonType> result = alert.showAndWait();
		forceBackgroundColor();
		if (result.isPresent()) {
			ButtonType pressed = result.get();
			if (pressed == showInExplorer) {
				openInExplorer(filepath);
			} else if (pressed == openInDefaultViewer) {
				openInDefaultViewer(filepath);
			}
		}
	}


	private void openInExplorer(Path path) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(path.getParent().toFile());
			}
		} catch (IOException e) {
			setStatusMessage("Could not open in explorer : " + e.getMessage());
		}
	}

	private void openInDefaultViewer(Path path) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(path.toFile());
			}
		} catch (IOException e) {
			setStatusMessage("Could not open in explorer : " + e.getMessage());
		}
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		switch (keyEvent.getCode()) {
			case F11:
				toggleFullscreen();
				break;
			case ENTER:
			case SPACE:
				showNextPicture();
				break;
			default:
		}
	}

	private void toggleFullscreen() {
		if (primaryStage.isFullScreen()) {
			primaryStage.setFullScreen(false);
		} else {
			primaryStage.setFullScreen(true);
		}
	}

	private void setStatusMessage(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.show();
	}

	private void forceBackgroundColor() {
		rootScene.setFill(Color.BLACK);
	}
}
