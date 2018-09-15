package tools;

import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) {
        new Test().main();
    }

    public void main() {
        Message msg=new Message(loadNLine("/home/penghao/yuntao",2));
    }

    private void loadMessage(){
        File inFile=new File("/home/penghao/mtest/test_file");
        if (!inFile.exists()) return;// 如果文件不存在就不加载了
        try {
            RandomAccessFile raf=new RandomAccessFile(inFile,"r");
            long fileLength=raf.length();
            long pos=fileLength-2;
            long pos0=pos+1;

            for (int i=0;i<7;i++){
                while (pos>0){
                    raf.seek(pos);
                    if (raf.readByte()==0) {
                        break;
                    } else {
                        pos--;
                    }
                }
                if (pos==0) {
                    raf.seek(0);break;
                } else {
                    raf.seek(pos+1);
                }
                byte[] bytes=new byte[(int) (pos0-pos-1)];
                pos0=pos;
                pos--;
                raf.read(bytes);
                String msgByJson=new String(bytes,Charset.forName("UTF-8"));
                System.out.println(msgByJson);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadNLine(String path,int lineNum){
        File inFile=new File(path);
        if (!inFile.exists()) return null;// 如果文件不存在就不加载了
        try {
            RandomAccessFile raf=new RandomAccessFile(inFile,"r");
            long fileLength=raf.length();
            long pos=fileLength-1;
            long pos0=pos;
            int count=0;
            while (pos>0){
                raf.seek(pos);
                if (raf.readByte()==0) {
                    count+=1;
                    pos--;
                    if (count>=lineNum) {
                        break;
                    }
                    pos0=pos;
                } else {
                    pos--;
                }
            }
            if (pos==0) {
                raf.seek(0);
            } else {
                raf.seek(pos+2);
            }
            byte[] bytes=new byte[(int) (pos0-pos-1)];
            raf.read(bytes);
            return new String(bytes,Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void creatMsgTestFile(){
        File testf=new File("/home/penghao/mtest/test_file");
        try {
            FileOutputStream fos=new FileOutputStream(testf);
            fos.write("line0".getBytes(Charset.forName("UTF-8")));
            fos.write((new byte[1])[0]=0);
            fos.write("line1".getBytes(Charset.forName("UTF-8")));
            fos.write((new byte[1])[0]=0);
            fos.write("line2".getBytes(Charset.forName("UTF-8")));
            fos.write((new byte[1])[0]=0);
            fos.write("line3".getBytes(Charset.forName("UTF-8")));
            fos.write((new byte[1])[0]=0);
            fos.write("line4".getBytes(Charset.forName("UTF-8")));
            fos.write((new byte[1])[0]=0);
            fos.write("line5".getBytes(Charset.forName("UTF-8")));
            fos.write((new byte[1])[0]=0);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MyInputStreamTest() throws IOException {
        ServerSocket ss=new ServerSocket(45678);
        Socket socket=ss.accept();
        System.out.println("得到连接");
        MyInputStream mis= new MyInputStream(socket.getInputStream());
        System.out.println(mis.readLine());
    }

    public void threadPoolTest(){
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(5));

        for(int i=0;i<16;i++){
            MyTask myTask = new MyTask(i);
            try {
                executor.execute(myTask);
            } catch (RejectedExecutionException e) {
                System.out.println("拒绝");
            }
            System.out.println("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
                    executor.getQueue().size()+"，已执行玩别的任务数目："+executor.getCompletedTaskCount());
        }
        executor.shutdown();
    }

}

class MyTask implements Runnable {
    private int taskNum;

    public MyTask(int num) {
        this.taskNum = num;
    }

    @Override
    public void run() {
        System.out.println("正在执行task "+taskNum);
        try {
            Thread.currentThread().sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("task "+taskNum+"执行完毕");
    }
}