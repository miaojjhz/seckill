package com.miaojj.exception;

/**
 * 重复秒杀异常(运行期异常)
 * RuntimeException 不需要try/catch 而且Spring 的声明式事务只接收RuntimeException回滚策略.
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }

}
