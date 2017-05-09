class DictionaryInterface:
    def getItem(self,key):
        raise NotImplementedError()

    def getKey(self,item):
        raise NotImplementedError()

    def pop(self):
        raise NotImplementedError()