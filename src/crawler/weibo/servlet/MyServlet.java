package crawler.weibo.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import crawler.weibo.service.CrawleWeiboUserService;

public class MyServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		System.out.println("自动加载启动.");
		System.out.println("自动加载启动.");
		CrawleWeiboUserService.startCrawle(null);
	}

}
