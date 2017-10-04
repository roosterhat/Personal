/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.cpp
 * Author: eriko
 *
 * Created on September 9, 2017, 4:55 PM
 */

#include <cstdlib>
#include <iostream>
#include <cstdlib>

using namespace std;
int sum(int array[], int size);
string reverse(string word);
/*
 * 
 */
int main() {
    for(int i=0;i<10;i++){
        int size = rand()%10+5;
        int *array = new int[size];
        int total = 0;
        for(int x=0;x<size;x++){
            int num = rand()%100;
            array[x] = num;
            if(x%2!=0)
                total+=num;
        }
        cout<<"Test "<<i<<": "<<sum(array,size)<<" == "<<total<<endl;
    }
    string testWords[] = {"alphabet","racecar!","water", "testing", "abcdefg"};
    for(string word: testWords)
        cout<<"Reverse: "<<word<<" -> "<<reverse(word)<<endl;
    return 0;
}

int sum(int array[], int size)
{
    size--;
    if(size<0)
        return 0;
    if(size%2==0)
        return sum(array,size);
    else
        return sum(array,size-1)+array[size];
}

string reverse(string word)
{
    if(word.empty())
        return "";
    return word[word.length()-1]+reverse(word.substr(0,word.length()-1));
}

