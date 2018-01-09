/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Comparator.h
 * Author: erik ostlind
 * jeo170030
 * Created on November 1, 2017, 12:55 PM
 */
#include "Value.h"

#ifndef COMPARATOR_H
#define COMPARATOR_H

class Comparator {
public:
    virtual bool compare(Value *a, Value *b)=0;
};

#endif /* COMPARATOR_H */

