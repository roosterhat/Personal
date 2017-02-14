import random
class LinkedList:
    def __init__(self,*args):
        self.first = None
        if len(args)!=0:
            for l in args:
                if isinstance(l,set):
                    for e in l:
                        if not isinstance(e,Node):
                            self.append(Node(e))
                else:
                    if not isinstance(l,Node):
                        l = Node(l)
                    self.append(l)

    def isEmpty(self):
        return self.first is None

    def __len__(self):
        if self.isEmpty():
            return 0
        else:
            return len(self.first)

    def __str__(self):
        if self.isEmpty():
            return ""
        else:
            return str(self.first)

    def __iter__(self):
        for i in range(0,len(self)):
            yield self.first[i]

    def __contains__(self, item):
        if self.isEmpty():
            return False
        else:
            return  item in self.first

    def __getitem__(self, index):
        if self.isEmpty():
            return None
        else:
            return  self.first[index]

    def __setitem__(self, key, value):
        self.insert(value,key)

    def getHead(self):
        return self.first

    def getTail(self):
        return self[len(self)-1]

    def push(self,n):
        assert isinstance(n,Node)
        if self.isEmpty():
            self.first = n
        else:
            n.setNext(self.first)
            self.first = n

    def append(self,n):
        assert isinstance(n,Node)
        if self.isEmpty():
            self.first = n
        else:
            self.first.append(n)

    def appendList(self,l):
        for e in l:
            if isinstance(e,Node):
                self.append(e)

    def insert(self,n,index):
        assert isinstance(n,Node)
        if index>=len(self):
            self.append(n)
        if index==0:
            if not self.isEmpty():
                n.setNext(self.first)
            self.first = n
        else:
            self.first.insert(n,index-1)

    def reverse(self):
        if not self.isEmpty():
            temp = LinkedList()
            for e in self:
                temp.push(Node(e.value))
            self.first = temp.getHead()






class Node:
    def __init__(self,v):
        self.value = v
        self.next = None

    def hasNext(self):
        return self.next is not None

    def __len__(self):
        if self.hasNext():
            return len(self.next)+1
        else:
            return 1

    def __str__(self):
        if self.hasNext():
            return str(self.value)+","+str(self.next)
        else:
            return str(self.value)

    def __contains__(self, item):
        if self.value==item:
            return True
        else:
            if self.hasNext():
                return item in self.next
            else:
                return False

    def __setitem__(self, key, value):
        self.insert(value,key)

    def __getitem__(self, index):
        if index == 0:
            return self
        if self.hasNext():
            return self.next[index-1]
        else:
            return None

    def setValue(self,v):
        self.value = v

    def setNext(self,n):
        assert isinstance(n, Node)
        self.next = n

    def append(self,n):
        assert isinstance(n, Node)
        if self.hasNext():
            self.next.append(n)
        else:
            self.next = n

    def insert(self,n,i):
        assert isinstance(n,Node)
        if i==0:
            if self.hasNext():
                n.setNext(self.next)
            self.next = n
        else:
            if self.hasNext():
                self.next.insert(n,i-1)



#ll = LinkedList({1,2,3,4})
ll = LinkedList(525,4321)
for i in range(0,10):
    ll.push(Node(random.randrange(0,1000)))
ll.append(Node(1234))
print(len(ll))
print(ll)
ll.insert(Node(1337),3)
print(ll)
ll.reverse()
print("reverse")
print(ll)




class DoubleLinkedList(LinkedList):
    def __init__(self,*args):
        LinkedList.__init__(self,*args)

    def push(self,n):
        assert isinstance(n,Node)
        if self.isEmpty():
            self.first = n
        else:
            self.first.setPrevious(n)
            n.setNext(self.first)
            self.first = n

    def reverse(self):
        if not self.isEmpty():
            temp = DoubleLinkedList()
            for e in self:
                temp.push(DoubleNode(e.value))
            self.first = temp.getHead()



