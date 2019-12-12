package be.algielen.randompicture.gui;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import be.algielen.randompicture.logic.ImageLoader;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainFX extends Application {

	private static final String PROPERTIES_FILE = "randompicture.properties";

	private List<Path> filepaths;
	private Path currentPath;
	private String defaultImage;
	private final ConcurrentLinkedQueue<Image> nextPictures = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<Path> nextPicturesPaths = new ConcurrentLinkedQueue<>();

	private Stage primaryStage;
	private ImageView imageView;
	private ImageLoader imageLoader;
	private Scene rootScene;
	private Label statusText;
	private HBox imageBox;

	public static void main(String[] args) {
		Application.launch(args);
	}

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

		imageBox = new HBox(imageView);
		imageBox.setAlignment(Pos.CENTER);

		HBox statusBar = createStatusBar();

		AnchorPane mainBorderPane = new AnchorPane(imageBox);
		mainBorderPane.getChildren().add(statusBar);

		fillParentCompletely(mainBorderPane);

		AnchorPane.setTopAnchor(imageBox, 0.0);
		AnchorPane.setLeftAnchor(imageBox, 0.0);
		AnchorPane.setRightAnchor(imageBox, 0.0);
		AnchorPane.setBottomAnchor(imageBox, 10.0);

		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);

		rootScene = new Scene(mainBorderPane, 1280, 720);
		rootScene.setFill(Color.BLACK);
		rootScene.setOnMouseClicked(this::handleClick);
		rootScene.setOnKeyPressed(this::handleKeyPressed);

		primaryStage.setScene(rootScene);
		primaryStage.show();

		int size = 10;
		imageLoader = new ImageLoader(nextPictures, filepaths, nextPicturesPaths, size);
		imageLoader.start();
	}

	private void fillParentCompletely(AnchorPane mainBorderPane) {
		AnchorPane.setTopAnchor(mainBorderPane, 0.0);
		AnchorPane.setBottomAnchor(mainBorderPane, 0.0);
		AnchorPane.setLeftAnchor(mainBorderPane, 0.0);
		AnchorPane.setRightAnchor(mainBorderPane, 0.0);
	}

	private HBox createStatusBar() {
		statusText = new Label("Started");
		statusText.setTextFill(Color.ANTIQUEWHITE);

		Button showInExplorer = new Button("Open folder");
		showInExplorer.setOnAction(event -> openInExplorer(currentPath));

		Button openInDefaultViewer = new Button("Open file");
		openInDefaultViewer.setOnAction(event -> openInDefaultViewer(currentPath));

		HBox statusBar = new HBox(statusText, showInExplorer, openInDefaultViewer);
		statusBar.setAlignment(Pos.CENTER_RIGHT);
		statusBar.setSpacing(10.0);
		statusBar.setBackground(new Background(new BackgroundFill(Color.grayRgb(120, 0.80), CornerRadii.EMPTY, Insets.EMPTY)));

		return statusBar;
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		imageLoader.finish();
	}

	private void loadProperties() {
		Properties props = new Properties();

		Path pathInSameDir = Paths.get(PROPERTIES_FILE);
		File propsInCurrentDir = pathInSameDir.toFile();


		if (propsInCurrentDir.exists() && propsInCurrentDir.canRead()) {
			try (Reader propertiesReader = Files.newBufferedReader(pathInSameDir)) {
				props.load(propertiesReader);

				String[] paths = props.getProperty("paths").split(";");

				filepaths = Arrays.stream(paths)
						.map(string -> Paths.get(string))
						.collect(Collectors.toList());
				defaultImage = props.getProperty("defaultImage");

				return;

			} catch (IOException e) {
				System.err.println("Couldn't read external properties, reverting to default settings");
				props.clear();
			}
		}
		if (filepaths == null || filepaths.isEmpty()) {
			loadBackupProperties(props);
		}
	}

	private void loadBackupProperties(Properties props) {
		try (InputStream input = MainFX.class
				.getResourceAsStream(getResourcePath("props.properties"))) {
			props.load(input);

			filepaths = Collections.singletonList(Paths.get(props.getProperty("path")));
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
				imageView.fitHeightProperty().bind(imageBox.heightProperty());
			} else {
				imageView.fitWidthProperty().bind(imageBox.widthProperty());
			}
			currentPath = nextPicturesPaths.poll();
			nextPictures.notifyAll();
			showInfo();
		}
	}

	private void showInfo() {
		updateStatus(currentPath.toString());
	}

	private void updateStatus(String text) {
		statusText.setText(text);
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
		updateStatus(message);
	}
}
