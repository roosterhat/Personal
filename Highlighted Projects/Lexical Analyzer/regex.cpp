#include <string>
#include <vector>
#include <map>
#include <set>
#include <iostream>
#include <algorithm>
#include <cstdio>
#include "regex.h"

using namespace std;

char E_NFA::EPSILON = 21;

vector<int> E_NFA::traverseEpsilon(int state){
    vector<int> reachable;
    reachable.push_back(state);
    int e_index = alpha_index[E_NFA::EPSILON];
    for(int s : transitions[state][e_index]){
        vector<int> r = traverseEpsilon(s);
        reachable.insert(reachable.end(), r.begin(), r.end());
    }
    return reachable;
}

NFA E_NFA::toNFA(){
    vector<vector<vector<int>>> newTransitions(states);
    vector<char> newAlpha(alphabet.begin(), alphabet.end()-1);
    set<int> final_set(final_s.begin(), final_s.end());
    for(int s = 0; s < states; s++){
        vector<int> e_traverse = traverseEpsilon(s);
        newTransitions[s] = vector<vector<int>>(newAlpha.size());
        for(int a = 0; a < newAlpha.size(); a++){
            newTransitions[s][a] = vector<int>{};
            for(int st : e_traverse){
                for(int t : transitions[st][a]){
                    vector<int> trans = traverseEpsilon(t);
                    newTransitions[s][a].insert(newTransitions[s][a].end(), trans.begin(), trans.end());
                }
                for(int f : final_s)
                    if(f==st)
                        final_set.insert(s);
            }
        }
    }
    vector<int> newFinal(final_set.begin(), final_set.end());
    return NFA(states, newAlpha, newTransitions, initial, newFinal);
}

DFA NFA::toDFA(){
    int alpha_size = alphabet.size();
    map<vector<int>,int> newStates;
    int stateIndex = 0;
    vector<vector<vector<int>>> newTransitions;
    set<int> final_set (final_s.begin(), final_s.end());
    map<vector<int>, int>::iterator itr;
    for(int i = 0; i<states; i++){
        vector<int> s {i};
        newTransitions.push_back(vector<vector<int>>(alpha_size));
        newStates[s] = stateIndex++;
    }
    vector<int> s {};
    newTransitions.push_back(vector<vector<int>>(alpha_size));
    newStates[s] = stateIndex++;
    bool created = false;
    do{
        created = false;
        for(itr=newStates.begin(); itr!=newStates.end(); ++itr){
            for(int a = 0; a < alpha_size; a++){
                set<int> trans_set;
                for(int s : itr->first){
                    for(int t :transitions[s][a])
                        trans_set.insert(t);
                }
                vector<int> trans_vec (trans_set.begin(), trans_set.end());
                sort(trans_vec.begin(), trans_vec.end());
                if(newStates.find(trans_vec) == newStates.end()){
                    newStates[trans_vec] = stateIndex++;
                    newTransitions.push_back(vector<vector<int>>(alpha_size));
                    created = true;
                }
                for(int s : itr->first){
                    for(int f : final_s)
                        if(s==f)
                            final_set.insert(itr->second);
                }
                vector<int> index {newStates[trans_vec]};
                newTransitions[itr->second][a] = index;
            }
        }
    }while(created);
    vector<int> newFinal (final_set.begin(), final_set.end());
    return DFA(states, alphabet, newTransitions, initial, newFinal);
}

bool DFA::consume(string input, vector<int> &indices){
    int state = initial;
    for(int i = 0; i<input.size(); i++){
        char c = input[i];
        for(int f : final_s)
            if(f==state)
                indices.push_back(i);
        if(alpha_index.find(c) == alpha_index.end())
            return false;
        state = transitions[state][alpha_index[c]][0];
    }
    for(int f : final_s)
        if(f==state){
            indices.push_back(input[input.size()-1]);
            return true;
        }
    return false;
}

void DFA::print(){
    cout<<"Initial: "<<initial<<endl;
    cout<<"Final States: ";
    for(int i : final_s)
        cout<<i<<" ";
    cout<<endl;
    cout<<"Transition"<<endl;
    int l = 0;
    for(vector<vector<int>> s : transitions)
        for(vector<int> a: s)
            l = max(l,(int)a.size());
    cout<<"   ";
    for(char c : alphabet)
        printf((" %-"+to_string(3*l+1)+"c ").c_str(),c);
    cout<<endl;
    for(int s = 0; s<transitions.size(); s++){
        printf("%2d ",s);
        for(vector<int> a: transitions[s]){
            printf("[");
            for(int t : a)
                printf("%2d,",t);
            printf(("%"+to_string(3*l+2 - 3*a.size()-1)+"s ").c_str(),"]");
        }
        printf("\n");
    }
    cout<<endl;
}

int Regex::findMatching(string exp, int pos, char open, char close){
    int count = 0;
    for(int i = pos; i< exp.size(); i++){
        if(exp[i]==open)
            count++;
        if(exp[i]==close)
            count--;
        if(count==0)
            return i;
    }
    return pos;
}

