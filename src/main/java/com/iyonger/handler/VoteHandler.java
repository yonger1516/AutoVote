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

	@Autowired
	public VoteHandler(TokenRepository tokenRepository, ProxyRepository proxyRepository, SuccessRepository repository) {
		this.tokenRepository = tokenRepository;
		this.successRepository = repository;
		this.proxyRepository = proxyRepository;
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
							                       Proxy proxy = proxyQueue.poll();
							                       if (null != proxy) {
								                       try {
									                       logger.info("Get an available proxy host {}", proxy);
									                       int max_unavailable = 10;
									                       for (int j = 0; j < 10; j++) {
										                       HttpResponse response = openTokenPage(proxy, httpclient);
										                       if (null != response) {
											                       Token token = parseAndFlush(response);
											                       response = sendVotePost(token, proxy, httpclient);


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

										                       EntityUtils.consume(response.getEntity());
									                       }
									                       if (max_unavailable <= 1) {
										                       proxy.setAvailable(false);
										                       logger.info("{} not available", proxy);
									                       }
								                       } catch (Exception e) {

								                       } finally {
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

	public HttpResponse openTokenPage(Proxy proxy, CloseableHttpAsyncClient httpClient) {
		logger.debug("Send request to get token...");
		HttpResponse resp = null;

		try {
			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
			RequestConfig config = RequestConfig.custom()
					.setProxy(proxyHost).setConnectTimeout(connection_timeout).setSocketTimeout(read_timeout)
					.build();
			HttpGet request = new HttpGet(target);
			request.setConfig(config);

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

		} finally {

		}
		return resp;
	}

	public HttpResponse sendVotePost(Token token, Proxy proxy, CloseableHttpAsyncClient httpClient) {

		logger.debug("Send vote request with {} ", token);

		HttpResponse resp = null;

		try {

			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
			RequestConfig config = RequestConfig.custom()
					.setProxy(proxyHost).setConnectTimeout(connection_timeout).setSocketTimeout(read_timeout)
					.build();

			HttpPost httpPost = new HttpPost(target2);
			httpPost.setConfig(config);

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
