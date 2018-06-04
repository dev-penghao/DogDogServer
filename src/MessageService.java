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
		String cmd = "";
		char[] once_char=new char[200];
		String[] ss;
		try {
			br=new BufferedReader(new InputStreamReader(Server.online_socket[mpid].getInputStream()));
			while(true) {
				while (true){
					if (br.read(once_char)!=-1){
						cmd+=String.valueOf(once_char);
					} else {
						sleep(300);
						break;
					}
				}
				if (Server.online_socket[mpid].getKeepAlive()) {
					System.out.println(Server.online_num[mpid]+"下线！");
					Server.online_num[mpid]=null;
					Server.online_socket[mpid]=null;
					break;
				}
//				if ((cmd=br.readLine())==null){
//					break;
//				}
				System.out.println("新消息： "+cmd);
				ss=cmd.split("/");
				int pid;
				if ((pid=isOnline(ss[0]))!=-1) {
					PrintStream tem_ps=new PrintStream(Server.online_socket[pid].getOutputStream());
					tem_ps.println(Server.online_num[mpid]+"/"+ss[1]);
					tem_ps.close();
				}
			}
		} catch (IOException | InterruptedException e) {
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
