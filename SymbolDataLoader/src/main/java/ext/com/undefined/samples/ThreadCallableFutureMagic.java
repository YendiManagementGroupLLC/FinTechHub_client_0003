package ext.com.undefined.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ThreadCallableFutureMagic {

	public static void main(String[] args) throws Exception {

		// FutureTask is a concrete class that
		// implements both Runnable and Future
		List<FutureTask<Integer>> randomNumberTasks = new ArrayList<FutureTask<Integer>>();
		int maxThreads = 25;

		for (int i = 0; i < maxThreads; i++) {
			Callable<Integer> callable = new CallableExample();

			// Create the FutureTask with Callable
			randomNumberTasks.add(i, new FutureTask<Integer>(callable));

			// As it implements Runnable, create Thread
			// with FutureTask
			Thread t = new Thread(randomNumberTasks.get(i));
			t.start();
			System.out.println(t.getName() + " => " + t.getState());
		}

		for (int i = 0; i < maxThreads; i++) {
			// As it implements Future, we can call get()
			System.out.println(randomNumberTasks.get(i).get());

			// This method blocks till the result is obtained
			// The get method can throw checked exceptions
			// like when it is interrupted. This is the reason
			// for adding the throws clause to main
		}
	}
}

class CallableExample implements Callable<Integer> {

	public Integer call() throws Exception {
		Random generator = new Random();
		Integer randomNumber = generator.nextInt(5);
		System.out.println("Now executing call for ... " + randomNumber);
		Thread.sleep(randomNumber * 1000);
		return randomNumber;
	}

}