import math
from tkinter import *
from tkinter.filedialog import askopenfilename

class MazeRunner:
    def __init__(self):
        self.maze = list()
        self.width = 0
        self.height = 0
        self.moves = []
        self.path = []
        self.directions = ("NORTH","EAST","SOUTH","WEST")
        #("NORTH","NORTHEAST","EAST","SOUTHEAST","SOUTH","SOUTHWEST","WEST","NORTHWEST")

    def loadMaze(self):
        Tk().withdraw()
        filename = askopenfilename()
        try:
            file = open(filename,"r")
            row = 0
            for line in file:
                col = 0
                self.maze.insert(row,list())
                for char in line.rstrip("\n"):
                    self.maze[row].insert(col,char)
                    col+=1
                row+=1
                self.width = col
            self.height = row
            self.moves = []
            self.path = []
        except(Exception):
            print("Failed to load maze")

    def printMaze(self):
        r = 0
        for row in self.maze:
            c = 0
            for col in row:
                move = False
                for m in self.path:
                    if m['position']==[r,c]:
                        print(self.getDirSymb(m['direction']),end='')
                        move = True
                if not move:
                    print(col,end='')
                c+=1
            r+=1
            print()

    def getDirSymb(self,dir):
        if dir=="NORTH":
            return "^"
        elif dir=="EAST":
            return ">"
        elif dir=="SOUTH":
            return "v"
        elif dir=="WEST":
            return "<"
        else:
            return "."

    def find(self,char):
        r = 0
        for row in self.maze:
            c = 0
            for col in row:
                if col==char:
                    return [r,c]
                c+=1
            r+=1
        return [-1,-1]

    def findStart(self):
        return self.find("S")
    
    def findEnd(self):
        return self.find("E")

    def getOppositeDir(self,dir):
        if self.directions.__contains__(dir):
            return self.directions[(self.directions.index(dir)+2)%(len(self.directions))]
        return dir

    def posInBounds(self,pos):
        return pos[0]<self.height and pos[0]>-1 and pos[1]<self.width and pos[1]>-1

    def isValidSpace(self,pos):
        if self.posInBounds(pos):
            return self.maze[pos[0]][pos[1]]==" " or self.maze[pos[0]][pos[1]]=="E"
        return False

    def canMove(self,pos,dir):
        return self.isValidSpace(self.move(pos,dir))

    def hasMove(self,pos,dir):
        return len(self.getPossibleMoves(pos,dir))>0

    def getPossibleMoves(self,pos,dir=None):
        mov = list()
        if dir is not None and self.canMove(pos,dir):
            mov.append(dir)
        for d in self.directions:
            if dir is None or d is not self. getOppositeDir(dir) and d is not dir:
                if self.canMove(pos,d):
                    mov.append(d)
        return mov

    def canChangeDir(self,pos,dir):
        return self.hasMove(pos,dir) and (not self.getPossibleMoves(pos,dir).__contains__(dir) or
                                          len(self.getPossibleMoves(pos,dir))>1)

    def move(self,pos,dir):
        if self.directions.__contains__(dir):
            theta = 360/len(self.directions)
            r = (theta*(-1*self.directions.index(dir)))%360 + 90
            x =    round(math.cos(math.radians(r)))
            y = -1*round(math.sin(math.radians(r)))
            return [pos[0]+y,pos[1]+x]

    def makeMove(self,pos,dir):
        self.moves.append({'position':pos,'direction':dir})
        self.path.append({'position':pos,'direction':dir})
        return self.move(pos,dir)

    def finished(self,pos):
        return self.maze[pos[0]][pos[1]]=="E"

    def backTrack(self,pos):
        index = 1
        for m in self.path:
            if m['position'] == pos:
                self.path = self.path[:index]
            index+=1

    def traverseMaze(self):
        start = self.findStart()
        if start[0]==-1:
            print("Unable to find start location")
            return
        pos = start
        dir = self.getPossibleMoves(pos)[0]
        print("Start Pos "+str(pos))
        print("Size "+str(self.height)+","+str(self.width))
        print("start")
        if self.traverseSegment(pos,dir):
            self.printMaze()

    def traverseSegment(self,pos,dir):
        while self.hasMove(pos,dir):
            if self.canChangeDir(pos,dir):
                for direction in self.getPossibleMoves(pos,dir):
                    self.backTrack(self.makeMove(pos,self. getOppositeDir(dir)))
                    if self.traverseSegment(self.makeMove(pos,direction),direction):
                        return True
                    else:
                        self.backTrack(pos)
                return False
            else:
                if self.canMove(pos,dir):
                    pos = self.makeMove(pos,dir)
                else:
                    return False
        if self.finished(pos):
            return True
        else:
            return False

m = MazeRunner()
m.loadMaze()
m.printMaze()
m.traverseMaze()
print("Moves: "+str(len(m.moves)))
print("Path Length: "+str(len(m.path)))
