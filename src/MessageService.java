import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MessageService extends Thread{

	private Socket socket;
	private PrintStream ps;
	private BufferedReader br;

	MessageService(Socket socket) {
		this.socket=socket;
	}
	@Override
	public void run() {
		String cmd;
		String[] ss;
		try {
			ps=new PrintStream(socket.getOutputStream());
			br=new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while(true) {
				cmd=br.readLine();
				ss=cmd.split("/");
				int pid;
				if ((pid=isOnline(ss[0]))!=-1) {
					new PrintStream(Server.online_socket[pid].getOutputStream()).println(ss[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.run();
	}

	private int isOnline(String num) {
		for(int i=0;i<Server.online_num.length;i++) {
			if (Server.online_num[i].equals(num)) {
				return i;
			}
		}
		return -1;
	}
}
