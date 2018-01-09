//Erik Ostlind jeo170030
//Matthew Hicks mgh160030
//Jemetric Freeman jrf160630

#include <iostream>
using namespace std;

struct node
{
    int num;
    node *next = nullptr;
    node(){}
    node(int num){
        this->num = num;
    }
};

void addNode(node *&);
void deleteNode(node *&, int);
void printList(node *);
int search(node* head, int number);
void insert(node* head, int number, int index);
void insertItem(node* head);
void searchList(node* head);
void removeItem(node* head);
void remove(node* &head, int index);

int main()
{
    int menu;
    node *head = nullptr;
        

    do
    {
        cout << "1. Add Node\n"
            << "2. Delete Node\n"
            << "3. Print List\n"
            << "4. Search List\n"
            << "5. Insert Node\n"
            << "6. Remove Node\n"
            << "7. Quit\n\n"
            << "Please pick an operation for the linked list: ";
        cin >> menu;

        if (menu == 1)
            addNode(head);
        else if (menu == 2)
        {
            int num2del;
            cout << "What number would you like to delete";
            cin >> num2del;
            deleteNode(head, num2del);
        }
        else if (menu == 3)
            printList(head);
        else if(menu==4)
            searchList(head);
        else if(menu==5)
            insertItem(head);
        else if(menu==6)
            removeItem(head);
        
    } while (menu != 7);
    cout<<search(head,1337)<<endl;
    cout<<search(head,0)<<endl;
    cout<<search(head,1)<<endl;

}

void searchList(node* head){
    int input;
    cout<<"Enter Number to Search For: ";
    cin>>input;
    cout<<search(head,input)<<endl;
}

void insertItem(node* head){
    int number;
    int index;
    cout<<"Enter Number to Insert: ";
    cin>>number;
    cout<<"Enter Index: ";
    cin>>index;
    insert(head,number,index);
}

void removeItem(node* head){
    int index;
    cout<<"Enter Index: ";
    cin>>index;
    remove(head,index);
}

void printList(node *head)
{
    node *ptr = head;
    while (ptr != nullptr)
    {
        cout << ptr->num << " ";
        ptr = ptr->next;
    }
    cout << endl;
}
void addNode(node *&head)
{
    node *A = new node, *ptr = head;

    cout << "Enter the value for the node: ";
    cin >> A->num;
    A->next = nullptr;

    ptr = head;
    if (head == nullptr)
    {
         head = A;
         return;
    }
    else if (A->num < head->num)
    {
        A->next = head;
        head = A;
        return;
    }
    else
    {
        while (ptr->next != nullptr)
        {
            if (ptr->next->num > A->num)
            {
                A->next = ptr->next;
                ptr->next = A;
                return;
            }
            ptr = ptr->next;
        }
    }

    //if (ptr->next == nullptr)
        ptr->next = A;
}

void deleteNode(node *&head, int del)
{
    node *ptr = head;

    if (head == nullptr)
    {
        cout << "Nothing to delete";
        return;
    }
    else if (del == head->num)
    {
        head = head->next;
        ptr->next = nullptr;
        delete ptr;
        return;
    }
    else
    {
        while (ptr->next != nullptr)
        {
            if (ptr->next->num == del)
            {
                node *hold = ptr->next;
                ptr->next = ptr->next->next;
                hold->next = nullptr;
                delete hold;
                return;
            }
            ptr = ptr->next;
        }

    }
    cout << "Number is not in list";
}

int search(node* head, int number){
    int index=0;
    while(head!=nullptr){
        if(head->num==number)
            return index;
        index++;
        head=head->next;
    }
    return -1;
}

void insert(node* head, int number, int index){
    node* previous = head;
    while(head!=nullptr && index>0){
        previous = head;
        head=head->next;
        index--;
    }
    
    if(head==nullptr){
        node* n = new node(number);
        previous->next = n;
    }
    else{
        node* n = new node(head->num);
        n->next = head->next;
        head->num = number;
        head->next = n;
    }
}

void remove(node* &head, int index){
    node* previous = head, *ptr=head;
    while(ptr!=nullptr && index>0){
        previous = ptr;
        ptr = ptr->next;
        index--;
    }
    if(previous!=nullptr && index==0){
        if(previous==ptr)
            head = head->next;
        else if(ptr!=nullptr)
            previous->next = ptr->next;
    }
}