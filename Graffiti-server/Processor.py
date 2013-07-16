from App import *
import ast
import numpy
import matplotlib.pyplot as plt
import datetime
import scipy
from scipy import signal, integrate
from numpy import trapz
class Processor(object):
    """ This class processes the data from the Phone"""
    def __init__(self, data):
        data = ast.literal_eval(data)

        # Apply wiener filter to the data
        self.x = signal.wiener([ float(x) for x in data["dataX"] ])
        self.y = signal.wiener([ float(y) for y in data["dataY"]])
        self.z = signal.wiener([ float(z) for z in data["dataZ"]])
        t = data["timestamps"]
        # set a message if one is included
        try:
            self.msg = data['msg']
        except KeyError:
            self.msg = False
        #convert timestamps into deltas
        self.t = [(int(x)-int(t[0]))*10**-9 for x in t]
    def mag(self,x,y,p,q):
        # given two vectors x and y (and a constant adjustment p and q,
        # compute the magnitude at each time
        mag = []
        for ind, el in enumerate(x):
            mag.append((float(el)-p)**2+(float(y[ind])-q)**2)
        return mag
    def smooth(self, x, length):
        # for length vaues of x, smooth the results by averaging over neighbors
        # Could be improved for sure
        smth = []
        smooth_rate = 30
        for index in xrange(length):
            val = 0
            ct = 1
            for s in xrange(smooth_rate):
                if s >= index:
                    continue
                ct+=1
                val+=x[index-s]
            smth.append(val/ct)
        return smth
    def peaks(self, a, b, show=False):
        # run several of the functions
        mag = self.mag(a,b,0,0)
        smooth = self.smooth(mag, len(self.t))
        avg = (self.avg(smooth))
        if show:
            plt.plot(self.t,[avg for x in xrange(len(self.t))],show+'--')
            plt.plot(self.t,smooth, show)
        return (smooth, self.function_crosses(smooth, avg, True))
    def avg(self,x):
        # avg an array
        return sum(x)/len(x)
    def function_crosses(self,function, boundry, preserve):
        # Find all of the crosses over the boundry for a dataset
        switch = False
        passes = 0
        passIndex =[]
        for index, el in enumerate(function):
            if (switch == False) and (el> boundry):
                switch = True
                passes+=1
                passIndex.append(index)
            else:
                pass
            if el < boundry:
                switch = False
        return passIndex


    def run(self):
        # run the tests and return results
        (smoothXY, xy) = self.peaks(self.x,self.y, show=None)
        return (xy,0,0)




    """
    Ignore this stuff for now
    """
    def calibrate(self, x):
        y = 0
        for x in xrange(100):
            y+=0
        return y/100
    def splitter(self, indexes, array):
        # split an array based on indices
        base = 0
        result = []
        for index in indexes:
            result.append(array[base:index])
            base = index
        return result
    def calcLength(self, x):
        # calculate length using a trapezoidal integration
        return trapz(trapz(x,self.t),self.t)
   

    def function_up_downs(self, function, boundry):
        switch = False
        secSwitch = True
        passes = 0
        ct = 0
        passIndex = []
        for index, el in enumerate(function):
            if (switch == False) and (el > boundry):
                switch = True
                if secSwitch:
                    passIndex.append(index)
                    secSwitch = False
            if el < boundry:
                switch = False
                ct+=1
                if ct == 2:
                    secSwitch = True
                    ct = 0
        return passIndex

