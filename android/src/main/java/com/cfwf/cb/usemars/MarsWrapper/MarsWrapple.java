package com.cfwf.cb.usemars.MarsWrapper;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.cfwf.cb.usemars.MarsControl;
import com.marslibrary.BuildConfig;
import com.tencent.mars.BaseEvent;
import com.tencent.mars.Mars;
import com.tencent.mars.app.AppLogic;
import com.tencent.mars.stn.StnLogic;
import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by antmon on 2018/5/25.
 */

public class MarsWrapple implements StnLogic.ICallBack, AppLogic.ICallBack {
    private Context context = null;

    public static int IDENTFY_CHECK_CMDID = 8; // for gate svr

    // for AppLogic callback
    private static final String TAG = "useMars.MarsWrapple";

    private AppLogic.AccountInfo accountInfo = new AppLogic.AccountInfo();

    public static final String DEVICE_NAME = android.os.Build.MANUFACTURER + "-" + android.os.Build.MODEL;
    public static String DEVICE_TYPE = "android-" + android.os.Build.VERSION.SDK_INT;
    private AppLogic.DeviceInfo info = new AppLogic.DeviceInfo(DEVICE_NAME, DEVICE_TYPE);

    private int clientVersion = 200;
    private ClientConnImp clientConnImp = null;
    private boolean isLogined = false;

    private static MarsWrapple instance = null;
    private static int count;
    private boolean bFisrt = true;//首次登陆成功

    public static MarsWrapple getSingleton() {
        if (null == instance) {
            instance = new MarsWrapple();
            count = 0;
        }
        return instance;
    }

    public void init(Context context, ClientConnImp clientConnImp) {
        this.context = context;
        this.clientConnImp = clientConnImp;
    }

    private static final int FIXED_HEADER_SKIP = 4 + 2 + 2 + 4 + 4;

    public void setForeground(int isForeground) {
        BaseEvent.onForeground(isForeground == 1);
    }

