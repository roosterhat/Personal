class LinkedPQueue:
    def __init__(self,v=None,comp = lambda x,y: x>y):
        self.parent = None
        self.child = None
        if isinstance(v,dict):
            self.value = v
        else:
            self.value = None
        if type(comp)=="<class 'function'>":
            self.comparator = comp
        else:
            self.comparator = lambda x, y: x > y


    def __iter__(self):
        return LinkedQueueIter(self)

    def __len__(self):
        if not self.hasChild():
            return 1
        return len(self.child)+1

    def __str__(self):
        res = ""
        if not self.hasParent():
            res += "["
        if self.value is not None:
            res += str(self.value['item'])
        if not self.hasChild():
            res += "]"
        else:
            res += ", "+str(self.child)
        return res

    def hasNext(self):
        return self.hasChild()

    def isEmpty(self):
        return not self.hasParent() and not self.hasChild() and self.value is None

    def hasParent(self):
        return self.parent is not None

    def hasChild(self):
        return self.child is not None

    def add(self,item,priority=0):
        if self.value is None:
            self.value = {'item': item,'priority':priority}
        elif self.comparator(priority,self.value['priority']):
            print(str(item)+","+str(self.value))
            if self.hasChild():
                self.child.add(self.value['item'],self.value['priority'])
            else:
                self.child = LinkedPQueue(self.value)
                self.child.parent = self
            self.value = {'item': item,'priority':priority}
        else:
            if self.hasChild():
                self.child.add(item,priority)
            else:
                self.child = LinkedPQueue({'item': item,'priority':priority})
                self.child.parent = self


    def remove(self):
        v = self.value
        if self.hasChild():
            self.value = self.child.value
            self.child = self.child.child
        else:
            self.value = None
        return v

    def peek(self):
        return self.value

class LinkedQueueIter:
    def __init__(self,node):
        if isinstance(node,LinkedPQueue):
            self.position = node

    def __iter__(self):
        return self

    def __next__(self):
        if self.position is not None:
            value = self.position.value
            self.position = self.position.child
            return value
        else:
            raise StopIteration()

s = LinkedPQueue()
s.add(10,0)
s.add(4,5)
s.add(3,3)
s.add(21,3)
s.add(1,100)
s.add(6,2)

print(s)