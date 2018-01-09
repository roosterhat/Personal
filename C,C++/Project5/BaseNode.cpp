/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   BaseNode.cpp
 * Author: erik ostlind
 * jeo170030
 * Created on November 16, 2017, 4:03 PM
 */

#include "BaseNode.h"

int BaseNode::getColumn(){
    return column;
}
int BaseNode::getRow(){
    return row;
}
string BaseNode::getValue(){
    return value;
}

void BaseNode::setColumn(int c){
    column = c;
}
void BaseNode::setRow(int r){
    row = r;
}
void BaseNode::setValue(string v){
    value = v;
}

