package com.miaojj.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.miaojj.dao.SeckillDao;
import com.miaojj.dao.SuccessKilledDao;
import com.miaojj.dao.cache.RedisDao;
import com.miaojj.dto.Exposer;
import com.miaojj.dto.SeckillExecution;
import com.miaojj.entity.Seckill;
import com.miaojj.entity.SuccessKilled;
import com.miaojj.enums.SeckillStatEnum;
import com.miaojj.exception.RepeatKillException;
import com.miaojj.exception.SeckillCloseException;
import com.miaojj.exception.SeckillException;
import com.miaojj.service.SeckillService;

@Service
public class SeckillServiceImpl implements SeckillService{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//注入Service依赖
	@Autowired
	private SeckillDao seckillDao;
	
	@Autowired
	private SuccessKilledDao successKilledDao;
	
	@Autowired
	private RedisDao redisDao;
	
	private final String slat = "jaksjf&;lkjasfdklajsfklj";
	
	@Override
	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	/**
	 * 使用注解事务控制的优点： 
	 * 1.开发团队达成一致约定，明确标注事务方法的编程风格。
	 * 2：保证事务方法的执行时间尽可能断，不要穿插其他网络操作，RPC/HTTP 请求/
	 *    如果还是需要，尽可能剥离到事务方法外部。
	 * 3：不是所有的方法都需要事务，如只有一条修改操作，制度操作不需要事务控制， 可以去看行级锁。
	 */
	@Override
	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	@Override
	public Exposer exposerSeckillUrl(long seckillId) {
		
		//优化点：缓存优化 超时的基础上维护一致性
		//1 访问redis
		Seckill seckill = redisDao.getSeckill(seckillId);
		if(seckill == null){
			//2.访问数据库
			seckill = getById(seckillId);
			if(seckill == null){
				return new Exposer(false,seckillId);
			}else{
				//3.放入redis
				redisDao.putSeckill(seckill);
			}
		}
		
//		Seckill seckill = seckillDao.queryById(seckillId);
//		if(seckill == null){
//			return new Exposer(false,seckillId);
//		}
		
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		//系统当前时间
		Date nowTime = new Date();
		if(nowTime.getTime() < startTime.getTime()
				|| nowTime.getTime()>endTime.getTime()){
			return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
		}
		String md5 = getMD5(seckillId);
		return new Exposer(true,md5,seckillId);
	}
	
	private String getMD5(long seckillId){
		String base = seckillId + "/" + slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	@Override
	@Transactional
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		if(md5 == null || !md5.equals(getMD5(seckillId))){
			throw new SeckillException("seckill data rewrite");
		}
		//执行秒杀逻辑：减库存+记录购买行为
		Date nowTime = new Date();
		
		try {
			
//			//减库存
//			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
//			if(updateCount <= 0){
//				//没有更新到记录，秒杀结束
//				throw new SeckillCloseException("seckill is closed");
//			}else{
//				//记录购买行为
//				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
//				//唯一：seckillId,userPhone
//				if(insertCount <= 0){
//					//重复秒杀
//					throw new RepeatKillException("seckill repeated");
//				}else{
//					//秒杀成功
//					SuccessKilled successkilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
//					return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successkilled);	
//				}
//			}
			
			//记录购买行为
            int inserCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (inserCount <= 0) {
                //重复秒杀
                throw new RepeatKillException(SeckillStatEnum.REPEAT_KILL.getStateInfo());
            } else {
                //减库存  热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //rollback 没有更新库存记录，说明秒杀结束
                    throw new SeckillCloseException(SeckillStatEnum.END.getStateInfo());
                } else {
                    //秒杀成功  commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
			
			
		}catch(SeckillCloseException e1){
			throw e1;
		}catch(RepeatKillException e2){
			throw e2;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			//所以编译期异常转化为运行期异常
			throw new SeckillException("seckill inner error:"+e.getMessage());
		}

	}

	@Override
	public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
		if (md5==null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException(SeckillStatEnum.DATA_REWRITE.getStateInfo());
        }

        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        //执行存储过程 result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);
            } else {
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
	}

}
