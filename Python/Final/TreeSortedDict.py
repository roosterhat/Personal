from AbstractDictionary import AbstractDictionary
from TreeStructure import TreeStructure


class TreeSortedDict(TreeStructure, AbstractDictionary):
    def __init__(self, other=None, comp=lambda x, y: x['key'] - y['key']):
        TreeStructure.__init__(self, other=other, comp=comp)

    def __delitem__(self, key):
        self.delete({'key':key})

    def __setitem__(self, key, value):
        self.insert(key, value)

    def __getitem__(self, key):
        res = self.get({'key':key})
        if res is not None:
            return res['item']
        else:
            return res

    def findKey(self, value):
        if self.hasData() and self.data['item'] == value:
            return self.data['key']
        if self.hasLeft():
            res = self._left.findKey(value)
            if res is not None:
                return res
        if self.hasRight():
            res = self._right.findKey(value)
            if res is not None:
                return res

    def pop(self):
        val = self
        self.delete(self.data['key'])
        return val

    def insert(self, key, item):
        self.add({'key':key,'item':item})



"""
bt = TreeSortedDict()
bt.insert(5, "test5")
bt.insert(3, "test3")
bt.insert(6, "test6")
bt.insert(7, "test7")
bt.insert(4, "test4")
bt.insert(1, "test1")
bt.insert(2, "test2")
bt[8] = "test8"

print(bt)

del bt[8]

print(bt)

print(bt.findKey("test6"))
print(bt[bt.findKey("test6")])
print(bt.findKey("test61"))
print(bt.inorder())
print()

del bt[5]

print(bt)"""
