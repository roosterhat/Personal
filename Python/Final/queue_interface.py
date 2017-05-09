class QueueInterface:

    def push(self, item):
        """Pushes item onto the queue"""
        raise NotImplementedError()

    def peek(self):
        """Returns element from the top of a queue"""
        raise NotImplementedError()

    def pop(self):
        """Returns element from the top of a stack and removes it from queue"""
        raise NotImplementedError()
