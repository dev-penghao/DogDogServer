import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class MessageService extends Thread{

	private int mpid;
	private BufferedReader br;

	MessageService(int mpid){
		this.mpid=mpid;
	}
	@Override
	public void run() {
		String cmd;
		String[] ss;
		try {
			br=new BufferedReader(new InputStreamReader(Server.online_socket[mpid].getInputStream()));

			while(true) {
				if ((cmd=br.readLine())==null){
					System.out.println(Server.online_num[mpid]+"下线！");
					Server.online_num[mpid]=null;
					Server.online_socket[mpid]=null;
					break;
				}
				System.out.println("新消息： "+cmd);
				ss=cmd.split("/");
				int pid;
				if ((pid=isOnline(ss[0]))!=-1) {
					new PrintStream(Server.online_socket[pid].getOutputStream()).println(Server.online_num[mpid]+"/"+ss[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.run();
	}

	private int isOnline(String num) {
		for(int i=0;i<Server.online_num.length;i++) {
			if (num.equals(Server.online_num[i])) {
				return i;
			}
		}
		return -1;
	}
}
