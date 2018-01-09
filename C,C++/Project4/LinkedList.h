/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   LinkedList.h
 * Author: eriko
 *
 * Created on November 1, 2017, 12:43 PM
 */
#include <string>
#include "Value.h"
#include "Comparator.h"

using namespace std;

#ifndef LINKEDLIST_H
#define LINKEDLIST_H

class LinkedList {
    public:
    LinkedList *next = nullptr;
    LinkedList *prev = nullptr;
    Value *value = nullptr;
    Comparator *comparator;
    LinkedList(Value* v, Comparator* c);
    LinkedList(Comparator * c);
    LinkedList();
    bool hasNext();
    bool hasPrev();
    void setNext(LinkedList* node);
    void setPrev(LinkedList *node);
    void add(Value * value);
    void push(Value * value);
    void append(Value * value);
    Value* pop();
    LinkedList* get(string value);
    LinkedList* get(int index);
    void remove(string value);
    void remove(int index);
    string toString();
    int size();
    bool isEmpty();
    
private:
    void deleteSelf();

};

class BasicComparator: public Comparator{
public: 
    bool compare(Value *a, Value *b);
};

#endif /* LINKEDLIST_H */

