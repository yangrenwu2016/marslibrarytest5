package com.cfwf.cb.usemars.MarsWrapper;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.cfwf.cb.business_proto.ClientConnCommon;
import com.cfwf.cb.business_proto.ClientConnIM;
import com.cfwf.cb.business_proto.ClientConnMessage;
import com.cfwf.cb.usemars.EventBusPojo;
import com.cfwf.cb.usemars.MarsControl;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tencent.mars.xlog.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by qinge on 2018/4/24.
 */

public class ClientConnImp {

    private static ClientConnImp instance = null;
    public boolean isLogin;

    public static ClientConnImp getSingleton() {
        if (null == instance) {
            instance = new ClientConnImp();
        }
        return instance;
    }

    private Context context = null;
    public final static int MOUDLE_ID_MARS = 0; //mars业务
    public final static int MOUDLE_ID_IM = 1; //即时通讯指令
    public final static int MOUDLE_ID_SCHOOL = 2; //学校业务
    /******
     * 登录结果
     *****/
    public final static int kLoginResultSuccess = 1; //登录成功
    public final static int kLoginResultUserOrPassError = 2; //用户名或密码错误
    public final static int kLoginResultTimeOut = 3; //登录超时
    public final static int kLoginResultCancel = 4; //取消登录
    public final static int kLoginResultError = 5; //其他错误
    public final static int kLoginResultIdentityError = 6; //登录限制了身份，用户实际身份与限定身份不符

    public final static int kLogOutBecauseInitiative = 0;     //用户主动退出
    public final static int kLogOutBecauseSessionLost = 1;     //因长时间处于离线状态，服务器上的登录状态已超时，需要重新登录
    public final static int kLogOutBecauseSomeoneLogin = 2;    //被其他设备上的相同帐号顶掉了
    public final static int kLogOutBecauseKickOut = 3;         //被服务器踢出了
    public final static int kLogOutBecausePasswordChange = 4;  //其他设备上修改了密码


    public native void NetInit(String config_url, String local_config_file, String logfilename, String local_version, String machine_info, String device_token, String limit_identity);

    public native void NetUnInit();

    public native void NetLogin(String loginid, String password);

    public native void NetCancelLogin();

    public native void NetLogout();

    public native String NetGetServerTime();


    public native String NetGetSessionKey();

    //public native int NetSendTask(int moudle_id, int cmd_id, byte[] data, int datalen, String user_data);

    //public native int NetSendHttpTask(String host, String cgi, String data, String user_data);
    public native String NetQueryWebConfig(String group, String key);

    public native String NetQueryWebApi(String group, String key);

    public native String NetQueryWebUrl(String group, String key);

    public native int NetGetNetworkStatus();

    //public native void NetOnForeground(boolean _isforeground);

    //public native void NetOnNetworkChange();

    public native int NetUploadFile(String files, String user_data);

    public native void NetCancelUpload(int taskid, String filename);


    public native int NetDownloadWebFile(String url, String post_data, String save_to_file, long file_size, String user_data);

    public native void NetCancelDownload(int taskid);

    public native int NetGetWebPage(String url, String post_data, String user_data);

    //[ mars
    public native byte[] NetGetIdentityCheckRequestData();

    public native boolean NetParseIdentityCheckResponseData(byte[] data);

    public native byte[] NetEnCryptLonglinkCmdData(byte[] data);

    public native byte[] NetDeCryptLonglinkCmdData(byte[] data);
    //]

    /**********
     * 手机注册， 找回密码相关  登录之前调用
     ************/
// 返回值： 0--调用失败，1--手机号未注册，2--手机号已注册
//    public native int NetCheckMobilePhoneAvailable(String phone_number); //老接口
    public native int NetCheckMobilePhoneAvailable(String phone_number,int user_ident); //新接口

    // 获取验证码
// business_type: 业务类型，对应关系为：1--注册，2--找回密码
    public native boolean NetGetCaptcha(String phone_number, int business_type);

    // 发送验证码到服务器进行验证
    public native boolean NetSendCaptcha(String phone_number, String check_code);

    // 注册新用户
// user_identity: 用户类型，对应关系为：1--中小学生，2--中小学老师
    public native String NetRegisterNewUser(String phone_number, String username_utf8, String password, int user_identity);

    // 修改密码
    public native boolean NetModifyPassword(String phone_number, String new_password);

    // 获得对应年级号 period为阶段，1小学2初中3高中；grade_year为学届 如2018
    public native int NetGetSchoolGrade(int period, int grade_year);

    public native String NetGetStringParamFromNetData(byte[] data, int index);

    public native int NetReset();

