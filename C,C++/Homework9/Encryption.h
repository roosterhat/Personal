/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Encryption.h
 * Author: erik ostlind
 * jeo170030
 *
 * Created on November 30, 2017, 1:31 PM
 */

#include "Filter.h"

#ifndef ENCRYPTION_H
#define ENCRYPTION_H

class Encryption: public Filter {
public:
    Encryption();
    Encryption(int);
    void doFilter(ifstream &in, ofstream &out);
private:
    int key;
};

#endif /* ENCRYPTION_H */

