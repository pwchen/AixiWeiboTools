package crawler.weibo.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import crawler.weibo.service.CrawleWeiboUserService;
import crawler.weibo.service.filter.UserFilterService;
import crawler.weibo.service.scheduler.InitSchedulerService;

public class CrawlerServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// super.doPost(req, resp);
		String type = req.getParameter("type");

		if (type != null && type.equals("task")) {
			System.out.println("取得参数" + type);
			String text = req.getParameter("data");
			int size = InitSchedulerService.feedTaskFromText(text);
			resp.getWriter().print("Import " + size + " tasks successfully!");
		} else if (type != null && type.equals("filter")) {
			System.out.println("取得参数" + type);
			String text = req.getParameter("data");
			int size = UserFilterService.feedFilterRuleFromText(text);
			resp.getWriter().print(
					"Import " + size + " filtered rules successfully!");
		} else if (type != null && type.equals("start")) {
			System.out.println("取得参数" + type);
			CrawleWeiboUserService.startCrawle();
			resp.getWriter().println("start crawling!");
		}
	}

}
