# Chinese Ancestor Plate (CAL)

A simple jar which sole purpose is to consume a list of names in excel format, and print them in a predefined PDF format.

Note: The name of the project is translated literally from chinese, 神主牌. Please forgive my laziness. :)

## Dependencies

This project has several library dependencies:
- Apache PdfBox 2
- Apache POI
- jackson-databind
- Commons-CLI
- and etc. (Please refer [POM file](pom.xml) for details.)

## Compilation

#### Enviroment
 1. Oracle JDK 1.8 and above installed.
 2. Maven 3.5 installed.
 3. Git 2.17 installed.

#### Command
```bash
$ git clone git@github.com:khenfei/cal.git
$ cd cal
$ mvn package

# [Optional: for unit test]
$ mvn test 
```

## Usage

#### Instruction
```instruction
cal -i xlsxfile -o output.pdf [-f fontfile]
 -f <file>   Font file. Specify custom True Type Font (TTF) file path
 -h,--help   Print this help message
 -i <file>   Input file. Specify excel (xlsx) file path
 -o <file>   Output file. Specify output file path
 -p <file>   Properties file. Specify properties file path
```

#### Example
```bash
# Show instruction
$ java -jar cald.jar

# Example
$ java -jar target/cald.jar -i sample.xlsx -o output.pdf
