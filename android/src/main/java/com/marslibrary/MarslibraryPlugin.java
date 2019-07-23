package com.marslibrary;

import android.content.Context;
import android.util.Log;


import com.cfwf.cb.usemars.MarsControl;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** MarslibraryPlugin */
public class MarslibraryPlugin implements MethodCallHandler {
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "marslibrary");
    channel.setMethodCallHandler(new MarslibraryPlugin());
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    Log.e("yang","tell me why 222");
    if (call.method.equals("getPlatformVersion")) {
      Log.e("yang","tell me why 333 方法方法付付付");
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }else if (call.method.equals("sayHello")){
      Log.e("yang","tell me why 333");
//      Context context = call.argument("context");
      //调用新通信层登陆
      MarsControl.getSingleton().netInit("200532", "123456", mContext);
//      MarsWrapple.marsInit(mContext);
    }else {
      result.notImplemented();
    }
  }
  private static  Context mContext;
  public static void setContext(Context context){
    mContext = context;
  }
}
