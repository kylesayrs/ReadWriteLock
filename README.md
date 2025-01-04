# Read Write Lock #
Lock to allow for concurrent and asynchronous reading but synchronous and sequential writing

## Readers ##
1. Readers asyncronous, meaning multiple reader threads can execute at the same time
2. Readers are unordered, meaning that readers read in arbitrary order
3. Readers cannot read while any writer is writing

## Writers ##
1. Writers are syncronous, meaning only one writer thread may execute at any given time
2. Writers are unordered, meaning that writers write in arbitary order
3. Writers cannot write while any reader is reading or any other writer is writing

## Usage ##
```java
ReadWriteLock lock = new ReadWriteLock();

// reader thread
lock.aquireReadLock();
lock.releaseReadLock();

// writer thread
lock.aquireWriteLock();
lock.releaseWriteLock();
```

## Starvation Prevention ##
Write operations can be made ordered by only using one writer thread which processes from a job queue. After each job is processed, the the writer thread releases the write lock and immediately attempts to aquire. Releasing and reaquiring prevents reader starvation due to a continuous stream of write jobs.

## TODO: explore sequential writing ##
* While it's likely an impossible/malformed problem to have writer threads which act in parallel and grab the writer lock sequentially, we can implement the easier/better formed problem of grabbing the writer thread which priority. If two threads have the same priority, then it's a race to see which thread grabs the lock first. If a writer passes its current time as it's priority, and if all writer threads have a synchronized clock, then we can achieve the best possible approximation of sequential writing.
