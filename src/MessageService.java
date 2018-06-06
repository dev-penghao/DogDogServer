import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class MessageService extends Thread{

	private int mpid;
	private BufferedReader br;
	private PrintStream ps;

	MessageService(int mpid){
		this.mpid=mpid;
	}
	@Override
	public void run() {
		String cmd;
		char[] once_char=new char[4000];
		String[] ss;
		try {
			br=new BufferedReader(new InputStreamReader(Server.online_socket[mpid].getInputStream()));
			while(true) {
				/*
				 *   read()会阻塞是个很讨厌的问题！
				 *   read()在没有数据可读的时候会阻塞，在输入流关闭后会返回-1
				 */
				if (br.read(once_char)!=-1){
					cmd=valueOf(once_char);
					for (int i=0;i<once_char.length;i++){
                        once_char[i]=0;
                    }
				} else {
					// 如果输入流关闭表明该用户下线
					System.out.println(Server.online_num[mpid]+"下线！");
					Server.online_num[mpid]=null;
					Server.online_socket[mpid]=null;
					break;
				}
				System.out.println("新消息： "+cmd);
				ss=cmd.split("/");
				int pid;
				if ((pid=isOnline(ss[0]))!=-1) {
					ps=new PrintStream(Server.online_socket[pid].getOutputStream());
					ps.print(Server.online_num[mpid]+"/"+ss[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.run();
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
	
	// String.valueOf(char[] data)就是个坑！
	private String valueOf(char[] chars){
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<chars.length;i++){
            if (chars[i]!=0){
                sb.append(chars[i]);
            } else {
                break;
            }
        }
        return sb.toString();
    }
}
