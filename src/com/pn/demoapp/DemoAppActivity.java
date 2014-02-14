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
package com.pn.demoapp;

import java.util.Properties;

import org.androidpn.demoapp.R;

import com.pn.client.ServiceManager;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * This is an androidpn client demo application.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class DemoAppActivity extends Activity {
  String tag =  "DemoAppActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DemoAppActivity", "onCreate()...");
      TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
      
      
      
      
//      WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);  
//      WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
//      int ipAddress = wifiInfo.getIpAddress();  
      
  //    Log.e(tag, "当前设备的唯一标示号码----"+tm.getDeviceId()+"手机ip"+ipAddress);
      
      
      
    
      
      
       super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Settings
        Button okButton = (Button) findViewById(R.id.btn_settings);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	 ServiceManager.viewNotificationSettings(DemoAppActivity.this);
            }
        });

        // Start the service
        
        	ServiceManager serviceManager = new ServiceManager(this);
            serviceManager.setNotificationIcon(R.drawable.notification);
            serviceManager.startService();
      
        
    }

}