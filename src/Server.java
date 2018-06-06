import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Server {

	// 输出流列表，记录了所有客户端的的输出流
	static String[] online_num = new String[50];
	static Socket[] online_socket = new Socket[50];
	// JDBC 驱动名及数据库 URL
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/hello?USESSL=false";

	// 数据库的用户名与密码，需要根据自己的设置
	private static final String USER = "penghao";
	private static final String PASS = "123456";

	static Statement statement = null;

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

		System.out.println("服务器正在等待客户端的连接请求----");
		try {
			ServerSocket ss = new ServerSocket(38380);
			// 用一个while循环可以同时响应多个客户端的请求
			while (true) {
				Socket sk = ss.accept();// 服务器监听对应端口的输入
				RequestThread t = new RequestThread(sk);
				t.start();
				System.out.println("得到请求" + sk.getInetAddress().getHostAddress());
			}
		} catch (IOException e) {
			System.err.println("登录模块故障！");
		}
	}
}