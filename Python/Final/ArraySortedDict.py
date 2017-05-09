from ArrayDict import ArrayDict

class ArraySortedDict(ArrayDict):
    def __init__(self,comp=lambda x,y:x-y):
        ArrayDict.__init__(self)
        self.comparator = comp

    def insert(self,key,item):
        index = 0
        for i in self._dict:
            if self.comparator(key,i['key'])<0:
                self._dict.insert(index,{'key':key,'item':item})
                break
            index += 1
        if index==len(self):
            self._dict.append({'key':key,'item':item})

