package com.utils;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 * 解压缩文件 工具类
 * 
 * @author zfy 使用jar包 ant
 */
public class ZipTool {

	public static void main(String[] args) {

		try {
//			zip(new File("f:/新建文本文档.zip"), "f:/U盘");
			unZip("f:/新建文本文档.zip", "F:/11/" );
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static final int BUFFER = 1024 * 1024 * 10;

	/**
	 * 用zip格式压缩文件
	 * 
	 * @param zipFileName
	 *            压缩后的文件名 包含路径 如："c:\\test.zip"
	 * @param inputFile
	 *            要压缩的文件 可以是文件或文件夹 如："c:\\test" 或 "c:\\test.doc"
	 * @throws Exception
	 *             ant下的zip工具默认压缩编码为UTF-8编码，
	 *             而winRAR软件压缩是用的windows默认的GBK或者GB2312编码
	 *             用ant压缩后放到windows上面会出现中文文件名乱码，用winRAR压缩的文件，用ant解压也会出现乱码，
	 *             所以，在用ant处理winRAR压缩的文件时，要设置压缩编码
	 */
	public static void zip(File zipFile, String inputFile) {
		File f = new File(inputFile);
		try {
			// File temp = new File(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
			// 设置压缩编码
			out.setEncoding("GBK");// 设置为GBK后在windows下就不会乱码了，如果要放到Linux或者Unix下就不要设置了
			zip(out, f, "");// 递归压缩方法
			System.out.println("zip done");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void zip(ZipOutputStream out, File f, String base) {
		// 记录日志，开始压缩
		System.out.println("Zipping   " + f.getName());
		try {
			// 如果是文件夹，则获取下面的所有文件
			if (f.isDirectory()) {
				File[] fl = f.listFiles();
				// 此处要将文件写到文件夹中只用在文件名前加"/"再加文件夹名
				out.putNextEntry(new ZipEntry(base + "/"));
				base = base.length() == 0 ? "" : base + "/";
				for (int i = 0; i < fl.length; i++) {
					zip(out, fl[i], base + fl[i].getName());
				}
			} else { // 如果是文件，则压缩
				System.out.println(f.getName());
				out.putNextEntry(new ZipEntry(base == "" ? f.getName() : base)); // 生成下一个压缩节点
				FileInputStream in = new FileInputStream(f); // 读取文件内容
				int len;
				byte[] buf = new byte[BUFFER];
				while ((len = in.read(buf, 0, BUFFER)) != -1) {
					out.write(buf, 0, len); // 写入到压缩包
				}
				in.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压缩zip文件
	 * 
	 * @param fileName
	 *            要解压的文件名 包含路径 如："c:\\test.zip"
	 * @param filePath
	 *            解压后存放文件的路径 如："c:\\temp"
	 * @throws Exception
	 */
	public static void unZip(String fileName, String filePath) throws Exception {

		ZipFile zipFile = new ZipFile(fileName, "GBK"); // 以“GBK”编码创建zip文件，用来处理winRAR压缩的文件。
		Enumeration<?> emu = zipFile.getEntries();

		while (emu.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) emu.nextElement();
			if (entry.isDirectory()) {

				new File(filePath + "\\" + entry.getName()).mkdirs();
				continue;
			}
			BufferedInputStream bis = new BufferedInputStream(
					zipFile.getInputStream(entry));

			File file = new File(filePath + "\\" + entry.getName());
			File parent = file.getParentFile();
			if (parent != null && (!parent.exists())) {
				parent.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
			byte[] buf = new byte[BUFFER];
			int len = 0;
			while ((len = bis.read(buf, 0, BUFFER)) != -1) {
				fos.write(buf, 0, len);
			}
			bos.flush();
			bos.close();
			bis.close();
			System.out.println("解压文件：" + file.getName());
		}
		zipFile.close();
	}
}
