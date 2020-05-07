CC=g++
CFLAGS=-c -Wall -I include/

LDFLAGS=`mysql_config --cflags --libs`
SOURCES=$(wildcard src/*.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE:=sql_server

.SUFFIXES: .o .cpp

all: $(SOURCES) $(OBJECTS)  MAIN FINAL

$(OBJECTS): $(SOURCES)  
	$(CC) $(CFLAGS) $(@:.o=.cpp) -o $@

MAIN : main.cpp
	$(CC) $(CFLAGS) main.cpp -o main.o

FINAL: $(OBJECTS)
	$(CC) main.o $(OBJECTS) -o ${EXECUTABLE} $(LDFLAGS)
	rm -f core *.o src/*.o 

clean:
	rm -f core *.o src/*.o ${EXECUTABLE}
	