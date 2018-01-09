/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Copy.h
 * Author: erik ostlind
 * jeo170030
 *
 * Created on November 30, 2017, 1:56 PM
 */

#include "Filter.h"

#ifndef COPY_H
#define COPY_H

class Copy: public Filter {
public:
    void doFilter(ifstream &in, ofstream &out);
};

#endif /* COPY_H */

