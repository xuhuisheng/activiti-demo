package com.mossle.bpm.doc.timer;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.DeleteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * 定时器事件, 对定时器进行测试
 * 
 * <pre>
 * ------------------------------------------ ISO 8601--------------------------------------------------------------------------------------
 *  ISO 8601:
 *  		国际标准化组织的国际标准ISO 8601是日期和时间的表示方法,，全称为《数据存储和交换形式·信息交换·日期和时间的表示方法》
 *  UTC:
 *  		协调世界时，又称世界统一时间，世界标准时间，国际协调时间，简称UTC
 *  日期表示：
 *  		年由4位数组成，以公历公元1年为0001年，以公元前1年为0000年，公元前2年为-0001年
 *  日历日期：
 *  		年为4位数，月为2位数，月中的日为2位数，例如2013年8月3日可写成2013-08-03或20130803。
 *  		可以将一年内的天数直接表示
 *  		平年365天，闰年366天。如2013年8月3日可以表示为2004-212或2004212	
 * 日历星期和日表示法(W)：
 * 			可以用2位数表示年内第几个日历星期，再加上一位数表示日历星期内第几天，但日历星期前要加上一个大写字母W，
 * 	 		如2013年8月3日可写成2013-W32-3或2013W323。
 * 日的时间表示法(Z):
 * 			小时、分和秒都用2位数表示，对UTC时间最后加一个大写字母Z，其他时区用实际时间加时差表示
 * 			如UTC时间下午2点30分5秒表示为14:30:05Z或143005Z
 * 			当时的北京时间表示为22:30:05+08:00或223005+0800，也可以简化成223005+08。
 * 日期和时间的组合表示法(T)
 * 			合并表示时，要在时间前面加一大写字母T
 * 			如要表示北京时间2013年8月3日下午5点30分8秒，
 * 			可以写成2013-08-03T17:30:08+08:00或20130803T093008+08。
 * 时间段表示法(P)
 * 			如果要表示某一作为一段时间的时间期间，前面加一大写字母P
 * 			但时间段后都要加上相应的代表时间的大写字母。如在一年三个月五天六小时七分三十秒内，可以写成P1Y3M5DT6H7M30S。
 * 重复时间表示法(R)
 * 			前面加上一大写字母R，如要从2013年8月3日北京时间下午1点起重复半年零5天3小时，要重复3次，
 * 			可以表示为R3/20130803T130000+08/P0Y6M5DT3H0M0S。
 * 
 *  ------------------------------------------ Quartz中时间表达式-->corn表达式 --------------------------------------------------------------------------------------
 *  时间格式: <!-- s m h d m w(?) y(?) -->,   分别对应: 秒>分>小时>日>月>周>年,  
 *   <value>0 59 23 * * ?</value>: 如下为每天23:59:00开始执行
 *   <value>0 1,2,3 11,12 * * ? </value>: 每天11:01,11:02,11:03; 12:01,12:02,12:03分执行任务
 *   Cron表达式的时间字段除允许设置数值外，还可使用一些特殊的字符，提供列表、范围、通配符等功能，细说如下：
 * 	星号(*)：
 * 			可用在所有字段中，表示对应时间域的每一个时刻，例如，*在分钟字段时，表示“每分钟”。
 * 	问号(?):
 * 			该字符只在日期和星期字段中使用，它通常指定为“无意义的值”，相当于点位符。
 * 	减号(-):
 * 			表达一个范围，如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12。
 * 	逗号(,):
 * 			表达一个列表值，如在星期字段中使用“MON,WED,FRI”，则表示星期一，星期三和星期五。
 * 	斜杠(/)：
 * 			x/y表达一个等步长序列，x为起始值，y为增量步长值.如在分钟字段中使用0/15，则表示为0,15,30和45秒，而5/15在分钟字段中表示5,20,35,50，你也可以使用(星号/y，它等同于0/y。
 * </pre>
 * 
 * @author LuZhao
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/applicationContext*.xml" })
public class StartTimerEventTest {

	@Resource
	protected RepositoryService repositoryService;
	@Resource
	protected RuntimeService runtimeService;
	@Resource
	protected TaskService taskService;
	@Resource
	protected ManagementService managementService;
	@Resource
	protected ProcessEngineConfigurationImpl processEngineConfiguration;
	
	protected Deployment deployment;
	
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * FIXME 死循环 需要手动删除
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCycleDateStartTimerEvent() throws Exception {
		// ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventExample");
		// assertNotNull(processInstance);

		ClockUtil.setCurrentTime(new Date());
		logger.info("------>当前时间：{}", ClockUtil.getCurrentTime());
		JobQuery jobQuery = managementService.createJobQuery();
		assertEquals(1, jobQuery.count());

		final ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample");

		moveByMinutes(5);

		waitForJobExecutorOnCondition(10000, 500, new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return 1 == processInstanceQuery.count();
			}
		});
		assertEquals(1, jobQuery.count());
		moveByMinutes(5);
		waitForJobExecutorOnCondition(10000, 500, new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return 2 == processInstanceQuery.count();
			}
		});
		assertEquals(1, jobQuery.count());
		// 只能供手动删除
		cleanDB();
	}

	private void cleanDB() {
		String jobId = managementService.createJobQuery().singleResult().getId();
		managementService.executeCommand(new DeleteJobsCmd(jobId));
	}

	private void moveByMinutes(int minutes) {
		ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + (minutes * 60 * 1000) + 5000));
	}

	public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
		
		JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();

		jobExecutor.start();

		try {
			Timer timer = new Timer();
			InteruptTask task = new InteruptTask(Thread.currentThread());
			timer.schedule(task, maxMillisToWait);
			boolean conditionIsViolated = true;
			try {
				while (conditionIsViolated) {
					Thread.sleep(intervalMillis);
					conditionIsViolated = !condition.call();
				}
			} catch (InterruptedException e) {
			} catch (Exception e) {
				throw new ActivitiException("Exception while waiting on condition: " + e.getMessage(), e);
			} finally {
				timer.cancel();
			}
			if (conditionIsViolated) {
				throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
			}
		} finally {
			jobExecutor.shutdown();
		}
	}

	private static class InteruptTask extends TimerTask {
		protected boolean timeLimitExceeded = false;
		protected Thread thread;

		public InteruptTask(Thread thread) {
			this.thread = thread;
		}

		public boolean isTimeLimitExceeded() {
			return timeLimitExceeded;
		}

		public void run() {
			timeLimitExceeded = true;
			thread.interrupt();
		}
	}

	@Before
	public void setUp() throws Exception {
		deployment = repositoryService.createDeployment() //
				.addClasspathResource("diagrams/bpmn/event/timer/start/CycleDateStartTimerEvent.bpmn") //
				.deploy();
	}

	@After
	public void tearDown() throws Exception {
		repositoryService.deleteDeployment(deployment.getId(), true);
	}
}
