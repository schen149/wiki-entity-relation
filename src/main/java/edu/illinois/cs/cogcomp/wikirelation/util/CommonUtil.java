package edu.illinois.cs.cogcomp.wikirelation.util;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommonUtil {

    /**
     * Bounds the number concurrent executing thread to 1/2 of the cores
     * available to the JVM. If more jobs are submitted than the allowed
     * upperbound, the caller thread will be executing the job.
     * @return a fixed thread pool with bounded job numbers
     */
    public static ThreadPoolExecutor getBoundedThreadPool(int poolSize) {
        poolSize = Math.max(1, poolSize - 1);
        poolSize = Math.min(poolSize, Runtime.getRuntime().availableProcessors());
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize, // Core count
                poolSize, // Pool Max
                15, TimeUnit.SECONDS, // Thread keep alive time
                new ArrayBlockingQueue<Runnable>(poolSize),// Queue
                new ThreadPoolExecutor.CallerRunsPolicy()// Blocking mechanism
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static Record deserializeRecordFromBytes(byte[] bytes) throws TException {
        Record rec = new Record();
        TDeserializer td = new TDeserializer();
        td.deserialize(rec, bytes);
        return rec;
    }
}
