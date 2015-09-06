all:
	javac -Xlint:all -Xlint:-serial *.java

.PHONY: clean
clean:
	rm -f *.class
