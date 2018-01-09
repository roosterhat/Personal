/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   BaseNode.h
 * Author: erik ostlind
 * jeo170030
 * Created on November 16, 2017, 4:03 PM
 */

#include <string>

using namespace std;

#ifndef BASENODE_H
#define BASENODE_H

class BaseNode{
public:
    int row,column;
    string value;
    string getValue();
    int getRow();
    int getColumn();
    void setValue(string v);
    void setRow(int r);
    void setColumn(int c);
    virtual string toString()=0;
};

#endif /* BASENODE_H */

