/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: eriko
 *
 * Created on September 5, 2017, 11:54 PM
 */

#include <cstdlib>
#include <iostream>
#include <fstream>
#include <string.h>
#include <string> 
#include <algorithm>

using namespace std;

void writeTo(fstream & file, string iF, string oF, string text, int pos);

int main() {
    string vowels = "AEIOU";
    cout<<"Input File Name: ";
    string input;
    getline(cin,input);
    string output = "vowels_"+input;
    fstream file(output,ios::out);
    file.close();
    file.open(input,ios::in);
    if(file.is_open()){
        int pos = 1;
        while(true){
            string letter (1,file.get());
            if(!file.eof())
            {
                string temp = letter;
                std::transform(temp.begin(), temp.end(),temp.begin(), ::toupper);
                if(vowels.find(temp) != string::npos || letter=="\n"){
                    if(letter=="\n")
                        pos++;
                    writeTo(file,input,output,letter,pos);
                }
                pos++;
            }
            else
                break;
        }
        cout<<"Output File: "<<output<<endl;
    }
    else
        cout<<"Failed to open files"<<endl;
    return 0;
}

void writeTo(fstream & file, string iF, string oF, string text, int pos)
{
    file.close();
    file.open(oF,ios::app);
    file<<text;
    file.close();
    file.open(iF,ios::in);
    file.seekg(pos,ios::beg);
}

/* this is how it should be done
 string vowels = "AEIOU";
    cout<<"Input File Name: ";
    string input;
    getline(cin,input);
    ifstream inputFile(input);
    ofstream outputFile("vowels_"+input);
    if(inputFile.is_open() && outputFile.is_open()){
        std::string line;
        while(getline(inputFile,line)){
            std::transform(line.begin(), line.end(),line.begin(), ::toupper);
            for(unsigned int i=0;i<line.length();i++){
                string letter = line.substr(i,1);
                if(vowels.find(letter) != string::npos)
                    outputFile<<letter;
            }
            outputFile<<endl;
        }
        inputFile.close();
        outputFile.close();
        cout<<"Output File: "<<"vowels_"<<input<<endl;
    }
    else
        cout<<"Failed to open files"<<endl;
 */

