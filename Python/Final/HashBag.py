from AbstractBag import AbstractBag
import random


class HashBag(AbstractBag):
    def __init__(self, size=10, hashcomp=None):
        self._bag = {}
        self.size = size
        if hashcomp is None:
            self.hashComparator = HashComparator()
        else:
            self.hashComparator = hashcomp

    def __len__(self):
        return len(self._bag)

    def __str__(self):
        res = ""
        for key in self._bag:
            entry = self._bag.get(key)
            res += "[" + str(key) + ": " + str(entry) + "],"
        return res[:-1]

    def __contains__(self, key):
        hashkey = self.hashComparator.hash(key)
        return self._bag.__contains__(hashkey)

    def __iter__(self):
        return self._bag.__iter__()

    def _getNewHashCode(self, key):
        hashcode = self.hashComparator.hash(key) % self.size
        while hashcode in self:
            hashcode = (hashcode + 1) % self.size
        return hashcode

    def get(self,key):
        if self.__contains__(key):
            return self._bag[key]

    def add(self, item):
        self.insert(self.hashComparator.randomKey(), item)

    def insert(self, key, item):
        hashkey = self._getNewHashCode(key)
        self._bag[hashkey] = item

    def delete(self, key):
        self._bag.__delitem__(key)

    def containsItem(self,item):
        for key in self:
            if self._bag.get(key) == item:
                return True
        return False

    def getAmount(self, item):
        count = 0
        for key in self:
            if self._bag.get(key) == item:
                count += 1
        return count


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

    def randomKey(self, length=8):
        key = ""
        for x in range(length):
            key += random.choice(self.alphabet)
        return key

    def randomHash(self, length=8):
        return self.hash(self.randomKey(length=length))

    def compare(self, x, y):
        if isinstance(x and y, str):
            return self.hash(x) - self.hash(y)
