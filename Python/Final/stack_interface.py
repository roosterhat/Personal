class StackInterface:

    def push(self, item):
        """Pushes item onto the stack"""
        raise NotImplementedError()

    def peek(self):
        """Returns element from the top of a stack"""
        raise NotImplementedError()

    def pop(self):
        """Returns element from the top of a stack and removes it from stack"""
        raise NotImplementedError()