    @Override
    public String getAppFilePath() {
        if (null == context) {
            return null;
        }

        try {
            File file = context.getFilesDir();
            if (!file.exists()) {
                file.createNewFile();
            }
            return file.toString();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    @Override
    public AppLogic.AccountInfo getAccountInfo() {
        return accountInfo;
    }

    @Override
    public int getClientVersion() {
        return clientVersion;
    }

    @Override
    public AppLogic.DeviceInfo getDeviceType() {
        return info;
    }

    // for StnLogic Callback
    @Override
    public boolean makesureAuthed() {
        return false;
    }

    @Override
    public String[] onNewDns(final String host) {
        return null;
    }

    public void onPush(final int cmdid, final byte[] data) {
        if (clientConnImp != null) {
            int groupid = cmdid;
            groupid = ((groupid & 0xFFFF0000) >> 16);
            int cmdId = cmdid;
            cmdId = (cmdid & 0x0000FFFF);
            Log.i(TAG, "onPush cmdid: " + cmdid + " groupid: " + groupid + " cmdId: " + cmdId);

            if (groupid == 0) {
                clientConnImp.OnServerPush(groupid, cmdId, data);
            } else {
                // decrypt data
                byte[] plainData = clientConnImp.NetDeCryptLonglinkCmdData(data);
                clientConnImp.OnServerPush(groupid, cmdId, plainData);
            }
        }
    }

    public boolean req2Buf(final int taskID, Object userContext, ByteArrayOutputStream reqBuffer, int[] errCode, int channelSelect) {
        Log.i(TAG, "req2Buf taskid: " + taskID);
        CGITask cgiTask = null;
        synchronized (this) {
            if (mapTask.containsKey(taskID)) {
                cgiTask = mapTask.get(taskID);
            } else {
                Log.e(TAG, "req2Buf not find taskid: " + taskID);
                return false;
            }
        }
        if (cgiTask.cmdid == 0) {
            Log.e(TAG, "req2Buf task is null");
        } else if (cgiTask.data.length > 0) {
            try {
                reqBuffer.write(cgiTask.data);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "req2buf write failed for short, check your encode process");
            }
        }
        return true;
    }

    public int buf2Resp(final int taskID, Object userContext, final byte[] respBuffer, int[] errCode, int channelSelect) {
        Log.i(TAG, "buf2Resp taskid: " + taskID);
        CGITask cgiTask = new CGITask();
        synchronized (this) {
            if (mapTask.containsKey(taskID)) {
                cgiTask = mapTask.get(taskID);
            } else {
                Log.e(TAG, "buf2Resp not find taskid: " + taskID);
                return StnLogic.RESP_FAIL_HANDLE_DEFAULT;
            }
        }

        // decrypt data
        byte[] plainData = clientConnImp.NetDeCryptLonglinkCmdData(respBuffer);

        // 投递给上层处理
        if (clientConnImp != null) {
            if (cgiTask.channelType == StnLogic.Task.ELong) {
                clientConnImp.OnTaskResponse(taskID, cgiTask.cmdGroupid, cgiTask.cmdid, plainData, cgiTask.userData1);
            }
        }
        return StnLogic.RESP_FAIL_HANDLE_NORMAL;
    }

    public int onTaskEnd(final int taskID, Object userContext, final int errType, final int errCode) {
        Log.i(TAG, "onTaskEnd taskid: " + taskID);
        CGITask cgiTask = new CGITask();
        synchronized (this) {
            if (mapTask.containsKey(taskID)) {
                cgiTask = mapTask.get(taskID);
                Log.i(TAG, "onTaskEnd remove from mapTask, taskid:" + taskID);
                mapTask.remove(taskID);
            } else {
                Log.e(TAG, "onTaskEnd not find taskid: " + taskID);
                return StnLogic.RESP_FAIL_HANDLE_DEFAULT;
            }
        }

        // 投递给上层处理
        if (errType != StnLogic.ectOK && clientConnImp != null) {
            if (cgiTask.channelType == StnLogic.Task.ELong) {
                clientConnImp.OnTaskFail(taskID, cgiTask.cmdGroupid, cgiTask.cmdid, errType, errCode, cgiTask.userData1);
            }
        }

        return StnLogic.RESP_FAIL_HANDLE_NORMAL;
    }

    public void trafficData(final int send, final int recv) {
        sendLen += send;
        recvLen += recv;
    }

    public void reportConnectInfo(int status, int longlinkstatus) {
        android.util.Log.e("yang", "reportConnectInfo status: ss " + status + " longlinkstatus: " + longlinkstatus);
        if (clientConnImp == null) {
            return;
        }
        if (longlinkstatus == StnLogic.CONNECTED) {
            //通信层逻辑
            if (!isLogined) {
                clientConnImp.onLoginSucceed(true);
            } else {
//                promptMsg("服务器已连接,如有数据相关操作，请重试",4);
                //写日志
                String msg = "连接状态变化 reportConnectInfo: 服务器已连接" + " userid:" + MarsControl.getSingleton().account;
            }
            isLogined = true;
            Log.i(TAG, "reportConnectInfo login ok");
        } else if (longlinkstatus == StnLogic.CONNECTTING) {
            android.util.Log.e("yang", "reportConnectInfo status: " + status + " longlinkstatus: " + longlinkstatus);
//            promptMsg("正在拼命连接服务器……",3);
            //写日志
            String msg = "连接状态变化 reportConnectInfo: 正在连接服务器" + " userid:" + MarsControl.getSingleton().account;
        } else {
            if (isLogined || bFisrt) {
                clientConnImp.onLoginSucceed(false);
                bFisrt = false;
            }
            isLogined = false;
            Log.i(TAG, "reportConnectInfo login failed");
            //写日志
            String msg = "连接状态变化 reportConnectInfo login failed: " + " userid:" + MarsControl.getSingleton().account;
        }

        clientConnImp.OnNetworkStatusChange(longlinkstatus);
    }

    private static Toast toast;

    //断线、重连提示信息
    public void promptMsg(final String msg, final int status) {
    }

    public int getLongLinkIdentifyCheckBuffer(ByteArrayOutputStream identifyReqBuf, ByteArrayOutputStream hashCodeBuffer, int[] reqRespCmdID) {
        reqRespCmdID[0] = IDENTFY_CHECK_CMDID;
        byte[] checkData = clientConnImp.NetGetIdentityCheckRequestData();

        try {
            identifyReqBuf.write(checkData);
            return StnLogic.ECHECK_NOW;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getLongLinkIdentifyCheckBuffer write failed for short, check your encode process");
        }
        return StnLogic.ECHECK_NEVER;
    }

    public boolean onLongLinkIdentifyResp(final byte[] buffer, final byte[] hashCodeBuffer) {
        return clientConnImp.NetParseIdentityCheckResponseData(buffer);
    }

    public void requestDoSync() {

    }

    public String[] requestNetCheckShortLinkHosts() {
        return null;
    }

    public boolean isLogoned() {
        return isLogined;
    }

    public void reportTaskProfile(String taskString) {

    }

    // For network
    private Map<Integer, CGITask> mapTask = null;
    Integer sendLen = 0;
    Integer recvLen = 0;

    public static class CGITask {
        public int channelType;
        public String cgi;
        public String host;

        public int cmdGroupid;
        public int cmdid;
        public byte[] data; // 加密后的protobuf序列数据

        public boolean bSendOnly;
        public boolean networkStatusSensitive;

        public String userData1;
        public String userData2;

        public CGITask() {
            cmdGroupid = 0;
            cmdid = 0;
            userData1 = null;
            bSendOnly = false;
            networkStatusSensitive = false;
        }

    }

    int startTask(CGITask task) {
        StnLogic.Task stnTask = new StnLogic.Task();
        stnTask.cgi = task.cgi;
        stnTask.cmdID = task.cmdGroupid;
        stnTask.cmdID = (stnTask.cmdID << 16 | task.cmdid);
        stnTask.sendOnly = task.bSendOnly;
        stnTask.networkStatusSensitive = task.networkStatusSensitive;
        stnTask.channelSelect = task.channelType;
        stnTask.userContext = task;
        stnTask.limitFlow = false;
        stnTask.limitFrequency = false;

        synchronized (this) {
            if (mapTask == null) {
                mapTask = new HashMap<Integer, CGITask>();
            }
            mapTask.put(stnTask.taskID, task);
        }

        Log.i(TAG, "startTask: taskid: " + stnTask.taskID);
        StnLogic.startTask(stnTask);
        return stnTask.taskID;
    }

    public int sendTask(int moudle_id, int cmd_id, byte[] data, int datalen, String user_data) {
        if (false == isLogoned()) {
            Log.i(TAG, "NetSendTask when not logined");
            return 0;
        }

        // encrypt data

        CGITask task = new CGITask();
        task.channelType = StnLogic.Task.ELong;
        task.cmdGroupid = moudle_id;
        task.cmdid = cmd_id;
        task.data = clientConnImp.NetEnCryptLonglinkCmdData(data);
        task.userData1 = user_data;
        task.bSendOnly = false;
        task.networkStatusSensitive = false;

        // Send
        startTask(task);
        return task.cmdGroupid;
    }

    public int sendLogoutCmd() {
        if (false == isLogoned()) {
            Log.i(TAG, "sendLogoutCmd when not logined");
            return 0;
        }

        CGITask task = new CGITask();
        task.channelType = StnLogic.Task.ELong;
        task.cmdGroupid = 0;
        task.cmdid = 9;
        task.data = "".getBytes();
        task.bSendOnly = true;
        task.networkStatusSensitive = true;

        startTask(task);
        return task.cmdGroupid;
    }

    // mars初始化，通讯层初始化后调用
    public static void marsInit(Context context) {
        // mars
        System.loadLibrary("stlport_shared");
        System.loadLibrary("marsxlog");
        System.loadLibrary("marsstn");

        final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String logPath = SDCARD + "/usemars/log";

        // this is necessary, or may cash for SIGBUS
        final String cachePath = context.getFilesDir() + "/usemars";

        //init xlog
        if (BuildConfig.DEBUG) {
            Xlog.appenderOpen(Xlog.LEVEL_DEBUG, Xlog.AppednerModeAsync, cachePath, logPath, "usemars", "");
            Xlog.setConsoleLogOpen(true);

        } else {
            Xlog.appenderOpen(Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, cachePath, logPath, "usemars", "");
            Xlog.setConsoleLogOpen(false);
        }

        Log.setLogImp(new Xlog());
        MarsWrapple marsWrapple = MarsWrapple.getSingleton();
        marsWrapple.init(context, ClientConnImp.getSingleton());
        Mars.init(marsWrapple.context, new Handler(Looper.getMainLooper()));
    }

    // mars初始化，通讯层初始化后调用
    public static void marsStart(long uin, String userName) {
        // set callback
        MarsWrapple marsWrapple = MarsWrapple.getSingleton();
        marsWrapple.accountInfo.uin = uin;
        marsWrapple.accountInfo.userName = userName;
        AppLogic.setCallBack(marsWrapple);
        StnLogic.setCallBack(marsWrapple);

        String ip = ClientConnImp.getSingleton().NetQueryWebConfig("gate_server", "ip");
        android.util.Log.i(TAG, "server addr: " + ip);
        String[] ipList = ip.split(";");
        ip = ipList[0];
        String portStr = ClientConnImp.getSingleton().NetQueryWebConfig("gate_server", "port");
        String[] portList = portStr.split(";");
        portStr = portList[0];
        int port = Integer.parseInt(portStr);
        // Initialize the Mars
        int[] longLinkPorts = {port};
        StnLogic.setLonglinkSvrAddr(ip, longLinkPorts);
        Mars.onCreate(true);

        BaseEvent.onForeground(true);
        StnLogic.makesureLongLinkConnected();
        //
    }

    // 程序退出时调用
    public static void marsUnit() {
        byte[] str = new byte[30];
//        marsSend(0,9,"".getBytes(),0,"marsUnit");  //有问题的调用
        Mars.onDestroy();
        Log.appenderClose();
        MarsWrapple.getSingleton().bFisrt = true;
        MarsWrapple.getSingleton().isLogined = false;
    }

    // 发送数据
    public static int marsSend(int moudle_id, int cmd_id, byte[] data, int datalen, String user_data) {
        //写日志
        String msg = "moudle_id:" + moudle_id + " cmd_id:" + cmd_id + " request:" + user_data + " " +
                " userid:" + MarsControl.getSingleton().account;

        return MarsWrapple.getSingleton().sendTask(moudle_id, cmd_id, data, datalen, user_data);
    }

    public static void Reset() {
        StnLogic.reset();
    }

    // 前后台切换时调用
    public static void onForeground(boolean isForeground) {
        BaseEvent.onForeground(isForeground);
    }

    // 网络切换时调用
    public static void onNetworkChange() {
        BaseEvent.onNetworkChange();
    }
}
