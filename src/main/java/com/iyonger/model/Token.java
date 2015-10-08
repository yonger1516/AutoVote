package com.iyonger.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by fuyong on 10/3/15.
 */

@Entity
@Table(name = "t_token")
public class Token extends BaseModel<Token>{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;
	long timestamp;
	String sign;

	boolean used;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}


	public String toString(){
		return "id:"+id+",timestamp:"+timestamp+",sign:"+sign;
	}
}
