/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Node.h
 * Author: erik ostlind
 * jeo170030
 * Created on November 16, 2017, 4:11 PM
 */

#include "BaseNode.h"

#ifndef NODE_H
#define NODE_H

class Node: public BaseNode {
public:
    Node* up;
    Node* down;
    Node* left;
    Node* right;
    Node();
    Node(int, int, string);
    void setUp(Node*);
    void setDown(Node*);
    void setLeft(Node*);
    void setRight(Node*);
    Node* getUp();
    Node* getDown();
    Node* getLeft();
    Node* getRight();
    string toString();
};

#endif /* NODE_H */

