/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Uppercase.h
 * Author: erik ostlind
 * jeo170030
 *
 * Created on November 30, 2017, 1:51 PM
 */

#include "Filter.h"

#ifndef UPPERCASE_H
#define UPPERCASE_H

class Uppercase: public Filter {
public:
    void doFilter(ifstream &in, ofstream &out);
    char transform(char c);
};

#endif /* UPPERCASE_H */

