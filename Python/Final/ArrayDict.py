from AbstractDictionary import AbstractDictionary

class ArrayDict(AbstractDictionary):
    def __init__(self):
        self._dict = []

    def __getitem__(self, key):
        for i in self._dict:
            if i['key'] == key:
                return i['item']

    def __delitem__(self, key):
        self.remove(key)

    def __setitem__(self, key, item):
        self.insert(key,item)

    def __len__(self):
        return len(self._dict)

    def __contains__(self, item):
        for i in self._dict:
            if i['item'] == item:
                return True
        return False

    def __str__(self):
        return str(self._dict)

    def __iter__(self):
        res = []
        for i in self._dict:
            res.append(i)
        return res.__iter__()

    def insert(self,key,item):
        self._dict.append({'key':key,'item':item})

    def remove(self,key):
        keys = self.keys()
        if key in keys:
            del self._dict[keys.index(key)]

    def pop(self):
        val = self._dict[0]
        self.remove(val)
        return val['item']

    def index(self,key):
        index = 0
        for i in self._dict:
            if i['key'] == key:
                return index
            index += 1
        return -1

    def get(self,index):
        return self._dict[index]

    def items(self):
        res = []
        for i in self._dict:
            res.append(i['item'])
        return res

    def keys(self):
        res = []
        for i in self._dict:
            res.append(i['key'])
        return res

    def clear(self):
        self._dict = []


