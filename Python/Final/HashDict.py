from AbstractDictionary import AbstractDictionary
import random


class HashDict(AbstractDictionary):
    def __init__(self, size=10, hashcomp=None):
        self._dict = {}
        self.size = size
        if hashcomp is None:
            self.hashComparator = HashComparator()
        else:
            self.hashComparator = hashcomp

    def __getitem__(self, key):
        if key in self:
            return self.get(self._getNewHashCode(key))['item']

    def __delitem__(self, key):
        self.remove(key)

    def __setitem__(self, key, item):
        self.insert(key, item)

    def __len__(self):
        return len(self._dict)

    def __contains__(self, key):
        return key in self._dict

    def __str__(self):
        return str(self._dict)

    def __iter__(self):
        return self._dict.items().__iter__()

    def _getNewHashCode(self, key):
        hashcode = self.hashComparator.hash(key) % self.size
        while hashcode in self:
            hashcode = (hashcode + 1) % self.size
        return hashcode

    def _findHashKey(self, key):
        hashcode = self.hashComparator.hash(key) % self.size
        while hashcode in self:
            if self.get(hashcode)['key']==key:
                return hashcode
            hashcode = (hashcode + 1) % self.size
        return None

    def full(self):
        return len(self) >= self.size

    def insert(self, key, item):
        if not self.full():
            hashkey = self._getNewHashCode(key)
            self._dict[hashkey] = {'key': key, 'item': item}

    def remove(self, key):
        hashkey = self._findHashKey(key)
        for k in self._dict:
            if k == hashkey:
                del self._dict[hashkey]
                break

    def pop(self):
        for k, i in self:
            del self._dict[k]
            return i

    def index(self, key=None, item=None):
        index = 0
        for _, i in self:
            if key is not None:
                if i['key'] == key:
                    return index
            if item is not None:
                if i['item'] == item:
                    return index
            index += 1
        return index

    def get(self, index):
        return self._dict[index]

    def valuse(self):
        res = []
        for _, i in self:
            res.append(i['item'])
        return res

    def keys(self):
        res = []
        for _, i in self:
            res.append(i['key'])
        return res

    def clear(self):
        self._dict = {}

    def containsItem(self,item):
        for _,i in self:
            if i['item']==item:
                return True
        return False

    def containsKey(self,key):
        for _,i in self:
            if i['key']==key:
                return True
        return False


class HashComparator:
    def __init__(self):
        self.alphabet = list(map(chr, range(ord('a'), ord('z') + 1)))
        self.alphabet += list(map(str.upper, self.alphabet))


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

