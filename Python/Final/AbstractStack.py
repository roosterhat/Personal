from AbstractCollection import AbstractCollection


class AbstractStack(AbstractCollection):

    def __init__(self):
        AbstractCollection.__init__(self)

    def add(self, item):
        raise NotImplementedError()
