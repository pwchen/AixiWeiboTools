package etl;

import graph.feature.CalcAllFeature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utils.FileUtils;

public class ETLService {
	private static final Log logger = LogFactory.getLog(ETLService.class);

	public static void main(String[] args) {
		String uId = "5121662959";
		String baseFileUrl = "D:" + "\\" + "data"
				+ "\\";
		int depth = 3;
		logger.info("当前提取节点方式：当个用户-" + uId);
		String fatheFilerUrl = baseFileUrl + uId + "\\";
		String nodeFileUrl = fatheFilerUrl + "allnodes.csv";
		ExtractNodesFromOracle.initNodeFileWriter(nodeFileUrl);
		Long[] idList = new Long[] { Long.parseLong(uId) };
		ExtractNodesFromOracle.extractNodesByIdList(idList, depth, nodeFileUrl);
		ExtractNodesFromOracle.generateEdgesFile(fatheFilerUrl);
		FileUtils.openFile(fatheFilerUrl);
		CalcAllFeature.main(new String[] { fatheFilerUrl });
	}
}
