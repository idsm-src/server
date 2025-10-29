package cz.iocb.load.stats;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;



public class PriorityThreadPoolExecutor extends ThreadPoolExecutor
{
    static interface Prioritized
    {
        int priority();
    }


    static class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<?>>
    {

        final int priority;
        final long seq;

        ComparableFutureTask(Callable<V> c, int priority, long seq)
        {
            super(c);
            this.priority = priority;
            this.seq = seq;
        }

        ComparableFutureTask(Runnable r, V v, int priority, long seq)
        {
            super(r, v);
            this.priority = priority;
            this.seq = seq;
        }

        @Override
        public int compareTo(ComparableFutureTask<?> o)
        {
            int p = Integer.compare(this.priority, o.priority);
            return (p != 0) ? p : Long.compare(this.seq, o.seq);
        }
    }


    private static final class PrioritizedCallable<V> implements Callable<V>, Prioritized
    {
        private final int priority;
        private final Callable<V> delegate;

        PrioritizedCallable(int priority, Callable<V> delegate)
        {
            this.priority = priority;
            this.delegate = delegate;
        }

        @Override
        public int priority()
        {
            return priority;
        }

        @Override
        public V call() throws Exception
        {
            return delegate.call();
        }
    }


    private static final class PrioritizedRunnable implements Runnable, Prioritized
    {
        private final int priority;
        private final Runnable delegate;

        PrioritizedRunnable(int priority, Runnable delegate)
        {
            this.priority = priority;
            this.delegate = delegate;
        }

        @Override
        public int priority()
        {
            return priority;
        }

        @Override
        public void run()
        {
            delegate.run();
        }
    }


    private static final AtomicLong SEQUENCER = new AtomicLong();


    public PriorityThreadPoolExecutor(int threads)
    {
        super(threads, threads, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
        prestartAllCoreThreads();
    }


    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
    {
        int p = (callable instanceof Prioritized) ? ((Prioritized) callable).priority() : 0;
        return new ComparableFutureTask<>(callable, p, SEQUENCER.getAndIncrement());
    }


    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
    {
        int p = (runnable instanceof Prioritized) ? ((Prioritized) runnable).priority() : 0;
        return new ComparableFutureTask<>(runnable, value, p, SEQUENCER.getAndIncrement());
    }


    public <T> Future<T> submit(Callable<T> task, int priority)
    {
        return super.submit(new PrioritizedCallable<>(priority, task));
    }


    public Future<?> submit(Runnable task, int priority)
    {
        return super.submit(new PrioritizedRunnable(priority, task));
    }
}
