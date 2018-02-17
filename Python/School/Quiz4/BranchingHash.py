import Quiz4.HashDictionary as hd
import Quiz4.ChainedHash as ch
import Quiz4.BinarySearchTree as bt
import random


class BranchingHash(ch.ChainedHash):
    def __init__(self, size=100, hashComp=None):
        ch.ChainedHash.__init__(self, size=size, hashComp=hashComp)
        self.comparator = lambda x, y: self.hc.compare(x.key, y.key)

    def __str__(self):
        res = ""
        for key in self._dictionary:
            res += "[" + str(key) + ": {" + str(self._dictionary.get(key)) + "}],"
        return res[:-1]

    def __contains__(self, key):
        for k in self._dictionary:
            tree = self._dictionary.get(k)
            if tree.__contains__(hd.Entry(key, None)):
                return True
        return False

    def get(self, key):
        if self.__contains__(key):
            res = self._dictionary.get(self.hc.hash(key) % len(self)).find(hd.Entry(key, None))
            if res is not None:
                return res.value
            return res

    def getItems(self):
        res = []
        for tree in self._dictionary:
            for entry in self._dictionary.get(tree).inorder():
                res.append(entry.item)
        return res

    def getKeys(self):
        res = []
        for tree in self._dictionary:
            for entry in self._dictionary.get(tree).inorder():
                res.append(entry.key)
        return res

    def add(self, key, item):
        hashkey = self.hc.hash(key) % len(self)
        if self._dictionary.get(hashkey) is None:
            self._dictionary[hashkey] = bt.BinaryTree(comparator=self.comparator)
        self._dictionary.get(hashkey).add(hd.Entry(key, item))

    def delete(self, key):
        hashkey = self.hc.hash(key) % len(self)
        if self._dictionary.__contains__(hashkey):
            if len(self._dictionary.get(hashkey)) > 1:
                self._dictionary.get(hashkey).delete(hd.Entry(key, None))
            else:
                self._dictionary.__delitem__(hashkey)


testChain = BranchingHash(100)
for i in range(50):
    key = ""
    for x in range(8):
        key += random.choice(testChain.hc.alphabet)
    testChain.add(key, key)

testkey = ""
for a in range(8):
    testkey += random.choice(testChain.hc.alphabet)
testChain.add(testkey, "This is a test")

print(testChain)
print(testChain.get(testkey))

testChain.delete(testkey)

print(testChain)
