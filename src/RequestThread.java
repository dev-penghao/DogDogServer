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
			StringBuilder string= new StringBuilder();
//			char[] ch=new char[1];
//			int isEOF;
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pStream=new PrintStream(socket.getOutputStream());
//			finish:while(true) {
//				while(true) {
				    string.append(br.readLine());
				    analyseCmd(string.toString());
//                    string = new StringBuilder();
//					isEOF= br.read(ch);
//					if (isEOF==-1) {
//						break finish;
//					}
//					if (ch[0]==0) {
//						break;
//					}
//					string.append(ch[0]);
//				}
//				analyseCmd(string.toString());
//			}
//			Server.online_num[pid]=null;
//			Server.count--;
//			System.out.println(socket.getInetAddress().getHostAddress()+"退出");
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.run();
	}

	//分析并处理客户端的请求
	private void analyseCmd(String cmd) {
		String[] ss=cmd.split("/");
		System.out.println("请求类型： "+ss[0]);
		String result;
		switch (ss[0]) {
			case "test":
				pStream.println("OK");
				break;
			case "sign_in":
				result = sign_in(ss[1], ss[2]);
				pStream.println(result);
				break;
			case "sign_up":
				result=sign_up(ss[1], ss[2], ss[3]);
				pStream.println(result);
				break;
			case "get_friend_list":
				result=get_friend_list(ss[1]);
				System.out.println("get_friend_list: "+result);
				pStream.println(result);
				break;
			case "find_user":
				result=find_user(ss[1]);
				System.out.println("find_user: "+result);
				pStream.println(result);
				break;
			case "add_friend":
				result=add_friend(ss[1],ss[2]);
				pStream.println(result);
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
			ResultSet resultSet=Server.statement.executeQuery("select id from user_lib where num="+"\""+num+"\"");
			if (resultSet.next()) {
				return "user aleady exist";
			}else {
				Server.statement.execute(cmd);
				return "sign_up success";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql erro";
		}
	}

	//登录
	private String sign_in(String num, String password) {
		String cmd="select num,password from user_lib where num="+"\""+num+"\"";
		try {
			ResultSet result=Server.statement.executeQuery(cmd);
			if (result.next()) {
				String number=result.getString("num");
				String pwInLib=result.getString("password");
				if (pwInLib.equals(password)) {
					for(int i=0;i<Server.online_num.length;i++) {
						if (Server.online_num[i]==null) {
							System.out.println("一个用户登录，pid="+i+"  账号： "+num);
							Server.online_num[i]=number;
							Server.online_socket[i]=socket;
							MessageService msgs=new MessageService(i);
							msgs.start();
							break;
						}
					}
					return "OK";
				}else {
					return "error password";
				}
			}else {
				return "user not fond";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "unknown error";
		}
	}

	//获取自己的好友列表
	private String get_friend_list(String my_num) {
		String select_friend="select user1,user2 from friend_list where user1=\""+my_num+"\" or user2=\""+my_num+"\"";
		List<String> friend_num=new ArrayList<>();
		List<JSON> objList=new ArrayList<>();
		JSONArray objArray=new JSONArray();
		try {
		    // 得到自己所有好友的账号，并储存到friend_num中
			ResultSet resultSet=Server.statement.executeQuery(select_friend);
			while(resultSet.next()) {
				if (resultSet.getString("user1").equals(my_num)) {
					friend_num.add(resultSet.getString("user2"));
				}else {
					friend_num.add(resultSet.getString("user1"));
				}
			}
			// 查找对应账号的昵称，并将一个用户的信息存到一个新生成的JSON数据包中
			for (String aFriend_num : friend_num) {
				resultSet = Server.statement.executeQuery("select name from user_lib where num=\"" + aFriend_num+"\"");
				if (resultSet.next()){
                    JSON obj=new JSON();
				    obj.putString("name",resultSet.getString("name"));
                    obj.putString("num",aFriend_num);
                    objList.add(obj);
				}
			}
			// 将所有JSON数据包合并为一个JSON数组并返回
			for (JSON obj:objList)
			    objArray.putJson(obj);
			return objArray.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}

	//指定账号查找用户
	private String find_user(String obj_num) {
		try {
			ResultSet resultSet = Server.statement.executeQuery("select name from user_lib where num=\"" + obj_num+"\"");
			if (resultSet.next()){
//				JSON obj=new JSON();
//				obj.putString("name",resultSet.getString("name"));
				return resultSet.getString("name");
			}
			return "user not found";
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}

	//请求添加指定用户为好友
	private String add_friend(String my_num, String obj_num) {
		try {
			ResultSet resultSet=Server.statement.executeQuery("select num from user_lib where num="+obj_num);
			if (resultSet.next()) {
				Server.statement.executeQuery("insert into friend_list (user1,user2) values ("+"\""+my_num+"\","+"\""+obj_num+"\"");
				return "add friend success";
			}else {
				return "user not found";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}
}