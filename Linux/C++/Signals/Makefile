#Files
EXEC := signals
SRC := signals.cpp
OBJ := $(patsubst %.cpp,%.o,$(SRC))
HOME := /home/011/j/je/jeo170030

#Options
CC 	:= g++ 
LDLIBS  := -lgsl -lgslcblas
LDFLAGS := -L$(HOME)/gsl-2.5/lib
LFLAGS 	:= -I$(HOME)/gsl-2.5/include

#Rules
$(EXEC): $(OBJ)
	$(CC) $(LDFLAGS) $(LFLAGS) $(LDLIBS) -o $@ $^
%.o: %.cpp
	$(CC) -c $(LFLAGS) $<

.PHONY: clobber clean neat echo
clobber: clean
	$(RM) $(EXEC)
clean: neat
	$(RM) $(OBJ)
neat:
	$(RM) *~ .*~
echo:
	@echo $(OBJ)
