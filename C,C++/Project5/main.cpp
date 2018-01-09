/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind
 * jeo170030
 * Created on November 16, 2017, 12:59 PM
 */


//########################
//Overloaded Operators
//222,232,234
//########################

#include <cstdlib>
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <math.h>
#include "Comparator.h"
#include "Value.h"
#include "LinkedList.h"
#include "Grid.h"

using namespace std;


bool readCommandFile(LinkedList * cmds, string file);
void paint(LinkedList* cmd, string file);
int* moveCursor(int cursor[], int amount, string dir);
vector<string> parseCommand(string cmd, string delimeter);
bool draw(Grid* canvas, vector<string> cmd, int pos[], bool pen, bool bold);
bool inbounds(int pos[]);
void updateInputFile(LinkedList * commands, ofstream & file);
string generateID(int count);

class Command: public Value{//A sub class of Value that stores command ID and the command
public:
    string id;
    string command;
    Command(string i, string c){
        id = i;
        command = c;
    }
    string getValue(){
        return "(ID: "+id+", Command: "+command+")";
    }
    bool equal(string val){
        return id==val || command==val;
    }
};

class StringComparator: public Comparator{//sub class of comparator that compares Commands
public:
    bool compare(Value *a, Value* b){//compares two strings and returns true/false if first string is greater than the second
        return convert(((Command*)a)->id)>convert(((Command*)b)->id);
    }
    
    int convert(string s){//hashes string
        int value=0;
        char *cstr = &s[0u];    //converts string to char array
        for(unsigned int i=0;i<s.length();i++){//gets the value of each letter and adds it to the total value 
            int c = int(cstr[i]);
            value += c+(26*i);
        }
        return value;
    }
};



class A{
    int a;
    friend void friend_set(A& p, int i){
        p.a = i; // this is a non-member function
    }
 public:
    void member_set(int i) {
        a = i; // this is a member function
    }
    int get(){
        return a;
    }
};

int main() {
    A a = A();
    a.member_set(0);
    cout<<a.get()<<endl;
    friend_set(a,1);
    cout<<a.get()<<endl;
    /*LinkedList* commands = new LinkedList(new StringComparator());
    string input="commands6.txt";
    string output="commands.txt";
    cout<<"Enter file names\nCommand file: ";
    getline(cin,input); //get input file path
    cout<<"Output file: ";
    getline(cin,output); //get output file path
    if(readCommandFile(commands, input)){ //reads the input file and sets values in 'commands' 
        paint(commands,output); //paint is called if input file was opened correctly
        ofstream file(input);
        updateInputFile(commands,file); //updates the command file with the revised list of commands
        file.close();
    }
    else
        cout<<"!!!Failed to read file!!!"<<endl;
    */
    return 0;
}

bool readCommandFile(LinkedList* cmds, string file)
{
    string line;
    ifstream myfile(file);  //open file stream
    if (myfile.is_open())
    {
        int count = 0;
        while ( getline (myfile,line) ){ //while there is a line, read line
            vector<string> input = parseCommand(line," ");
            if(input.size()>=2)
                cmds->add(new Command(input[0],input[1]));   //add new command to linked list in correct position
            else if(input.size()==1)
                cmds->add(new Command(generateID(count),input[0]));                
            count++;
        }
        myfile.close();
    }
    else
        return false;
    return true;
}

string generateID(int count){
    int length = (int) floor(log(25*(count+1))/log(26));
    char* buf = new char[length];
    for (int i = length-1;i>=0;i--) {
        count--;
        buf[i] = (char)('A'+count%26);
        count /= 26;
    }
    return string(buf,length);
}

void updateInputFile(LinkedList * commands, ofstream & file){   //recursively updates input file with command list
    if(commands!=nullptr){
        Command *command = (Command*)commands->pop();    //pop first element (removes first element)
        if(command!=nullptr)
            file<<command->id<<" "<<command->command<<endl; //write command ID and command to file
        if(commands->hasNext())
            updateInputFile(commands,file);                 //recursive call
    }
}

