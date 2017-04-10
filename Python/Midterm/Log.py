import time
import re
import math

class EventLog:
    def __init__(self,title,format):
        self.title = "Time Stamp    "+title
        self.logFormat = format
        self.log = []

    def __str__(self):
        res = self.title+"\n"
        for entry in self.log:
            res += entry+"\n"
        return res

    def __iter__(self):
        return self.log.__iter__()

    def __len__(self):
        return len(self.log)

    def printRange(self,s,e):
        print(self.title)
        for i in range(s,e):
            print(self.log.index(i))

    def getRange(self,s,e):
        res = self.title+"\n"
        for i in range(s,e):
            res = res+self.log.index(i)+"\n"
        return res

    def _parseFormat(self):
        res = []
        for f in re.findall("{[^}]+}",self.logFormat):
            if f is not "":
                res.append(f)
        return res

    def newEntry(self,*args):
        localtime = time.strftime("%m/%d %H:%M",time.localtime())
        res = "{:<14s}".format(localtime)
        forms = self._parseFormat()
        count = 0
        if len(args)>1 and isinstance(args[1],ObjectConverter):#args[1] = ObjectConverter, args[0] = object
            args = args[1].toArray(args[0])
        for i in args:
            if len(forms) > count:
                res += forms[count].format(i)
                count += 1
        self.log.append(res)

class DynamicLog:
    def __init__(self,*args):
        temp = []
        for t in args:
            temp.append(t)
        self.title = temp
        self.log = []
        self.buffer = 2
        self.timeStamp = True
        self.index = False

    def __iter__(self):
        return self.toTable().__iter__()

    def __len__(self):
        return len(self.log)

    def __str__(self):
        res = ""
        for line in self.toTable():
            res+=line+"\n"
        return res

    def _addExtras(self,index,time):
        res = ""
        if self.index:
            res += ("{:<"+str(max(len(self)+2, 7))+"s}").format(str(index))
        if self.timeStamp:
            res += "{:<13s}".format(str(time))
        return res

    def _formatTitle(self):
        form = self._parseFormat(self._getFormat())
        count = 0
        res = self._addExtras("Index","Time Stamp")
        for f in form:
            res += f.format(self.title[count])
            count += 1
        return res

    def toTable(self):
        return self.getRange(len(self)-1)

    def _getRangeIndex(self,args):
        start = 0
        end = -1
        if len(args) is 1 and isinstance(args[0],int):
            start = 0
            end = args[0]
        if len(args) is 2 and isinstance(args[0] and args[1],int):
            start = args[0]
            end = args[1]
        return start,end

    def printRange(self,*args):
        s,e = self._getRangeIndex(args)
        for line in self.getRange(s,e):
            print(line)

    def _formatFloat(self,val):
        if isinstance(val,float) and val is not math.inf:
            val = ("{:"+str(len(str(val).split(".")[0]))+".2f}").format(val)
        return val

    def getRange(self,*args):
        rangeTable = []
        form = self._parseFormat(self._getFormat())
        rangeTable.append(self._formatTitle())
        start,end = self._getRangeIndex(args)
        if start+1 and end+1 in range(len(self)+1):
            for i in range(start,end+1):
                count = 2
                res = self._addExtras(self.log[i][0],self.log[i][1])
                for f in form:
                    val = self._formatFloat(self.log[i][count])
                    res += f.format(str(val))
                    count += 1
                rangeTable.append(res)
        return rangeTable

    def _parseFormat(self,form):
        res = []
        for f in re.findall("{[^}]+}",form):
            if f is not "":
                res.append(f)
        return res

    def _getFormat(self):
        res = ""
        spacing = []
        for t in self.title:
            spacing.append(len(t)+self.buffer)
        for entry in self.log:
            count = 0
            for val in entry[2:]:
                val = self._formatFloat(val)
                spacing[count] = max(spacing[count],len(str(val))+self.buffer)
                count += 1
        for i in spacing:
            res += "{:<"+str(i)+"s}"
        return res

    def newEntry(self,*args):
        localtime = time.strftime("%m/%d %H:%M",time.localtime())
        entry = []
        entry.append(len(self))
        entry.append(localtime)
        if len(args)>1 and isinstance(args[1],ObjectConverter):#args[1] = ObjectConverter, args[0] = object
            args = args[1].toArray(args[0])
        for i in args:
            entry.append(i)
        self.log.append(entry)

    def setTimeStampState(self,b):
        if isinstance(b,bool):
            self.timeStamp = b

    def setIndexState(self,b):
        if isinstance(b,bool):
            self.index = b

    def clearLog(self):
        self.log = []


class ObjectConverter:
    def toArray(self,obj):
        return []

class ProductConverter(ObjectConverter):
    def toArray(self,obj):
        return [obj.name,obj.description,obj.price,obj.category.name,obj.stock]

class UserConverter(ObjectConverter):
    def toArray(self,obj):
        return [obj.getUsername(),obj.getPassword(),obj.getRank()]

class CustomConverter(ObjectConverter):
    def __init__(self,method):
        if str(type(method)) == "<class 'function'>":
            self.conversionMethod = method
        else:
            raise Exception("argument must be <class 'function'>, given: "+str(type(method)))
        ObjectConverter.__init__(self)

    def toArray(self,obj):
        try:
            return self.conversionMethod(obj)
        except (RuntimeError, TypeError):
            pass

