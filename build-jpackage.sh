jpackage \
--type app-image \
--input C:/development/sources/RandomPicture/target/shade \
--dest C:/development/sources/RandomPicture/target/exe \
--name RandomPicture-1-4 \
--icon C:/development/sources/RandomPicture/src/main/resources/heavybreathing.ico
--main-jar RandomPicture-shaded-1.4.jar \
--main-class be.algielen.randompicture.gui.Main \
--module-path C:/development/resources/javafx-jmods-15.0.1/ \
--add-modules java.base,javafx.controls,javafx.web,javafx.graphics,javafx.media,java.datatransfer,java.desktop,java.scripting,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,javafx.fxml,java.naming,java.sql,jdk.charsets
