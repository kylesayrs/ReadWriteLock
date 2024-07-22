package ReadWriteLock;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class ReadWriteLockTest 
    extends TestCase
{
    public ReadWriteLockTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite( ReadWriteLockTest.class );
    }

    public Thread makeWriterThread(ReadWriteLock lock, ArrayList<Integer> log, Integer value) {
        return new Thread() {
            @Override
            public void run() {
                try {
                    lock.acquireWriteLock();
                    log.add(value);
                    lock.releaseWriteLock();
                } catch (InterruptedException exception) {}
            }
        };
    }

    public Thread makeReaderThread(ReadWriteLock lock, ArrayList<Integer> log, AtomicReference<Integer> value) {
        return new Thread() {
            @Override
            public void run() {
                try {
                    lock.acquireReadLock();
                    try {
                        value.set(log.getLast());
                    } catch (NoSuchElementException e) {
                        value.set(null);
                    }
                    lock.releaseReadLock();
                } catch (InterruptedException exception) {}
            }
        };
    }

    public void testAsyncReaders() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();

        lock.acquireReadLock();
        lock.acquireReadLock();
        lock.acquireReadLock();

        lock.releaseReadLock();

        lock.acquireReadLock();

        lock.releaseReadLock();
        lock.releaseReadLock();
        lock.releaseReadLock();
    }

    public void testIllegalReaderRelease() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();

        boolean raisedException = false;
        try {
            lock.acquireReadLock();
            lock.releaseReadLock();

            lock.releaseReadLock();
        } catch (IllegalMonitorStateException e) {
            raisedException = true;
        }

        assertTrue(raisedException);
    }

    public void testReaderExclusivity() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();
        ArrayList<Integer> log = new ArrayList<>();

        AtomicReference<Integer> value = new AtomicReference<>();
        Thread readerThread = makeReaderThread(lock, log, value);

        lock.acquireWriteLock();
        readerThread.start();

        log.add(0);
        lock.releaseWriteLock();

        readerThread.join();
        assertTrue(value.get() != null && value.get() == 0);
    }

    public void testWriterReaderExclusivity() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();
        ArrayList<Integer> log = new ArrayList<>();
        
        Thread writerThread = makeWriterThread(lock, log, 0);
        
        lock.acquireReadLock();
        writerThread.start();

        assertTrue(log.isEmpty());

        lock.releaseReadLock();
        writerThread.join();
    }

    public void testWriterWriterExclusivity() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();
        ArrayList<Integer> log = new ArrayList<>();

        Thread writerThread = makeWriterThread(lock, log, 1);

        lock.acquireWriteLock();
        writerThread.start();

        log.add(0);

        lock.releaseWriteLock();
        writerThread.join();

        ArrayList<Integer> expectedLog = new ArrayList<>();
        expectedLog.add(0);
        expectedLog.add(1);
        assertTrue(log.equals(expectedLog));
    }

    public void testWriterOrder() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();
        ArrayList<Integer> log = new ArrayList<>();
        int numThreads = 100;

        // make threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = makeWriterThread(lock, log, i);
            threads[i].start();
        }

        // start threads
        //for (int i = 0; i < numThreads; i++) {
        //}

        // join threads
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        // make expected
        ArrayList<Integer> expectedLog = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            expectedLog.add(i);
        }

        System.out.println(log);
        System.out.println(expectedLog);

        assertTrue(log.equals(expectedLog));
    }
}
