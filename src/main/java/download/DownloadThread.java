package download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * @author fantasy
 * @date 2018/11/2
 * @time 16:57
 */
public class DownloadThread extends Thread{

    /**
     * 计数器类对象实例
     */
    private CountDownLatch countDownLatch;

    private String remotePath;
    private RandomAccessFile randomAccessFile;
    private Integer startPosition;
    private Integer size;
    private Integer hasRead;

    public DownloadThread(String remotePath, RandomAccessFile file, Integer startPosition, Integer size, CountDownLatch countDownLatch) {
        this.remotePath = remotePath;
        this.randomAccessFile = file;
        this.startPosition = startPosition;
        this.size = size;
        this.countDownLatch = countDownLatch;
        this.hasRead = 0;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(remotePath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000); // 设置超时时间为5秒
            conn.setRequestMethod("GET");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("accept", "*/*");
            InputStream inputStream = conn.getInputStream();
            inputStream.skip(startPosition);

            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hasRead += len;
                if (hasRead <= size) {
                    randomAccessFile.write(bytes, 0, len);
                } else {
                    randomAccessFile.write(bytes, 0, size - (hasRead - len));
                    break;
                }
            }
            hasRead = hasRead > size ? size : hasRead;
            randomAccessFile.close();
            countDownLatch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getHasRead() {
        return hasRead;
    }
}
