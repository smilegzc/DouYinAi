import java.awt.image.ColorModel;
import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 调用实例：
 * AiFace face = new Aiface();
 * String imgDir = "C:/....";
 * face.setImage(imgDir);
 *
 * 获取图片中人数
 * int faceSum = face.getFaceSum();
 * 获取每个人的信息
 * int[][] faceSAB = face.getSAB();
 *
 */

public class AiFace {
	private final String app_id = "1106946554";
	private final String app_key = "MAYVvK21YfWzjsZo";
	private final String url = "https://api.ai.qq.com/fcgi-bin/face/face_detectface";
	private HttpWeb faceInfo = new HttpWeb();
	private int[][] sab = null;
	private int faceSum = 0;
	private int ret = 0;
	
	public void setImage(String imgDir) {
		String param = getParam(getBase64(imgDir));
		faceInfo.connectUrl(url, param);
		getSABInfo();
	}
	
	public int[][] getSAB() {
		return sab;
	}
	
	public int getFaceSum() {
		return faceSum;
	}
	
	public int getRet() {
		return ret;
	}
	
	public int getSex(int index) {
		return sab[index][0];
	}
	
	public int getAge(int index) {
		return sab[index][1];
	}
	
	public int getBeauty(int index) {
		return sab[index][2];
	}
	
	private void getSABInfo() {
		JSONObject json = faceInfo.getJSONObject();
		ret = json.getInt("ret");
		
		if(json != null && ret == 0) {
			JSONArray faceList = json.getJSONObject("data").getJSONArray("face_list");
			faceSum = faceList.length();
			sab = new int[faceSum][3];
			for(int i = 0; i < faceSum; i ++) {
				JSONObject face = faceList.getJSONObject(i);
				sab[i][0] = face.getInt("gender");
				sab[i][1] = face.getInt("age");
				sab[i][2] = face.getInt("beauty");
			}
		}
	}
	//计算图片base64编码
	private String getBase64(String fileDir) {
		byte[] fileByte = null;
		try {
			FileInputStream fis = new FileInputStream(new File(fileDir));
			fileByte = new byte[fis.available()];
			int read = fis.read(fileByte);
			fis.close();
			//如果图片过大，则进行压缩
			if(read > 1024*1024) {
				fileByte = compressPic(fileByte, 725, 725, true);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		byte[] fileBase64 = null;
		if(fileByte != null) {
			fileBase64 = Base64.encodeBase64(fileByte);
		}
		if(fileBase64 != null) {
			try {
				//System.out.println(new String(fileBase64, "UTF-8"));
				return new String(fileBase64, "UTF-8");
			} catch(UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	//图片压缩，返回压缩后图片的字节数组
	public byte[] compressPic(byte[] imageByte, int width, int height, boolean gp) {
		byte[] inByte = null;
		try {
			ByteArrayInputStream byteInput = new ByteArrayInputStream(imageByte);
			Image img = ImageIO.read(byteInput);
			// 判断图片格式是否正确
			if(img.getWidth(null) == -1) {
				return inByte;
			} else {
				int newWidth;
				int newHeight;
				// 判断是否是等比缩放
				if(gp == true) {
					// 为等比缩放计算输出的图片宽度及高度
					double rate1 = ((double) img.getWidth(null)) / (double) width + 0.1;
					double rate2 = ((double) img.getHeight(null)) / (double) height + 0.1;
					// 根据缩放比率大的进行缩放控制
					double rate = rate1 > rate2 ? rate1 : rate2;
					newWidth = (int) (((double) img.getWidth(null)) / rate);
					newHeight = (int) (((double) img.getHeight(null)) / rate);
				} else {
					newWidth = width; // 输出的图片宽度
					newHeight = height; // 输出的图片高度
				}
				BufferedImage tag = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_INT_RGB);
				img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
				/*
				 * Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的
				 * 优先级比速度高 生成的图片质量比较好 但速度慢
				 */
				tag.getGraphics().drawImage(img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
				
				ImageWriter imgWrier;
				ImageWriteParam imgWriteParams;
				// 指定写图片的方式为 jpg
				imgWrier = ImageIO.getImageWritersByFormatName("jpg").next();
				imgWriteParams = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(null);
				// 要使用压缩，必须指定压缩方式为MODE_EXPLICIT
				imgWriteParams.setCompressionMode(imgWriteParams.MODE_EXPLICIT);
				// 这里指定压缩的程度，参数qality是取值0~1范围内，
				imgWriteParams.setCompressionQuality(1);
				
				imgWriteParams.setProgressiveMode(imgWriteParams.MODE_DISABLED);
				ColorModel colorModel = ColorModel.getRGBdefault();
				// 指定压缩时使用的色彩模式
				imgWriteParams.setDestinationType(new javax.imageio.ImageTypeSpecifier(colorModel, colorModel
						.createCompatibleSampleModel(100, 100)));
				//将压缩后的图片返回字节流
				ByteArrayOutputStream out = new ByteArrayOutputStream(imageByte.length);
				imgWrier.reset();
				// 必须先指定 out值，才能调用write方法, ImageOutputStream可以通过任何 OutputStream构造
				imgWrier.setOutput(ImageIO.createImageOutputStream(out));
				// 调用write方法，就可以向输入流写图片
				imgWrier.write(null, new IIOImage(tag, null, null), imgWriteParams);
				out.flush();
				out.close();
				byteInput.close();
				inByte = out.toByteArray();
				
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		return inByte;
	}
	
	private String getParam(String imgBase64) {
		String time = String.valueOf(new Date().getTime()/1000);//获取时间戳
		
		Map<String, String> param = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		param.put("app_id", app_id);
		param.put("time_stamp", time);
		param.put("nonce_str", String.valueOf(new Date().getTime()));
		param.put("image", imgBase64);
		param.put("mode", "0");
		
		Set<String> keySet = param.keySet();
		Iterator<String> iter = keySet.iterator();
		StringBuilder sb;
		sb = new StringBuilder();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = param.get(key);
			try {
				sb.append(key).append('=').append(URLEncoder.encode(value, "UTF-8")).append('&');
			} catch(UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		sb.append("app_key=").append(app_key);
		String sign = stringMD5(sb.toString());
		sb.append("&sign=").append(sign.toUpperCase());
		return sb.toString();
	}
	
	private String stringMD5(String input) {
		String[] strHex = { "0", "1", "2", "3", "4", "5",
				            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
		StringBuilder sb = new StringBuilder();
		try {
			// 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
			MessageDigest messageDigest =MessageDigest.getInstance("MD5");
			
			// 输入的字符串转换成字节数组
			byte[] inputByteArray = input.getBytes();
			
			// inputByteArray是输入字符串转换得到的字节数组
			messageDigest.update(inputByteArray);
			
			// 转换并返回结果，也是字节数组，包含16个元素
			byte[] resultByteArray = messageDigest.digest();
			
			for(byte aResultByteArray : resultByteArray) {
				int d = aResultByteArray;
				if(d < 0) {
					d += 256;
					
				}
				int d1 = d / 16;
				int d2 = d % 16;
				sb.append(strHex[d1]).append(strHex[d2]);
				
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}