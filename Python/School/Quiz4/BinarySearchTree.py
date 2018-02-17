class BinaryTree:
    def __init__(self,value=None,parent=None,comparator=None):
        self.value = value
        self.parent = parent
        self.left = None
        self.right = None
        if comparator is None:
            self.comparator = lambda x,y: x-y
        else:
            self.comparator = comparator

    def __contains__(self, value):
        if self.comparator(value,self.value)==0:
            return True
        if self.comparator(value,self.value)<0:
            if self.hasLeft():
                if self.left. __contains__(value):
                    return True
        else:
            if self.hasRight():
                if self.right. __contains__(value):
                    return True
        return False

    def __len__(self):
        res = 1
        if self.hasLeft():
            res += len(self.left)
        if self.hasRight():
            res += len(self.right)
        return res

    def __str__(self):
        return "(<"+str(self.value)+">"+str(self.left)+str(self.right)+")"

    def __del__(self):
        self.delete(self.value)

    def hasRight(self):
        return self.right is not None

    def hasLeft(self):
        return self.left is not None

    def hasValue(self):
        return self.value is not None

    def setValue(self,value):
        self.value = value

    def setLeft(self,node):
        if isinstance(node,BinaryTree):
            self.left = node

    def setRight(self,node):
        if isinstance(node,BinaryTree):
            self.right = node

    def setParent(self,node):
        if isinstance(node,BinaryTree):
            self.parent = node

    def find(self,value):
        if self.comparator(value,self.value)==0:
            return self
        if self.comparator(value,self.value)<0:
            if self.hasLeft():
                return self.left.find(value)
        else:
            if self.hasRight():
                return self.right.find(value)

    def unlink(self,node):
        if self.left == node:
            self.setLeft(None)
        if self.right == node:
            self.setRight(None)

    def add(self,value):
        if not self.hasValue():
            self.setValue(value)
        else:
            if self.comparator(value,self.value)<=0:
                if not self.hasLeft():
                    self.setLeft(BinaryTree(value=value,comparator=self.comparator,parent=self))
                else:
                    self.left.add(value)
            elif self.comparator(value,self.value)>=0:
                if not self.hasRight():
                    self.setRight(BinaryTree(value=value,comparator=self.comparator,parent=self))
                else:
                    self.right.add(value)

    def delete(self,value):
        if self.comparator(value,self.value)==0:
            if self.hasRight():
                curr = self.right
                while curr.hasLeft():
                    curr = curr.left
                self.value = curr.value
                del curr
            elif self.hasLeft():
                curr = self.left
                while curr.hasRight():
                    curr = curr.right
                self.value = curr.value
                del curr
            else:
                if self.parent is not None:
                    self.parent.unlink(self)
        else:
            if self.comparator(value,self.value)<0:
                if self.hasLeft():
                    self.left.delete(value)
            else:
                if self.hasRight():
                    self.right.delete(value)

    def preorder(self):
        res = [self.value]
        if self.hasLeft():
            res += self.left.preorder()
        if self.hasRight():
            res += self.right.preorder()
        return res

    def inorder(self):
        res = []
        if self.hasLeft():
            res += self.left.inorder()
        res.append(self.value)
        if self.hasRight():
            res += self.right.inorder()
        return res

    def postorder(self):
        res = []
        if self.hasLeft():
            res += self.left.postorder()
        if self.hasRight():
            res += self.right.postorder()
        res.append(self.value)
        return res


"""bt = BinaryTree()
nums = [4,6,8,12,3,5,1]
for i in nums:
    bt.add(i)
    print(bt)

print(bt.preorder())
print(bt.inorder())
print(bt.postorder())
print(bt.find(6))
print(bt.find(61))"""
