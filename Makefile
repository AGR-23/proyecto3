# Nombre de tu JAR y clases
JAR = KarelJRobot.jar
SRC = *.java
MAIN = main

.PHONY: all compile run clean

# Objetivo por defecto: compila todo
all: compile

# Compilar todas las .java
compile:

javac -cp .:$(JAR) $(SRC)

# Ejecutar, compilando antes si hace falta
run: compile
	
java -cp .:$(JAR):. $(MAIN)

# Borrar las clases compiladas
clean:
<tab>rm -f *.class