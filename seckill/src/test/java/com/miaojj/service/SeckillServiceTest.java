package com.miaojj.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.miaojj.dto.Exposer;
import com.miaojj.dto.SeckillExecution;
import com.miaojj.entity.Seckill;
import com.miaojj.exception.RepeatKillException;
import com.miaojj.exception.SeckillCloseException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml" })
public class SeckillServiceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SeckillService seckillService;

	@Test
	public void testGetSeckillList() {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}", list);
	}

	@Test
	public void testGetById() {
		long id = 1000L;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill={}", seckill);

	}

	// 集成測試代码完整逻辑，注意可重复执行。
	@Test
	public void testSeckillLogic() {
		long id = 1001L;
		Exposer exposer = seckillService.exposerSeckillUrl(id);
		if (exposer.isExposed()) {
			logger.info("exposer={}" + exposer);
			long phone = 17876324329L;
			String md5 = exposer.getMd5();
			try {
				SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
				logger.info("result={}" + seckillExecution);
			} catch (RepeatKillException e) {
				logger.error(e.getMessage());
			} catch (SeckillCloseException e) {
				logger.error(e.getMessage());
			}
		} else {
			// 秒杀未开启
			logger.warn("exposer={}", exposer);
		}
	}

	@Test
	public void testExecuteSeckillProcedure() {
		long seckillId = 1000L;
		long phone = 17869573897L;

		Exposer exposer = seckillService.exposerSeckillUrl(seckillId);

		if (exposer.isExposed()) {
			String md5 = exposer.getMd5();
			SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
			System.out.println(execution.getStateInfo());
		}

	}

}
