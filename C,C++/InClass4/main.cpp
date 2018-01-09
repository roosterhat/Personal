/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind, Belmin Vehabovic
 * jeo170030
 * bxv151230
 *
 * Created on November 7, 2017, 1:14 PM
 */

#include <cstdlib>
#include <string>
#include <iostream>
#include <vector>


#include "Temperature.h"
#include "Substance.h"

using namespace std;

/*
 * 
 */
int main() {
    Temperature* t = new Temperature;
    while(true){
        int temp;
        string input;
        cout<<"Enter Temperature (f): ";
        getline(cin,input);
        temp = stoi(input);
        t->setTemperature(temp);
        cout<<"Substances that Freeze at "+to_string(temp)+" f"<<endl;
        for(Substance* s: t->getFreezing(temp))
            cout<<"* "<<s->toString()<<endl;
        cout<<endl;
        cout<<"Substances that Boil at "+to_string(temp)+" f"<<endl;
        for(Substance* s: t->getBoiling(temp))
            cout<<"* "<<s->toString()<<endl;
    }
    return 0;
}

