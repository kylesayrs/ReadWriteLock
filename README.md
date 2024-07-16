# Read Write Lock #

## Readers ##
1. Readers asyncronous, meaning multiple reader threads can execute at the same time
2. Readers are unordered, meaning that readers read in arbitrary order
3. Readers cannot read while any writer is writing

## Writers ##
1. Writers are syncronous, meaning only one writer thread may execute at any given time
2. Writers are unordered, meaning that writers write in arbitary order
3. Writers cannot write while any reader is reading or any other writer is writing

With respect to (2), write operations can be made ordered by only using one writer thread which processes from a job queue. After each job is processed, the the writer thread releases the write lock and immediately attempts to aquire. Releasing and reaquiring prevents reader starvation due to a continuous stream of write jobs.
