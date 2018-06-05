import org.json.JSONObject;
import java.net.*;
import java.io.*;

/**
 * 负责网络传输，使用示例
 * HttpWeb http = new HttpWeb();
 * //传入url和参数
 * http.connectUrl(url, param)
 * //拿到返回信息
 * JSONObject jsonObj = http.getJSONObject();
 * JSONValue jsonValue = http.getJSONValue(value);
 */
public class HttpWeb {
	private JSONObject json = null;
	
	private Boolean connect = false;
	
	public HttpWeb(String url, String param) {
		json = sendGet(url, param);
	}
	
	public HttpWeb() {
		
	}
	
	public void connectUrl(String url, String param) {
		json = sendGet(url, param);
	}
	
	public String getJSONValue(String key) {
		if(json != null && json.has(key)) {
			return json.getString(key);
		}
		return null;
	}
	
	public JSONObject getJSONObject() {
		return json;
	}
	
	public Boolean isConnect() {
		return connect;
	}
	
	private JSONObject sendGet(String url, String param) {
		JSONObject jsObj = null;
		BufferedReader br = null;
		HttpURLConnection connection = null;
		try {
			URL postUrl = new URL(url);
			// 打开连接
			connection = (HttpURLConnection) postUrl.openConnection();
			// 设置是否向connection输出，因为这个是post请求，参数要放在
			// http正文内，因此需要设为true
			connection.setDoOutput(true);
			// Read from the connection. Default is true.
			connection.setDoInput(true);
			// 默认是 GET方式
			connection.setRequestMethod("POST");
			// Post 请求不能使用缓存
			connection.setUseCaches(false);
			//设置本次连接是否自动重定向
			connection.setInstanceFollowRedirects(true);
			// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
			// 意思是正文是urlencoded编码过的form参数
			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			// 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
			// 要注意的是connection.getOutputStream会隐含的进行connect。
			connection.connect();
			DataOutputStream out = new DataOutputStream(connection
					.getOutputStream());
			// DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
			out.writeBytes(param);
			//流用完记得关
			out.flush();
			out.close();
			
			//读取URL的响应
			br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			StringBuilder buf = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				buf.append(line);
			}
			/*
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			*/
			
			buf = new StringBuilder(buf.toString().replace("null", ""));
			//System.out.println(buf);
			jsObj = new JSONObject(buf.toString());
			
			connect = true;
		} catch (IOException e) {
			connect = false;
		} finally {
			if(br != null) {
				try {
					br.close();
					connection.disconnect();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return jsObj;
	}
}