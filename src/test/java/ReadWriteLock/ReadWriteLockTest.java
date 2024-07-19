package ReadWriteLock;

import java.util.ArrayList;
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
                    lock.aquireWriteLock();
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
                    lock.aquireReadLock();
                    value.set(log.getLast());
                    lock.releaseReadLock();
                } catch (InterruptedException exception) {}
            }
        };
    }

    public void testAsyncReaders() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();

        lock.aquireReadLock();
        lock.aquireReadLock();
        lock.aquireReadLock();

        lock.releaseReadLock();

        lock.aquireReadLock();

        lock.releaseReadLock();
        lock.releaseReadLock();
        lock.releaseReadLock();
    }

    public void testIllegalReaderRelease() throws InterruptedException {
        ReadWriteLock lock = new ReadWriteLock();

        boolean raisedException = false;
        try {
            lock.aquireReadLock();
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

        lock.aquireWriteLock();
        readerThread.start();

        log.add(0);
        lock.releaseWriteLock();

        readerThread.join();
        assertTrue(value.get() != null && value.get() == 0);
    }
}
