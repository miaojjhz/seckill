package com.miaojj.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.miaojj.entity.SuccessKilled;

/**
 * 配置Spring和junit整合，Junit启动时加载springIod容器
 * spring-test,junit
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
	//注入Dap实现类
	@Resource
	private SuccessKilledDao successKilledDao;
	
	@Test
	public void testInsertSuccessKilled() throws Exception{
		/*
		 * 第一次：insertCount=1
		 * 第二次：insertCount=0
		 */
		long id = 1001L;
		long phone = 13502181181L;
		int insertCount = successKilledDao.insertSuccessKilled(id, phone);
		System.out.println("insertCount="+ insertCount);
	}

	@Test
	public void testQueryByIdWithSeckill() throws Exception{
		long id = 1001L;
		long phone = 13502181181L;
		SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id, phone);
		System.out.println(successKilled);
		System.out.println(successKilled.getSeckill());
		/*
		 * SuccessKilled [seckillId=1001, userPhone=13502181181, state=0, 
		 * createTime=Sun May 07 19:48:42 CST 2017]
          Seckill [seckillId=1001, name=800元秒杀ipad, number=200, 
          startTime=null, endTime=Sun May 01 00:00:00 CST 2016, createTime=Sun Apr 09 18:10:38 CST 2017]
		 */
	}
}
