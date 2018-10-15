package ext.com.undefined.samples;

public class ThreadMagic {
	public static void main(String args[]) {
		new MyThread("One");
		new MyThread("Two");
		new MyThread("Three");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Main thread exiting.");

		separate();
		
		Chat m = new Chat();
		new T1(m);
		new T2(m);
	}
	
	private static void separate() {
		System.out.println("===============================================");
	}
}

class MyThread implements Runnable {
	String name;
	Thread t;

	MyThread(String threadname) {
		name = threadname;
		t = new Thread(this, name);
		System.out.println("New thread: " + t);
		t.start();
	}

	public void run() {
		try {
			for (int i = 5; i > 0; i--) {
				System.out.println(name + ": " + i);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			System.out.println(name + "Interrupted");
		}
		System.out.println(name + " exiting.");
	}
}

class Chat {
	boolean flag = false;

	public synchronized void Question(String msg) {
		if (flag) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(msg);
		flag = true;
		notify();
	}

	public synchronized void Answer(String msg) {
		if (!flag) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println(msg);
		flag = false;
		notify();
	}
}

class T1 implements Runnable {
	Chat m;
	String[] s1 = { "Hi", "How are you ?", "I am also doing fine!" };

	public T1(Chat m1) {
		this.m = m1;
		new Thread(this, "Question").start();
	}

	public void run() {
		for (int i = 0; i < s1.length; i++) {
			m.Question(s1[i]);
		}
	}
}

class T2 implements Runnable {
	Chat m;
	String[] s2 = { "Hi", "I am good, what about you?", "Great!" };

	public T2(Chat m2) {
		this.m = m2;
		new Thread(this, "Answer").start();
	}

	public void run() {
		for (int i = 0; i < s2.length; i++) {
			m.Answer(s2[i]);
		}
	}
}