class ArrayPQueue:
    def __init__(self,comp=lambda x, y: x > y):
        self.queue = list()
        if type(comp)=="<class 'function'>":
            self.comparator = comp
        else:
            self.comparator = lambda x, y: x > y

    def __iter__(self):
        return self.queue.__iter__()

    def __len__(self):
        return len(self.queue)

    def __str__(self):
        temp = list()
        for i in self:
            temp.append(i['item'])
        return str(temp)

    def __getitem__(self, index):
        return self.queue[index]

    def add(self,item,priority=0):
        for index in range(len(self)):
            if self.comparator(priority,self[index]['priority']):
                self.queue.insert(index,{'item': item,'priority': priority})
                return
        self.queue.append({'item': item,'priority': priority})

    def remove(self):
        return self.queue.pop(0)['item']

    def peek(self):
        return self.queue[0]['item']

s = ArrayPQueue()
s.add(10)
s.add(4,5)
s.add(3,3)
s.add(21,3)
s.add(1,100)
s.add(6,2)

print(s)
