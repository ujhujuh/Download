package download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author fantasy
 * @date 2018/11/2
 * @time 16:58
 */
public class TestDownload {

    private static final Logger logger = LoggerFactory.getLogger(TestDownload.class);

    public static void main(String[] args) throws InterruptedException {
        String remotePath = "http://zdzt-common-uat.oss-cn-shenzhen.aliyuncs.com/0302A201-1541149236511.xlsx";
        String targetPath = "/Users/fantasy/Desktop/test.xlsx";
        Integer threadNum = 5;

        CountDownLatch countDownLatch = new CountDownLatch(threadNum + 1);
        DownloadDto downloadDto = new DownloadDto(remotePath, targetPath, threadNum, countDownLatch);
        logger.info("开始下载......");
        downloadDto.download();

        // 查看下载进度
        new Thread(() -> {
            while (downloadDto.getDownloadRate() != 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            countDownLatch.countDown();
        }).start();

        countDownLatch.await();
        logger.info("下载完成......");
    }
}
