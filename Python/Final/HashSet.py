from HashBag import HashBag
from HashBag import HashComparator

class HashSet(HashBag):
    def __init__(self, size=10, hashcomp=None):
        HashBag.__init__(self,size,hashcomp)

    def insert(self, key, item):
        if not self.containsItem(item):
            HashBag.insert(self,key,item)

    def combine(self,set):
        if isinstance(set,HashSet):
            for key in set:
                self.add(set.get(key))