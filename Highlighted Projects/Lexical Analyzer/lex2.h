#include <string>
#include <iostream>
#include "regex.h"

#pragma once

class Token{
    public:
        enum Type {RESERVED, TYPE_REF, ID, TOKEN, INVALID};
        std::string token, lexeme;
        Token::Type type;
        Token(){}
        Token(std::string t, Token::Type ty){
            token = t;
            type = ty;
        }
        Token(const Token& t) {
            token = t.token;
            type = t.type;
            lexeme = t.lexeme;
        }
};

class Matcher{
    public:
        std::string pattern;
        Regex exp;
        Token token;
        Matcher(){}
        Matcher(std::string p, Token t){
            pattern = p;
            token = t;
            exp = Regex(pattern);
        }
        bool match(std::string value){
            return true;
        }
        Token generate(std::string value){
            token.lexeme = value;
            return token;
        }
};

class LexicalAnalyzer{
        int pc;
    public:
        static Token INVALID_TOKEN;
        std::string sourceCode; 
        std::vector<Matcher> matchers;
        LexicalAnalyzer(std::string code){
            pc = 0;
            sourceCode = code;
        }
        bool next(Token &token);
        bool isWhiteSpace(char c){
            return c==9||c==10||c==11||c==12||c==13||c==32;
        }
        void reset(){
            pc = 0;
        }
};

void buildMatchers(LexicalAnalyzer &lexer);
int main(int argc, char** args);



