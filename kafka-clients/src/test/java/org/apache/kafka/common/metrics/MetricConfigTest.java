package org.apache.kafka.common.metrics;

import static org.junit.Assert.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.*;
import java.util.concurrent.TimeUnit;

/**
 * MetricConfig Test.
 * @author 章云
 * @date 2020/1/16 9:29
 */
public class MetricConfigTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test MetricConfig Class Start...");
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test MetricConfig Class End...");
    }

    /**
     * 模拟kafka生产者初始化的MetricConfig
     */
    @Test
    public void testProducer() throws Exception {
        MetricConfig metricConfig = new MetricConfig().samples(2).timeWindow(30000, TimeUnit.MILLISECONDS);
        // 上下界测试
        assertNull(metricConfig.quota());
        Quota quota = Quota.upperBound(10.0);
        metricConfig.quota(quota);
        assertEquals(quota, metricConfig.quota());
        // 事件窗口
        assertEquals(metricConfig.eventWindow(), Long.MAX_VALUE);
        metricConfig.eventWindow(10000);
        assertEquals(metricConfig.eventWindow(), 10000);
        // 时间窗口
        assertEquals(metricConfig.timeWindowMs(), 30000);
        metricConfig.timeWindow(10000, TimeUnit.MILLISECONDS);
        assertEquals(metricConfig.timeWindowMs(), 10000);
        // samples
        assertEquals(metricConfig.samples(), 2);
        metricConfig.samples(1);
        assertEquals(metricConfig.samples(), 1);
        // 时间单位
        assertEquals(metricConfig.timeUnit(), TimeUnit.SECONDS);
        metricConfig.timeUnit(TimeUnit.MILLISECONDS);
        assertEquals(metricConfig.timeUnit(), TimeUnit.MILLISECONDS);
    }

}
