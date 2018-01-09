/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   LinkedList.cpp
 * Author: eriko
 * 
 * Created on November 1, 2017, 12:43 PM
 */
#include <cstdlib>
#include <string>
#include "Value.h"
#include "Comparator.h"
#include "LinkedList.h"


using namespace std;

bool BasicComparator::compare(Value *a, Value *b){
    return a->getValue()>b->getValue();
}


LinkedList::LinkedList(Value* v, Comparator* c){
    value = v;
    comparator = c;
}

LinkedList::LinkedList(Comparator * c){
    comparator = c;
}

LinkedList::LinkedList(){
    comparator = new BasicComparator();
}

bool LinkedList::hasNext(){
    return next!=nullptr;
}

bool LinkedList::hasPrev(){
    return prev!=nullptr;
}

void LinkedList::setNext(LinkedList* node){ //sets the next node of the linked list and sets the previous node for the new node to the current node
    next = node;
    if(node!=nullptr)
        node->prev = this;
}

void LinkedList::setPrev(LinkedList *node){ //sets the previous node of the current node to the given node sets the next node of the new node to the current node
    prev = node;
    if(node!=nullptr)
        node->next = this;
}

void LinkedList::add(Value * value){    //adds a new value to the linked list in sorted order
    if(this->value==nullptr)                                //if empty set value to given value
        this->value = value;
    else{
        if(comparator->compare(value,this->value)){         
            if(hasNext())                                   //if current node has a next node, go to the next node
                next->add(value);
            else
                setNext(new LinkedList(value,comparator));  //else set next node to new node
        }
        else{
            push(value);                                    //else current node>value insert value into current position
        }
    }
}

void LinkedList::push(Value * value){   //inserts value into current position
    if(value!=nullptr){                 //if no value exists
        if(hasNext())                   //and current node has a next node
            next->push(this->value);    //push current node to next node
        else
            setNext(new LinkedList(this->value,comparator));    //else set the next node to a copy of the current node
    }
    this->value = value;    //set the value of the current node to the given value
}

void LinkedList::append(Value * value){
    if(hasNext())
        this->next->append(value);
    else{
        if(this->value==nullptr)
            this->value = value;
        else
            setNext(new LinkedList(value,comparator));
    }
}

Value* LinkedList::pop(){   //return and remove the first value of the list
    Value* value = this->value; //get current value
    deleteSelf();               //delete current node
    return value;               //return value
}

LinkedList* LinkedList::get(string value){  //returns the node containing the given value
    if(this->value->equal(value))       //compares value against current node
        return this;
    if(hasNext())                       //if current node has next node
        return this->next->get(value);  //check next node
    return nullptr;                     //if it does not return null pointer
}

LinkedList* LinkedList::get(int index){     //returns the node at given index
    if(index==0)                //if index = 0 return current node
        return this;
    if(hasNext())               //if has next node
        return next->get(index-1);  //check next node while decrementing index
    return nullptr;             //else return null pointer
}

void LinkedList::remove(string value){  //remove node containing given value
    if(this->value->equal(value))   //if current node contains given value 
        deleteSelf();               //delete current node
    else if(hasNext())              //if has next node 
        next->remove(value);        //check next node        
}

void LinkedList::remove(int index){     //remove node at given index
    if(index==0)            //if index = 0 
        deleteSelf();       //delete current node
    else if(hasNext())      //if has next node 
        next->remove(index-1);  //check next node while decrementing index
}

string LinkedList::toString(){  //converts list to string
    string res = "";
    if(value!=nullptr)                      //if current value is not null
        res+=value->getValue();             //add value to string
    if(hasNext())                           //if current has next node
        return res+=","+next->toString();   //add ',' to string and get string of next node
    return res;                             //return result
}

int LinkedList::size(){ //gets the size of the list
    if(hasNext())               //if current node has next node
        return next->size()+1;  //get size of next node and add 1
    if(value!=nullptr)          //if value is not null
        return 1;               
    return 0;
}

bool LinkedList::isEmpty(){
    return size()==0;
}

void LinkedList::deleteSelf(){  //deletes current node from list
    if(hasPrev())                           //if current node has previous node
        prev->setNext(next);                //chain previous node to next node
    else{
        if(hasNext()){                      //if has next
            this->value = next->value;      //replace current value with next value
            this->next = this->next->next;  //replace current's next node with the next node of current's next node
        }
        else
            this->value = nullptr;          //otherwise set value to null pointer
    }
}


