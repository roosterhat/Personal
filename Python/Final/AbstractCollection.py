class AbstractCollection:
    def __len__(self):
        raise NotImplementedError()

    def __contains__(self, item):
        raise NotImplementedError()

    def __str__(self):
        raise NotImplementedError()

    def __iter__(self):
        raise NotImplementedError()

    def add(self,item):
        raise NotImplementedError()

    def delete(self,item):
        raise NotImplementedError()

