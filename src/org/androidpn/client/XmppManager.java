/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This class is to manage the XMPP connection between client and server.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class XmppManager {

	private static final String LOGTAG = "XmppManager";

	private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

	private Context context;

	private NotificationService.TaskSubmitter taskSubmitter;

	private NotificationService.TaskTracker taskTracker;

	private SharedPreferences sharedPrefs;

	private String xmppHost;

	private int xmppPort;

	private XMPPConnection connection;

	private String username;

	private String password;

	private ConnectionListener connectionListener;

	private PacketListener notificationPacketListener;

	private Handler handler;

	private List<Runnable> taskList;

	private boolean running = false;

	private Future<?> futureTask;

	private Thread reconnection;

	public XmppManager(NotificationService notificationService) {
		context = notificationService;
		taskSubmitter = notificationService.getTaskSubmitter();
		taskTracker = notificationService.getTaskTracker();
		sharedPrefs = notificationService.getSharedPreferences();

		xmppHost = sharedPrefs.getString(Constants.XMPP_HOST, "localhost");
		xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT, 5222);
		username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
		password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");

		connectionListener = new PersistentConnectionListener(this);
		
		
		notificationPacketListener = new NotificationPacketListener(this);

		handler = new Handler();
		taskList = new ArrayList<Runnable>();
		reconnection = new ReconnectionThread(this);
	}

	public Context getContext() {
		return context;
	}

	public void connect() {

		submitLoginTask();
	}

	public void disconnect() {

		terminatePersistentConnection();
	}

	public void terminatePersistentConnection() {
		Log.d(LOGTAG, "terminatePersistentConnection()...");
		Runnable runnable = new Runnable() {

			final XmppManager xmppManager = XmppManager.this;

			public void run() {
				if (xmppManager.isConnected()) {
					Log.d(LOGTAG, "terminatePersistentConnection()... run()");
					xmppManager.getConnection().removePacketListener(
							xmppManager.getNotificationPacketListener());
					xmppManager.getConnection().disconnect();
				}
				xmppManager.runTask();
			}

		};
		addTask(runnable);
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public PacketListener getNotificationPacketListener() {
		return notificationPacketListener;
	}

	public void startReconnectionThread() {
		synchronized (reconnection) {
			if (!reconnection.isAlive()) {
				reconnection.setName("Xmpp Reconnection Thread");
				reconnection.start();
			}
		}
	}

	public Handler getHandler() {
		return handler;
	}

	public void reregisterAccount() {
		removeAccount();
		submitLoginTask();
		runTask();
	}

	public List<Runnable> getTaskList() {
		return taskList;
	}

	public Future<?> getFutureTask() {
		return futureTask;
	}

	public void runTask() {
		Log.d(LOGTAG, "runTask()...");
		synchronized (taskList) {
			running = false;
			futureTask = null;
			if (!taskList.isEmpty()) {
				Runnable runnable = (Runnable) taskList.get(0);
				taskList.remove(0);
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			}
		}
		taskTracker.decrease();
		Log.d(LOGTAG, "runTask()...done");
	}

	private String newRandomUUID() {
		String uuidRaw = UUID.randomUUID().toString();
		return uuidRaw.replaceAll("-", "");
	}

	private boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	private boolean isAuthenticated() {
		return connection != null && connection.isConnected()
				&& connection.isAuthenticated();
	}

	private boolean isRegistered() {
		return sharedPrefs.contains(Constants.XMPP_USERNAME)
				&& sharedPrefs.contains(Constants.XMPP_PASSWORD);
	}

	private void submitConnectTask() {

		addTask(new ConnectTask());

	}

	private void submitRegisterTask() {

		submitConnectTask();
		addTask(new RegisterTask());
	}

	// 提交登录任务
	private void submitLoginTask() {

		submitRegisterTask();
		addTask(new LoginTask());
	}

	private void addTask(Runnable runnable) {
		
		taskTracker.increase();
		synchronized (taskList) {
			if (taskList.isEmpty() && !running) {
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			} else {
				taskList.add(runnable);
			}
		}
		 
	}

	private void removeAccount() {
		Editor editor = sharedPrefs.edit();
		editor.remove(Constants.XMPP_USERNAME);
		editor.remove(Constants.XMPP_PASSWORD);
		editor.commit();
	}

	/**
	 * A runnable task to connect the server.
	 * 
	 *  观察整个项目，只有后整个地方有有一个连接网络的结构，以就是说。在整个应用中只基本上只要连接网络异常就ok.   在连接以后就保存整个连接。
	 *  以便下面的注册，登录。推送等等
	 *  
	 */
	private class ConnectTask implements Runnable {

		final XmppManager xmppManager;

		private ConnectTask() {
			this.xmppManager = XmppManager.this;
		}

		public void run() {

			if (!xmppManager.isConnected()) {
				// Create the configuration for this new connection
				// set xmppHost and xmppPort
				ConnectionConfiguration connConfig = new ConnectionConfiguration(
						xmppHost, xmppPort);
				Log.e(LOGTAG, "xmppHost--" + xmppHost + "---xmppPort--"
						+ xmppPort);

				// 配置连接网络断的信息，
				// connConfig.setSecurityMode(SecurityMode.disabled);
				connConfig.setSecurityMode(SecurityMode.required);
				connConfig.setSASLAuthenticationEnabled(false);
				connConfig.setCompressionEnabled(false);
				XMPPConnection connection = new XMPPConnection(connConfig);
				xmppManager.setConnection(connection);
				try {
					// Connect to the server
					connection.connect();
					// packet provider
					ProviderManager.getInstance().addIQProvider("notification",
							"androidpn:iq:notification",
							new NotificationIQProvider());

				} catch (XMPPException e) {
					Log.e(LOGTAG, "XMPP connection failed", e);
				}

				xmppManager.runTask();

			} else {
				Log.i(LOGTAG, "XMPP connected already");
				xmppManager.runTask();
			}
		}
	}

	/**
	 * A runnable task to register a new user onto the server.
	 * 
	 * 这里的注册，其实对于当前客户端所在的设备在服务端的一个注册。一般情况是第一次安装这个app客户端的时候;
	 * 
	 * 
	 * 这里有一个弊端。判断该客户端设备是否注册，是通过存放在本地sp中的记录来实现的。
	 * 当我第一次设备已经注册后。我清除app的数据（这时候sp的数据已经清空）。再次启动的时候会app会认为我的设备没有注册，从而再次发送注册的确请求，产生僵尸
	 * 
	 * 可以同通过   TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
      
      Log.e(tag, "当前设备的唯一标示号码----"+tm.getDeviceId()); 
          来获取设备的号码
	 * 
	 *  
	 */
	private class RegisterTask implements Runnable {

		final XmppManager xmppManager;

		private RegisterTask() {
			xmppManager = XmppManager.this;
		}

		public void run() {
		  
			Log.e(LOGTAG, "注册了吗？？"+xmppManager.isRegistered());
		
			if (!xmppManager.isRegistered()) {
				final String newUsername = newRandomUUID();
				final String newPassword = newRandomUUID();

				Registration registration = new Registration();

				//包过滤
				PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
						registration.getPacketID()), new PacketTypeFilter(
						IQ.class));

				PacketListener packetListener = new PacketListener() {

					public void processPacket(Packet packet) {
						Log.d("RegisterTask.PacketListener",
								"processPacket().....");
						Log.d("RegisterTask.PacketListener",
								"packet=" + packet.toXML());

						if (packet instanceof IQ) {
							IQ response = (IQ) packet;
							if (response.getType() == IQ.Type.ERROR) {
								if (!response.getError().toString()
										.contains("409")) {
									Log.e(LOGTAG,
											"Unknown error while registering XMPP account! "
													+ response.getError()
															.getCondition());
								}
							} else if (response.getType() == IQ.Type.RESULT) {
								xmppManager.setUsername(newUsername);
								xmppManager.setPassword(newPassword);
								Log.d(LOGTAG, "username=" + newUsername);
								Log.d(LOGTAG, "password=" + newPassword);

								Editor editor = sharedPrefs.edit();
								editor.putString(Constants.XMPP_USERNAME,
										newUsername);
								editor.putString(Constants.XMPP_PASSWORD,
										newPassword);
								editor.commit();
								Log.i(LOGTAG, "Account registered successfully");
								xmppManager.runTask();
							}
						}
					}
				};

				connection.addPacketListener(packetListener, packetFilter);

				registration.setType(IQ.Type.SET);
				// registration.setTo(xmppHost);
				// Map<String, String> attributes = new HashMap<String,
				// String>();
				// attributes.put("username", rUsername);
				// attributes.put("password", rPassword);
				// registration.setAttributes(attributes);
				registration.addAttribute("username", newUsername);
				registration.addAttribute("password", newPassword);
				connection.sendPacket(registration);

			} else {
				Log.i(LOGTAG, "Account registered already");
				xmppManager.runTask();
			}
		}
	}

	/**
	 * A runnable task to log into the server.
	 */
	private class LoginTask implements Runnable {

		final XmppManager xmppManager;

		private LoginTask() {
			this.xmppManager = XmppManager.this;
		}

		public void run() {
			// 是否身份验证
			if (!xmppManager.isAuthenticated()) {

				try {
					// 如果没有验证身份的话。需要再次获得username和password从新连接
					xmppManager.getConnection().login(
							xmppManager.getUsername(),
							xmppManager.getPassword(), XMPP_RESOURCE_NAME);

					// connection listener
					if (xmppManager.getConnectionListener() != null) {
						xmppManager.getConnection().addConnectionListener(
								xmppManager.getConnectionListener());
					}

					// 接下来会在当前连接上添加包过滤器 PacketFilter packetFilter 和包监听器
					// NotificationPacketListener
					// packetListener，包过滤器用来校验从服务器发送过来的数据包是否符合 NotificationIQ
					// 格式，打开 NotificationIQ
					// 类我们可以看到这个类中定义了数据包中需要封装的信息：id、apiKey、title、message、uri。包监听器则是用来真正处理从服务器发过来的数据。
					// packet filter
					PacketFilter packetFilter = new PacketTypeFilter(
							NotificationIQ.class);
					// packet listener
					PacketListener packetListener = xmppManager
							.getNotificationPacketListener();
					connection.addPacketListener(packetListener, packetFilter);
					xmppManager.runTask();

				} catch (XMPPException e) {
					Log.e(LOGTAG, "LoginTask.run()... xmpp error");
					Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
							+ e.getMessage());
					String INVALID_CREDENTIALS_ERROR_CODE = "401";
					String errorMessage = e.getMessage();
					if (errorMessage != null
							&& errorMessage
									.contains(INVALID_CREDENTIALS_ERROR_CODE)) {
						xmppManager.reregisterAccount();
						return;
					}
					xmppManager.startReconnectionThread();

				} catch (Exception e) {
					Log.e(LOGTAG, "LoginTask.run()... other error");
					Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
							+ e.getMessage());
					xmppManager.startReconnectionThread();
				}

			} else {
				Log.i(LOGTAG, "Logged in already");
				xmppManager.runTask();
			}

		}
	}

}
