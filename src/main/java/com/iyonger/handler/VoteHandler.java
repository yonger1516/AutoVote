package com.iyonger.handler;

import com.iyonger.model.Proxy;
import com.iyonger.model.Success;
import com.iyonger.model.Token;
import com.iyonger.repository.ProxyRepository;
import com.iyonger.repository.SuccessRepository;
import com.iyonger.repository.TokenRepository;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.iyonger.utils.CommonUtil.*;

/**
 * Created by fuyong on 10/4/15.
 */

@Component
public class VoteHandler {
	private static final Logger logger = LoggerFactory.getLogger(VoteHandler.class.getSimpleName());
	private static final String target = "http://adonotify.meirixue.com/jinpai/wap/index2.php?no=4029&from=singlemessage&isappinstalled=0";

	private static final String target2 = "http://adonotify.meirixue.com/jinpai/api.php";
	private static final String ids = "4029";
	static final int connection_timeout = 60 * 1000;
	static final int read_timeout = 60 * 1000;

	static final int read_proxy_internal = 10 * 60 * 1000;
	static final int max_threads = 100;

	String reg_ts = "var timesp = '([0-9]+)';";
	String reg_sign = "var sign = '(.*)';";

	Pattern pattern_ts = Pattern.compile(reg_ts);
	Pattern pattern_sign = Pattern.compile(reg_sign);

	TokenRepository tokenRepository;
	SuccessRepository successRepository;
	ProxyRepository proxyRepository;

	CloseableHttpAsyncClient httpclient;

	Queue<Proxy> proxyQueue = new ConcurrentLinkedQueue<Proxy>();

	private static List<String> agents=new ArrayList<String>();

	private static final String User_Agent="Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)";

