package com.iyonger;

import com.iyonger.handler.VoteHandler;
import com.iyonger.handler.ImportProxy;
import com.iyonger.handler.VoteHandler2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.iyonger.utils.CommonUtil.*;
/**
 * Created by fuyong on 10/3/15.
 */
@SpringBootApplication
public class VoteMain implements CommandLineRunner {

	@Autowired
	ImportProxy importProxy;

	@Autowired
	VoteHandler voteHandler;

	@Autowired
	VoteHandler2 voteHandler2;

	public static void main(String[] args) {
		SpringApplication.run(VoteMain.class, args);

		System.out.println("Start Voting...");

	}


	@Override
	public void run(String... strings) throws Exception {
		importProxy.start();
		delay(5);
		voteHandler.start();
		/*delay(5);
		voteHandler.start();*/

	}
}
