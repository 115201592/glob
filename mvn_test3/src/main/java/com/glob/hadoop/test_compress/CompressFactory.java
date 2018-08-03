package com.glob.hadoop.test_compress;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.ReflectionUtils;

public class CompressFactory {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Configuration conf = new Configuration();
        String[] uargs = new GenericOptionsParser(conf, args).getRemainingArgs();
        String opt_type = uargs[0];
        String[] code_class = new String[]{
                "org.apache.hadoop.io.compress.DefaultCodec",
                "org.apache.hadoop.io.compress.GzipCodec",
//                "org.apache.hadoop.io.compress.Bzip2Codec",
                "com.hadoop.compression.lzo.LzopCodec",
                "org.apache.hadoop.io.compress.Lz4Codec",
                "org.apache.hadoop.io.compress.SnappyCodec"
        };
//        String code_class = uargs[1];
        String source_dir = uargs[2];
        String goal_dir = uargs[3];
        for(String cls:code_class){
            String[] sp=cls.split("\\.");
            String end_name=sp[sp.length-1];
            long start = System.currentTimeMillis();

            if("compress".equals(opt_type)){
                compress(cls,source_dir,goal_dir+"/"+end_name,conf);
            }else if("uncompress".equals(opt_type)){
                uncompress(cls,source_dir,goal_dir+"/"+end_name,conf);
            }
            System.out.println();
            System.out.printf("%s:%s",end_name,System.currentTimeMillis()-start);
            System.out.println();
        }
    }

    /**
     * 压缩
     * @param code_class
     * @param source_dir
     * @param goal_dir
     * @param conf
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void compress(String code_class,String source_dir,String goal_dir,Configuration conf) throws ClassNotFoundException, IOException{
        Class<?> codeClass  = Class.forName(code_class);
        FileSystem fs = FileSystem.get(conf);
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codeClass, conf);
        FSDataOutputStream output = fs.create(new Path(goal_dir), new Progressable(){
            @Override
            public void progress() {
                System.out.print("*");
            }
        });

        FSDataInputStream input = fs.open(new Path(source_dir));

        CompressionOutputStream compress_out = codec.createOutputStream(output);
        IOUtils.copyBytes(input, compress_out, conf);
        IOUtils.closeStream(input);
        IOUtils.closeStream(compress_out);
    }

    /**
     * 解压缩
     * @param decode_class
     * @param source_dir
     * @param goal_dir
     * @param conf
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void uncompress(String decode_class,String source_dir,String goal_dir,Configuration conf) throws ClassNotFoundException, IOException{
        Class<?> decodeClass = Class.forName(decode_class);
        FileSystem fs = FileSystem.get(conf);
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(decodeClass, conf);
        System.out.println("load lzo class completed: "+decodeClass.getName());
        FSDataInputStream input = fs.open(new Path(source_dir));
        CompressionInputStream codec_input = codec.createInputStream(input);
        OutputStream output = fs.create(new Path(goal_dir));
        System.out.println(codec_input.toString()+"--"+output.toString());
        IOUtils.copyBytes(codec_input, output, conf);
        System.out.println("run uncompress completed");
        IOUtils.closeStream(input);
        IOUtils.closeStream(output);
    }
}
