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
package com.pn.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.androidpn.demoapp.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * This class is to notify the user of messages with NotificationManager.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class Notifier {

	private static final String LOGTAG ="Notifier";

	private static final Random random = new Random(System.currentTimeMillis());

	private Context context;

	private SharedPreferences sharedPrefs;

	private NotificationManager notificationManager;

	public Notifier(Context context) {
		this.context = context;
		this.sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		this.notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void notify(String notificationId, String apiKey, String title,
			String message, String uri) {
		Log.d(LOGTAG, "notify()...");

		Log.d(LOGTAG, "notificationId=" + notificationId);
		Log.d(LOGTAG, "notificationApiKey=" + apiKey);
		Log.d(LOGTAG, "notificationTitle=" + title);
		Log.d(LOGTAG, "notificationMessage=" + message);
		Log.d(LOGTAG, "notificationUri=" + uri);

		if (isNotificationEnabled()) {
			// Show the toast
			if (isNotificationToastEnabled()) {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}

			// Notification
			Notification notification = new Notification();
			notification.icon = getNotificationIcon();
			notification.defaults = Notification.DEFAULT_LIGHTS;
			if (isNotificationSoundEnabled()) {
				notification.defaults |= Notification.DEFAULT_SOUND;
			}
			if (isNotificationVibrateEnabled()) {
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.when = System.currentTimeMillis();
			notification.tickerText = message;

			// Intent intent;
			// if (uri != null
			// && uri.length() > 0
			// && (uri.startsWith("http:") || uri.startsWith("https:")
			// || uri.startsWith("tel:") || uri.startsWith("geo:"))) {
			// intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			// } else {
			// String callbackActivityPackageName = sharedPrefs.getString(
			// Constants.CALLBACK_ACTIVITY_PACKAGE_NAME, "");
			// String callbackActivityClassName = sharedPrefs.getString(
			// Constants.CALLBACK_ACTIVITY_CLASS_NAME, "");
			// intent = new Intent().setClassName(callbackActivityPackageName,
			// callbackActivityClassName);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// }

			Intent intent = new Intent(context,
					NotificationDetailsActivity.class);
			intent.putExtra(Constants.NOTIFICATION_ID, notificationId);
			intent.putExtra(Constants.NOTIFICATION_API_KEY, apiKey);
			intent.putExtra(Constants.NOTIFICATION_TITLE, title);
			intent.putExtra(Constants.NOTIFICATION_MESSAGE, message);
			intent.putExtra(Constants.NOTIFICATION_URI, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(context, title, message,
					contentIntent);
			notificationManager.notify(random.nextInt(), notification);

			// --------------myself---����wiget-------------------------------
//			ComponentName name = new ComponentName(context.getPackageName(),
//					"org.androidpn.client.widget.MyWidget");
//			RemoteViews views = new RemoteViews(context.getPackageName(),
//					R.layout.example_appwidget);
//			// views.setTextViewText(R.id.widget_tv, message);
//
//			views.setOnClickPendingIntent(R.layout.example_appwidget,
//					contentIntent);
//			String path = message.substring(message.indexOf("http"), message.indexOf("jpg")+3);
// 			Log.e(LOGTAG, "·��"+path);
// 			if("".equals(path)||path==null){
// 				Log.e(LOGTAG, "û��ͼƬ���͹���");
// 				return;
// 			}
//			URL url = null;
//			HttpURLConnection conn = null;
//			InputStream is = null;
//			Bitmap bitmap = null;
//
//			try {
//				// �����path Ĭ���ǻ�ȡ�����͵�ʱ���message ���ݡ� ������͵����ݲ��Ǵ�url����ô����ᱨ��
//				
//				
//				 
//				url = new URL(path);
//				conn = (HttpURLConnection) url.openConnection();
//				is = conn.getInputStream();
//
//				bitmap = BitmapFactory.decodeStream(is);
//
//				views.setImageViewBitmap(R.id.widget_iv, bitmap);
//
//				AppWidgetManager am = AppWidgetManager.getInstance(context);
//
//				am.updateAppWidget(name, views);
//
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				Log.e("test", "MalformedURLException");
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				Log.e("test", "IOException");
//				e.printStackTrace();
//			} finally {
//				if (bitmap != null && !bitmap.isRecycled()) {
//					bitmap.recycle();
//					bitmap = null;
//				}
//
//				if (is != null) {
//					try {
//						is.close();
//						is = null;
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				conn.disconnect();
//			}

			// Intent clickIntent = new Intent(
			// Constants.ACTION_NOTIFICATION_CLICKED);
			// clickIntent.putExtra(Constants.NOTIFICATION_ID, notificationId);
			// clickIntent.putExtra(Constants.NOTIFICATION_API_KEY, apiKey);
			// clickIntent.putExtra(Constants.NOTIFICATION_TITLE, title);
			// clickIntent.putExtra(Constants.NOTIFICATION_MESSAGE, message);
			// clickIntent.putExtra(Constants.NOTIFICATION_URI, uri);
			// // positiveIntent.setData(Uri.parse((new StringBuilder(
			// // "notif://notification.adroidpn.org/")).append(apiKey).append(
			// // "/").append(System.currentTimeMillis()).toString()));
			// PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
			// context, 0, clickIntent, 0);
			//
			// notification.setLatestEventInfo(context, title, message,
			// clickPendingIntent);
			//
			// Intent clearIntent = new Intent(
			// Constants.ACTION_NOTIFICATION_CLEARED);
			// clearIntent.putExtra(Constants.NOTIFICATION_ID, notificationId);
			// clearIntent.putExtra(Constants.NOTIFICATION_API_KEY, apiKey);
			// // negativeIntent.setData(Uri.parse((new StringBuilder(
			// // "notif://notification.adroidpn.org/")).append(apiKey).append(
			// // "/").append(System.currentTimeMillis()).toString()));
			// PendingIntent clearPendingIntent = PendingIntent.getBroadcast(
			// context, 0, clearIntent, 0);
			// notification.deleteIntent = clearPendingIntent;
			//
			// notificationManager.notify(random.nextInt(), notification);

		} else {
			Log.w(LOGTAG, "Notificaitons disabled.");
		}
	}

	private int getNotificationIcon() {
		return sharedPrefs.getInt(Constants.NOTIFICATION_ICON, 0);
	}

	private boolean isNotificationEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_NOTIFICATION_ENABLED,
				true);
	}

	private boolean isNotificationSoundEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_SOUND_ENABLED, true);
	}

	private boolean isNotificationVibrateEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_VIBRATE_ENABLED, true);
	}

	private boolean isNotificationToastEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_TOAST_ENABLED, false);
	}

}
