#Files
EXEC  := mighty
SRC  := mighty.cpp
OBJ  := $(patsubst %.cpp,%.o,$(SRC))
HOME  := /home/011/j/je/jeo170030

#Options
CC  := g++ 
LDLIBS  := 
LDFLAGS  := 
LFLAGS  := 
HEADER := 

#Rules
$(EXEC): $(OBJ)
	$(CC) $(LDFLAGS) $(LFLAGS) $(LDLIBS) -o $@ $^
%.o: %.cpp
	$(CC) -c $(LFLAGS) $<
$(OBJ): $(HEADER)

.PHONY: clobber clean neat echo
clobber: clean
	$(RM) $(EXEC)
clean: neat
	$(RM) $(OBJ)
neat:
	$(RM) *~ .*~
echo:
	@echo $(OBJ)
