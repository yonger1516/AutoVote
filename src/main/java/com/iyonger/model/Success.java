package com.iyonger.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by fuyong on 10/3/15.
 */
@Entity
@Table(name="t_success")
public class Success {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;

	Date date;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