    public void OnTaskResponse(int taskid, int moudle_id, int cmd_id, byte[] resp_data, String user_data) {
        android.util.Log.e("yang", "this is OnTaskResponse run" + taskid + " moduleid:" + moudle_id + " cmdid:" + cmd_id + " userdata:" + user_data);
        //写日志
        String msg = "OnTaskResponse taskid:" + taskid + " moudle_id:" + moudle_id + " cmd_id :" + cmd_id
                + " user_data:" + user_data;
        MarsControl.getSingleton().OnTaskResponse(taskid, moudle_id, cmd_id, resp_data, user_data);
    }

    public void OnTaskFail(final int taskid, final int moudle_id, final int cmd_id, final int err_type, final int err_code, final String user_data) {
        android.util.Log.e("yang", "this is OnTaskFail run taskid:" + taskid + " moduleid:" + moudle_id + " cmdid:" + cmd_id + " err_type:" + err_type + " errcode:"
                + err_code + " userdata:" + user_data);
        //写日志
        String msg = "OnTaskFail taskid:" + taskid + " moudle_id:" + moudle_id + " cmd_id :" + cmd_id + " err_type: " + err_type
                + " err_code:" + err_code + " user_data:" + user_data;
        //失败了也要回归主线程，以免影响后续操作

    }

    public void OnHttpTaskResponse(int taskid, String host, String cgi, byte[] resp_data, String user_data) {
        android.util.Log.e("yang", "this is OnHttpTaskResponse run taskid:" + taskid + " host:" + host + " cgi:" + cgi + " userdata:" + user_data);
    }

    public void OnHttpTaskFail(int taskid, String host, String cgi, int err_type, int err_code, String user_data) {
        android.util.Log.e("yang", "this is OnHttpTaskFail run");
    }

    public void OnServerPush(int moudle_id, int cmd_id, byte[] data) {
        //写日志
        String msg = "OnServerPush :" + " moudle_id:" + moudle_id + " cmd_id :" + cmd_id;
  /*
    服务器推送:
        //收到在线转发消息(由其他用户调用 kCmdTransOnlineMsgToFriend 指令发送)
        //cfwf::message.TranspondMessage
        kOnSeverTransOnlineMessage = 10000; //
        //收到在线推送消息。只有在线才会收取，离线时消息丢弃不保存
        //cfwf::message.OnlineMessage
        kOnSeverPushOnlineMessage = 10001; //
        //收到通用消息通知：对聊、群聊、群通知、其他类型通知、广播 等，见 cfwf.message.CommonMessage.msg_type。 收到后需要检查发送方是否自己。如果是自己，则表示该消息是自己在另一设备上发送的同步消息
        //SeverPushCommonMessage
        //重要：在收到这种通知消息后，上层应主动调用指令  kCmdSetMessageReceived 通知服务器，消息已经送达。
        kOnSeverPushCommonMessage = 10002; //
        //收到计数统计型型消息通知：校内通知、家庭作业、微课、随堂检测
        //SeverPushCounterMessage
        kOnSeverPushCounterMessage = 10003; //
         */
        if (moudle_id == 0 && cmd_id == 255) {
            //与服务器的连接被断开了，断开的原因可能是有人把你顶掉了，收到后需要重新登录下
            String reason = NetGetStringParamFromNetData(data, 0);
            int logout_reason = kLogOutBecauseSessionLost;
            if (reason.equals("session_lost"))
                logout_reason = kLogOutBecauseSessionLost;
            else if (reason.equals("someone_login"))
                logout_reason = kLogOutBecauseSomeoneLogin;
            else if (reason.equals("kick_out"))
                logout_reason = kLogOutBecauseKickOut;
            else if (reason.equals("password_modify"))
                logout_reason = kLogOutBecausePasswordChange;
            else if (reason.equals("logout"))
                logout_reason = kLogOutBecauseInitiative;
            android.util.Log.e("yang", "NetGetStringParamFromNetData reason:" + reason);
            NetReset();
            OnLogOut(logout_reason);
            return;
        }
        switch (cmd_id) {
            case ClientConnIM.CMD_ID.kOnSeverTransOnlineMessage_VALUE://10000
                try {
                    ClientConnMessage.TranspondMessage transmsg = ClientConnMessage.TranspondMessage.parseFrom(data);

                } catch (Exception e) {
                }
                break;
            case ClientConnIM.CMD_ID.kOnSeverPushOnlineMessage_VALUE://10001  //一般在线推送
                try {
                    ClientConnMessage.OnlineMessage onlinemsg = ClientConnMessage.OnlineMessage.parseFrom(data);
//                    android.util.Log.e("yang", "kOnSeverPushOnlineMessage onlinemsg getContent :" + onlinemsg.getContent()+" value"+onlinemsg.getMsgTypeValue());
                    MarsControl.getSingleton().onlineMessage(onlinemsg);
                } catch (Exception e) {
                    android.util.Log.e("yang", "kOnSeverPushOnlineMessage error :" + e.getLocalizedMessage());
                }
                break;
            case ClientConnIM.CMD_ID.kOnSeverPushCommonMessage_VALUE://10002
                try {
                    ClientConnIM.SeverPushCommonMessage info = ClientConnIM.SeverPushCommonMessage.parseFrom(data);
                    ClientConnMessage.CommonMessage message = info.getMsg();
                    android.util.Log.e("yang", "收到在线的数据推送-----getMsgType:++++" + message.getMsgType());
                    MarsControl.getSingleton().setCommonMessage(info, message, 1);//不同消息的处理方法

                    //设置为已读数据(这是通用的)
                    ClientConnIM.SetMessageReceivedRequest.Builder request = ClientConnIM.SetMessageReceivedRequest.newBuilder();
                    request.setUserid(Long.parseLong(MarsControl.getSingleton().account));
                    request.setPushMsgid(info.getPushMsgid());
                    request.setOsType(ClientConnCommon.OS_TYPE.kOsTypeAndroid);
                    byte[] receiveddata = request.build().toByteArray();
                    MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_IM, ClientConnIM.CMD_ID.kCmdSetMessageReceived_VALUE, receiveddata, receiveddata.length, "setMessageReceived");

                    MarsControl.getSingleton().getGroupUserInfo();

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                break;
            case ClientConnIM.CMD_ID.kOnSeverPushCounterMessage_VALUE://10003
                try {
                    android.util.Log.e("dahui", "收到在线的数据推送--10003---getMsgType:++++");
                    ClientConnIM.SeverPushCounterMessage counterMessage = ClientConnIM.SeverPushCounterMessage.parseFrom(data);
                    ClientConnMessage.CounterMessage message = counterMessage.getMsg();
                } catch (Exception e) {
                }
                break;
//            case 255: //与服务器的连接被断开了，断开的原因可能是有人把你顶掉了，收到后需要重新登录下
//                break;
        }
//        android.util.Log.e("yang", "this is OnServerPush run");
    }

