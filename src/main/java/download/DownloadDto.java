package download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;


/**
 * @author fantasy
 * @date 2018/11/2
 * @time 16:58
 */
public class DownloadDto {

    private static final Logger logger = LoggerFactory.getLogger(DownloadDto.class);

    private String remotePath;
    private String targetPath;
    private Integer threadNum;
    private DownloadThread[] threads;
    private Integer fileLength;
    private CountDownLatch countDownLatch;

    public DownloadDto(String remotePath, String targetPath, Integer threadNum, CountDownLatch countDownLatch) {
        this.remotePath = remotePath;
        this.targetPath = targetPath;
        this.threadNum = threadNum;
        this.countDownLatch = countDownLatch;
        threads = new DownloadThread[threadNum];
    }

    public void download() {
        try {
            URL url = new URL(remotePath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000); // 设置超时时间为5秒
            conn.setRequestMethod("GET");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("accept", "*/*");

            fileLength = conn.getContentLength();
            int size = (int) Math.ceil(fileLength.doubleValue() / threadNum);

            for (int i = 0; i < threadNum; i++) {
                int startPosition = size * i;
                RandomAccessFile randomAccessFile = new RandomAccessFile(new File(targetPath), "rw");
                randomAccessFile.seek(startPosition);
                DownloadThread thread = new DownloadThread(remotePath, randomAccessFile, startPosition, size, countDownLatch);
                threads[i] = thread;
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public double getDownloadRate() {
        Integer hasDownload = 0;
        for (int i = 0; i < threadNum; i++) {
            DownloadThread thread = threads[i];
            hasDownload += thread.getHasRead();
        }
        logger.info("总字节{},已下载{}, 已完成{}", fileLength, hasDownload, hasDownload.doubleValue()/fileLength * 100 + "/%");
        return hasDownload/fileLength;
    }
}
