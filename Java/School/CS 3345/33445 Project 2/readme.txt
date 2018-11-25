IDE: NetBeans
Instructions:
	* Run 'Project2.java'
	* Enter command name or id number.
	* Type help for list of commands.
	* Type help <command name or id> for more detailed information.
	* Some commands allow for parameters (specified in help <command>). Parameters are separated by spaces.
	* If not parameters are given, commands will prompt user for information when needed
Sample:
LinkedList Interaction Interface
Type 'help' to see commands
Enter Command: help
=====Command Help=====
0: HELP
1: EMPTY
2: FIND
3: PREPEND
4: DELETE
5: DELETEID
6: PRINT
7: DONE
Enter Command: help help
=====Command Help=====
(0)HELP:
Displays information about commands
Parameters (<String> Command ID | <String> Command Name)
Enter Command: prepend 1111 mag pub
Magazine Added
Enter Command: prepend 2222 mag pub
Magazine Added
Enter Command: prepend 3333 mag pub
Magazine Added
Enter Command: prepend 1111 mag pub
Failed to Add Magazine: ID already exists
Enter Command: 6
[3333,2222,1111]
Enter Command: find 2222
Magazine: mag
Publisher: pub
Magazine ID: 2222
Enter Command: print
[3333,2222,1111]
Enter Command: delete
Node Deleted
Magazine: mag
Publisher: pub
Magazine ID: 3333
Enter Command: print
[2222,1111]
Enter Command: deleteid 1111
Magazine: mag
Publisher: pub
Magazine ID: 1111
Enter Command: print
[2222]
Enter Command: done
DONE