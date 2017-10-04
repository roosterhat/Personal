/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind
 * NetID: jeo170030
 *
 * Created on September 18, 2017, 11:26 AM
 */

#include <cstdlib>
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <math.h>
#include <regex>


using namespace std;

class Command   //command class( used to store information about each command)
{
    public:

    string name;        
    string description;
    int id;
    int argCount; 
    
    Command(string n, int i, int a, string desc="There is no argument description"){
        name = n;
        id = i;
        argCount = a;
        description = desc;
    }
};

void createCommands(vector<Command> & cmds);
void getCommand(string* command, vector<string> &  args, vector<Command> commands);
vector<string> parseCommand(string input,string expression);
void swapRows(float** vectors, int size, vector<string> args);
void multiply(float** vectors, int size, vector<string> args);
void add(float** vectors,int size, vector<string> args);
float** readFile(float** vectors,vector<string> args, int &size);
void help(vector<Command> commands,vector<string> args);
bool isNumber(string s);
string toUpper(string input);
void buildArray(vector<string> parts, float vectors[4],string variables);
void output(float** vectors,int size);
bool checkName(vector<string> & parts, string name);
bool completed(float** vectors,int size);
bool checkEchelon(float* echelon, float** vectors,int size);
int getEquationCount(string fileName);
int sum(float* vector, int size);

int main() {
    float** vectors;            //2d array used to store matrix values
    int size = 0;               //size of array
    vector<Command> commands;   //list of used to store commands
    createCommands(commands);   //adds all commands to commands list
    cout<<"For Help type 'help' or '6'"<<endl;
    while(true){
        string command;
        vector<string> args;
        getCommand(&command,args,commands);     //gets the next command from user
        if(command=="Swap Rows"){               //check to see which command was called 
            if(completed(vectors,size))         //used to check if matrix is in reduce echelon form
                cout<<"Vectors are in Reduced Echelon form, row operations are not allowed"<<endl;
            else
                swapRows(vectors, size, args);
        }
        else if(command=="Multiply Scalar"){
            if(completed(vectors,size))
                cout<<"Vectors are in Reduced Echelon form, row operations are not allowed"<<endl;
            else
                multiply(vectors, size, args);
        }
        else if(command=="Add Scalar"){
            if(completed(vectors,size))
                cout<<"Vectors are in Reduced Echelon form, row operations are not allowed"<<endl;
            else
                add(vectors, size, args);
        }
        else if(command=="Read File")
            vectors = readFile(vectors, args, size);
        else if(command=="Output"){
            output(vectors,size);
        }
        else if(command=="Help")//
            help(commands,args);
        else if(command=="Quit")//
            break;        
    }
    return 0;
}

void createCommands(vector<Command> & cmds){    //creates all commands and adds them to commands list
    cmds.push_back(Command("Swap Rows",1,2,"args: row, row\nDescription: Swaps the given row indexes"));
    cmds.push_back(Command("Multiply Scalar",2,2,"args: row, scalar\nDescription: Multiplies the given row by the given scalar"));
    cmds.push_back(Command("Add Scalar",3,2,"args: row, scalar\nDescription: Adds the given scalar to the given row"));
    cmds.push_back(Command("Read File",4,1,"args: file name\nDescription: Reads any equations from the given file and parses them into matrix form"));
    cmds.push_back(Command("Output",5,0,"Does not expect arguments"));
    cmds.push_back(Command("Help",6,0,"args: (leave blank) or argument name / number"));
    cmds.push_back(Command("Quit",7,0,"Does not expect arguments"));
}

void getCommand(string* command, vector<string> & args, vector<Command> commands)//used to get a valid command with valid arguments
{
    while(true){            //while the command is invalid
        string input;       
        cout<<":> ";        
        getline(cin,input); //get the user input
        vector<string> parts = parseCommand(input,"(\\S+)");    //parse the given command at all spaces
        if(parts.size()>0){
            for(Command c: commands){                           //iterate through all available commands
                if((isNumber(parts[0]) && stoi(parts[0])==c.id) || checkName(parts,c.name)){    //if the command is represented by its ID then check against command ID
                    if(c.argCount<=parts.size()-1){     //<-checks if correct number of         //otherwise check to see if command matches command Name
                        *command = c.name;              //  arguments was given
                        int count=0;
                        for(string part:parts){         //copies the arguments given to the arguments list
                            if(count>0)                 //dont copy the command name
                                args.push_back(part);
                            count++;
                        }                          
                        return;
                    }
                    cout<<"'"<<c.name<<"' expects "<<c.argCount<<" arguments, given: "<<parts.size()-1<<endl;
                    break;
                }         
            }
            cout<<"Invalid Command"<<endl;
        }
    }
}

