Lexical Analyzer that parses input files for predefined tokens, using homemade Regex matcher. Prints token type and lexeme.
Uses Protobuf to store compiled matcher objects. Requires a valid protobuf library inorder to compile. 
If you want to run the program yourself youll need to modify/create Makefile which includes protobuf headers and libraries.