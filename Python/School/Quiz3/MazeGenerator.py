import math
import random


class MazeGenerator:
    def __init__(self):
        self.open = " "
        self.wall = "#"
        self.directions = ("NORTH", "EAST", "SOUTH", "WEST")
        self.EMPTY = "~"

    def generateMaze(self, width, height):
        maze = self._generateBlankMaze(width, height)
        while not self._mazeComplete(maze):
            maze = self._generatePath(maze)
        return maze

    def _generateBlankMaze(self, width, height):
        maze = []
        for y in range(0, height):
            maze.append(list())
            for x in range(0, width):
                maze[y].append(self.EMPTY)
        return maze

    def _generatePath(self, maze):
        if self._isValidMaze(maze):
            maxLength = int((len(maze) - 2) * (len(maze[0]) - 2) * (1 / 3))
            length = random.randint(1, maxLength)
            pos = self._findStart(maze)
            if pos[0] > -1 and pos[1] > -1:
                while length > 0:
                    maze[pos[1]][pos[0]] = "#"
                    pos = self._nextMove(maze, pos)
                    if pos[0] < 0:
                        break
                    length -= 1
        return maze

    def _nextMove(self, maze, pos):
        possibleMoves = self._getPossibleMoves(maze, pos)
        if len(possibleMoves) == 0:
            return [-1. - 1]
        return possibleMoves[random.randint(0, len(possibleMoves) - 1)]

    def _getPossibleMoves(self, maze, pos):
        mov = list()
        for d in self.directions:
            if self._canMove(maze, pos, d):
                mov.append(self._move(pos, d))
        return mov

    def _move(self, pos, dir):
        if self.directions.__contains__(dir):
            theta = 360 / len(self.directions)
            r = (theta * (-1 * self.directions.index(dir))) % 360 + 90
            x = round(math.cos(math.radians(r)))
            y = -(round(math.sin(math.radians(r))))
            return [pos[0] + x, pos[1] + y]
        return pos

    def _canMove(self, maze, pos, dir):
        newPos = self._move(pos, dir)
        if self._inbounds(newPos, maze):
            if maze[newPos[1]][newPos[0]] is self.EMPTY:
                for d in [-1, 0, 1]:
                    curdir = self.directions[(self.directions.index(dir) + d) % len(self.directions)]
                    testpos = self._move(newPos, curdir)
                    if self._inbounds(testpos, maze):
                        if maze[testpos[1]][testpos[0]] is not self.EMPTY:
                            return False
                return True
        return False

    def _inbounds(self, pos, maze):
        if 0 <= pos[0] < len(maze[0]):
            if 0 <= pos[1] < len(maze[1]):
                return True
        return False

    def _getOppositeDir(self, dir):
        if self.directions.__contains__(dir):
            return self.directions[(self.directions.index(dir) + 2) % (len(self.directions))]
        return dir

    def _findStart(self, maze):
        if self._isValidMaze(maze):
            for y in range(0, len(maze)):
                for x in range(0, len(maze[0])):
                    if maze[y][x] is self.EMPTY:
                        for i in range(0, 4):
                            testpos = self._move([x, y], self.directions[i])
                            if self._inbounds(testpos,maze):
                                if maze[testpos[1]][testpos[0]] is not self.EMPTY:
                                    break
                        return [x, y]
        return [-1, -1]

    def _isValidMaze(self, maze):
        return len(maze) > 0 and len(maze[0]) > 0

    def _mazeComplete(self, maze):
        for row in maze:
            for col in row:
                if col is self.EMPTY:
                    return False
        return True

    def printMaze(self, maze):
        if maze is not None:
            for row in maze:
                for col in row:
                    print(col, end='')
                print()


m = MazeGenerator()
mz = m.generateMaze(10, 10)
m.printMaze(mz)
