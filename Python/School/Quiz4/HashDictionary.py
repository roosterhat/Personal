import random
import BinarySearchTree

class HashDictionary:
    def __init__(self, size=100, hashComp=None):
        self._dictionary = {}
        self.size = size
        if hashComp is not None:
            self.hc = hashComp
        else:
            self.hc = HashComparator()

    def __str__(self):
        res = ""
        for key in self._dictionary:
            entry = self._dictionary.get(key)
            res += "[" + str(key) + ": " + str(entry)+ "],"
        return res[:-1]

    def __len__(self):
        return self.size

    def __contains__(self, key):
        return self._dictionary.__contains__(key)

    def __iter__(self):
        return self._dictionary.__iter__()

    def _getNewHashCode(self, key):
        hashcode = self.hc.hash(key)
        while self.__contains__(hashcode % len(self)):
            hashcode += 1
        return hashcode % len(self)

    def _findkeyHash(self, key):
        hashcode = self.hc.hash(key)
        entry = self._dictionary.get(hashcode % len(self))
        while entry is not None:
            if entry.key == key:
                return hashcode % len(self)
            hashcode += 1
            entry = self._dictionary.get(hashcode % len(self))

    def get(self, key):
        if self.containsKey(key):
            return self._dictionary.get(self._findkeyHash(key)).item

    def containsItem(self, item):
        return self.getItems().__contains__(item)

    def containsKey(self, key):
        return self.getKeys().__contains__(key)

    def getItems(self):
        res = []
        for key in self._dictionary:
            entry = self._dictionary.get(key)
            res.append(entry.item)
        return res

    def getKeys(self):
        res = []
        for key in self._dictionary:
            entry = self._dictionary.get(key)
            res.append(entry.key)
        return res

    def add(self, key, item):
        if len(self._dictionary)<len(self):
            hashkey = self._getNewHashCode(key)
            self._dictionary[hashkey] = Entry(key, item)

    def delete(self, key):
        hashkey = self._findkeyHash(key)
        if self.__contains__(hashkey):
            del self._dictionary[hashkey]






class Entry:
    def __init__(self, key, item):
        self.key = key
        self.item = item

    def __str__(self):
        return str(self.key)+","+str(self.item)


class HashComparator:
    def __init__(self):
        self.alphabet = list(map(chr, range(ord('a'), ord('z') + 1)))
        self.alphabet += list(map(lambda x: str(x).upper(), self.alphabet))

    def hash(self, key):
        prime = 11
        code = 1
        if isinstance(key, str):
            for l in key:
                if self.alphabet.__contains__(l):
                    code = code * prime + ord(l)
        return code

    def compare(self, x, y):
        if isinstance(x and y, str):
            return self.hash(x) - self.hash(y)

class IntComparator:
    def hash(self, key):
        prime = 11
        return key * prime

    def compare(self, x, y):
        return self.hash(x) - self.hash(y)

"""
hd = HashDictionary(size=19,hashComp=IntComparator())
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
"""