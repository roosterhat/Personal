class LinkedQueue:
    def __init__(self,v=None):
        self.parent = None
        self.child = None
        self.value = v

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
            res += str(self.value)
        if not self.hasChild():
            res += "]"
        else:
            res += ","+str(self.child)
        return res

    def hasNext(self):
        return self.hasChild()

    def isEmpty(self):
        return not self.hasParent() and not self.hasChild() and self.value is None

    def hasParent(self):
        return self.parent is not None

    def hasChild(self):
        return self.child is not None

    def add(self,item):
        if self.value is None:
            self.value = item
        elif self.hasChild():
            self.child.add(item)
        else:
            self.child = LinkedQueue(item)
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
        if isinstance(node,LinkedQueue):
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

s = LinkedQueue()
print(s)
s.add(1337)
print(s)
for i in range(10):
    s.add(i)

print(len(s))
print(s.peek())
print(s.remove())
print(s)
for i in s:
    print(i)
