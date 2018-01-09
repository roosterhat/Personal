/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Function.h
 * Author: eriko
 *
 * Created on November 14, 2017, 1:10 PM
 */
#include <string>
#include "Value.h"
#include "Node.h"

using namespace std;

#ifndef FUNCTION_H
#define FUNCTION_H

class Node;

class Function: public Value {
public:
    virtual void derive(Node* l) = 0;

};

#endif /* FUNCTION_H */

