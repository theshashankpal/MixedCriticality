# MixedCriticality

Till allocation part it has been done

Flow of the program ->

1) Starts with main.java.
2) Reads tasks fro task.json using gson library.
3) Creates a Data Structure for processors which stores list of active and backup tasks that will be allocated to each processor.
4) Creates an object of Allocater class , then calls allocater.allocate.
5) allocate() function then finds the utilization ratio of primary and backup tasks  and calls tryToAllocate() function to allocate primary and backup tasks to processor using first fit with constraint that backup and primary or backup and backup of same task can't be allocated to the same processor.

(Ignore all other functions )
