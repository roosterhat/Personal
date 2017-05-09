from TreeSortedBag import TreeSortedBag

class TreeSortedSet(TreeSortedBag):
    def __init__(self,item=None,comparator=None):
        TreeSortedBag.__init__(self,item,comparator)

    def add(self,item):
        if not self.__contains__(item):
            TreeSortedBag.add(self,item)