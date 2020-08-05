package com.limouren;

import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * 文件操作帮助类
 * @author xs
 */
public class FileUtils {
	private static Logger log = Logger.getLogger(FileUtils.class);
	
	/**
	 * 根据地址获得数据的字节流
	 * @param strUrl 网络连接地址
	 * @return
	 */
	public static byte[] getImageFromNetByUrl(String strUrl) {
		try {
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
			byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
			return btImg;
		} catch (Exception e) {
			log.error("",e);
		}
		return null;
	}

	/**
	 * 根据地址获得数据的字节流
	 * @param strUrl 本地连接地址
	 * @return
	 */
	public static byte[] getImageFromLocalByUrl(String strUrl) {
		try {
			File imageFile = new File(strUrl);
			InputStream inStream = new FileInputStream(imageFile);
			byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
			return btImg;
		} catch (Exception e) {
			log.error("",e);
		}
		return null;
	}

	/**
	 * 从输入流中获取数据
	 * @param inStream 输入流
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception {
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    byte[] buffer = new byte[10240];
	    int len = 0;
	    while ((len = inStream.read(buffer)) != -1) {
	        outStream.write(buffer, 0, len);
	    }
	    inStream.close();
	    return outStream.toByteArray();
	}

	/**
	 * 将图片写入到磁盘
	 * @param img      图片数据流
	 * @param zipImageUrl 文件保存时的名称
	 */
	public static void writeImageToDisk(byte[] img, String zipImageUrl) {
		try {
			File file = new File(zipImageUrl);
			FileOutputStream fops = new FileOutputStream(file);
			fops.write(img);
			fops.flush();
			fops.close();
			log.info("图片已经写入" + zipImageUrl);
		} catch (Exception e) {
			log.error("",e);
		}
	}
	
	/**
	 * 根据url获取文件名
	 * @param imgUrl      图片url
	 * @return 文件名
	 */
	public static String getFileNameByUrl(String imgUrl) {
		try {
			File file = new File(imgUrl);
			String str = file.getName();
			String suffix = str.substring(str.lastIndexOf(".") + 1);
			String fileName = System.currentTimeMillis()+"."+suffix;
			return fileName;
		} catch (Exception e) {
			log.error("",e);
			return null;
		}
	}
	
	/**
	 * 根据url删除文件
	 * @param imgUrl      图片url
	 */
	public static void deleteFile(String imgUrl) {
		try {
			File file = new File(imgUrl);
			if (file.exists()) {
				file.delete();
				log.info("文件已经被成功删除="+imgUrl);
			}else {
				log.info("文件不存在="+imgUrl);
			}
		} catch (Exception e) {
			log.error("删除文件失败",e);
		}
	}
	
	/**
	 * 根据文件路径获取文件MD5
	 * @param strUrl
	 * @return
	 */
	public static String getFileMd5(String strUrl) {
		return getFileMd5(getImageFromLocalByUrl(strUrl));
	}
	
	/**
	 * 根据文件字节流获取文件MD5
	 * @param fileBytes
	 * @return
	 */
	public static String getFileMd5(byte[] fileBytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] mdBytes = md.digest(fileBytes);
	        BigInteger bigInt = new BigInteger(1, mdBytes);
	        return bigInt.toString(16);
		} catch (Exception e) {
			log.error("删除文件失败",e);
			return null;
		}
	}



	public static void main(String[] args) {
		try {
//			byte[] btImg1 = FileUtils.getImageFromNetByUrl("http://www.zngyng.com/online-store/images/paytype/FF5DCF43180A61A831CBE9336CB752BC.jpg");
//			if (null != btImg1 && btImg1.length > 0) {
//				System.out.println("读取到：" + btImg1.length + " 字节");
//				String fileZipUrl1 = "f:\\abc.jpg";
//				FileUtils.writeImageToDisk(btImg1, fileZipUrl1);
//			} else {
//				System.out.println("没有从该连接获得内容");
//			}
			
			System.out.println(FileUtils.getFileNameByUrl("http://www.zngyng.com/online-store/images/paytype/FF5DCF43180A61A831CBE9336CB752BC.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
