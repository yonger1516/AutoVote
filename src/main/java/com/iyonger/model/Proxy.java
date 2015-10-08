package com.iyonger.model;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * Created by fuyong on 10/3/15.
 */

@Entity
@Table(name = "t_proxy")
public class Proxy extends BaseModel<Proxy> {
	@Id
	String ip;

	Integer port;

	boolean available;

	@Column(name = "last_modify_time")
	Date lastModifyTime;

	public Date getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Date lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}


	@Override
	public boolean equals(Object other) {
		if (((Proxy) other).getIp().equals(this.ip)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString(){
		return "Ip:"+ip+",port"+Integer.toString(port)+",available:"+available+",last modify time:"+lastModifyTime;
	}

}
