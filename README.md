# RandomPicture
A simple Java app that shows a random picture from a directory tree.
# How to make
Add a `props.properties` file with the following properties
```
path=YOUR_PATH_HERE
defaultImage=heavybreathing.gif
```

and run mvn package.

# How to run
Launch the previously generated jar with `java -jar JAR_NAME`


# JLink
Create a native image in target/random-picture/ with the command `mvn clean javafx:jlink
` and run it with the script RandomPicture.bat