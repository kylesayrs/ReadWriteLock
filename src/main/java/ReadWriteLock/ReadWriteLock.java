package ReadWriteLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ReadWriteLock {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition readersOkay = lock.newCondition();
    private final Condition writersOkay = lock.newCondition();
    private int numReaders = 0;
    private int numWriters = 0;

    public void aquireReadLock() throws InterruptedException {
        lock.lock();
        try {
            // cannot read while writers are writing
            while (numWriters > 0) { 
                // atomically suspends thread and releases lock
                readersOkay.await();
                // reaquires lock before returning to thread
            }
            numReaders += 1;
            
        } finally {
            lock.unlock();
        }
    }

    public void releaseReadLock() {
        lock.lock();
        try {
            if (numReaders <= 0) {
                throw new IllegalMonitorStateException();
            }

            numReaders -= 1;
            // notify one writer to start
            writersOkay.signal();

        } finally {
            lock.unlock();
        }
    }

    public void aquireWriteLock() throws InterruptedException {
        lock.lock();
        try {
            // cannot write while readers are reading or writers are writing
            while (numReaders >= 0 || numWriters >= 0) {
                // atomically suspends thread and releases lock
                writersOkay.await();
                // requires lock before returning to thread
            }
            numWriters += 1;

        } finally {
            lock.unlock();
        }
    }

    public void releaseWriteLock() {
        lock.lock();
        try {
            if (numWriters <= 0) {
                throw new IllegalMonitorStateException();
            }
            numWriters -= 1;

            // give both readers and writers opportunities to run
            // preventing starvation
            readersOkay.signalAll();
            writersOkay.signal();
            
        } finally {
            lock.unlock();
        }
    }
}
