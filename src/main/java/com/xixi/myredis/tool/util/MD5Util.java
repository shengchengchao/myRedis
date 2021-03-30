package com.xixi.myredis.tool.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * md5加密（不可逆）工具类
 */
public class MD5Util {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MD5Util.class);

	private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E','F' };

	private static String bytesToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		int t;
		for (int i = 0; i < 16; i++) {
			t = bytes[i];
			if (t < 0)
				t += 256;
			sb.append(hexDigits[(t >>> 4)]);
			sb.append(hexDigits[(t % 16)]);
		}
		return sb.toString();
	}

	/**
	 * 生成16位MD5加密字符串
	 * 
	 * @param input 加密字符串
	 * @return 密文(字母大写)
	 * @throws Exception
	 * @return: String
	 * 
	 * @version 创建时间：2017年5月25日 下午3:43:29
	 */
	public static String md516(String input) throws Exception {
		return code(input, 16);
	}

	/**
	 * 生成32位MD5加密字符串
	 * 
	 * @param input 加密字符串
	 * @return 密文(字母大写)
	 * @throws Exception
	 * @return: String
	 * 
	 * @version 创建时间：2017年5月25日 下午3:43:59
	 */
	public static String md532(String input) throws Exception {
		return code(input, 32);
	}

	/**
	 * 生成16位或32位MD5加密字符串，失败返回空字符串
	 * 
	 * @param input 加密字符串
	 * @param bit 16表示16位长度的字符串，
	 * @return 密文(字母大写)
	 * @throws Exception
	 * @return: String
	 * 
	 * @version 创建时间：2017年5月25日 下午3:45:07
	 */
	public static String code(String input, int bit){
		try {
			if (StringUtils.isNotEmpty(input)) {
				MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
				if (bit == 16)
					return bytesToHex(md.digest(input.getBytes("utf-8"))).substring(8, 24);
				return bytesToHex(md.digest(input.getBytes("utf-8")));
			}
		} catch (Exception e) {
			log.info("系统报错",e);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 获取md5字符串（32位），优先使用MD5.algorithm算法加密，md5算法备选
	 * 
	 * @param source 加密字符串
	 * @return 密文(字母小写)
	 * @throws Exception
	 * @return: String
	 * 
	 * @version 创建时间：2017年5月25日 下午3:57:58
	 */
	public static String md5_3(String source) throws Exception {
		MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
		byte[] a = md.digest(source.getBytes());
		a = md.digest(a);
		a = md.digest(a);

		return bytesToHex(a);
	}

	/** 
	 * 获取md5字符串（32位），失败返回空字符串
	 * 
	 * @param source 加密字符串
	 * @return 密文(字母小写)
	 * @return: String
	 * 
	 * @version 创建时间：2017年5月25日 下午3:58:04
	 * @deprecated 建议使用<b>md532</b>函数替代
	 */
	public static String getMD5String(String source) {
		if (source == null || "".equals(source)) {
			return StringUtils.EMPTY;
		}
		StringBuffer buffer = new StringBuffer();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(source.getBytes("UTF-8"));
			byte[] bytes = messageDigest.digest();
			for (byte b : bytes) {
				if (Integer.toHexString(0xFF & b).length() == 1) {
					buffer.append("0");
					buffer.append(Integer.toHexString(0xFF & b));
				} else {
					buffer.append(Integer.toHexString(0xFF & b));
				}
			}
		} catch (Exception e) {
			log.info("系统报错",e);
		}
		return buffer.toString();
	}
}
