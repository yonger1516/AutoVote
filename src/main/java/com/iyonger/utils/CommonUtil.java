package com.iyonger.utils;

/**
 * Created by fuyong on 10/4/15.
 */
public class CommonUtil {

	public static void delay(long i){
		try {
			Thread.sleep(i*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
