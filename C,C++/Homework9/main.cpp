/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: erik ostlind
 * jeo170030
 *
 * Created on November 30, 2017, 1:19 PM
 */

#include <cstdlib>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>

#include "Copy.h"
#include "Encryption.h"
#include "Uppercase.h"


using namespace std;

/*
 * 
 */
class Item{
public:
    Item(Filter* f, string file){
        filter = f;
        outfile = file;
    }
    Filter* filter;
    string outfile;
};


int main() {
    vector<Item*> filters = {new Item(new Copy,"copy.txt"),new Item(new Encryption(1),"encryption.txt"),new Item(new Uppercase,"uppercase.txt")};
    for(Item* i: filters){
        ifstream in("test.txt");
        ofstream out(i->outfile);
        i->filter->doFilter(in,out);
        in.close();
    }
    return 0;
}

