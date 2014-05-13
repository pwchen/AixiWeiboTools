package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtils {

	private static final Log logger = LogFactory.getLog(FileUtils.class);

	/**
	 * 初始化微博数据文件：判断文件是否存在如果已存在,将原文件备份为BAK,再删除这个文件
	 * 
	 * @param userId
	 * @throws IOException
	 */
	public static void initDataFile(String fileName) throws IOException {
		if (new File(fileName).exists()) {
			FileUtils.copyFile(new File(fileName), new File(fileName + ".bak"));
			FileUtils.del(fileName);
		}
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile)
			throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	// 复制文件夹
	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		// 新建目标目录
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	}

	/**
	 * 
	 * @param srcFileName
	 * @param destFileName
	 * @param srcCoding
	 * @param destCoding
	 * @throws IOException
	 */
	public static void copyFile(File srcFileName, File destFileName,
			String srcCoding, String destCoding) throws IOException {// 把文件转换为GBK文件
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					srcFileName), srcCoding));
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(destFileName), destCoding));
			char[] cbuf = new char[1024 * 1024];
			int len = cbuf.length;
			int off = 0;
			int ret = 0;
			while ((ret = br.read(cbuf, off, len)) > 0) {
				off += ret;
				len -= ret;
			}
			bw.write(cbuf, 0, off);
			bw.flush();
		} finally {
			if (br != null)
				br.close();
			if (bw != null)
				bw.close();
		}
	}

	/**
	 * 
	 * @param filepath
	 * @throws IOException
	 */
	public static void del(String filepath) throws IOException {
		File f = new File(filepath);// 定义文件路径
		if (f.exists() && f.isDirectory()) {// 判断是文件还是目录
			if (f.listFiles().length == 0) {// 若目录下没有文件则直接删除
				f.delete();
			} else {// 若有则把文件放进数组，并判断是否有下级目录
				File delFile[] = f.listFiles();
				int i = f.listFiles().length;
				for (int j = 0; j < i; j++) {
					if (delFile[j].isDirectory()) {
						del(delFile[j].getAbsolutePath());// 递归调用del方法并取得子目录路径
					}
					delFile[j].delete();// 删除文件
				}
			}
		} else if (f.exists()) {
			f.delete();
		}
	}

	/**
	 * 
	 * @param FilePath
	 *            要移到的文件路径
	 * @param FolderPath
	 *            目标文件夹路径
	 */

	public static void move(String FilePath, String FolderPath) {
		// 文件原地址
		File oldFile = new File(FilePath);
		// new一个新文件夹
		File fnewpath = new File(FolderPath);
		// 判断文件夹是否存在
		if (!fnewpath.exists())
			fnewpath.mkdirs();
		// 将文件移到新文件里
		File fnew = new File(FolderPath + oldFile.getName());
		oldFile.renameTo(fnew);
	}

	public static boolean saveToFile(String string, String filename,
			String contentEncoding) {
		File file = new File("html\\" + filename);
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs()) {
				System.out.println("创建目录文件所在的目录失败！");
			}
		}
		if (string == null)
			return false;
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			fo.write(string.getBytes(contentEncoding));
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * 初始化文件，若该文件路径不存在则创建该文件，若该文件存在，则将该文件备份成bak文件
	 */
	public static void initFile(String url) {
		if (url != null) {
			File file = new File(url);
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					logger.info("目标文件的父文件夹不存在，准备创建父目录时失败！");
				}
				logger.info("目标文件的父目录创建成功！" + file.getParentFile().toString());
			} else if (file.exists()) {
				File bakFile = new File(url + ".bak");
				if (bakFile.exists()) {
					logger.info("备份文件时发现备份文件名已经存在该目录，删除该文件" + bakFile);
					bakFile.delete();
				}
				file.renameTo(new File(url + ".bak"));
				if (new File(url + ".bak").exists()) {
					logger.info("目标文件名已经存在，已经将原文件备份至" + bakFile);
				}
			}
			return;
		}
	}

	/**
	 * 根据文件名称获取该文件的FileWriter，若append为true则fw为原文件的追加写入，若false则返回新文件的fw
	 * 
	 * @param url
	 * @param append
	 * @return
	 */
	public static FileWriter getFileWriter(String url, boolean append) {
		File file = new File(url);
		FileWriter fw = null;
		if (append) {
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					logger.info("目标文件的父文件夹不存在，准备创建父目录时失败！");
				}
				logger.info("目标文件的父目录创建成功！" + file.getParentFile().toString());
			}
			try {
				fw = new FileWriter(file, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			initFile(url);
			try {
				fw = new FileWriter(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return fw;
	}

	/**
	 * append的方式对文件进行一次写入并关闭
	 * 
	 * @param url
	 *            文件路径
	 * @param str
	 *            写入内容
	 */
	public static void writeNewFileOnce(String url, String str) {
		FileWriter fw = FileUtils.getFileWriter(url, false);
		try {
			fw.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeFileWriter(fw);
		}
	}

	/**
	 * append的方式对文件进行一次写入并关闭
	 * 
	 * @param url
	 *            文件路径
	 * @param str
	 *            写入内容
	 */
	public static void appendWriteOnce(String url, String str) {
		FileWriter fw = FileUtils.getFileWriter(url, true);
		try {
			fw.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeFileWriter(fw);
		}
	}

	/**
	 * 关闭该FileWriter
	 * 
	 * @param fw
	 */
	public static void closeFileWriter(FileWriter fw) {
		try {
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 关闭该FileReader
	 * 
	 * @param fw
	 */
	public static void closeFileReader(FileReader fr) {
		try {
			fr.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 关闭该BufferedReader
	 * 
	 * @param fw
	 */
	public static void closeBufferedReader(BufferedReader br) {
		try {
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 用cmd执行该文件
	 * 
	 * @param forwardFilefatherUrl
	 */
	public static void openFile(String fileUrl) {
		try {
			String[] cmd = new String[5];
			cmd[0] = "cmd";
			cmd[1] = "/c";
			cmd[2] = "start";
			cmd[3] = " ";
			cmd[4] = fileUrl;
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("打开结果目录");
	}
	
	/**
	 * 关机
	 */
	public static void shutDoun() {
		try {
			String[] cmd = new String[2];
			cmd[0] = "shutdown";
			cmd[1] = "-s";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("打开结果目录");
	}
}
