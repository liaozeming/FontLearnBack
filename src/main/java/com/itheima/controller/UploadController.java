package com.itheima.controller;

import cn.hutool.crypto.SecureUtil;
import com.itheima.pojo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Random;

@RestController
public class UploadController {

    @Value("${file.upload.path}")
    private String path;

    /**
     * 文件上传
     *
     * @param file
     * @return 上传信息
     */
    @PostMapping("/fileupload")
    public Result<String> fileUpload(MultipartFile file) {
        //获取文件原始名称含扩展名
        String fileName = file.getOriginalFilename();
        String newFilename = SecureUtil.md5(System.currentTimeMillis() + new Random().nextInt(1000) + "");
        String[] filename = fileName.split("\\.");
        //定义文件存放位置
        String filePath = path + newFilename + "." + filename[1];
        //将路径放入File实例
        File dest = new File(filePath);
        //获取文件大小(单位kb)  getSize返回字节bit/1024/1024取MB做单位-->1048576=1024x1024
        float size = (((float) file.getSize()) / 1048576);
        //获取文件类
        String type = file.getContentType();
        //判断文件目录是否存在若不存在将进行创建
        if (!dest.exists()) {
            //创建目录
            dest.mkdirs();
        }
        try {
            //将接收到的文件传输到给定的目标文件
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            //出现异常返回上传失败的信息
            return Result.error("Upload file success : 上传失败");
        }
        //上传成功返回文件存放路径
/*        return "Upload file success : 上传成功！文件存放路径为:" + dest.getAbsolutePath()
                + "\n文件大小:" + size + "MB" + "\n文件类型:" + type;*/
        return Result.success("http://localhost:8080/download?fileName=" + newFilename + "." + filename[1]);
    }

    /**
     * 多个文件上传
     *
     * @param files
     * @return 上传信息
     */
    @PostMapping("/fileuploads")
    public String fileUpload(MultipartFile[] files) {
        //遍历文件
        for (MultipartFile file : files) {
            //获取文件原始名称含扩展名
            String fileName = file.getOriginalFilename();
            String newFilename = SecureUtil.md5(System.currentTimeMillis() + new Random().nextInt(1000) + "");
            String[] filename = fileName.split("\\.");
            //定义文件存放位置
            String filePath = path + newFilename + "." + filename[1];
            //将路径放入File实例
            File dest = new File(filePath);
            //判断文件目录是否存在若不存在将进行创建
            if (!dest.exists()) {
                //创建目录
                dest.mkdirs();
            }
            try {
                //将接收到的文件传输到给定的目标文件
                file.transferTo(dest);
            } catch (IOException e) {
                e.printStackTrace();
                //出现异常返回上传失败的信息
                return "Upload file success : 上传失败";
            }
        }
        return "Upload file success : 成功上传" + files.length + "个文件";
    }


    @GetMapping("/download")
    public String downloadFile(@RequestParam String fileName, HttpServletResponse response) {
        if (fileName != null) {
            //设置文件路径
            File file = new File(path, fileName);
            if (file.exists()) {
                // 设置强制下载不打开
//                response.setContentType("application/force-download");
                // 设置文件名
//                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                response.setContentType("image/jpeg");

                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    //文件输入流读取本地文件
                    fis = new FileInputStream(file);
                    //将文件输入流转成字节缓冲输入流
                    bis = new BufferedInputStream(fis);
                    //定义字节输出流
                    OutputStream os = response.getOutputStream();
                    //遍历输出字节流
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    return "下载成功";
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //关闭字节缓冲输入流
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //关闭文件输入流
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "下载失败";
    }


}
