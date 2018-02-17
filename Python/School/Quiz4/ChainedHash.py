from Quiz4.HashDictionary import HashDictionary
from Quiz4.HashDictionary import Entry
import random


class ChainedHash(HashDictionary):
    def __init__(self, size=100, hashComp=None):
        HashDictionary.__init__(self,size=size,hashComp=hashComp)

    def __str__(self):
        res = ""
        for key in self._dictionary:
            res += "[" + str(key) + ": {"
            for entry in self._dictionary.get(key):
                res += "("+str(entry)+"), "
            res += "}],"
        return res[:-1]

    def __contains__(self, key):
        for k in self._dictionary:
            bucket = self._dictionary.get(k)
            if bucket.__contains__(key):
                return True
        return False

    def get(self, key):
        for entry in self._dictionary.get(self.hc.hash(key)% len(self)):
            if entry.key == key:
                return entry.item

    def getItems(self):
        res = []
        for bucket in self._dictionary:
            for entry in self._dictionary.get(bucket):
                res.append(entry.item)
        return res

    def getKeys(self):
        res = []
        for bucket in self._dictionary:
            for entry in bucket:
                res.append(entry.key)
        return res

    def add(self,key,item):
        hashkey = self.hc.hash(key)% len(self)
        if self._dictionary.get(hashkey) is None:
            self._dictionary[hashkey] = []
        self._dictionary[hashkey].append(Entry(key,item))

    def delete(self,key):
        hashkey = self.hc.hash(key)% len(self)
        for entry in self._dictionary.get(hashkey):
            if entry.key == key:
                index = self._dictionary.get(hashkey).index(entry)
                self._dictionary.get(hashkey).__delitem__(index)
                if len(self._dictionary.get(hashkey))==0:
                    del self._dictionary[hashkey]

class IntComparator:
    def hash(self, key):
        prime = 11
        return key * prime

    def compare(self, x, y):
        return self.hash(x) - self.hash(y)

hd = ChainedHash(size=19,hashComp=IntComparator())
hd.add(2138,2138)
hd.add(1142,1142)
hd.add(1477,1477)
hd.add(1800,1800)
hd.add(2128,2128)
hd.add(3590,3590)
hd.add(3095,3095)
hd.add(2482,2482)
hd.add(3144,3144)
print(hd)

print(hd.get(2482))
print(hd.get(3095))
print(hd.get(4002))

hd.delete(1477)
hd.delete(3590)

print(hd)

hd.add(2677,2677)

print(hd)

