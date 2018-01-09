/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Encryption.cpp
 * Author: erik ostlind
 * jeo170030
 * 
 * Created on November 30, 2017, 1:31 PM
 */

#include "Encryption.h"

Encryption::Encryption(){
    key = 5;
}

Encryption::Encryption(int key){
    this->key = key;
}

void Encryption::doFilter(ifstream& in, ofstream& out){
    if(in.is_open() && out.is_open()){
        in.seekg(0,in.beg);
        char c;
        while(!in.get(c).fail()){
            c+=char(key);
            if(c<32)
                c = char(127-(32-c));
            if(c>126)
                c = char(c%127+32);
            out<<c;
        }
        out.close();
    }
}