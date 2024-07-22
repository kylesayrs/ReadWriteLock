package ReadWriteLock;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ReadWriteLock {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition readersOkay = lock.newCondition();
    private final LinkedBlockingDeque<Condition> writersQueue = new LinkedBlockingDeque<>();
    private int numReaders = 0;
    private boolean writing = false;

    public void acquireReadLock() throws InterruptedException {
        lock.lock();
        try {
            // cannot read while writers are writing or pending
            while (writing || !writersQueue.isEmpty()) { 
                readersOkay.await();
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
            if (!writersQueue.isEmpty()) {
                writersQueue.getFirst().signal();
            }

        } finally {
            lock.unlock();
        }
    }

    public void acquireWriteLock() throws InterruptedException {
        lock.lock();
        try {
            // cannot write while readers or other writers active
            if (writing || numReaders > 0) {
                Condition writerCondition = lock.newCondition();
                writersQueue.add(writerCondition);

                while (writing || numReaders > 0) {
                    writerCondition.await();
                }

                writersQueue.pop();
            }

            writing = true;

        } finally {
            lock.unlock();
        }
    }

    public void releaseWriteLock() {
        lock.lock();
        try {
            if (!writing) {
                throw new IllegalMonitorStateException();
            }
            writing = false;

            // give both readers and writers opportunities to run
            // preventing starvation
            readersOkay.signalAll();
            if (!writersQueue.isEmpty()) {
                writersQueue.getFirst().signal();
            }
            
        } finally {
            lock.unlock();
        }
    }
}
