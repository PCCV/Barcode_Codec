include Makefile.os

JC = javac
JFLAGS = -g -deprecation -Xlint:unchecked -source 1.6
# -target 1.6

ifeq "$(PLATFORM)" "win32"
	LIBS = $(IMAGEJ)
	SHELL = sh.exe
else
	LIBS = /home/ImageJ/jars
endif

SOURCEFILES = $(wildcard *.java)
CLASSFILES = $(SOURCEFILES:.java=.class)

DEST = ..

.SUFFIXES: .java .class

%.class:
	$(JC) $(JFLAGS) -extdirs $(LIBS) $*.java

# begin ---- JAR support ----------
JARFILE = Barcode_Codec.jar

$(JARFILE): $(CLASSFILES) $(SOURCEFILES)
	jar cf $(JARFILE) <<manifest.tmp *.class licence.txt COPYING.txt


default: $(CLASSFILES)

all: $(JARFILE) $(CLASSFILES) install clean

install:
	mv $(JARFILE) $(DEST)

clean:
	$(RM) *.class
