package com.limouren;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Http 工具类
 * 支持https，支持代理
 * 
 * @author lixiang
 *
 */
public class HttpsClientUtil {
    private Logger logger = Logger.getLogger(HttpsClientUtil.class);
    private Map<String, String> baseheader = new HashMap<>();
    private boolean isProxy = false;
    private String proxyIp;
    private int proxyPort;
    private List<Cookie> cookies;
    
    public void setBaseheader(Map<String, String> baseheader) {
    	this.baseheader = baseheader;
	}
    
    public void setProxy(boolean isProxy, String proxyIp, int proxyPort) {
    	this.isProxy = isProxy;
    	this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
	}

    public String doGet(String url, int times, int sleepSecond, boolean iscookie) throws InterruptedException {
        int errTimes = 0;
        String result = "";
        while(times-- > 0) {
            result = doGet(url, iscookie);
            if(result.length() != 0)
                break;
            Thread.sleep(sleepSecond);
            logger.info("失败重试第 " + ++ errTimes + " 次");
        }
        if(result.length() == 0)
            logger.info(url + "  失败了，共重试了 " + errTimes + " 次");
        logger.info(result);
        return result;
    }

    public String doGet(String url, int times, int sleepSecond) throws InterruptedException {
        return doGet(url, times, sleepSecond, false);
    }

    public String doGet(String url, boolean iscookie) {
        logger.info("GET请求地址：" + url);
        CloseableHttpClient client = null;
        String resultString = "";
        CloseableHttpResponse response = null;
        HttpGet httpGet = null;
        try {
        	BasicCookieStore cookieStore = new BasicCookieStore();

            //创建自定义的httpclient对象
        	client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            //创建get方式请求对象
            httpGet = new HttpGet(url);

            // 设置超时时间
            httpGet.setConfig(setTimeOutConfig());

            //设置header信息
            for (Map.Entry<String, String> entry : baseheader.entrySet()) {
                if(!iscookie && "cookie".equals(entry.getKey()))
                    continue;
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }

            // 执行请求
            response = client.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
            
            // 获取cookies信息
            cookies = cookieStore.getCookies();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpGet != null) {
                    httpGet.abort();
                }
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
    
    public String doPost(String url, Map<String, String> parmsMap, boolean iscookie) {
        logger.info("POST请求地址：" + url);
        CloseableHttpClient client = null;
        String resultString = "";
        CloseableHttpResponse response = null;
        HttpPost httpPost = null;
        try {
        	BasicCookieStore cookieStore = new BasicCookieStore();

            //创建自定义的httpclient对象
        	client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            //创建get方式请求对象
            httpPost = new HttpPost(url);

            // 设置超时时间
            httpPost.setConfig(setTimeOutConfig());
            
            // 设置参数列表
            if(parmsMap != null && parmsMap.size() > 0) {
            	List<NameValuePair> parmsList = new ArrayList<NameValuePair>();
            	for (String key : parmsMap.keySet()) {
            		parmsList.add(new BasicNameValuePair(key, parmsMap.get(key)));
				}
            	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parmsList);
            	httpPost.setEntity(entity);
            }

            //设置header信息
            for (Map.Entry<String, String> entry : baseheader.entrySet()) {
                if(!iscookie && "cookie".equals(entry.getKey()))
                    continue;
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            // 执行请求
            response = client.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
            
            // 获取cookies信息
            cookies = cookieStore.getCookies();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpPost != null) {
                	httpPost.abort();
                }
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
    
    
    public List<Cookie> getCookies() {
		return cookies;
	}

    public String downloadBitFile(String url) {
        String downloadDir = "download/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/";
        File saveDir = new File(downloadDir);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        String downfileFile = url.substring(url.lastIndexOf("/") + 1);
        String extName = downfileFile.substring(0, downfileFile.indexOf("?")).substring(downfileFile.indexOf("."));
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        HttpGet httpGet = null;
        try {
        	BasicCookieStore cookieStore = new BasicCookieStore();
        	
            //创建自定义的httpclient对象
        	client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            //创建get方式请求对象
            httpGet = new HttpGet(url);

            // 设置超时时间
            httpGet.setConfig(setTimeOutConfig());

            //设置header信息
            for (Map.Entry<String, String> entry : baseheader.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }

            //执行请求操作，并拿到结果（同步阻塞）
            response = client.execute(httpGet);

            // 下载图片
            if (response.getStatusLine().getStatusCode() == 200) {
                //得到实体
                HttpEntity entity = response.getEntity();

                byte[] data = EntityUtils.toByteArray(entity);

                //图片存入磁盘
                downfileFile = FileUtils.getFileMd5(data) + extName;
                FileOutputStream fos = new FileOutputStream(downloadDir + downfileFile);
                fos.write(data);
                fos.close();
            }
            // 获取cookies信息
            cookies = cookieStore.getCookies();
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpGet != null) {
                    httpGet.abort();
                }
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(downloadDir + downfileFile).getAbsolutePath();
    }

    public SSLContext createIgnoreVerifySSL() {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSLv3");

            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sc.init(null, new TrustManager[]{trustManager}, null);
        } catch (Exception e) {}
        return sc;
    }

    /**
     * 生成 Http 客户端
     */
    public CloseableHttpClient getHttpClient(){
        SSLContext sslcontext = createIgnoreVerifySSL();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        //创建自定义的httpclient对象
        CloseableHttpClient client;
        if(isProxy) {
            HttpHost proxy = new HttpHost(proxyIp, proxyPort);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            client = HttpClients.custom().setRoutePlanner(routePlanner).setConnectionManager(connManager).build();
        } else {
            client = HttpClients.custom().setConnectionManager(connManager).build();
        }

        return client;
    }

    /**
     * 设置 连接超时、 请求超时 、 读取超时  毫秒
     * @return
     */
    private RequestConfig setTimeOutConfig(){
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000).setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000).build();
        return requestConfig;
    }

}
