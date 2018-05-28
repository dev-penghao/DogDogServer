import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestThread extends Thread{

	private Socket socket;
	private BufferedReader br;
	private PrintStream pStream;

	RequestThread(Socket socket) {
		this.socket=socket;
	}
	/*
	 *开启一个线程循环读取该客户端的输出流，如果有新消息
	 *则向所有已连接的客户端发送该消息
	 */
	@Override
	public void run() {
		try {
			String string="";
			char[] ch=new char[1];
			int isEOF=0;
			br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pStream=new PrintStream(socket.getOutputStream());
			finish:while(true) {
				while(true) {
					isEOF=br.read(ch);
					if (isEOF==-1) {
						break finish;
					}
					if (ch[0]==0) {
						break;
					}
					string+=ch[0];
				}
				analyseCmd(string);
				string="";
			}
//			Server.online_num[pid]=null;
			Server.count--;
			System.out.println(socket.getInetAddress().getHostAddress()+"退出");
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.run();
	}

	//分析并处理客户端的请求
	private void analyseCmd(String cmd) {
		String[] ss=cmd.split("/");
		String result="";
		switch (ss[0]) {
			case "message":
//			Server.sendMessage(ss[1],pid);
				break;
			case "test":
				pStream.println("OK");
				break;
			case "sign_in":
				result = sign_in(ss[1], ss[2]);
				pStream.println(result);
				break;
			case "sign_up":
				sign_up(ss[1], ss[2], ss[3]);
				break;
			case "find_friend":

				break;
			default:
				System.err.println("Bad cmd!");
				break;
		}
	}

	//注册
	private String sign_up(String name, String num, String password) {
		String cmd="insert into user_lib (name,num,password) values ("+"\""+name+"\","+"\""+num+"\","+"\""+password+"\""+")";
		try {
			ResultSet resultSet=Server.statement.executeQuery("select id from user_lib where num="+num);
			if (resultSet.next()) {
				return "user aleady exist";
			}else {
				Server.statement.executeQuery(cmd);
				return "sign_in success";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql erro";
		}
	}

	//登录
	private String sign_in(String num, String password) {
		String cmd="select num,password from user_lib where num="+num;
		try {
			ResultSet result=Server.statement.executeQuery(cmd);
			if (result.next()) {
				String number=result.getString("num");
				String pwInLib=result.getString("password");
				if (pwInLib.equals(password)) {
					for(int i=0;i<Server.online_num.length;i++) {
						if (Server.online_num[i]==null) {
							Server.online_num[i]=number;
							Server.online_socket[i]=socket;
							MessageService msgs=new MessageService(socket);
							msgs.start();
							break;
						}
					}
					return "OK";
				}else {
					return "eorr passwprd";
				}
			}else {
				return "user not fond";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "unknow eorr";
		}
	}

	//获取自己的好友列表
	public String get_friend_list(String my_num) {
		String select_friend="select user1,user2 from friend_list where user1="+my_num+" or user2="+my_num;
		List<String> friend_num=new ArrayList<>();
		List<String> friend_name=new ArrayList<>();
		try {
			ResultSet resultSet=Server.statement.executeQuery(select_friend);
			while(resultSet.next()) {
				if (resultSet.getString("user1").equals(my_num)) {
					friend_num.add(resultSet.getString("user2"));
				}else {
					friend_num.add(resultSet.getString("user1"));
				}
			}
			for (String aFriend_num : friend_num) {
				resultSet = Server.statement.executeQuery("select name from user_lib where num=" + aFriend_num);
				if (resultSet.next()) {
					friend_name.add(resultSet.getString("name"));
				}
			}
			return friend_name.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql erro";
		}
	}

	//查找指定账号的用户
	public void find_user(String obj_num) {

	}

	//请求添加指定用户为好友
	public String add_friend(String my_num,String obj_num) {
		try {
			ResultSet resultSet=Server.statement.executeQuery("select num from user_lib where num="+obj_num);
			if (resultSet.next()) {
				Server.statement.executeQuery("insert into friend_list (user1,user2) valuse ("+"\""+my_num+"\","+"\""+obj_num+"\"");
				return "add friend success";
			}else {
				return "user not found";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql erro";
		}
	}
}