/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Grid.cpp
 * Author: erik ostlind
 * jeo170030
 * 
 * Created on November 16, 2017, 1:06 PM
 */

#include "Grid.h"
#include "Node.h"


Grid::Grid(int row, int col, Grid* above, Grid* left){
    if(above==nullptr && left!=nullptr){
        if(left->hasAbove())
            if(left->above->hasRight())
                left->above->right->setBelow(this);
        this->left = left;
    }
    if(left==nullptr && above!=nullptr){
        if(above->hasLeft())
            if(above->left->hasBelow())
                above->left->below->setRight(this);
        this->above = above;
    }
    findPosition();
    setValue(new Node(this->row,this->column," "));
    if(col-1>this->column)
        right = new Grid(row,col,nullptr,this);
    if(row-1>this->row && this->column==0)
        below = new Grid(row,col,this);
    
    
}
Grid::Grid(Node* node){
    value = node;
    findPosition();
}
Grid::Grid(Grid* g): Grid(g->row,g->column){
    findPosition();
    for(int y=0;y<g->getRows();y++)
        for(int x=0;x<g->getColumns();x++)
            set(new Node(y,x,g->get(y,x)->getValue()->getValue()),y,x);
}
Grid::Grid(){
    value = new Node(0,0,"");
    findPosition();
}

bool Grid::hasAbove(){
    return above!=nullptr;
}
bool Grid::hasBelow(){
    return below!=nullptr;
}
bool Grid::hasLeft(){
    return left!=nullptr;
}
bool Grid::hasRight(){
    return right!=nullptr;
}
bool Grid::hasValue(){
    return value!=nullptr;
}

void Grid::setAbove(Grid* g){
    updateSurrounding(above,g);
    g->below = this;
    above = g;
    g->findPosition();
}
void Grid::setBelow(Grid* g){
    updateSurrounding(below,g);
    g->above = this;
    below = g;
    g->findPosition();
}
void Grid::setLeft(Grid* g){
    updateSurrounding(left,g);
    g->right = this;
    left = g;
    g->findPosition();
}
void Grid::setRight(Grid* g){
    updateSurrounding(right,g);
    g->left = this;
    right = g;
    g->findPosition();
}

void Grid::setValue(Node* n){
    value = n;
}

void Grid::updateSurrounding(Grid* old, Grid* neww){
    if(old!=nullptr && neww!=nullptr){
        neww->below = old->below;
        neww->above = old->above;
        neww->right = old->right;
        neww->left  = old->left;
    }
}

Grid* Grid::getAbove(){
    return above;
}
Grid* Grid::getBelow(){
    return below;
}
Grid* Grid::getRight(){
    return right;
}
Grid* Grid::getLeft(){
    return left;
}


Node* Grid::getValue(){
    return value;
}

Grid* Grid::get(int row, int col){
    if(this->row==row && this->column==col)
        return this;
    
    if(this->row<row && hasBelow())
        return this->below->get(row,col);
    else if(this->row>row && hasAbove())
        return this->above->get(row,col);

    if(this->column<col && hasRight())
        return this->right->get(row,col);
    else if(this->column>col && hasLeft())
        return this->left->get(row,col);
    
    return nullptr;
}

void Grid::set(Node* node, int row, int col){
    Grid* temp = get(row,col);
    if(temp!=nullptr)
        temp->setValue(node);
}

int Grid::getRows(){
    if(hasBelow())
        return 1+below->getRows();
    return 1;
}

int Grid::getColumns(){
    if(hasRight())
        return 1+right->getColumns();
    return 1;
}

string Grid::toString(){
    string res = hasValue() ? value->getValue() : "";
    if(hasRight())
        res+=right->toString();
    if(column==0 && hasBelow())
        res+="\n"+below->toString();
    return res;
}

ostream& operator<<(ostream& stream, Grid* g){
        stream<<g->toString();
        return stream;
} 

fstream& operator<<(fstream& stream, Grid* g){
    stream<<g->toString();
    return stream;
}

void Grid::findPosition(){
    row = hasAbove() ? above->row+1 : 0;
    column = hasLeft() ? left->column+1 : 0;
}
