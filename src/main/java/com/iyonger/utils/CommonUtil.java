package com.iyonger.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fuyong on 10/4/15.
 */
public class CommonUtil {

	private static final SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");

	public static void delay(long i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String getToday() {
		return dft.format(new Date()) + " 00:00:00";
	}

	public static Date getTodayDate() {
		try {
			return dft.parse(dft.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	/*public static void main(String[] args) {
		System.out.println(getToday());
		System.out.println(getTodayDate());
	}*/
}
