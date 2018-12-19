struct record{
	int percentage, trials, successes, failures;
};
void readData(int percentage, FILE * in);
bool isInteger(char * c);
int percentage;
char * filename;
FILE * input;
