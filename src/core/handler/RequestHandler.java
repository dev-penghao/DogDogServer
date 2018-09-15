package core.handler;

import core.MessageService;
import core.Server;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestHandler implements Runnable {

	private Socket socket;
	private PrintStream pStream;

	public RequestHandler(Socket socket) {
		this.socket=socket;
	}
	/*
	 *开启一个线程循环读取该客户端的输出流，如果有新消息
	 *则向所有已连接的客户端发送该消息
	 */
	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pStream=new PrintStream(socket.getOutputStream());
		    analyseCmd(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 分析并处理客户端的请求
	private void analyseCmd(String cmd) {
		String[] ss=cmd.split("/");
		System.out.println("请求类型： "+ss[0]);
		String result;
		switch (ss[0]) {
			case "test":
				pStream.println("OK");
				break;
			case "sign_in":// 登录
				result = sign_in(ss[1], ss[2]);
				pStream.println(result);
				break;
			case "sign_up":// 注册
				result=sign_up(ss[1], ss[2], ss[3]);
				pStream.println(result);
				break;
			case "get_friend_list":// 获取自己的所有好友
				result=get_friend_list(ss[1]);
				System.out.println("get_friend_list: "+result);
				pStream.println(result);
				break;
			case "find_user":// 查找指定用户
				result=find_user(ss[1]);
				System.out.println("find_user: "+result);
				pStream.println(result);
				break;
			case "add_friend":// 将指定用户添加为好友
				result=add_friend(ss[1],ss[2]);
				pStream.println(result);
				break;
			case "search_user":// 搜索用户
				result=search_user(ss[1]);
				pStream.println(result);
				System.out.println("search_user: "+result);
				break;
			case "get_one_details":// 得到指定的用户的详细资料
				break;
			case "get_chat_record":// 获取与某人的聊天记录
				break;
			case "sign_off":// 注销账号
				break;
			default:
				System.err.println("Bad cmd!");
				break;
		}
	}

	// 注册
	private String sign_up(String name, String num, String password) {
		String cmd="insert into user_lib (name,num,password) values ("+"\""+name+"\","+"\""+num+"\","+"\""+password+"\""+")";
		try {
			ResultSet resultSet= Server.statement.executeQuery("select id from user_lib where num="+"\""+num+"\"");
			if (resultSet.next()) {
				return "user aleady exist";
			}else {
				Server.statement.execute(cmd);// insert不能使用executeQuery()
				return "sign_up success";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql erro";
		}
	}

	// 登录
	private String sign_in(String num, String password) {
		String cmd="select num,password from user_lib where num="+"\""+num+"\"";
		try {
			ResultSet result=Server.statement.executeQuery(cmd);
			if (result.next()) {
				String number=result.getString("num");
				String pwInLib=result.getString("password");
				if (pwInLib.equals(password)) {
					// 该用户是否在线
					for(int i=0;i<Server.online_num.length;i++) {
						if (Server.online_num[i]!=null && Server.online_num[i].equals(num)){
							return "This user had online now";
						}
					}					
					for(int i=0;i<Server.online_num.length;i++) {
						if (Server.online_num[i]==null) {
							System.out.println("一个用户登录，pid="+i+"  账号： "+num);
							Server.online_num[i]=number;
							Server.online_socket[i]=socket;
							Thread msgServer=new Thread(new MessageService(i));
							msgServer.start();
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

	// 获取自己的好友列表
	private String get_friend_list(String my_num) {
		String select_friend="select user1,user2 from friend_list where user1=\""+my_num+"\" or user2=\""+my_num+"\"";
		List<String> friend_num=new ArrayList<>();
		List<JSONObject> objList=new ArrayList<>();
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
                    JSONObject obj=new JSONObject();
				    obj.put("name",resultSet.getString("name"));
                    obj.put("num",aFriend_num);
                    obj.put("isOnline",Server.isOnline(aFriend_num)!=-1);
                    objList.add(obj);
				}
			}
			return new JSONArray(objList).toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}

	// 指定账号查找用户
	private String find_user(String obj_num) {
		try {
			ResultSet resultSet = Server.statement.executeQuery("select name from user_lib where num=\"" + obj_num+"\"");
			if (resultSet.next()){
//				json.JSON obj=new json.JSON();
//				obj.putString("name",resultSet.getString("name"));
				return resultSet.getString("name");
			}
			return "user not found";
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}
	
	// 模糊查询
	private String search_user(String key_word) {
		// 这条语句长成这样：select name , num from user_lib where name like "%key_word%" or num like "%key_word%";
		try {
			List<JSONObject> objs=new ArrayList<>();
			ResultSet resultSet=Server.statement.executeQuery("select name, num from user_lib where name like "+"\"%"+key_word+"%\""+" or "+"num like "+"\"%"+key_word+"%\"");
			while(resultSet.next()) {
				JSONObject obj=new JSONObject();
				obj.put("name", resultSet.getString("name"));
				obj.put("num", resultSet.getString("num"));
				objs.add(obj);
			}
			if (objs.isEmpty()) {
				return "not found any user";
			}
			return new JSONArray(objs).toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}

	// 请求添加指定用户为好友
	private String add_friend(String my_num, String obj_num) {
		try {
			if (my_num.equals(obj_num)) {
				return "不能添加自己为好友";// 这句话的意思是：不能添加自己为好友
			}
			ResultSet resultSet=Server.statement.executeQuery("select num from user_lib where num="+"\""+obj_num+"\"");
			if (resultSet.next()) {
				resultSet=Server.statement.executeQuery("select * from friend_list where user1="+"\""+my_num+"\""+" and user2="+"\""+obj_num+"\"");
				if (resultSet.next()) {
					return "你们已经是好友了";// 你们已经是好友了
				} else {
					resultSet=Server.statement.executeQuery("select * from friend_list where user2="+"\""+my_num+"\""+" and user1="+"\""+obj_num+"\"");
					if (resultSet.next()) {
						return "你们已经是好友了";// 你们已经是好友了
					} else {
						Server.statement.execute("insert into friend_list (user1,user2) values ("+"\""+my_num+"\","+"\""+obj_num+"\")");
						return "be friend success";						
					}
				}
			}else {
				return "user not found";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "mysql error";
		}
	}
}