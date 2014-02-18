package com.pn.client.mywidget;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

 

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class WidgetService extends Service {
   private Timer timer;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate() {
		 
		 timer = new Timer();
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
//				 ComponentName name = new ComponentName(getPackageName(), getPackageName()+".MyWidget");
//				RemoteViews views = new RemoteViews(getPackageName(), R.layout.example_appwidget); 
//				int time =new Random().nextInt();
//				views.setTextViewText(R.id.textView1,time+"");
//				AppWidgetManager  am =	AppWidgetManager.getInstance(getApplicationContext());
//				 
//				am.updateAppWidget(name, views);
//				System.out.println("----"+time);
//				
				
			}
		};
		timer.scheduleAtFixedRate(task, 0, 1000);
	};
  @Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	     System.out.println("服务关闭了");
	     if(timer!=null){
	    	 timer.cancel();
	     }
		super.onDestroy();
	}
}
