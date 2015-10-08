package com.iyonger.handler;

import com.iyonger.model.Proxy;
import com.iyonger.repository.ProxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by fuyong on 10/3/15.
 */
@Component
public class ImportProxy extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(ImportProxy.class.getSimpleName());
	private static final String path = "ipList.txt";
	private static final String split_dot = ":";


	ProxyRepository proxyRepository;

	@Autowired
	public ImportProxy(ProxyRepository proxyRepository) {
		this.proxyRepository = proxyRepository;
	}

	public boolean importFromText(String path, String split) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(split_dot);
				String ip = arr[0];
				String port = arr[1];

				if (proxyRepository.findByIp(ip) == null) {
					Proxy proxy = new Proxy();
					proxy.setIp(ip);
					proxy.setPort(Integer.parseInt(port));
					proxy.setAvailable(true);
					SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
					proxy.setLastModifyTime(dft.parse("2015-10-03 10:00:00"));
					proxyRepository.saveAndFlush(proxy);
				}

			}

		} catch (FileNotFoundException e) {
			logger.error("File not found");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void run() {

		if (importFromText(path, split_dot)) {
			logger.info("import proxy from text successful.");
		} else {
			logger.info("import proxy from text failed.");
		}


	}

	public void start() {
		new Timer().schedule(this, 0, 60 * 60 * 1000);

		logger.info("Import proxy list to database...");
	}


}
