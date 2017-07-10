package com.miaojj.service;

import java.util.List;

import com.miaojj.dto.Exposer;
import com.miaojj.dto.SeckillExecution;
import com.miaojj.entity.Seckill;
import com.miaojj.exception.RepeatKillException;
import com.miaojj.exception.SeckillCloseException;
import com.miaojj.exception.SeckillException;

/**
 *业务接口：站在“使用者”角度设计接口
 *三个方面：方法定义粒度，参数，返回类型（return类型/异常）
 */
public interface SeckillService {

	/*
	 * 查询所有秒杀记录
	 */
	List<Seckill> getSeckillList();
	
	/**
	 * 查询单个秒杀记录
	 * @param seckillId
	 * @return
	 */
	Seckill getById(long seckillId);
	
	/**
	 * 秒杀开启是输出秒杀接口地址，
	 * 否则输出系统时间和秒杀时间
	 * @param seckillId
	 * @return
	 */
	Exposer exposerSeckillUrl(long seckillId);
	
	/**
	 * 执行秒杀操作
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 */
	SeckillExecution executeSeckill(long seckillId,long userPhone,String md5) throws SeckillException
    , RepeatKillException, SeckillCloseException;
	
	/**
	 * 执行秒杀操作by存储过程
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 * @return
	 */
	 SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);
}
