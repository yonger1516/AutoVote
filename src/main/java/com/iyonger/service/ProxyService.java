package com.iyonger.service;

import com.iyonger.model.Proxy;
import com.iyonger.repository.ProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by fuyong on 10/4/15.
 */
@Service
public class ProxyService {
	ProxyRepository proxyRepository;

	@Autowired
	public ProxyService(ProxyRepository proxyRepository){
		this.proxyRepository=proxyRepository;
	}

	public List<Proxy> getAllAvailable(){
		return proxyRepository.findAllAvailableProxies(true);
	}

	public List<Proxy> findByAvailable(boolean available){
		return proxyRepository.findByAvailable(available);
	}

	public void save(Proxy proxy){
		Proxy exist=proxyRepository.findByIp(proxy.getIp());
		if (null!=exist){
			proxy=exist.merge(proxy);
		}
		proxyRepository.saveAndFlush(proxy);
	}




}
