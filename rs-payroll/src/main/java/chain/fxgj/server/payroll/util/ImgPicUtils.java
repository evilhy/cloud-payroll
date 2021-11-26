package chain.fxgj.server.payroll.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 图片压缩Utils 压缩到自定内存大小
 *
 * @author worstEzreal
 * @version V1.1.0
 * @date 2018/3/12
 */
public class ImgPicUtils {

    private static Logger logger = LoggerFactory.getLogger(ImgPicUtils.class);

    public static void main(String[] args) throws Exception {
        String imgPath = "D:\\demo\\2.jpg";
        String goalPath = "D:\\demo\\dd2.jpg";
        compression(imgPath, goalPath);
    }

    /**
     * 文件压缩
     *
     * @param imgPath  原图片地址
     * @param goalPath 目标文件地址
     */
    public static void compression(String imgPath, String goalPath) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(new File(imgPath));
        long l = System.currentTimeMillis();
        bytes = ImgPicUtils.compressPicForScale(bytes, 90, "x");// 图片小于300kb
        System.out.println(System.currentTimeMillis() - l);
        FileUtils.writeByteArrayToFile(new File(goalPath), bytes);
    }

    /**
     * 根据指定大小压缩图片
     *
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @param imageId     影像编号
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize, String imageId) {
        if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < desFileSize * 1024) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;
        double accuracy = getAccuracy(srcSize / 1024);
        try {
            while (imageBytes.length > desFileSize * 1024) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
            }
            logger.info("【图片压缩】imageId={} | 图片原大小={}kb | 压缩后大小={}kb",
                    imageId, srcSize / 1024, imageBytes.length / 1024);
        } catch (Exception e) {
            logger.error("【图片压缩】msg=图片压缩失败!", e);
        }
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     *
     * @param size 源图片大小
     * @return 图片压缩质量比
     */
    private static double getAccuracy(long size) {
        double accuracy;
        if (size < 900) {
            accuracy = 0.85;
        } else if (size < 2047) {
            accuracy = 0.6;
        } else if (size < 3275) {
            accuracy = 0.44;
        } else {
            accuracy = 0.4;
        }
        return accuracy;
    }

}