    public void OnLoginResult(int ret, long userid) {
        android.util.Log.e("yang", "this is OnLoginResult run ret " + ret);
        String msg = "";
        switch (ret) {
            case kLoginResultSuccess: //登录成功
                loginStateEvent(msg, kLoginResultSuccess);
                MarsControl.getSingleton().account = userid + "";
                MarsWrapple.marsStart(userid, "");
                break;
            case kLoginResultUserOrPassError:
                msg = "用户名或密码错误";
                loginStateEvent(msg, kLoginResultUserOrPassError);
                break;
            case kLoginResultTimeOut:
                msg = "登录超时";
                loginStateEvent(msg, kLoginResultTimeOut);
                break;
            case kLoginResultCancel:
                msg = "取消登录";
                loginStateEvent(msg, kLoginResultCancel);
                break;
            case kLoginResultError:
                msg = "其他错误";
                loginStateEvent(msg, kLoginResultError);
                break;
            case kLoginResultIdentityError:
//                msg = "账号的身份与限定身份不符";
                msg = "";
                loginStateEvent(msg, kLoginResultIdentityError);
               downloadStudentVersion();
                break;
        }
    }

    private void loginStateEvent(String msg, int state) {
    }

    public void onLoginSucceed(boolean isLogin) { //登录成功或断线重连
        android.util.Log.e("yang", "this is OnLoginSucceed run " + isLogin);
        MarsControl.getSingleton().sessionKey = ClientConnImp.getSingleton().NetGetSessionKey();
        //获取登录者个人信息
        if (isLogin) {
        }
    }

    public boolean isManual; //是否手动退出

    public void OnLogOut(int logOutBecause) {
        Log.e("yang", "this is OnLogOut run11 ret:" + logOutBecause);
        MarsWrapple.Reset();
        Log.e("yang", "this is OnLogOut run222 ret:" + logOutBecause);
        isManual = true;
        switch (logOutBecause) {
            case kLogOutBecauseInitiative:  //用户主动退出
                Log.e("yang", "this is OnLogOut run333 ret:" + logOutBecause);
//                sameAccountLogin("确定退出当前账号？");
                break;
            case kLogOutBecauseSessionLost: //因长时间处于离线状态，服务器上的登录状态已超时，需要重新登录
//                MarsWrapple.getSingleton().promptMsg("登录状态已超时,正在重新自动登陆……",-1);
                sameAccountLogin("登录状态已超时,请重新登陆");
                break;
            case kLogOutBecauseSomeoneLogin:  //被其他设备上的相同帐号顶掉了
                sameAccountLogin("该用户在其他地方登录");
                break;
            case kLogOutBecauseKickOut:   //被服务器踢出了
                sameAccountLogin("和服务器的连接已断开");
                break;
            case kLogOutBecausePasswordChange:  //其他设备上修改了密码
                sameAccountLogin("您在其他设备修改了密码，为了安全起见，请退出重新登录");
                break;

        }
        Log.e("yang", "this is OnLogOut run88888 ret:" + logOutBecause);
///       Control.getSingleton().getClose();
        //MarsWrapple.marsUnit();
    }

