class ArrayQueue:
    def __init__(self):
        self.queue = list()

    def __iter__(self):
        return self.queue.__iter__()

    def __len__(self):
        return len(self.queue)

    def __str__(self):
        return str(self.queue)

    def add(self,item):
        self.queue.append(item)

    def remove(self):
        return self.queue.pop(0)

    def peek(self):
        return self.queue[0]

s = ArrayQueue()
print(s)
for i in range(10):
    s.add(i)

print(s.peek())
print(s.remove())
print(s)