package com.iyonger.handler;

import com.iyonger.model.Proxy;
import com.iyonger.model.Token;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by fuyong on 10/4/15.
 */

@Component
public class VoteHandler2 {
	private static final Logger logger = LoggerFactory.getLogger(VoteHandler2.class.getSimpleName());
	private static final String target = "http://adonotify.meirixue.com/jinpai/api.php";
	private static final String ids = "4029";
	/*Queue<Token> tokens = new LinkedList<Token>();
	TokenRepository tokenRepository;
	SuccessRepository successRepository;

	@Autowired
	public VoteHandler(TokenRepository tokenRepository, SuccessRepository successRepository) {
		this.tokenRepository = tokenRepository;
		this.successRepository = successRepository;
	}

	public void inQueueSchedule() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {

				logger.info("get available tokens for every 60 seconds...");
				List<Token> list = tokenRepository.findByUsed(false);

				for (Token token : list) {
					if (!tokens.contains(token)) {
						tokens.offer(token);
					}
				}
			}
		}, 1, 60 * 1000);
	}*/

	/*public void start() {

		logger.info("Vote thread has started...");
		inQueueSchedule();

		delay(5);
		final ExecutorService executorService = Executors.newFixedThreadPool(5);

		int i = 5;
		while (i > 0) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {

					while (true) {
						Token token = tokens.poll();
						if (null != token) {
							logger.info("Vote thread {} get an available token...", Thread.currentThread().getId());
							token.setUsed(true);
							tokenRepository.saveAndFlush(token);
							HttpResponse response = sendVotePost(token,new Proxy(),HttpAsyncClients.createDefault());

							try {
								if (EntityUtils.toString(response.getEntity()).contains("200")) {
									Success success = new Success();
									success.setDate(new Date());

									logger.info("Vote success!!!");
									successRepository.saveAndFlush(success);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				}
			});
			i--;
		}
	}*/

	public HttpResponse sendVotePost(Token token,Proxy proxy,CloseableHttpAsyncClient httpclient) {

		logger.info("Send vote request with {} and proxy host {}",token,proxy);

		HttpResponse resp = null;

		try {

			HttpHost proxyHost = new HttpHost(proxy.getIp(), proxy.getPort());
			RequestConfig config = RequestConfig.custom()
					.setProxy(proxyHost).setConnectTimeout(30*1000).setSocketTimeout(30*1000)
					.build();

			HttpPost httpPost = new HttpPost(target);
			httpPost.setConfig(config);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("ids", ids));
			nvps.add(new BasicNameValuePair("timesp", Long.toString(token.getTimestamp())));
			nvps.add(new BasicNameValuePair("sign", token.getSign()));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

			Future<HttpResponse> future = httpclient.execute(httpPost, null);
			resp = future.get();


		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {

		}
		return resp;
	}
}