    public void OnGetVersionInfo(String version, String download_url, int filesize, String update_list_url) {
        android.util.Log.e("yang", "this is OnGetVersionInfo run");
        MarsControl.getSingleton().OnGetVersionInfo(version, download_url, filesize, update_list_url);
    }

    public void OnNetworkStatusChange(int status) {
        Log.e("yang", "this is OnNetworkStatusChange run");
    }

    public void OnUploadFileBegin(int taskid, String filename, long filesize, String user_data) {
        android.util.Log.e("yang", "this is OnUploadFileBegin run taskid:" + taskid + " filename:" + filename + " filesize:" + filesize);
    }

    public void OnUploadFileFinish(int taskid, String filename, String file_resid, String user_data) {
        android.util.Log.e("yang", "this is OnUploadFileFinish run taskid:" + taskid + " filename:" + filename);
    }

    public void OnUploadFileFail(int taskid, String filename, String errmsg, String user_data) {

        android.util.Log.e("yang", "this is OnUploadFileFail run taskid:" + taskid + " filename:" + filename);
    }

    public void OnUploadFileProcess(int taskid, String filename, long filesize, long finish_size, String user_data) {
        android.util.Log.e("yang", "this is OnUploadFileProcess run taskid:" + taskid + " filename:" + filename);
    }

    public void OnUploadEnd(int taskid, boolean upload_success, String file_res_list, String user_data) {
        Log.e("yang", "this is OnUploadEnd run file_res_list：" + file_res_list);
    }

    public void OnDownLoadFileBegin(int taskid, String url, String save_to_file, long filesize, String user_data) {
        android.util.Log.e("yang", "this is OnDownLoadFileBegin run");
    }

    public void OnDownLoadFileFinish(int taskid, String url, String save_to_file, String user_data) {
        Log.e("yang", "this is OnDownLoadFileFinish run");
    }

    public void OnDownLoadFileFail(int taskid, String url, String save_to_file, String errmsg, String user_data) {
        android.util.Log.e("yang", "this is OnDownLoadFileFail run");
        if (MarsControl.getSingleton().downloadApk) { //当前正在下载apk安装包
        }
    }

    public void OnDownLoadFileProcess(int taskid, String url, String save_to_file, long filesize, long finish_size, String user_data) {
//        android.util.Log.e("yang", "this is OnDownLoadFileProcess run filesize："+filesize + " finish_size:"+finish_size);
        if (MarsControl.getSingleton().downloadApk) { //当前正在下载apk安装包
            final double size = (double) finish_size / (double)  filesize;

        }
    }

    public void OnGetWebPageFinish(int taskid, String url, String response_content, String user_data) {
        Log.e("yang", "this is OnGetWebPageFinish run");
    }

    public void OnGetWebPageFail(int taskid, String url, String errmsg, String user_data) {
        Log.e("yang", "this is OnGetWebPageFail run");
    }

    //安装包下载完成处理
    private void apkDownFinishEvent(boolean isSuccess) {
        MarsControl.getSingleton().downloadApk = false;
    }
    //学生端安装包下载完成处理
    private void apkDownFinishStudent(boolean isSuccess) {
        MarsControl.getSingleton().downloadApk = false;
        String[]  url = download_url.split("\\/");
        String fileName = url[url.length-1];
    }
    //同一账号登录，被挤下线
    public void sameAccountLogin(final String msg) {
        Activity activity = null;
        showExitPrompt(activity, msg);
    }

    private void showExitPrompt(final Activity activity, final String msg) {
    }
    private String  download_url;
    //去下载学生版本
    private void downloadStudentVersion(){
         download_url = ClientConnImp.getSingleton().NetQueryWebConfig("android_update_student","download_url"); //android_update_student  android_update
        String  version = ClientConnImp.getSingleton().NetQueryWebConfig("android_update_student","version"); //version
        String   filesize = ClientConnImp.getSingleton().NetQueryWebConfig("android_update_student","filesize"); //filesize
        Log.e("yang","downloadUrl:"+download_url+" version:"+version+" filesize:"+filesize);
        try {
            if (!TextUtils.isEmpty(download_url)){
                //手机中没有安装学生版（家庭版）
            }
//            CommonUtil.getInstance().installProgramStudent(OverallSituation.APKPATH+"caibao_student.apk");
        }catch (Exception e){
            Log.e("yang","Exception: error "+e.getLocalizedMessage());
        }
    }

}
