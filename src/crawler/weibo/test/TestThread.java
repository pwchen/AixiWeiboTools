package crawler.weibo.test;

public class TestThread implements Runnable {

	int num;

	public TestThread(int n) {
		this.num = n;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		tes1.me(num--);
	}

	public static void main(String args[]) {

		Thread[] Threads = new Thread[10];
		for (int i = 0; i < 10; i++) {
			Threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName()
							+ tes1.me(3));
				}

			});
			Threads[i].start();
		}
		// new TestThread(7).start();
		// new TestThread(6).start();
		// new TestThread(5).start();

	}
}
