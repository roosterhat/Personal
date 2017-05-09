from abstract_collection import AbstractCollection


class TreeStructure(AbstractCollection):
    def __init__(self, other=None, comp=lambda x, y: x - y):
        self.data = None
        self._left = None
        self._right = None
        self._parent = None
        self.comparator = comp
        AbstractCollection.__init__(self, other)

    def __str__(self):
        return str(self.inorder())

    def __len__(self):
        return len(self.inorder())

    def __contains__(self, item):
        return self.inorder().__contains__(item)

    def __delitem__(self, item):
        self.delete(item)

    def __iter__(self):
        return self.inorder().__iter__()

    def hasLeft(self):
        return self._left is not None

    def hasRight(self):
        return self._right is not None

    def hasParent(self):
        return self._parent is not None

    def hasData(self):
        return self.data is not None

    def setLeft(self, node):
        if isinstance(node, TreeStructure):
            self._left = node
            self._left.setParent(self)

    def setRight(self, node):
        if isinstance(node, TreeStructure):
            self._right = node
            self._right.setParent(self)

    def setParent(self, node):
        if isinstance(node, TreeStructure):
            self._parent = node

    def getLeft(self):
        return self._left

    def getRight(self):
        return self._right

    def getParent(self):
        return self._parent

    def unlink(self, node):
        if isinstance(node, TreeStructure):
            if self._right == node:
                self._right = None
            elif self._left == node:
                self._left = None

    def add(self, item):
        if not self.hasData():
            self.data = item
        else:
            if self.comparator(item, self.data) >= 0:
                if self.hasLeft():
                    self._left.add(item)
                else:
                    self.setLeft(self.__class__(other=item,comp=self.comparator))
                    self._left.setParent(self)
            else:
                if self.hasRight():
                    self._right.add(item)
                else:
                    self.setRight(self.__class__(other=item,comp=self.comparator))
                    self._right.setParent(self)

    def delete(self, item):
        if self.comparator(item, self.data) == 0:
            if self.hasLeft() or self.hasRight():
                curr = None
                if self.hasRight():
                    curr = self._right
                    while curr.hasLeft():
                        curr = curr.getLeft()
                elif self.hasLeft():
                    curr = self._left
                self.data = curr.data
                curr.delete(curr.data)
            else:
                if self.hasParent():
                    self._parent.unlink(self)
                else:
                    self.data = None
        else:
            if self.hasLeft():
                self._left.delete(item)
            if self.hasRight():
                self._right.delete(item)

    def get(self, key):
        if self.hasData() and self.comparator(key,self.data) == 0:
            return self.data
        if self.hasData() and self.comparator(key,self.data) > 0:
            if self.hasLeft():
                return self._left.get(key)
        else:
            if self.hasRight():
                return self._right.get(key)

    def inorder(self):
        res = []
        if self.hasLeft():
            res += self._left.inorder()
        res.append(str(self.data))
        if self.hasRight():
            res += self._right.inorder()
        return res