	@Autowired
	public VoteHandler(TokenRepository tokenRepository, ProxyRepository proxyRepository, SuccessRepository repository) {
		this.tokenRepository = tokenRepository;
		this.successRepository = repository;
		this.proxyRepository = proxyRepository;


		agents.add("Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30");
		agents.add("Mozilla/5.0 (Windows NT 5.1; rv:5.0) Gecko/20100101 Firefox/5.0");
		agents.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET4.0E; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C)");
		agents.add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET4.0E; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C)");
		agents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727) ");
		agents.add("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)");
		agents.add("Opera/9.80 (Windows NT 5.1; U; zh-cn) Presto/2.9.168 Version/11.50");
		agents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		agents.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET4.0E; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C)");
		agents.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1");
		agents.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; ) AppleWebKit/534.12 (KHTML, like Gecko) Maxthon/3.0 Safari/534.12");
		agents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; TheWorld)");
		agents.add("Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_2 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5");
		agents.add("MQQBrowser/25 (Linux; U; 2.3.3; zh-cn; HTC Desire S Build/GRI40;480*800)\n" +
				"Mozilla/5.0 (Linux; U; Android 2.3.3; zh-cn; HTC_DesireS_S510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");

		agents.add("Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A5313e Safari/7534.48.3");
	}

	public void inQueueSchedule() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {

				logger.info("Get available proxies for every {} second ...", read_proxy_internal / 1000);
				List<Proxy> proxies = proxyRepository.findAllAvailableProxies(true, getTodayDate());

				for (Proxy proxy : proxies) {
					if (!proxyQueue.contains(proxy)) {
						proxyQueue.offer(proxy);
					}
				}
			}
		}, 0, read_proxy_internal);
	}

	public void start() throws Exception {
		logger.info("Vote handler started ...");

		inQueueSchedule();

		delay(10);
		final ExecutorService executorService = Executors.newFixedThreadPool(max_threads);

		ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
		PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
		cm.setMaxTotal(100);
		httpclient = HttpAsyncClients.custom().setConnectionManager(cm).build();
		httpclient.start();


		int i = max_threads;
		while (i > 0) {
			executorService.submit(new Runnable() {
				                       @Override
				                       public void run() {

					                       //int max_try = 1;
					                       while (true) {
						                       try {
							                       logger.info("Vote handler running and queue size {}", proxyQueue.size());
							                       int agent_num=proxyQueue.size()%14;
							                       HttpResponse response=null;
							                       Proxy proxy = proxyQueue.poll();
							                       if (null != proxy) {
								                       try {
									                       logger.info("Get an available proxy host {}", proxy);
									                       int max_unavailable = 10;
									                       for (int j = 0; j < 10; j++) {
										                      response = openTokenPage(proxy, httpclient,agent_num);
										                       if (null != response) {
											                       Token token = parseAndFlush(response);
											                       response = sendVotePost(token, proxy, httpclient,agent_num);


											                       if (EntityUtils.toString(response.getEntity()).contains("\"code\":200")) {
												                       Success success = new Success();
												                       success.setDate(new Date());
												                       successRepository.saveAndFlush(success);

												                       logger.info("Vote success!!!");
											                       }

										                       } else {
											                       logger.debug("Response is not available.");
											                       max_unavailable--;

										                       }

									                       }
									                       if (max_unavailable <= 1) {
										                       proxy.setAvailable(false);
										                       logger.info("{} not available", proxy);
									                       }
								                       } catch (Exception e) {

								                       } finally {
									                       if (null!=response){
										                       EntityUtils.consume(response.getEntity());
									                       }
									                       proxy.setLastModifyTime(new Date());
									                       proxyRepository.saveAndFlush(proxy);
								                       }

							                       } else {
								                       delay(300);
								                       /*if (max_try++ < 10) {
									                       delay(60);

								                       } else {
									                       break;
								                       }*/

							                       }
						                       } catch (Exception e) {
							                       // e.printStackTrace();
						                       }
					                       }

					                       //logger.info("Vote handler thread done after 10 times trying.");

				                       }

			                       }
			);
			i--;
		}
	}

	private Token parseAndFlush(HttpResponse response) {
		String res = null;
		Token token = new Token();

		try {
			res = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Matcher matcher = pattern_ts.matcher(res);
		if (matcher.find()) {
			token.setTimestamp(Long.parseLong(matcher.group(1)));

		}

		Matcher matcher2 = pattern_sign.matcher(res);
		if (matcher2.find()) {
			token.setSign(matcher2.group(1));
		}
		token.setUsed(false);

		logger.debug("flush token:{} to database.", token);
		//tokenRepository.saveAndFlush(token);

		return token;
	}

	public HttpResponse openTokenPage(Proxy proxy, CloseableHttpAsyncClient httpClient,int agentNum) {
		logger.debug("Send request to get token...");
		HttpResponse resp = null;

		try {
			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
			RequestConfig config = RequestConfig.custom()
					.setProxy(proxyHost).setConnectTimeout(connection_timeout).setSocketTimeout(read_timeout)
					.build();
			HttpGet request = new HttpGet(target);
			request.setConfig(config);

			request.addHeader("User-Agent", agents.get(agentNum));
			//logger.info(request.toString());
			Future<HttpResponse> future = httpClient.execute(request, null);
			HttpResponse response = future.get();
			if (null == response || response.getStatusLine().getStatusCode() != 200) {
				//proxy.setAvailable(false);
				resp = null;
			} else {
				//proxy.setAvailable(true);
				resp = response;
			}

		} catch (InterruptedException e) {

		} catch (ExecutionException e) {

		} catch (Exception e){
		}
		finally {

		}
		return resp;
	}

	public HttpResponse sendVotePost(Token token, Proxy proxy, CloseableHttpAsyncClient httpClient,int agentNum) {

		logger.debug("Send vote request with {} ", token);

		HttpResponse resp = null;

		try {

			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
			RequestConfig config = RequestConfig.custom()
					.setProxy(proxyHost).setConnectTimeout(connection_timeout).setSocketTimeout(read_timeout)
					.build();

			HttpPost httpPost = new HttpPost(target2);
			httpPost.setConfig(config);
			httpPost.addHeader("User-Agent", agents.get(agentNum));
			httpPost.addHeader("Referer","http://adonotify.meirixue.com/jinpai/wap/index2.php?no=4029");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("ids", ids));
			nvps.add(new BasicNameValuePair("timesp", Long.toString(token.getTimestamp())));
			nvps.add(new BasicNameValuePair("sign", token.getSign()));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

			Future<HttpResponse> future = httpClient.execute(httpPost, null);
			resp = future.get();


		} catch (InterruptedException e) {

		} catch (ExecutionException e) {

		} catch (UnsupportedEncodingException e) {

		} finally {

		}

		logger.debug("thread {} send vote request done.", Thread.currentThread().getId());
		return resp;
	}


}
