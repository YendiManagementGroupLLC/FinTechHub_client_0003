package ext.com.undefined.samples;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ThreadPoolMagic extends RecursiveTask<BigInteger> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2975013376804460180L;
	private int start = 1;
	private int n;
	private static final int THRESHOLD = 20;

	public static final int MAXLOOP = 10;
	public static final int MAXTHREADS = 10;

	public ThreadPoolMagic(int start, int mid) {
		this.start = start;
		this.n = mid;
	}

	public ThreadPoolMagic(int start) {
		this.start = start;
		this.n = THRESHOLD;
	}

	private static void separate() {
		System.out.println("===============================================");
	}

	@Override
	protected BigInteger compute() {
		if ((n - start) >= THRESHOLD) {
			return ForkJoinTask.invokeAll(createSubtasks()).stream().map(ForkJoinTask::join).reduce(BigInteger.ONE,
					BigInteger::multiply);
		} else {
			return calculate(start, n);
		}
	}

	private Collection<ThreadPoolMagic> createSubtasks() {
		List<ThreadPoolMagic> dividedTasks = new ArrayList<>();
		int mid = (start + n) / 2;
		dividedTasks.add(new ThreadPoolMagic(start, mid));
		dividedTasks.add(new ThreadPoolMagic(mid + 1, n));
		return dividedTasks;
	}

	private BigInteger calculate(int start, int n) {
		return IntStream.rangeClosed(start, n).mapToObj(BigInteger::valueOf).reduce(BigInteger.ONE,
				BigInteger::multiply);
	}

	public static void main(final String... args) throws InterruptedException, ExecutionException {
		fixedThreadPoolTest();
		separate();
		fixedThreadPoolWithFactoryTest();
		separate();
		scheduledThreadPoolTest();
		separate();
		cachedThreadPoolTest();
		separate();
		scheduledThreadPoolExecutorTest();
	}

	public static void scheduledThreadPoolExecutorTest(String... args) throws InterruptedException, ExecutionException {
		int corePoolSize = ThreadPoolMagic.MAXTHREADS;
		// creates ScheduledThreadPoolExecutor object with number of thread 2
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(corePoolSize);
		// starts runnable thread
		stpe.execute(new RunnableThread());
		// starts callable thread that will start after 2 seconds
		ScheduledFuture<Integer> sf = stpe.schedule(new CallableThread(), 2, TimeUnit.SECONDS);
		// gets value returned by callable thread
		int res = sf.get();
		System.out.println("value returned by Callable Thread." + res);
		// returns active thread
		int activeCnt = stpe.getActiveCount();
		System.out.println("activeCnt:" + activeCnt);
		// stops all the threads in ScheduledThreadPoolExecutor
		stpe.shutdownNow();
		System.out.println(stpe.isShutdown());
	}

	public static void cachedThreadPoolTest(String... args) throws InterruptedException, ExecutionException {
		// creates cached thread pool
		ExecutorService exService = Executors.newCachedThreadPool();
		// runnable thread start to execute.
		exService.execute(new RunnableThread());
		// callable thread starts to execute
		Future<Integer> future = exService.submit(new CallableThread());
		// gets value of callable thread
		int val = future.get();
		System.out.println(val);
		// checks for thread termination
		boolean isTerminated = exService.isTerminated();
		System.out.println(isTerminated);
		// waits for termination for 30 seconds only
		exService.awaitTermination(30, TimeUnit.SECONDS);
		exService.shutdownNow();
	}

	public static void scheduledThreadPoolTest(final String... args) throws InterruptedException, ExecutionException {
		// creates thread pool with 2 thread
		final ScheduledExecutorService schExService = Executors.newScheduledThreadPool(ThreadPoolMagic.MAXTHREADS);
		// Object creation of runnable thread.
		final Runnable ob = new DemoThread();
		// Thread scheduling
		schExService.scheduleWithFixedDelay(ob, 2, 3, TimeUnit.SECONDS);
		// waits for termination for 30 seconds only
		schExService.awaitTermination(10, TimeUnit.SECONDS);
		// shutdown now.
		schExService.shutdownNow();
		System.out.println("Shutdown Complete");
	}

	public static void fixedThreadPoolTest(final String... args) throws InterruptedException, ExecutionException {
		final int noOfTh = ThreadPoolMagic.MAXTHREADS;
		// creates fixed thread pool
		final ExecutorService exService = Executors.newFixedThreadPool(noOfTh);
		// runnable thread start to execute. "done" will be returned after successful
		// thread run
		final Future<String> runFuture = exService.submit(new RunnableThread(), "done");
		// gets value for Runnable thread
		final String runval = runFuture.get();
		System.out.println("Task: " + runval);
		// callable thread starts to execute
		final Future<Integer> callFuture = exService.submit(new CallableThread());
		// gets value of callable thread
		final int callval = callFuture.get();
		System.out.println("Return Value: " + callval);
		// checks for thread termination
		boolean isTerminated = exService.isTerminated();
		System.out.println("isTerminated => " + isTerminated);
		// waits for termination for 30 seconds only
		exService.awaitTermination(30, TimeUnit.SECONDS);
		exService.shutdown();
		// checks for thread termination
		isTerminated = exService.isTerminated();
		System.out.println("isTerminated => " + isTerminated);
	}

	public static void fixedThreadPoolWithFactoryTest(final String... args)
			throws InterruptedException, ExecutionException {
		final int noOfTh = ThreadPoolMagic.MAXTHREADS;
		final ExecutorService exService = Executors.newFixedThreadPool(noOfTh, new MyThreadFactory());
		exService.execute(new RunnableThread());
		final Future<Integer> callFuture = exService.submit(new CallableThread());
		final int callval = callFuture.get();
		System.out.println("Return Value:" + callval);
		exService.shutdown();
	}
}

class CallableThread implements Callable<Integer> {
	@Override
	public Integer call() {
		int cnt = 1;
		for (; cnt <= ThreadPoolMagic.MAXLOOP; cnt++) {
			System.out.println("Callable: " + cnt);
		}
		return cnt;
	}
}

class RunnableThread implements Runnable {
	@Override
	public void run() {
		for (int cnt = 1; cnt <= ThreadPoolMagic.MAXLOOP; cnt++) {
			System.out.println("Runnable: " + cnt);
		}
	}
}

class MyThreadFactory implements ThreadFactory {
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setPriority(Thread.MAX_PRIORITY);
		System.out.println("---Thread Created---");
		return t;
	}
}

//Runnable thread
class DemoThread implements Runnable {
	@Override
	public void run() {
		for (int cnt = 1; cnt <= ThreadPoolMagic.MAXLOOP; cnt++) {
			System.out.println("runnable thread:" + cnt);
		}
		System.out.println("Done");
	}
}
