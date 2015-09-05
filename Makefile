classes := $(patsubst %.java,%.class,$(wildcard *.java))

all: $(classes) 

%.class: %.java
	javac -Xlint:all -Xlint:-serial $<

.PHONY: clean
clean:
	rm -f *.class
