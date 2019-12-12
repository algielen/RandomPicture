# RandomPicture
A simple Java app that shows a random picture from a directory tree.
# How to make
Run mvn package to create a JAR in target/shade/  
Create `props.properties` file with the following properties
```
paths=YOUR_PATH_HERE;ANOTHER_ONE
defaultImage=heavybreathing.gif
```
and place it alongside the generated JAR.


# How to run
Launch the previously generated jar with `java -jar JAR_NAME`


# JLink
Create a native image in target/random-picture/ with the command `mvn clean javafx:jlink
` and run it with the script RandomPicture.bat