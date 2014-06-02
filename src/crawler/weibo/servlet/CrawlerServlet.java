package crawler.weibo.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawler.weibo.service.CrawleWeiboUserService;
import crawler.weibo.service.filter.UserFilterService;
import crawler.weibo.service.scheduler.InitSchedulerService;

public class CrawlerServlet extends HttpServlet {
	private static final Log logger = LogFactory.getLog(HttpServlet.class);

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
			logger.info("取得参数" + type);
			String text = req.getParameter("data");
			int size = InitSchedulerService.feedTaskFromText(text);
			resp.getWriter().print("Import " + size + " tasks successfully!");
		} else if (type != null && type.equals("filter")) {
			logger.info("取得参数" + type);
			String text = req.getParameter("data");
			int size = UserFilterService.feedFilterRuleFromText(text);
			resp.getWriter().print(
					"Import " + size + " filtered rules successfully!");
		} else if (type != null && type.equals("start")) {
			logger.info("取得参数" + type);
			CrawleWeiboUserService.startCrawle();
			resp.getWriter().print("start crawling successfully!");
		} else if (type != null && type.equals("pause")) {
			logger.info("取得参数" + type);
			CrawleWeiboUserService.pauseCrawler();
			resp.getWriter().print("pause crawling successfully!");
		} else if (type != null && type.equals("resume")) {
			logger.info("取得参数" + type);
			CrawleWeiboUserService.resumeCrawler();
			resp.getWriter().print("resume crawling successfully!");
		}
	}
}
