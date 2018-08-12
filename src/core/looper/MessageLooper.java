package core.looper;

import core.Server;
import tools.Message;

import java.io.IOException;
import java.nio.charset.Charset;

public class MessageLooper implements Runnable {

    @Override
    public void run() {
        System.out.println("MessageLooper啟動！");
        while (true){
            if (Server.msgQueue.size()!=0){
                handMessage(Server.msgQueue.get(0));
                System.out.println("消息已發送"+Server.msgQueue.get(0).toString());
                Server.msgQueue.remove(0);
            }
            try {
                Thread.sleep(2000);
                System.out.println("MsgQueue Size="+Server.msgQueue.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handMessage(Message msg){
        String to=msg.getTo();
        int toPid=isOnline(to);
        if (toPid!=-1){
            try {
                Server.online_socket[toPid].getOutputStream().write(msg.toString().getBytes(Charset.forName("UTF-8")));
                Server.online_socket[toPid].getOutputStream().write((new byte[1])[0]=0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 给定一个账号，判断该用户是否在线。如果在线则返回该用户的pid,否则返回-1
    private int isOnline(String num) {
        for(int i=0;i<Server.online_num.length;i++) {
            if (num.equals(Server.online_num[i])) {
                return i;
            }
        }
        return -1;
    }
}
