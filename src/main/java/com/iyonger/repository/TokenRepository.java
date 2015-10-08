package com.iyonger.repository;

import com.iyonger.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by fuyong on 10/3/15.
 */
public interface TokenRepository extends JpaRepository<Token,Long>{
	public List<Token> findByUsed(boolean use);


	@Modifying
	@Query("update Token t set t.used=?2 where t.id=?1")
	public int updateUsedStatus(Long id,boolean used);
}
