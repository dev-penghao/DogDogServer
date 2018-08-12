package core;

import core.looper.MessageLooper;
import core.looper.RequestLooper;
import tools.Message;

import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {

    public static final int MAX_ONLINE_NUM=50;
	// 输出流列表，记录了所有客户端的的输出流
	public static String[] online_num = new String[MAX_ONLINE_NUM];
	public static Socket[] online_socket = new Socket[MAX_ONLINE_NUM];
	public static List<Message> msgQueue = new ArrayList<>();

	// JDBC 驱动名及数据库 URL
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/hello?USESSL=false";
	// 数据库的用户名与密码，需要根据自己的设置
	private static final String USER = "penghao";
	private static final String PASS = "123456";
	public static Statement statement = null;

	public static void main(String[] args) throws Exception {

		try {
			Class.forName(JDBC_DRIVER);
			System.out.println("数据库驱动加载成功！");
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("数据库连接成功！");

			statement = con.createStatement();
		} catch (ClassNotFoundException e) {
			System.err.println("驱动加载失败");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Thread requestLooper=new Thread(new RequestLooper());
		Thread messageLooper=new Thread(new MessageLooper());
		requestLooper.start();
		messageLooper.start();

		System.out.println("服务器正在等待客户端的连接请求----");
	}
}