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
 * Created on November 7, 2017, 10:02 AM
 */
#include <iostream>
#include <cstdlib>
#include <stdlib.h>
#include "Classy.h"

using namespace std;

/*
 * 
 */
int main() {
    Classy* smallList = new Classy;
    Classy* bigList = new Classy(100);
    Classy* listOfLists[] = {smallList,bigList};
    for(auto l: listOfLists){
        int size = l->getSize();
        for(int i = 0;i<size;i++)
            l->set(rand()%1000+1.0,i);
        int index = rand()%size;
        cout<<l->toString()<<endl;
        cout<<"Size: "<<size<<endl;
        cout<<"Get ["<<index<<"] = "<<l->get(index)<<endl;
        cout<<"Get ["<<size+1<<"] = "<<l->get(size+1)<<endl;
        cout<<"Highest: "<<l->highest()<<endl;
        cout<<"Lowest: "<<l->lowest()<<endl;
        cout<<"Average: "<<l->average()<<endl;
        cout<<endl;
    }
    return 0;
}

