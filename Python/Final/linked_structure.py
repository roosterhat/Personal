from abstract_collection import AbstractCollection


class LinkedStructure(AbstractCollection):
    def __init__(self, other=None, comp=lambda x, y: x == y):
        self._head = None
        self._data = None
        self._tail = None
        self.comparator = comp
        AbstractCollection.__init__(self, other)

    def __iter__(self):
        return self.toList().__iter__()

    def __len__(self):
        return len(self.toList())

    def __str__(self):
        out = "["
        for item in self:
            out += str(item) + ","
        out = out[:-1] + "]"
        return out

    def __getitem__(self, index):
        if not index >= len(self) or index < 0:
            if index == 0:
                return self._data
            elif self.hasTail():
                return self._tail[index - 1]

    def __setitem__(self, index, value):
        if not index >= len(self) or index < 0:
            if index == 0:
                self._data = value
            elif self.hasTail():
                self._tail[index - 1] = value

    def __delitem__(self, index):
        self.remove(index)

    def __contains__(self, value):
        if self.comparator(value, self._data):
            return True
        elif self.hasTail():
            return value in self.getTail()
        else:
            return False

    def hasTail(self):
        return self._tail is not None

    def hasHead(self):
        return self._head is not None

    def hasData(self):
        return self._data is not None

    def getHead(self):  # hehehe
        return self._head

    def getTail(self):
        return self._tail

    def setHead(self, node, set=True):
        if isinstance(node, LinkedStructure):
            self._head = node
            if set:
                node.setTail(self, set=False)
        elif node is None:
            self._tail = node

    def setTail(self, node, set=True):
        if isinstance(node, LinkedStructure):
            self._tail = node
            if set:
                node.setHead(self, set=False)
        elif node is None:
            self._tail = node

    def add(self, item):
        if not self.hasData():
            self._data = item
        elif self.hasTail():
            self._tail.add(item)
        else:
            self.setTail(LinkedStructure(item,comp=self.comparator))

    def insert(self, index, value):
        if index >= self._size:
            self.add(value)
        elif index == 0:
            temp = LinkedStructure(value,comp=self.comparator)
            self._head.setTail(temp)
        elif self.hasTail():
            self._tail.insert(index - 1, value)
        else:
            self.add(value)

    def index(self, item, index=0):
        if self.comparator(item, self._data):
            return index
        elif self.hasTail():
            return self.getTail().index(item, index + 1)
        else:
            return -1

    def remove(self, item):
        if self.comparator(item, self._data):
            if self.hasHead():
                self.getHead().setTail(self._tail)
            else:
                if self.hasTail():
                    self._data = self[1]
                    self.setTail(self._tail.getTail())
        elif self.hasTail():
            self._tail.remove(item)

    def get(self,item):
        index = self.index(item)
        if index>=0:
            return self[index]
        else:
            return None

    def toList(self):
        res = [self._data]
        if self.hasTail():
            return res + self.getTail().toList()
        return res

    def clear(self):
        self._data = None
        self._head = None
        self._tail = None
        self._size = 0


if __name__ == "__main__":
    lyst = LinkedStructure([1, 2, 3, 4])
    print(str(lyst))
    lyst.insert(5, 100)
    print(lyst)
    print(len(lyst))
    lyst.remove(lyst.index(2))
    lyst[0] = 1337
    print(lyst)
    print(lyst[3])
