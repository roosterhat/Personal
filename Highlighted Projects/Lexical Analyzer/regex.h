#include <string>
#include <vector>
#include <map>
#include <set>
#include <iostream>

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
        void minimize();
        void print();
        bool consume(std::string input, std::vector<int> &indices);
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
        bool match(std::string target, Match &match);
};

