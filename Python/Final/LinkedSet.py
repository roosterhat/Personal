from LinkedBag import LinkedBag

class LinkedSet(LinkedBag):
    def __init__(self,value=None):
        LinkedBag.__init__(self,value)

    def add(self,item):
        if not self.__contains__(item):
            LinkedBag.add(self,item)

    def combine(self,set):
        if isinstance(set,LinkedBag):
            for i in set:
                self.add(i.item)