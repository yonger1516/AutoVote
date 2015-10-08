package com.iyonger.repository;

import com.iyonger.model.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Created by fuyong on 10/3/15.
 */
@Transactional
public interface ProxyRepository extends JpaRepository<Proxy,Long> {
	public List<Proxy> findByAvailable(boolean available);

	public Proxy findByIp(String ip);

	@Query("select p from Proxy p where p.available=?1 and p.lastModifyTime<?2")
	public List<Proxy> findAllAvailableProxies(boolean available,Date date);
}
