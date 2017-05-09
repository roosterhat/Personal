from linked_structure import LinkedStructure


def compare(x, y):
    if isinstance(x, Node):
        return x.value == y.value
    else:
        return x == y.value


class Node:
    def __init__(self, value=None):
        self.value = value
        self.connections = LinkedStructure(comp=compare)

    def __str__(self):
        return str(self.value)

    def addEdge(self, node):
        if isinstance(node, Node):
            self.connections.add(node)

    def isReachable(self, node, visited=[]):
        visited.append(self)
        if node == self:
            return True
        for n in self.connections:
            if n not in visited and n is not None:
                if n.isReachable(node,visited.copy()):
                    return True
        return False

    def _getShortest(self, l):
        if len(l) > 0:
            min = l[0]
            for p in l:
                if len(p) < len(min):
                    min = p
            return min

    def shortestPath(self, node, visited=[]):
        paths = []
        if compare(node, self):
            return [str(self)]
        for n in self.connections:
            if n is not None and n not in visited:
                visited.append(n)
                res = n.shortestPath(node, visited.copy())
                if res is not None:
                    res = [str(self)] + res
                    paths.append(res)
        return self._getShortest(paths)


class LinkedDirectedGraph(LinkedStructure):
    def __init__(self, other=None):
        LinkedStructure.__init__(self, other=other, comp=compare)

    def __str__(self):
        res = ""
        for n in self:
            res += "[" + str(n) + "] -> " + str(n.connections) + "\n"
        return res

    def add(self, node):
        if isinstance(node, Node):
            LinkedStructure.add(self, node)
        else:
            LinkedStructure.add(self, Node(value=node))

    def insert(self, index, node):
        if isinstance(node, Node):
            LinkedStructure.insert(self, index, node)
        else:
            LinkedStructure.insert(self, index, Node(value=node))

    def _getNodes(self,n1,n2):
        if not isinstance(n1 ,Node):
            n1 = self.get(n1)
        if not isinstance(n2 ,Node):
            n2 = self.get(n2)
        return n1,n2

    def addEdge(self,node,target):
        node,target = self._getNodes(node,target)
        if node and target is not None:
            if node in self:
                node.addEdge(target)

    def isReachable(self, start, end):
        start,end = self._getNodes(start,end)
        if start and end is not None:
            return self.get(start).isReachable(end)
        return False

    def shortestPath(self, start, end):
        start,end = self._getNodes(start,end)
        if start and end is not None:
            return self.get(start).shortestPath(end)
        return []

"""
g = LinkedDirectedGraph(['A', 'B', 'C', 'D'])
g.get('A').addEdge(g.get('B'))
e = Node('E')
g.add(e)
e.addEdge(g.get('A'))
e.addEdge(g.get('C'))
e.addEdge(g.get('D'))
g.get('B').addEdge(e)
g.add(Node('F'))
g.get('C').addEdge(g.get('F'))
g.addEdge('C','D')

print(g)

print(g.isReachable(g.get('A'), g.get('C')))
print(g.isReachable('C', 'A'))
print(g.shortestPath('A', 'D'))
"""
