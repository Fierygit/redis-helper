package com.pugwoo.wooutils;

import com.pugwoo.wooutils.redis.RedisHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@ContextConfiguration(locations = {"classpath:applicationContext-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class AutoIncrementIdTest {

	@Autowired
	private RedisHelper redisHelper;

	@Test
	public void test() throws Exception {
		final List<Long> ids = new Vector<Long>();

		long start = System.currentTimeMillis();
		List<Thread> threads = new ArrayList<Thread>();

		int THREAD = 100;
		int TIMES = 200;

		// redis最大连接数一般到900，受服务器打开文件数限制，需要修改系统配置: 最大连接数
		for(int t = 0; t < THREAD; t++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					for(int i = 0; i < TIMES; i++) {
						Long id = redisHelper.getAutoIncrementId("ORDER");
						//       System.out.println(id);
						if (id != null) { // null可能是网络原因导致的，不加入
							ids.add(id);
						}
					}
				}
			});
			thread.start();
			threads.add(thread);
		}

		for(Thread thread : threads) {
			thread.join();
		}

		long end = System.currentTimeMillis();
		System.out.println("cost:" + (end - start) + "ms");

		// 校验正确性
		Set<Long> set = new HashSet<Long>();
		boolean isDup = false;
		for(Long id : ids) {
			if(set.contains(id)) {
				isDup = true;
			} else {
				set.add(id);
			}
		}
		System.out.println(isDup ? "数据错误，有重复" : "数据正确");

		assert !isDup;
		assert ids.size() >= THREAD * TIMES - 3; // 最多允许3个失败

	}

}