int* moveCursor(int cursor[], int amount, string dir)
{
    const double halfC = M_PI / 180;    //half circle for radian to decimal conversion
    vector<string> directions = {"N","NE","E","SE","S","SW","W","NW"};  //used to find the index of the given direction 'dir'
    double theta = 360 / directions.size(); //sets theta to the total angle of each direction
    int index = find(directions.begin(), directions.end(), dir) - directions.begin(); //gets the index of 'dir' in direction
    double r = fmod(theta * index,360.0) + 90.0; //gets the angle in radians of the given 'dir'
    double x = -round(cos(r*halfC)); //calculates x component
    double y = -round(sin(r*halfC)); //calculates y component
    cursor[0] = (int)(cursor[0]+x*amount); //adds x component to position
    cursor[1] = (int)(cursor[1]+y*amount); //adds y component to position
    return cursor;
}

vector<string> parseCommand(string cmd, string delimiter)
{
    vector<string> parsedCommand;
    size_t pos = 0;
    while ((pos = cmd.find(delimiter)) != string::npos) {//sets 'pos' to the position of 'delimiter' in the string 'cmd' and returns whether or not it failed
        parsedCommand.push_back(cmd.substr(0, pos));    //adds the substring from the beginning to 'pos' 
        cmd.erase(0, pos + delimiter.length());         //removes the current segment from the string 'cmd'
    }
    parsedCommand.push_back(cmd);//adds the rest of 'cmd' to the vector 
    return parsedCommand;
}

bool draw(Grid* canvas, int pos[], vector<string> cmd, bool pen, bool bold) //executes draw command, returns whether or not draw head went out of bounds
{
    string drawChar = bold ? "#" : "*"; //if bold is true set 'drawchar' to "#" otherwise "*"
    string direction = cmd.at(1);   
    int length = stoi(cmd.at(2));       //string to integer
    cout<<"draw: length: "<<length<<" direction: "<<direction<<endl;
    int temp[2] = {pos[0],pos[1]};
    if(inbounds(moveCursor(temp,length,direction))){    //if command stays inbounds
        while(length>0){
            moveCursor(pos,1,direction);  //gets the new position 1 space in 'direction'
            length--;
            if(inbounds(pos) && pen)//if inbounds and pen down
                if(!(!bold && canvas->get(pos[1],pos[0])->getValue()->getValue()=="#"))
                    canvas->set(new Node(pos[1],pos[0],drawChar),pos[1],pos[0]);
        }
        return true;
    }
    else{
        cout<<"     draw command out of bounds"<<endl;
        return false;
    }
}

bool inbounds(int pos[])    //checks if current position is within bounds
{
    return pos[0]>=0 && pos[0]<50 &&
            pos[1]>=0 && pos[1]<50;
}


void paint(LinkedList* cmds, string file)
{
    Grid* canvas = new Grid(50,50);  //create canvas
    int pos[] = {0,0};
    bool pen = false;
    bool bold = false;
    for(int i=0;i<cmds->size();i++)
    {
        Command *value = (Command*)cmds->get(i)->value;
        string command = value->command;
        vector<string> parsedCommand = parseCommand(command,","); //parse command
        string cmd = parsedCommand[0];
        if(cmd=="1"){ //pen up
            pen = false;
            cout<<"pen up"<<endl;
        }else if(cmd=="2"){//pen down
            pen = true;
            cout<<"pen down"<<endl;
        }else if(cmd=="3"){//draw line
            if(!draw(canvas,pos,parsedCommand,pen,bold)){   //if draw command goes out of bounds
                cmds->remove(value->id);                     //remove that command
                i--;                                    //decrement index
            }
        }else if(cmd=="4"){//output drawing
            cout<<canvas<<endl;
        }else if(cmd=="B"){//bold on
            bold = true;
            cout<<"bold on"<<endl;
        }else if(cmd=="b"){//bold off
            bold = false;
            cout<<"bold off"<<endl;
        }
    }
    cout<<canvas<<endl;  
    ofstream f(file);
    f<<canvas<<endl;
    f.close();
}

