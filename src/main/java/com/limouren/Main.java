package com.limouren;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws Exception {

        int count = 1000, success = 0;

        for (int i = 0; i < count; i++) {
            // 图片地址
            String path = "download" + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + File.separator + new Date().getTime() + ".jpg";

            // 生成图片
            CreateVerifyCode create = new CreateVerifyCode();
            create.write(path);

            // 识别图片
            VerifyCode verify = new VerifyCode();
            String verStr = verify.verifyCode(path);

            boolean flag;
            if(flag = create.getText().toLowerCase().equals(verStr.toLowerCase())) {
                success ++;
            }

            System.out.println(create.getText() + "   " + verStr + " : " + flag);
        }

        System.out.println("成功数：" + success);
        System.out.println("成功率：" + ((double) success / count * 100) + "%");

    }
}
