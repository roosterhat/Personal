/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   LinkedList.h
 *Author: erik ostlind
 * jeo170030
 * Created on November 1, 2017, 12:43 PM
 */
#include <string>
#include "Value.h"
#include "Comparator.h"

using namespace std;

#ifndef LINKEDLIST_H
#define LINKEDLIST_H


class LinkedListIType;

class LinkedList {
public:
    LinkedList *next = nullptr;
    LinkedList *prev = nullptr;
    Value *value = nullptr;
    Comparator *comparator;
    LinkedList(Value* v, Comparator* c = nullptr);
    LinkedList(Comparator * c);
    LinkedList();
    LinkedListIType begin();
    LinkedListIType end();
    bool hasNext();
    bool hasPrev();
    bool hasValue();
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

class LinkedListIType{
public:
    LinkedList* l;
    LinkedListIType(LinkedList* l){this->l = l;}
    bool operator!=(LinkedListIType rhs){return l != rhs.l;}
    Value* operator*(){return l->value;}
    void operator++(){l = l->next;}
};


#endif /* LINKEDLIST_H */

