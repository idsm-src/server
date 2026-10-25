package cz.iocb.load.stats;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;



/**
 * Fixed-size thread pool that runs the waiting tasks in the order of their priorities (lower values first) and the
 * tasks of the same priority in the order of their submission.
 */
public class PriorityThreadPoolExecutor extends ThreadPoolExecutor
{
    /**
     * Task carrying a priority.
     */
    static interface Prioritized
    {
        /**
         * Returns the priority of the task.
         *
         * @return the priority, lower values run first
         */
        int priority();
    }


    /**
     * Future task ordered by its priority and then by its sequence number.
     *
     * @param <V> the type of the result
     */
    static class ComparableFutureTask<V> extends FutureTask<V> implements Comparable<ComparableFutureTask<?>>
    {

        /**
         * Priority of the task, lower values run first.
         */
        final int priority;

        /**
         * Sequence number of the submission.
         */
        final long seq;

        /**
         * Creates the task of a callable.
         *
         * @param c the callable
         * @param priority the priority of the task
         * @param seq the sequence number of the submission
         */
        ComparableFutureTask(Callable<V> c, int priority, long seq)
        {
            super(c);
            this.priority = priority;
            this.seq = seq;
        }

        /**
         * Creates the task of a runnable.
         *
         * @param r the runnable
         * @param v the result returned on completion
         * @param priority the priority of the task
         * @param seq the sequence number of the submission
         */
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


    /**
     * Callable with a priority.
     *
     * @param <V> the type of the result
     */
    private static final class PrioritizedCallable<V> implements Callable<V>, Prioritized
    {
        /**
         * Priority of the callable.
         */
        private final int priority;

        /**
         * Wrapped callable.
         */
        private final Callable<V> delegate;

        /**
         * Creates the callable.
         *
         * @param priority the priority
         * @param delegate the wrapped callable
         */
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


    /**
     * Runnable with a priority.
     */
    private static final class PrioritizedRunnable implements Runnable, Prioritized
    {
        /**
         * Priority of the runnable.
         */
        private final int priority;

        /**
         * Wrapped runnable.
         */
        private final Runnable delegate;

        /**
         * Creates the runnable.
         *
         * @param priority the priority
         * @param delegate the wrapped runnable
         */
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


    /**
     * Source of the sequence numbers of the submissions.
     */
    private static final AtomicLong SEQUENCER = new AtomicLong();


    /**
     * Creates the pool and starts all its threads.
     *
     * @param threads the number of threads
     */
    public PriorityThreadPoolExecutor(int threads)
    {
        super(threads, threads, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>());
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


    /**
     * Submits a callable with a priority.
     *
     * @param <T> the type of the result
     * @param task the callable
     * @param priority the priority, lower values run first
     * @return the future of the result
     */
    public <T> Future<T> submit(Callable<T> task, int priority)
    {
        return super.submit(new PrioritizedCallable<>(priority, task));
    }


    /**
     * Submits a runnable with a priority.
     *
     * @param task the runnable
     * @param priority the priority, lower values run first
     * @return the future of the completion
     */
    public Future<?> submit(Runnable task, int priority)
    {
        return super.submit(new PrioritizedRunnable(priority, task));
    }
}
