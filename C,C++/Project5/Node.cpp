/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Node.cpp
 * Author: erik ostlind
 * jeo170030
 * Created on November 16, 2017, 4:11 PM
 */

#include "Node.h"

Node::Node(){
    row = 0;
    column = 0;
    value = "";
}

Node::Node(int r, int c, string v){
    row = r;
    column = c;
    value = v;
}

void Node::setUp(Node* n){
    up = n;
}
void Node::setDown(Node* n){
    down = n;
}
void Node::setLeft(Node* n){
    left = n;
}
void Node::setRight(Node* n){
    right = n;
}
Node* Node::getUp(){
    return up;
}
Node* Node::getDown(){
    return down;
}
Node* Node::getLeft(){
    return left;
}
Node* Node::getRight(){
    return right;
}

string Node::toString(){
    return to_string(row)+","+to_string(column)+" '"+value+"'";
}