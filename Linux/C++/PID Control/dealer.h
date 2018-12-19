struct record{
	int percentage, trials, successes, failures;
};

bool isInteger(char * c);
record runTrials(int trials, int percentage,  int childProc, bool verbose);
int percentage = 50;
int trials = 10;
int childProcesses = 1;
bool verbose = false;
char * filename;
FILE * output; 
