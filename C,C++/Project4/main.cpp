/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind
 * ID: jeo170030
 *
 * Created on October 30, 2017, 1:28 PM
 */

#include <cstdlib>
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <math.h>
#include <regex>

#include "Value.h"
#include "Comparator.h"
#include "LinkedList.h"
#include "Node.h"


using namespace std;


vector<string> parseLine(string input,string expression);
vector<string> readFile(string filename);
vector<string> cleanExpressions(vector<string> v);
void populateLinkedList(LinkedList* l, vector<string> equation);
Node* createNode( string eq);
Function* getTrig(string trig);
void deriveEquations(LinkedList* l);
void removeZeros(LinkedList* l);
string combineEquation(LinkedList* l);
string removeSpaces(string s);
void writeToFile(string fileName,LinkedList* l);

class NodeComparator: public Comparator{
    bool compare(Value* a, Value* b){
        return ((Node*) a)->function!=nullptr || ((Node*) a)->exponent<((Node*) b)->exponent;
    }
};

int main() {
    string input = "functions.txt";
    string output = "derive.txt";
    vector<string> equations = readFile(input);              //reads contents of file into vector
    string trigregex = "(sin )|(cos )|(tan )|(sec )|(csc )|(cot )";                                     //Patterns used in parsing
    string pattern = "(^(\\s*\\-\\s*)|\\(\\-*\\d+\\/\\-*\\d+\\)|(\\^\\-*)|[\\d\\.]|x|"+trigregex+")+";  //...
    ofstream f("derive.txt");                                   //clears output file
    f.close();
    for(auto eq: equations){                                    //for every string in equations
        cout<<endl;     
        vector<string> parsedEquation = parseLine(eq,pattern);  //parses string with 'pattern'
        parsedEquation = cleanExpressions(parsedEquation);      //removes spaces from string
        LinkedList* l = new LinkedList(new NodeComparator);     //create new LinkedList
        populateLinkedList(l,parsedEquation);                   //create and add nodes into LinkedList
        cout<<combineEquation(l)<<endl;     
        deriveEquations(l);                                     //preform derivatives
        removeZeros(l);                                         //remove zeros 
        cout<<combineEquation(l)<<endl;
        writeToFile(output,l);                                  //output to file
    }
    return 0;
}

vector<string> readFile(string filename){//read contents of file and store in vector
    vector<string> results;             //create vector
    ifstream file(filename);            //creates filestream
    if(file.is_open()){                 //if file is open
        string line;                    
        while(getline(file,line))       //get next line
            results.push_back(line);    //add to vector
    }
    return results;
}

vector<string> parseLine(string input,string expression){//parses the given command with the given regex expression
    vector<string> res;                                     //      regex is a parsing protocol that is commonly used in token parsers
    regex pattern(expression);                              //creates the regex expression
    smatch matches;                                         //used to hold all of the matches found after searching
    while (regex_search(input,matches,pattern)) {   //while there is something in the string that matches the regex expression 
        string part = matches[0];                   //get the matched string
        if(!part.empty())                           
            res.push_back(part);                    //add part to list
        input = regex_replace(matches.suffix().str(),regex("[^\\x00-\\x7F]+"),"-");//remove the part found from the list and replace illegal characters 
       
    }
    return res;
}

vector<string> cleanExpressions(vector<string> v){                      //remove spaces from each string in vector
    for(unsigned int i=0;i<v.size();i++)
        v[i] = removeSpaces(v[i]);
    return v;
}

void deriveEquations(LinkedList* l){//derive every node in LinkedList
    ((Node*)l->value)->derive();
    if(l->hasNext())
        deriveEquations(l->next);
}

void populateLinkedList(LinkedList* l, vector<string> equation){//create all nodes and add them to LinkedList
    for(auto part: equation){
        Node *n = createNode(removeSpaces(part));
        l->add(n);
    }
}

