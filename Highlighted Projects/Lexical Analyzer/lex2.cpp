#include <iostream>
#include <fstream>
#include <string>
#include "lex2.h"

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

void buildMatchers(LexicalAnalyzer &lexer){
    cout<<"Compiling Expressions..."<<endl;
    string p_0to9 = "0123456789";
    string p_1to9 = "123456789";
    string p_atoz = "abcdefghijklmnopqrstuvwxyz";
    string p_AtoZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    Matcher TAGS("TAGS",Token("TAGS",Token::RESERVED));
    Matcher BEGIN("BEGIN",Token("BEGIN",Token::RESERVED));
    Matcher SEQ("SEQUENCE",Token("SEQUENCE",Token::RESERVED));
    Matcher INT("INTEGER",Token("INTEGER",Token::RESERVED));
    Matcher DATE("DATE",Token("DATE",Token::RESERVED));
    Matcher END("END",Token("END",Token::RESERVED));

    Matcher ASSIGN("::=",Token("ASSIGN",Token::TOKEN));
    Matcher LCURLY("{",Token("LCURLY",Token::TOKEN));
    Matcher RCURLY("}",Token("RCURLY",Token::TOKEN));
    Matcher COMMA(",",Token("COMMA",Token::TOKEN));
    Matcher LPAREN("\\(",Token("LPAREN",Token::TOKEN));
    Matcher RPAREN("\\)",Token("RPAREN",Token::TOKEN));
    Matcher RANGE("..",Token("Range_Seperator",Token::TOKEN));
    Matcher NUMBER("0|(["+p_1to9+"]["+p_0to9+"]*)",Token("Number",Token::TOKEN));
    Matcher VERT("\\|",Token("VERT",Token::TOKEN));

    Matcher TYPEREF("["+p_AtoZ+"](["+p_AtoZ+p_atoz+p_0to9+"]|(-["+p_AtoZ+p_atoz+p_0to9+"]))*",Token("TypeRef",Token::TYPE_REF));
    Matcher IDEN("["+p_atoz+"](["+p_AtoZ+p_atoz+p_0to9+"]|(-["+p_AtoZ+p_atoz+p_0to9+"]))*",Token("Identifier",Token::ID));

    lexer.matchers = vector<Matcher> {TAGS,BEGIN,SEQ,INT,DATE,END,ASSIGN,LCURLY,RCURLY,COMMA,LPAREN,RPAREN,RANGE,VERT,NUMBER,TYPEREF,IDEN};
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