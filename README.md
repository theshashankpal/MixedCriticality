# MixedCriticality

Till allocation part it has been done

Flow of the program ->

1) Starts with main.java.
2) Reads tasks fro task.json using gson library.
3) Creates a Data Structure for processors which stores list of active and backup tasks that will be allocated to each processor.
4) Creates an object of Allocater class , then calls allocater.allocate.
5) allocate() function then tries to allocate tasks to processor by taking assumption that edf will be used , and the allocation is done based on first fit.

(Ignore all other functions )