Node* createNode( string eq){//parse string and create node 
    string trigexp = "(sin)|(cos)|(tan)|(sec)|(csc)|(cot)";                     //Parsing Patterns
    regex XcoefficientPattern("\\-*[\\d\\.]*x");                                //...
    regex XfractionPattern("\\-?\\s*\\(\\-*\\d\\/\\-*\\d\\)x");                 //...
    regex TrigfractionPattern("\\-?\\s*\\(\\-*\\d\\/\\-*\\d\\)("+trigexp+")");  //...
    regex TrigcoefficientPattern("\\-*[\\d\\.]*("+trigexp+")");                 //...
    regex trigPattern(trigexp);                                                 //...
    regex exponentPattern("\\^\\-*[\\d\\.]+");                                  //...
    regex number("\\-*[\\d\\.]+");                                              //...
    smatch matches;
    
    regex_search(eq,matches,trigPattern);                   //find any trig functions
    string trig = matches[0];
    
    regex_search(eq,matches,XcoefficientPattern);           //find X coefficient
    string temp = matches[0];
    double Xcoefficient;
    if(!temp.empty()){                                      //if result string contains anything 
        regex_search(temp,matches,number);                  //search result string for numbers 
        if(((string)matches[0]).empty())                    //if no numbers found
            Xcoefficient = temp.find("-")!=-1 ? -1.0 : 1.0; //set value to '-1' if '-' exists in result string otherwise '1'
        else
            Xcoefficient = stof((string)matches[0]);        //if number exists convert result string to float
    }
    else if(regex_match(eq,number))                         //if string is only a number
        Xcoefficient = stof(eq);                            //convert string to float
    else
        Xcoefficient = 1.0;                                 //else set to '1'
    
    regex_search(eq,matches,XfractionPattern);              //find any fractions infront of 'x'
    string Xfraction = matches[0];
    
    regex_search(eq,matches,TrigfractionPattern);           //find any fractions infront of trig functions
    string Trigfraction = matches[0];
    
    regex_search(eq,matches,TrigcoefficientPattern);        //find any trig coefficient
    temp = matches[0];
    double Trigcoefficient;
    if(!temp.empty()){                                      //if result contains anything
        regex_search(temp,matches,number);                  //search for a number
        if(((string)matches[0]).empty())                    //if no number is found
            Trigcoefficient = temp.find("-")!=-1 ? -1.0 : 1.0;  //if result contains '-' set value to '-1', if not '1'
        else
            Trigcoefficient = stof((string)matches[0]);     //if number exists, convert result to float
    }
    else
        Trigcoefficient = 1.0;                              //else set to '1'
    
    regex_search(eq,matches,exponentPattern);               //search for exponent behind 'x'
    temp = matches[0];
    regex_search(temp,matches,number);                      //search for number
    string exponent = matches[0];
    
    if(exponent.empty())                                    //if empty
        exponent = "1";                                     //set to '1'
    if( eq.find("x")==-1)                                   //if equation does not contain 'x'
        exponent = "0";                                     //set to '0'

    
    if(!Xfraction.empty()){                                                             //if there is a fraction infront of 'x'
        int index = Xfraction.find("/");                                                //find position of '/'
        double numerator = stof(Xfraction.substr(Xfraction.find("(")+1,index));         //find numerator and convert to float
        double denomenator = stof(Xfraction.substr(index+1,Xfraction.find(")")-index)); //find denomenator and convert to float
        Xcoefficient = numerator/denomenator;                                           //get decimal value
        Xcoefficient = Xfraction.substr(0,Xfraction.find("(")).find("-")!=-1 ? Xcoefficient*-1.0 : Xcoefficient;      //if string contains '-' in the front set to negative  
    }
    
        if(!Trigfraction.empty()){                                                              //if there is a fraction infront of a trig function
        int index = Trigfraction.find("/");                                                     //find position of '/'
        double numerator = stof(Trigfraction.substr(Trigfraction.find("(")+1,index));           //find numerator and convert to float
        double denomenator = stof(Trigfraction.substr(index+1,Trigfraction.find(")")-index));   //find denomenator and conver to float
        Trigcoefficient = numerator/denomenator;                                                //get decimal value
        Trigcoefficient = Trigfraction.find("-")!=-1 ? Trigcoefficient*-1.0 : Trigcoefficient;  //if string contains '-' set to negative 
    }
    
    Node* n = new Node(Xcoefficient,stof(exponent),Trigcoefficient,getTrig(trig));  //create node

    return n;
    
}

Function* getTrig(string trig){//returns the trig function corresponding to the given string value
    Function* functions[6] = {new Sin(), new Cos(), new Tan(), new Sec(), new Csc(), new Cot()};// create list of trig functions
    for(auto t:functions)                       //for each function
        if(removeSpaces(t->getValue())==trig)   //if function name is equal to given string
            return t;                           //return function
    return nullptr;                             //if none are found return nullptr
}

string removeSpaces(string s){//removes spaces from a given string
    string::iterator end_pos = remove(s.begin(), s.end(), ' ');
    s.erase(end_pos, s.end());
    return s;
}

void removeZeros(LinkedList* l){//remove any values that are zero from the given LinkedList
    for(;l!=nullptr;l=l->next)
        if(l->value->getValue().empty())
            l->pop();
}

string combineEquation(LinkedList* l){//combines the nodes in a linkedlist into a single string and formats the resulting string
    string res = "";
    for(;l!=nullptr;l=l->next){     //iterates through LinkedList
        res += l->value->getValue();//concatinates value of node to resulting string
        if(l->hasNext())
            res += (((Node*)l->next->value)->coefficient>=0 || //adds the correct operator
                    ((Node*)l->next->value)->functionCoefficient>=0) ? "+" : "-";            
    }
    regex zeros("[//+//-]*\\D(0\\.0)|^(0\\.0)");//Formating patterns 
    regex doubleNeg("\\-{2}");                  //...
    regex neg("(\\+\\-)|(\\-\\+)");             //...
    regex remove("^\\+");                       //...

    res = std::regex_replace(res,zeros,"");         //removes all extraneous zeros 
    res = std::regex_replace(res, doubleNeg, "+");  //replaces all '--' with '+'
    res = std::regex_replace(res, neg, "-");        //replaces all '+-' or '-+' with '-'
    res = std::regex_replace(res, remove, "");      //removes any leading '+'
    return res;
}

void writeToFile(string fileName,LinkedList* l){//outputs the given LinkedList to the given file
    ofstream file(fileName,ios::app);           //create filestream with given file
    if(file.is_open())                          //if file is open
        file<<combineEquation(l)<<endl;;        //output to file
    file.close();
}