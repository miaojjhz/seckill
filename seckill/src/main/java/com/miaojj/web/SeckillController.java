package com.miaojj.web;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miaojj.dto.Exposer;
import com.miaojj.dto.SeckillExecution;
import com.miaojj.dto.SeckillResult;
import com.miaojj.entity.Seckill;
import com.miaojj.enums.SeckillStatEnum;
import com.miaojj.exception.RepeatKillException;
import com.miaojj.exception.SeckillCloseException;
import com.miaojj.service.SeckillService;

@Controller
//@RequestMapping("/seckill") //url:模块/资源/{}/细分
public class SeckillController {

	private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //list.jsp + model = ModelAndView
        //获取列表页

        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);

        return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {

        if (seckillId == null) {
            return "redirect:/seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);

        if (seckill == null) {
            return "forward:/seckill/list";
        }

        model.addAttribute("seckill", seckill);

        return "detail";

    }

    //ajax json
    @RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {

        SeckillResult<Exposer> result;

        try {
            Exposer exposer = seckillService.exposerSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            log.error(e.getMessage());
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }

        return result;
    }

    //    ajax json
    @RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId, @PathVariable("md5") String md5,
                                                   @CookieValue(value = "killPhone", required = false) Long killPhone) {

        if (killPhone == null) {
            return new SeckillResult<SeckillExecution>(false, SeckillStatEnum.NOT_LOGIN.getStateInfo());
        }

        try {
           // SeckillExecution execution = seckillService.executeSeckill(seckillId, killPhone, md5);
            
        	//存储过程调用
        	SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, killPhone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (RepeatKillException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);

        } catch (SeckillCloseException e2) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);

        } catch (Exception e) {
            log.error(e.getMessage());
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, execution);
        }

    }

    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult(true, now.getTime());
    }
}