class DoubleNode(Node):
    def __init__(self,v):
        self.previous = None
        self.value = v
        self.next = None

    def hasPrevious(self):
        return self.previous is not None

    def setPrevious(self,n):
        assert isinstance(n,Node)
        if isinstance(n,DoubleNode) and self.hasPrevious():
            n.setPrevious(self.previous)
        self.previous = n
        n.next = self

    def setNext(self,n):
        Node.setNext(self,n)
        if isinstance(n,DoubleNode):
            n.previous = self

    def append(self,n):
        assert isinstance(n, Node)
        if self.hasNext():
            self.next.append(n)
        else:
            self.next = n
            if isinstance(n,DoubleNode):
                n.setPrevious(self)

    def insert(self,n,i):
        assert isinstance(n,Node)
        if i==0:
            if self.hasNext():
                n.setNext(self.next)
                if isinstance(next,DoubleNode):
                    next.setPrevious(n)
            self.next = n
            if isinstance(n,DoubleNode):
                n.setPrevious(self)
        else:
            if self.hasNext():
                self.next.insert(n,i-1)

dl = DoubleLinkedList()
for i in range(0,10):
    dl.append(DoubleNode(i))

print(dl)
dl.insert(DoubleNode(1337),2)
print(dl)
dl.reverse()
print(dl)


class File:
    def __init__(self,n,p):
        self.name = n
        self.path = p
        self.size = 0
        self.data = ()
        self.lastModified = "Today"
        self.root = None

    def __str__(self):
        return self.name

    def getPath(self):
        if self.root is not None:
            return self.root.getPath()+"/"+self.name
        else:
            return "/"+self.name

class Folder:
    def __init__(self,n):
        self.name = n
        self.date = 0
        self.contents = []
        self.root = None

    def __str__(self):
        self.printAllFileNames()

    def __iter__(self):
        return FSIter(self.contents)

    def getPath(self):
        if self.root is not None:
            return self.root.getPath()+"/"+self.name
        else:
            return "/"+self.name

    def getSubDir(self):
        temp = []
        for f in self.contents:
            if isinstance(f,Folder):
                temp.append(f)
        return temp

    def add(self,f):
        assert isinstance(f,Folder) or isinstance(f,File)
        f.root = self
        self.contents.append(f)

    def __delete__(self, instance):
        if isinstance(instance,Folder) or isinstance(instance,File):
            self.contents.__delattr__(instance)
        else:
            for f in self.contents:
                if isinstance(f,File):
                    if f.name==instance or f.size==instance or f.lastModified==instance:
                        self.contents.__delattr__(f)
                if isinstance(f,Folder):
                    if f.name==instance or f.date==instance:
                        self.contents.__delattr__(f)

    def printAllFileNames(self):
        for fold in self.contents:
            print(fold)

class FSIter:
    def __init__(self,f):
        self.pos = 0
        self.files = f

    def __iter__(self):
        return self

    def __next__(self):
        if self.pos>=len(self.files):
            raise StopIteration()
        else:
            return self.files[self.pos]


root = Folder("root")
user = Folder("User")
root.add(user)
me = Folder("James")
user.add(Folder("test"))
user.add(me)
me.add(Folder("Documents"))
me.add(Folder("Pictures"))
me.add(Folder("Music"))
me.add(Folder("Movies"))
root.add(Folder("System"))
root.add(Folder("Windows"))


def printfiles(folder):
    print(folder.getPath())
    def helper(f,offset):
        for file in f.contents:
            if isinstance(file,Folder):
                print(offset+file.name)
                helper(file,offset+"."+" "*3)
            if isinstance(file,File):
                print(offset+file.name)
    helper(folder,"."+" "*3)


print(printfiles(root))


def makeMoney(prices):
    assert isinstance(prices,list)
    if len(prices)>0:
        low = prices[0]
        high = 0
        buy = 0
        sell = 0
        for i,p in enumerate(prices):
            if p<low:
                low = p
                buy = i
            if p>high:
                high = p
                sell = i
        if buy<sell:
            return high-low
    return None

print(makeMoney([1,2,3,4,5,6,7,8,9]))