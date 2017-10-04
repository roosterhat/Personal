/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind
 * ID: jeo170030
 * -Wall -Wextra -Wuninitialized -pedantic-errors -Wconversion
 * Created on August 28, 2017, 7:44 PM
 */

#include <cstdlib>
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <math.h>


using namespace std;

struct Node{
    string id;
    string command;
    struct Node* next = NULL;
    Node(string i, string c){
        id = i;
        command = c;
    }
};

class Comparator{
public:
    bool compare(string a, string b){
        return a>b;
    }
};

class LinkedList{
    struct Node *head = NULL;
    Comparator c = Comparator();
    public:
    
        void add(struct Node node){
            struct Node *current = head;
            while(current!=NULL){
                cout<<"test"<<endl;
               
                if(c.compare(current->id,node.id))
                    current = current->next;
                else{
                    struct Node* temp = current;
                    *current = node;
                    node.next = temp;
                    return;
                }      
                 cout<< current->id<<endl;
            }
            current = &node;
           // cout<< current->id<<endl;
        }
        
        int size(){
            struct Node *current = head;
            int count = 0;
            while(current!=NULL){
                count++;
                current = current->next;
            }
            return count;
        }
        
};

bool readCommandFile(vector<string> & cmds, string file);
void paint(vector<string> cmd, string file);
int* moveCursor(int cursor[], int amount, string dir);
vector<string> parseCommand(string cmd);
void drawCanvas(string file);
void draw(ofstream & file, vector<string> cmd, int pos[], bool pen, bool bold);
bool inbounds(int pos[]);
void fillCanvas(ofstream & file);


int main() {
    LinkedList l = LinkedList();
    
    l.add(Node("A","test"));
    l.add(Node("B","test"));
    l.add(Node("D","test"));
    l.add(Node("C","test"));
    l.add(Node("AA","test"));
    l.add(Node("Z","test"));
    cout<<l.size()<<endl;
    /*string fileName = "commands.txt";
    string output = "paint.txt";
    cout<<"Enter file name(leave blank for default)\n"<<"Command file: ";
    string input = "";
    getline(cin,input); //get input file path
    if(!input.empty())  //if input is not empty set 'fileName' as input otherwise it remains as default 
        fileName = input;
    cout<<"Output file: ";
    getline(cin,input); //get output file path
    if(!input.empty())  //same as before
        output = input;
    vector<string> commands;
    if(readCommandFile(commands, fileName)) //reads the input file and sets values in 'commands' 
        paint(commands,output); //paint is called if input file was opened correctly
    */
    return 0;
}

bool readCommandFile(vector<string> & cmds, string file)
{
    string line;
    ifstream myfile(file);  //open file stream
    if (myfile.is_open())
    {
        while ( getline (myfile,line) ) //while there is a line, read line
            cmds.push_back(line);   //push to vector
        myfile.close();
    }
    else{
        cout << "Unable to open file '"<<file<<"'"<<endl; 
        return false;
    }
    return true;
}

int* moveCursor(int cursor[], int amount, string dir)
{
    const double halfC = M_PI / 180;    //half circle for radian to decimal conversion
    vector<string> directions;  //used to find the index of the given direction 'dir'
    directions.push_back("N");
    directions.push_back("NE");
    directions.push_back("E");
    directions.push_back("SE");
    directions.push_back("S");
    directions.push_back("SW");
    directions.push_back("W");
    directions.push_back("NW");
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

void drawCanvas(string file)
{
    cout<<"draw to canvas"<<endl;
    ifstream myfile(file);  //opens file stream
    string line;
    cout<<string(52,'/')<<endl;
    if(myfile.is_open())
        while(getline(myfile,line)) //while there is a line, get line
            cout<<"/"<<line<<"/"<<endl;   //print line
    else
        cout<<"Failed to open '"<<file<<"'"<<endl;
    cout<<string(52,'/')<<endl;
    myfile.close();
}

void draw(ofstream & file, int pos[], vector<string> cmd, bool pen, bool bold)
{
    string drawChar = bold ? "#" : "*"; //if bold is true set 'drawchar' to "#" otherwise "*"
    string direction = cmd.at(1);   
    int length = stoi(cmd.at(2));       //string to integer
    cout<<"draw: length: "<<length<<" direction: "<<direction<<endl;
    while(length>0){
        int* newpos = moveCursor(pos,1,direction);  //gets the new position 1 space in 'direction'
        pos = newpos;
        length--;
        if(inbounds(newpos) && pen)//if inbounds and pen down
        {
            int cursorpos = 52*pos[1]+pos[0]; //get the carat position based on the x,y position
            file.seekp(cursorpos,ios::beg);     //set the current carat position
            file<<drawChar;                     //write to file
        }
    }
}

bool inbounds(int pos[])
{
    return pos[0]>=0 && pos[0]<50 &&
            pos[1]>=0 && pos[1]<50;
}

void fillCanvas(ofstream & file)    //fills the canvas with blank spaces
{
    for(int y=0;y<50;y++)
    {
        for(int x=0;x<50;x++)
            file<<" ";
        file<<"\n";
    }
}

void paint(vector<string> cmds, string file)
{
    ofstream myfile(file);  //open file stream
    if(!myfile.is_open()){  //detect if file opened correctly
        cout<<"Failed to open '"<<file<<"'"<<endl;
        return;
    }        
    fillCanvas(myfile);     //prepare the new file
    int pos[] = {0,0};
    bool pen = false;
    bool bold = true;
    for(auto command : cmds)  //iterate through the given commands
    {
        vector<string> parsedCommand = parseCommand(command,","); //parse command
        string cmd = parsedCommand[0];
        if(cmd=="1"){ //pen up
            pen = false;
            cout<<"pen up"<<endl;
        }else if(cmd=="2"){//pen down
            pen = true;
            cout<<"pen down"<<endl;
        }else if(cmd=="3"){//draw line
            draw(myfile,pos,parsedCommand,pen,bold);
        }else if(cmd=="4"){//output drawing
            drawCanvas(file);
        }else if(cmd=="B"){//bold on
            bold = true;
            cout<<"bold on"<<endl;
        }else if(cmd=="b"){//bold off
            bold = false;
            cout<<"bold off"<<endl;
        }
    }
    myfile.close();     //close file
    drawCanvas(file);   //draw final product  
}

