package com.pn.client.mywidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.sax.StartElementListener;

public class MyWidget extends AppWidgetProvider {
   private 	 Intent  i;
	@Override
	public IBinder peekService(Context myContext, Intent service) {
		// TODO Auto-generated method stub
		return super.peekService(myContext, service);
	}

	 
//	public void onAppWidgetOptionsChanged(Context context,
//			AppWidgetManager appWidgetManager, int appWidgetId,
//			Bundle newOptions) {
//		 
//		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
//				newOptions);
//	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		 
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
	 
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		 
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		 
		 super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		 
		super.onDisabled(context);
	}

}
