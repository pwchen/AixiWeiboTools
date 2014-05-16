package crawler.weibo.test;

public class tes1 {

	public static double me(int n) {
		int mn = 5;
		System.out.println(Thread.currentThread().getName() + ":" + n-- + "-"
				+ mn--);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Math.random();
	}

}
