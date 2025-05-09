# Nombre de tu JAR y clases
JAR       := KarelJRobot.jar
SRC       := $(wildcard *.java)
CLASSES   := $(SRC:.java=.class)
MAIN      := main

# Opciones de classpath (unix: “:”, windows: “;”)
CP        := .:$(JAR)

.PHONY: all compile run clean

# Objetivo por defecto
all: compile

# Compila todos los .java
compile: $(CLASSES)

%.class: %.java
	javac -cp $(CP) $<

# Ejecuta (se asegura de compilar antes)
run: compile
	java -cp $(CP) $(MAIN)

# Limpia los .class
clean:
	rm -f $(CLASSES)