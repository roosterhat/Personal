#include <string>
#include <iostream>
#include "regex.h"
#include "proto/Cache.pb.h"

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
        Token(cache::Token t){
            token = t.token();
            lexeme = t.lexeme();
            type = static_cast<Token::Type>(t.type());
        }
        cache::Token toProto(){
            cache::Token c_token;
            c_token.set_token(token);
            c_token.set_lexeme(lexeme);
            c_token.set_type(static_cast<uint32_t>(type));
            return c_token;
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
        Matcher(cache::Matcher matcher){
            pattern = matcher.pattern();
            token = Token(matcher.token());
            exp = Regex(matcher.exp());
        }
        bool match(std::string value){
            return true;
        }
        Token generate(std::string value){
            token.lexeme = value;
            return token;
        }
        cache::Matcher toProto(){
            cache::Matcher matcher;
            matcher.set_pattern(pattern);
            *matcher.mutable_exp() = exp.toProto();
            *matcher.mutable_token() = token.toProto();
            return matcher;
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

bool loadMatcher(std::string name, Matcher * out);
bool cacheMatcher(std::string name, Matcher * matcher);
void buildMatchers(LexicalAnalyzer &lexer);
int main(int argc, char** args);
std::string cacheDir = "cache";



