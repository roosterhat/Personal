#include <string>
#include <vector>
#include <map>
#include <set>
#include <iostream>
#include "proto/Cache.pb.h"
#include <google/protobuf/map.h>

#pragma once

class Match{
    public:
        std::string text;
        int start, end;
        Match(){}
        Match(std::string match, int s, int e){
            text = match;
            start = s;
            end = e;
        }
};

class DFA{
    public:
        std::vector<std::vector<std::vector<int>>> transitions;
        int states;
        std::vector<char> alphabet;
        std::map<char,int> alpha_index;
        int initial;
        std::vector<int> final_s;
        DFA(){}
        DFA(int state, 
            std::vector<char> alpha, 
            std::vector<std::vector<std::vector<int>>> trans, 
            int init, 
            std::vector<int> fin){
            states = state;
            alphabet = alpha;
            for(int i = 0;i<alphabet.size();i++)
                alpha_index[alphabet[i]] = i;
            transitions = trans;
            initial = init;
            final_s = fin;
        }
        DFA(cache::DFA dfa){
            states = dfa.states();
            initial = dfa.initial();
            for(cache::Pair pair: *dfa.mutable_alpha_index())
                alpha_index[pair.key()] = pair.value();
            alphabet = std::vector<char>(dfa.mutable_alphabet()->begin(), dfa.mutable_alphabet()->end());
            final_s = std::vector<int>(dfa.mutable_final_s()->begin(), dfa.mutable_final_s()->end());
            int x,y;
            for(x = 0; x < dfa.mutable_transitions()->size(); x++){
                transitions.push_back(std::vector<std::vector<int>>());
                cache::V_2D v_2d = dfa.mutable_transitions()->at(x);
                for(y = 0; y < v_2d.mutable_array()->size(); y++){
                    transitions[x].push_back(std::vector<int>());
                    cache::V_1D v_1d = v_2d.mutable_array()->at(y);
                    for(auto i : *v_1d.mutable_item()){
                        transitions[x][y].push_back(i);
                    }
                }
            }
        }
        void minimize();
        void print();
        bool consume(std::string input, std::vector<int> &indices);
        cache::DFA toProto(){
            cache::DFA dfa;
            dfa.set_states(states);
            dfa.set_initial(initial);
            for(std::pair<const char, int> p : alpha_index){
                cache::Pair* pair = dfa.add_alpha_index();
                pair->set_key((uint32_t) p.first);
                pair->set_value(p.second);
            }
            for(char c : alphabet)
                dfa.add_alphabet((uint32_t)c);
            for(int s : final_s)
                dfa.add_final_s(s);
            int x,y,z;
            for(x = 0; x < transitions.size(); x++){
                cache::V_2D *v_2d = dfa.add_transitions();
                for(y = 0; y < transitions[x].size(); y++){
                    cache::V_1D *v_1d = v_2d->add_array();
                    for(z = 0; z < transitions[x][y].size(); z++){
                        v_1d->add_item(transitions[x][y][z]);
                    }
                }
            }
            return dfa;
        }
};

class NFA : public DFA{
    public:
        NFA(int state, 
            std::vector<char> alpha, 
            std::vector<std::vector<std::vector<int>>> trans, 
            int init, 
            std::vector<int> fin):  DFA(state, alpha, trans, init, fin){}
        DFA toDFA();
};

class E_NFA : public NFA{
    private:
        std::vector<int> traverseEpsilon(int state);
    public:
        static char EPSILON;
        E_NFA(  int state, 
                std::vector<char> alpha, 
                std::vector<std::vector<std::vector<int>>> trans, 
                int init, 
                std::vector<int> fin):  NFA(state, alpha, trans, init, fin){}
        NFA toNFA();
};

class Group{
    private:
        void printInternal();
    public:
        enum Type {PAREN, OR, LIST, STAR, ATOM};
        char symbol;
        int s,e;
        std::string exp;
        Group::Type type;
        std::vector<Group> subgroups;
        std::map<Group::Type, std::string> types;
        Group(){}
        Group(std::string exp, int s, int e, Group::Type type){
            types[Group::ATOM] = "ATOM";
            types[Group::STAR] = "STAR";
            types[Group::OR] = "OR";
            types[Group::LIST] = "LIST";
            types[Group::PAREN] = "PAREN";
            this->exp = exp;
            this->s = s;
            this->e = e;
            if(s==e)
                this->symbol = exp[s];
            this->type = type;
        }
        void print();
};

class Regex{
    private:
        
        E_NFA toE_NFA();
        std::vector<Group> findGroups(std::string exp);
        int findMatching(std::string exp, int pos, char open, char close);
        void buildAlphabet(std::vector<Group> groups, std::set<char> & alpha);
        std::vector<std::vector<std::vector<int>>> createTransitions(std::vector<Group> groups, 
                                                                            std::map<char,int> a_index, 
                                                                            int &state);
    public:
        DFA dfa;
        std::string expression;
        Regex(){}
        Regex(std::string exp){
            expression = exp;
            E_NFA e_nfa = toE_NFA();
            NFA nfa = e_nfa.toNFA();
            dfa = nfa.toDFA();
            //dfa.minimize();
        }
        Regex(cache::Regex regex){
            dfa = DFA(regex.dfa());
            expression = regex.expression();
        }
        bool match(std::string target, Match &match);
        cache::Regex toProto(){
            cache::Regex regex;
            *regex.mutable_dfa() = dfa.toProto();
            regex.set_expression(expression);
            return regex;
        }
};

