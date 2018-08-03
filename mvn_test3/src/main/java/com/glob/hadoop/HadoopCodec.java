package com.glob.hadoop;
 
import java.io.BufferedInputStream; 
import java.io.FileInputStream; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.net.URI; 
 
import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.FileSystem; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.io.IOUtils; 
import org.apache.hadoop.io.compress.CompressionCodec; 
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.Compressor;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.util.ReflectionUtils; 
 
/** 
 *  
 * Description: 
 *  
 * @author charles.wang 
 * @created May 26, 2012 3:23:21 PM 
 *  
 */ 
public class HadoopCodec { 
 
    /** 
     * @param args 
     */ 
    public static void main(String[] args) throws Exception {
        String inputFile = "dm_crm_users_fact.csv";
//        String inputFile = "F:\\dm_crm_users_fact.csv";
        String outputFolder = "hdfs://10.40.6.251:8020/user/root/test3/";
        // String outputFile="bigfile.gz"; 
        // 读取hadoop文件系统的配置
        Configuration conf = new Configuration(); 
        conf.set("hadoop.job.ugi", "hadoop-user,hadoop-user"); 
 
        //测试各种压缩格式的效率 
        //gzip 
        long gzipTime = copyAndZipFile(conf, inputFile, outputFolder, "org.apache.hadoop.io.compress.GzipCodec", "gz");
        //bzip2 
        long bzip2Time = copyAndZipFile(conf, inputFile, outputFolder, "org.apache.hadoop.io.compress.BZip2Codec", "bz2");
        //deflate 
        long deflateTime = copyAndZipFile(conf, inputFile, outputFolder, "org.apache.hadoop.io.compress.DefaultCodec", "deflate");
        //lz4
//        long lz4 = copyAndZipFile(conf, inputFile, outputFolder, "org.apache.hadoop.io.compress.Lz4Codec", "lz4");

        System.out.println("被压缩的文件名为： "+inputFile);
        System.out.println("使用gzip压缩，时间为： "+gzipTime+"毫秒!");
        System.out.println("使用bzip2压缩，时间为： "+bzip2Time+"毫秒!");
        System.out.println("使用deflate压缩，时间为： "+deflateTime+"毫秒!");
//        System.out.println("使用deflate压缩，时间为： "+lz4+"毫秒!");
    }
 
    public static long copyAndZipFile(Configuration conf, String inputFile, String outputFolder, String codecClassName, 
            String suffixName) throws Exception { 
        long startTime = System.currentTimeMillis(); 
        // 因为本地文件系统是基于java.io包的，所以我们创建一个本地文件输入流
        InputStream in = new BufferedInputStream(new FileInputStream("F://dm_crm_users_fact.csv"));
//        InputStream in = new BufferedInputStream(new FileInputStream("/root/dm_crm_users_fact.csv"));
        //去掉扩展名提取basename
        String baseName = inputFile.substring(0, inputFile.indexOf(".")); 
        //构造输出文件名，它是路径名+基本名+扩展名 
        String outputFile = outputFolder + baseName + "."+suffixName; 
        FileSystem fs = FileSystem.get(URI.create(outputFile), conf);
        // 创建一个编码解码器，通过反射机制根据传入的类名来动态生成其实例
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(Class.forName(codecClassName), conf); 
        // 创建一个指向HDFS目标文件的压缩文件输出流
        OutputStream out = codec.createOutputStream(fs.create(new Path(outputFile))); 
        // 用IOUtils工具将文件从本地文件系统复制到HDFS目标文件中 
        try {
            IOUtils.copyBytes(in, out, conf); 
        } finally {
            IOUtils.closeStream(in); 
            IOUtils.closeStream(out); 
        } 
        return System.currentTimeMillis() - startTime;
    }


}