bool checkName(vector<string> & parts, string name){//checks the parsed command if it contains the given command name
    string comb = "";                               //This is mainly used to check names with spaces which may be split if the parser is set to split at spaces
    name = toUpper(name);           //converts the given command name to upper case
    int count = 0;
    for(string part: parts){        //check each word in parts
        if(!comb.empty())           
            comb+=" ";
        comb+=part;                 //combine the next word to make a larger possible command, i.e. 'read' + 'file' => 'read file'
        if(toUpper(comb)==name){    //convert the current combination of words to upper case and check if it matches the given command name
            if(count>0){            //if the first word matches, dont need to remove words within combination
                parts.erase(parts.begin(),parts.begin()+count); //remove parts contained within the combined parts from the list
                parts[0] = comb;                                //set the first element to the combination of parts
            }
            return true;
        }
        count++;
    }
    return false;
}

bool isNumber(string s)//checks if given string is a integer or float
{
    return regex_match(s,regex("[\\d\\+\\-\\.]+"));
}

string toUpper(string input){//converts the given string to upper case
    for(auto & c: input) c = toupper(c);
    return input;
}

vector<string> parseCommand(string input,string expression){//parses the given command with the given regex expression
    vector<string> res;                                     //      regex is a parsing protocol that is commonly used in token parsers
    regex pattern(expression);                              //creates the regex expression
    smatch matches;                                         //used to hold all of the matches found after searching
    while (regex_search(input,matches,pattern)) {   //while there is something in the string that matches the regex expression 
        string part = matches[0];                   //get the matched string
        if(!part.empty())                           
            res.push_back(part);                    //add part to list
        input = matches.suffix().str();             //remove the part found from the list
    }
    return res;
}

    
void swapRows(float** vectors, int size, vector<string> args){  //swaps two given rows in the matrix
    if(isNumber(args[0]) && isNumber(args[1])){                 //checks if both arguments are numbers
        int index1 = stoi(args[0])-1;                             //convert argument 1 to number
        int index2 = stoi(args[1])-1;                             //convert argument 2 to number
        if(index1>=0 && index1<size && index2>=0 && index2<size){//check to see if indexes are in bounds
            float* temp = *(vectors+index1);                    //store first index in temporary array
            *(vectors+index1) = *(vectors+index2);              //set first index to second index
            *(vectors+index2) = temp;                           //set second index to temporary array
        }
        else{
            cout<<"Index does not exist"<<endl;
            return;
        }
        output(vectors,size);   //output the current matrix
    }
    else
        cout<<"Invalid arguments"<<endl;
}

void multiply(float** vectors, int size, vector<string> args){  //multiplies a given row by a given scalar value
    if(isNumber(args[0])&& isNumber(args[1])){                  //checks if both arguments are numbers
        int index = stoi(args[0])-1;                              //converts argument 1 to number
        if(index>=0 && index<size){                             //checks if index is in bounds
            float *pointer = *(vectors+index);                  //sets a pointer to the index of the array
            float scalar = stof(args[1]);                       //converts argument 2 to a float
            for(int i=0;i<4;i++){                               //iterates through each element and multiplies it by the scalar 
                *pointer *= scalar;
                pointer++;                                      //increments the pointer
            }
        }
        else{
            cout<<"Index does not exist"<<endl;
            return;
        }
        output(vectors,size);
    }
    else
        cout<<"Invalid arguments"<<endl;
}

void add(float** vectors, int size, vector<string> args){   //adds a given scalar value to given row
    if(isNumber(args[0])&& isNumber(args[1])){              //checks if both arguments are numbers
        int index = stoi(args[0])-1;                          //converts argument 1 to a number
        if(index>=0 && index<size){                         //checks if index is in bounds 
            float *pointer = *(vectors+index);              //creates a pointer from the array at the index
            float scalar = stof(args[1]);                   //converts argument 2 a float
            for(int i=0;i<4;i++){                           
                *pointer += scalar;                         //adds the scalar to the value at the pointer
                pointer++;                                  //increments the pointer
            }
        }
        else{
            cout<<"Index does not exist"<<endl;
            return;
        }
        output(vectors,size);
    }
    else
        cout<<"Invalid arguments"<<endl;
}

float** readFile(float** vectors, vector<string> args, int& size){  //reads any equations from the given file and converts them into matrix form
    cout<<"Opening: "<<args[0]<<endl;                       
    ifstream file(args[0],ios::in | ios::out | ios::app);           //opens a file stream for the specified file
    int count = 0;                                                  
    if(file.is_open()){                                             
        vectors = new float*[getEquationCount(args[0])];            //allocates the space for the array dynamically
        string line;
        while(getline(file,line)){                                  //gets the next line from the file
            vector<string> parts = parseCommand(line,"[+-=]*[0-9\\.a-zA-Z]+");  //parses the line from the file looking for +,-,= followed by numbers or letters
            float* array = new float[4];                            //creates array for matrix
            buildArray(parts,array,"xyz");                                //converts the parsed line to matrix form and enters them into the correct positions in the arrau
            vectors[count] = array;                                 //adds the new array to the vector list
            count++;
        }
    }
    else
        cout<<"'"<<args[0]<<"' failed to open"<<endl;
    file.close();
    size = count;                                                   //sets the size of the array
    output(vectors,size);
    return vectors;
}

