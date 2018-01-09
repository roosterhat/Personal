/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Grid.h
 * Author: erik ostlind
 * jeo170030
 * Created on November 16, 2017, 1:05 PM
 */
#include <iostream>
#include <fstream>
#include <string>
#include "Node.h"

using namespace std;

#ifndef GRID_H
#define GRID_H

class Grid{
public:
    Grid();
    Grid(int row, int col, Grid* above=nullptr, Grid* left=nullptr);
    Grid(Node*);
    Grid(Grid*);
    bool hasAbove();
    bool hasBelow();
    bool hasLeft();
    bool hasRight();
    bool hasValue();
    void setAbove(Grid*);
    void setBelow(Grid*);
    void setLeft(Grid*);
    void setRight(Grid*);
    void setValue(Node*);
    Grid* getAbove();
    Grid* getBelow();
    Grid* getLeft();
    Grid* getRight();
    Node* getValue();
    Grid* get(int, int);
    void set(Node*, int, int);
    string toString();
    int getRows();
    int getColumns();
    friend ostream& operator<<(ostream&, Grid*);
    friend fstream& operator<<(fstream&, Grid*);
private:
    int row;
    int column;
    Node* value = nullptr;
    Grid* above = nullptr;
    Grid* below = nullptr;
    Grid* left = nullptr;
    Grid* right = nullptr;
    void updateSurrounding(Grid*, Grid*);
    void findPosition();
};

#endif /* GRID_H */

