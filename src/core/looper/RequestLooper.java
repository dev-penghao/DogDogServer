package core.looper;

import core.handler.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestLooper implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(38380);
            // 用一个while循环可以同时响应多个客户端的请求
            while (true) {
                Socket sk = ss.accept();// 服务器监听对应端口的输入
                Thread t=new Thread(new RequestHandler(sk));
                t.start();
                System.out.println("得到请求" + sk.getInetAddress().getHostAddress());
            }
        } catch (IOException e) {
            System.err.println("登录模块故障！");
        }
    }
}
