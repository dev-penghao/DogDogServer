package core;

import tools.Message;
import tools.MyInputStream;

import java.io.IOException;

public class MessageService implements Runnable{

    private int mpid;
	private MyInputStream mis;

	public MessageService(int mpid){
		this.mpid=mpid;
	}

	@Override
	public void run() {
        init();
	    while (true){
            try {
                String msgByString=mis.readString();// 读取一条消息
                if (msgByString == null) break;
                Message message=new Message(msgByString);
                Server.msgQueue.add(message);// 添加到消息队列中
                System.out.println("收到消息:"+message.toString());
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        // 如果输入流关闭表明该用户下线
        System.out.println(core.Server.online_num[mpid]+"下线！");
        core.Server.online_num[mpid]=null;
        core.Server.online_socket[mpid]=null;
	}

	private void init(){
        try {
            mis= new MyInputStream(Server.online_socket[mpid].getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	
	// String.valueOf(char[] data)就是个坑！
	private String valueOf(char[] chars){
        StringBuilder sb=new StringBuilder();
        for (char aChar : chars) {
            if (aChar != 0) {
                sb.append(aChar);
            } else {
                break;
            }
        }
        return sb.toString();
    }
}