vector<Group> Regex::findGroups(string exp){
    vector<Group> groups;
    bool ignore = false;
    for(int i = 0; i<exp.size(); i++){
        char cur = exp[i];
        if(ignore){
            groups.push_back(Group(exp,i,i,Group::ATOM));
            ignore = false;
        }
        else{
            if(cur=='('){
                int close = findMatching(exp, i, '(', ')');
                Group paren(exp,i,close,Group::PAREN);
                paren.subgroups = findGroups(exp.substr(i+1,close-i-1));
                groups.push_back(paren);
                i = close;
            }
            else if(cur=='['){
                int close = findMatching(exp, i, '[', ']');
                Group list(exp,i,close,Group::LIST);
                list.subgroups = findGroups(exp.substr(i+1,close-i-1));
                groups.push_back(list);
                i = close;
            }
            else if(cur=='|'){
                Group left = groups[groups.size()-1];
                Group right = findGroups(exp.substr(i+1))[0];
                Group orr(exp,left.s,i + 1 + right.e,Group::OR);
                orr.subgroups.push_back(left);
                orr.subgroups.push_back(right);
                groups[groups.size()-1] = orr;
                i = orr.e;
            }
            else if(cur=='*'){
                Group left = groups[groups.size()-1];
                Group star(exp,left.s,i,Group::STAR);
                star.subgroups.push_back(left);
                groups[groups.size()-1] = star;
            }
            else if(cur=='\\'){
                ignore = true;
            }
            else{
                groups.push_back(Group(exp,i,i,Group::ATOM));
            }
        }
    }
    return groups;
}

void Group::printInternal(){
    cout<<"<"<<types[type];
    if(type==Group::ATOM)
        cout<<": "<<symbol;
    for(Group g : subgroups){
        cout<<"{";
        g.printInternal();
        cout<<"}";
    }
    cout<<">";
}

void Group::print(){
    printInternal();
    cout<<endl;
}

vector<vector<vector<int>>> Regex::createTransitions(vector<Group> groups, map<char,int> a_index, int &state){
    vector<vector<vector<int>>> trans;
    int a_size = a_index.size();
    trans.push_back(vector<vector<int>>(a_size));
    int inner_state = 0;
    for(Group group : groups){
        if(group.type==Group::PAREN){
            return createTransitions(group.subgroups,a_index,state);
        }
        else if(group.type==Group::OR || group.type==Group::LIST){
            vector<pair<int,int>> bounds;
            for(Group sub : group.subgroups){
                int start = state+1;
                vector<vector<vector<int>>> inner = createTransitions(vector<Group>{sub},a_index,++state);
                trans.insert(trans.end(), inner.begin(), inner.end());
                bounds.push_back(pair<int,int>(start,trans.size()-1));
            }
            trans.push_back(vector<vector<int>>(a_size));
            state++;
            for(pair<int,int> bound : bounds){
                trans[inner_state][a_index[E_NFA::EPSILON]].push_back(bound.first);
                trans[bound.second][a_index[E_NFA::EPSILON]].push_back(state);
            }
            inner_state = trans.size()-1;
        }
        else if(group.type==Group::STAR){
            int start = state+1;
            vector<vector<vector<int>>> inner = createTransitions(group.subgroups,a_index,++state);
            trans.insert(trans.end(), inner.begin(), inner.end());
            trans[trans.size()-1][a_index[E_NFA::EPSILON]].push_back(start);
            trans[inner_state][a_index[E_NFA::EPSILON]].push_back(start);
            state++;
            trans[inner_state][a_index[E_NFA::EPSILON]].push_back(state);
            trans[trans.size()-1][a_index[E_NFA::EPSILON]].push_back(state);
            trans.push_back(vector<vector<int>>(a_size));
            inner_state = trans.size()-1;
        }
        else if(group.type==Group::ATOM){
            trans[inner_state++][a_index[group.symbol]] = vector<int>{++state};
            trans.push_back(vector<vector<int>>(a_size));
        }
    }
    return trans;
}

void Regex::buildAlphabet(vector<Group> groups, set<char> & alpha){
    for(Group group: groups){
        if(group.type==Group::ATOM)
            alpha.insert(group.symbol);
        else
            buildAlphabet(group.subgroups, alpha);
    }
}

E_NFA Regex::toE_NFA(){
    set<char> alpha;
    vector<Group> groups = findGroups(expression);
    buildAlphabet(groups, alpha);
    vector<char> alphabet(alpha.begin(), alpha.end());
    alphabet.push_back(E_NFA::EPSILON);
    map<char,int> alpha_index;
    int count = 0;
    for(char c : alphabet)
        alpha_index[c] = count++;
    int state = 0;
    vector<vector<vector<int>>> trans = createTransitions(groups, alpha_index, state);
    vector<int> final_s {state};
    return E_NFA(++state,alphabet,trans,0,final_s);
}

bool Regex::match(string input, Match &match){
    vector<int> indices;
    dfa.consume(input, indices);
    if(!indices.empty()){
        int index = indices[indices.size()-1];
        string m = input.substr(0,index);
        match = Match(m,0,index);
        return true;
    }
    return false;
}

//DFA Minimization