int getEquationCount(string fileName){                      //gets the number of equation contained in a file 
    ifstream file(fileName,ios::in | ios::out | ios::app);  //opens files stream for given file
    int count = 0;                                          //number of equations
    if(file.is_open()){
        string line;
        while(getline(file,line))                           //while file contains line get next line
            count++;                                        //increment number of equations
    }
    file.close();
    return count;
}

void buildArray(vector<string> parts, float* vectors, string variables){      //converts the list of strings into matrix form(array)
    regex variablePattern("["+variables+"]");               //creates a regex expression with variables
    regex equalPattern(".*=.*");                            //creates a pattern for '='
    for(string part: parts){                                //for each string in parts
        smatch matches;         
        if(regex_search(part,matches,variablePattern)) {    //if the current string contains the pattern
            int index = variables.find(matches[0].str());   //get the index of the variable that is found
            part.erase(part.find(matches[0].str()),1);      //remove the variable from the string
            vectors[index] = stof(part);                    //convert the string to a float and add it to the correct index
        }
        else if(regex_match(part,equalPattern)){            //if the current string contains '='
            part.erase(part.find("="),1);                   //remove the '=' from the string
            vectors[3] = stof(part);                        //convert the string to a float and add it to the correct index
        }
        else{
            cout<<"Invalid syntax for '"<<part<<"'"<<endl;
            vectors = new float[4]{0,0,0,0};                //if string does not contain variable or '=' then set array to default
            break;
        }
    }
}

void output(float** vectors,int size){  //outputs the current matrix 
    cout<<"Vectors"<<endl;
    for(int row=0;row<size;row++){
        for(int i=0;i<4;i++){
            printf("%.2f",vectors[row][i]); //print value with 2 decimal places
            if(i<3)
                cout<<",";
        }
        cout<<endl;
    }
}

bool completed(float** vectors,int size){   //checks if the given matrix is in reduced echelon form 
    vector<float*> echelon;                 //list of possible echelon combinations for a 3x4 matrix
    echelon.push_back(new float[3]{1,0,0}); 
    echelon.push_back(new float[3]{0,1,0});
    echelon.push_back(new float[3]{0,0,1});
    int matched = 0;                        //keeps track of the number of matches rows
    for(float* e: echelon)                  //for each possible echelon 
        if(checkEchelon(e,vectors,size))    //check against all rows of the matrix 
            matched++;                      //if match is found increment
    return (size!=0 && matched==size);      
}

bool checkEchelon(float* echelon, float** vectors, int size){   //compares the matrix against the given echelon 
    for(int row=0;row<size;row++){                              //for each row in the matrix 
        bool match = true;                                      
        for(int i=0;i<3;i++)                                    //for each value in the row
            if(vectors[row][i]!=echelon[i])                     //compare to corresponding echelon value
                match = false;
        if(match || sum(vectors[row],3)==0)                     //if match is found(meaning no value was different) or all values are 0
            return true;                                        //return true
    }
    return false;                                               //return false after all have been tested and none succeeded
}

int sum(float* vector, int size){
    int sum = 0;
    float* pointer = vector;
    for(int i=0;i<size;i++){
        sum+=*pointer;
        pointer++;
    }
    return sum;
}

void help(vector<Command> commands, vector<string> args){   //gives help for commands
    if(args.size()==0){                                     //if no arguments are given then print all commands
        cout<<"\n(help -command)"<<endl;
        cout<<"Command Help"<<endl;                         
        for(Command c:commands)
            cout<<c.id<<": "<<c.name<<endl;
        cout<<endl;
    }
    else{                                                   //if a command was given as argument
        if(isNumber(args[0])){                              //check if its a number( as in a ID)
            int index = stoi(args[0])-1;                    //convert argument to number
            if(index>=0 && index<commands.size()){          //check if number is inbounds
                Command c = commands[index];                //get command at index
                cout<<c.name<<": "<<c.description<<endl;
                return;
            }
        }
        else
            for(Command c: commands){                       //else command name is assumed
                if(checkName(args,c.name)){                 //check name against argument(allows for names with spaces)
                    cout<<c.name<<": "<<c.description<<endl;
                    return;
                }
            }
        cout<<"'"<<args[0]<<"' doesnt exist"<<endl;
    }
}
