/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Filter.h
 * Author: erik ostlind
 * jeo170030
 *
 * Created on November 30, 2017, 1:25 PM
 */
#include <iostream>
#include <fstream>

using namespace std;

#ifndef FILTER_H
#define FILTER_H

class Filter {
public:
    virtual void doFilter(ifstream &in, ofstream &out)=0;
};

#endif /* FILTER_H */

