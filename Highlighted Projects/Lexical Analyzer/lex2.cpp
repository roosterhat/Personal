#include <iostream>
#include <fstream>
#include <string>
#include <windows.h>
#include "lex2.h"
#include "proto/Cache.pb.h"

using namespace std;

Token LexicalAnalyzer::INVALID_TOKEN = Token("INVALID",Token::INVALID);

bool LexicalAnalyzer::next(Token &token){
    while(pc<sourceCode.length() && isWhiteSpace(sourceCode.at(pc)))
        pc++;
    if(pc<sourceCode.length()){
        string s = sourceCode.substr(pc);
        Match match;
        for(Matcher m : matchers){
            if(m.exp.match(s, match)){
                token = Token(m.token);
                token.lexeme = match.text;
                pc += match.end;
                return true;
            }
        }
        token = LexicalAnalyzer::INVALID_TOKEN;
        int start = pc;
        while(pc<sourceCode.length() && !isWhiteSpace(sourceCode.at(pc)))
            pc++;
        token.lexeme = sourceCode.substr(start,pc-start);
        return true;
    }
    return false;
}

bool loadMatcher(string name, Matcher * out){
    fstream in(cacheDir+"/"+name, ios::in | ios::binary);
    if(in.is_open()){
        cache::Matcher matcher;
        if(!matcher.ParseFromIstream(&in))
            return false;
        *out = Matcher(matcher);
        in.close();
        return true;
    }
    return false;
}

bool cacheMatcher(string name, Matcher * matcher){
    fstream out(cacheDir+"/"+name, ios::out | ios::trunc | ios::binary);
    if(out.is_open()){
        matcher->toProto().SerializeToOstream(&out);
        out.close();
        return true;
    }
    return false;
}

void buildMatchers(LexicalAnalyzer &lexer){

    cout<<"Loading Expressions..."<<endl;
    string p_0to9 = "0123456789";
    string p_1to9 = "123456789";
    string p_atoz = "abcdefghijklmnopqrstuvwxyz";
    string p_AtoZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    struct Matcher_info{
        string name;
        Matcher * matcher;
        string pattern;
        Token::Type type;
    };

    Matcher TAGS, BEGIN, SEQ, INT, DATE, END, ASSIGN, LCURLY, RCURLY, COMMA, LPAREN, RPAREN, RANGE, NUMBER, VERT, TYPEREF, IDEN;
    
    vector<Matcher_info> matchers {
        {"TAGS", &TAGS, "TAGS", Token::RESERVED},
        {"BEGIN", &BEGIN, "BEGIN", Token::RESERVED},
        {"SEQUENCE", &SEQ, "SEQUENCE", Token::RESERVED},
        {"INTEGER", &INT, "INTEGER", Token::RESERVED},
        {"DATE", &DATE, "DATE", Token::RESERVED},
        {"END", &END, "END", Token::RESERVED},
        {"ASSIGN", &ASSIGN, "::=", Token::TOKEN},
        {"LCURLY", &LCURLY, "{", Token::TOKEN},
        {"RCURLY", &RCURLY, "}", Token::TOKEN},
        {"COMMA", &COMMA, ",", Token::TOKEN},
        {"LPAREN", &LPAREN, "\\(", Token::TOKEN},
        {"RPAREN", &RPAREN, "\\)", Token::TOKEN},
        {"RANGE", &RANGE, "..", Token::TOKEN},
        {"NUMBER", &NUMBER, "0|(["+p_1to9+"]["+p_0to9+"]*)", Token::TOKEN},
        {"VERT", &VERT, "\\|", Token::TOKEN},
        {"TYPEREF", &TYPEREF, "["+p_AtoZ+"](["+p_AtoZ+p_atoz+p_0to9+"]|(-["+p_AtoZ+p_atoz+p_0to9+"]))*", Token::TYPE_REF},
        {"IDEN", &IDEN, "["+p_atoz+"](["+p_AtoZ+p_atoz+p_0to9+"]|(-["+p_AtoZ+p_atoz+p_0to9+"]))*", Token::ID},
    };

    CreateDirectory("cache", NULL);
    for(Matcher_info m : matchers){
        if(!loadMatcher(m.name, m.matcher)){
            *m.matcher = Matcher(m.pattern, Token(m.name, m.type));
            cacheMatcher(m.name, m.matcher);
        }
    }

    lexer.matchers = vector<Matcher> {TAGS, BEGIN, SEQ, INT, DATE, END, ASSIGN, LCURLY, RCURLY, COMMA, LPAREN, RPAREN, RANGE, VERT, NUMBER, TYPEREF, IDEN};
    cout<<"Complete"<<endl;
}

int main(int argc, char** args){
    if(argc>1){
        ifstream file(args[1]);
        if(file.is_open()){
            string sourceCode((istreambuf_iterator<char>(file)),(istreambuf_iterator<char>()));
            file.close();
            LexicalAnalyzer lexer(sourceCode);
            buildMatchers(lexer);
            Token token;
            while(lexer.next(token))
                cout<<"["<<token.type<<"] "<<token.token<<" "<<token.lexeme<<endl;
            cout<<"SUCCESS"<<endl;
	}
        else{
            cout<<"Unable to open file '"<<args[1]<<"'"<<endl;
            return 1;
        }
    }
    else{
        cout<<"Invalid Syntax: expected argument, filepath"<<endl;
        return 1;
    }
}