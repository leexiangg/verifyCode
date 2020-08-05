package com.limouren;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class VerifyCode {

    public void requireOrderEndDateJob()
    {
        String imageUrl = "http://hyfw.95306.cn/gateway/DzswNewD2D/Dzsw/security/jcaptcha.jpg";
        HttpsClientUtil clientUtil = new HttpsClientUtil();
        try {

            // 1、设置通用请求头信息
            Map<String, String> header = new HashMap<String, String>();
            header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            header.put("Accept-Encoding", "gzip, deflate");
            header.put("Accept-Language", "zh-CN,zh;q=0.9");
            header.put("Cache-Control", "max-age=0");
            header.put("Host", "demo1.zving.com");
            header.put("Proxy-Connection", "keep-alive");
            header.put("Upgrade-Insecure-Requests", "1");
            header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");
            clientUtil.setBaseheader(header);

            // 2、获取并识别图片
            String verification = "";
            while(verification.length() != 5) {
                String imgLocal = clientUtil.downloadBitFile(imageUrl);
                System.out.println("验证码文件：" + imgLocal);

                verification = verifyCode(imgLocal);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理图片
     * 		其实可以不对图片做处理，直接使用Tess4j直接读取图片文字。
     * 		不过不经过图片处理的图片识别率较低，大概只有10%的成功率。
     * 		经过处理的图片，识别率提高到了50%左右。
     * @param imagePath 图片的绝对或相对路径
     * @return 处理后的图片保存路径
     * @throws IOException
     */
    public static String dealImage(String imagePath) throws IOException {
        String formatName = imagePath.substring(imagePath.indexOf(".") + 1);
        File file = new File(imagePath);
        BufferedImage image = ImageIO.read(file);

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage outImage = new BufferedImage(width, height, image.getType());
        int backgroudColor = image.getRGB(0, 0);
        int backgroudR = (backgroudColor >> 16) & 0xff;
        int backgroudG = (backgroudColor >> 8) & 0xff;
        int backgroudB = backgroudColor & 0xff;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = image.getRGB(i, j);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                int newColor = color;

                // 去除干扰信息，干扰信息为黑色相近64/256之内全部清理
                if(r < 64 && g < 64 && b < 64) {
                    if(j-1 >= 0)
                        newColor = image.getRGB(i, j-1);
                    else if(i-1 >= 0)
                        newColor = image.getRGB(i-1, j);
                    else if(j+1 < height)
                        newColor = image.getRGB(i, j+1);
                    else if(i+1 < width)
                        newColor = image.getRGB(i+1, j);
                    r = (newColor >> 16) & 0xff;
                    g = (newColor >> 8) & 0xff;
                    b = newColor & 0xff;
                }

                // 去除背景颜色，相近的±30之内的全部设置为白色，灰色的干扰信息改为白色，文字改为黑色
                if(Math.abs((r - backgroudR)) <= 30 && Math.abs((g - backgroudG)) <= 30 && Math.abs((b - backgroudB)) <= 30) {
                    newColor = 0xffffff;
                } else if(r > 150 && g > 150 && b > 150){
                    newColor = 0xffffff;
                } else {
                    newColor = 0x000000;
                }
                outImage.setRGB(i, j, newColor);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(outImage, formatName, out);
        String outPath = new File(imagePath).getParent() + File.separator + getFileMd5(out.toByteArray()) + "." + formatName;
        File newFile = new File(outPath);
        ImageIO.write(outImage, formatName, newFile);
        return outPath;
    }

    /**
     * 根据文件字节流获取文件MD5
     * @param fileBytes
     * @return
     */
    public static String getFileMd5(byte[] fileBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] mdBytes = md.digest(fileBytes);
            BigInteger bigInt = new BigInteger(1, mdBytes);
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 识别本地图片
     * @param imgLocal
     * @return
     * @throws Exception
     */
    public String verifyCode(String imgLocal) throws Exception {
        dealImage(imgLocal);
        File imageFile = new File(imgLocal);
        Tesseract instance = new Tesseract();
        instance.setTessVariable("user_defined_dpi", "300");
        instance.setDatapath("tessdata");
        String verification = instance.doOCR(imageFile);
        verification = verification.replaceAll("[^0-9|a-z|A-Z]", "");
        return verification;
    }

}
