package com.cfwf.cb.usemars;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.cfwf.cb.business_proto.ClientConnIM;
import com.cfwf.cb.business_proto.ClientConnMessage;
import com.cfwf.cb.business_proto.ClientConnSchool;
import com.cfwf.cb.usemars.MarsWrapper.ClientConnImp;
import com.cfwf.cb.usemars.MarsWrapper.MarsWrapple;
import com.tencent.mars.xlog.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;


/**
 * Created by antmon on 2018/5/26.
 */

// follows are business test

public class MarsControl {
    public static final String TAG = "yang";

    private static MarsControl instance = null;
    public boolean isReconnect; //是不是被服务器断开，断线重连
    public ClientConnIM.GetMyInfoResponse LoginFenboo; //登录者个人信息, 对应原通讯层的 TNConnEventData_LoginFenboo
    public ClientConnIM.GetUserBaseInfoResponse baseInfoResponse; //设置个人信息, 对应原通讯层的 NConnSetMyBaseInfo
    public ClientConnIM.GetUserBaseInfoResponse userBaseInfo;  //对应原通讯层的,  NConnGetUserBaseInfo
    public String account, passWord; //账号 、密码
    public String sessionKey;
    public ClientConnSchool.GetMyTeachSchoolInfoResponse teachSchoolInfoResponse; //老师教学信息
    public ClientConnSchool.GetSchoolInfoResponse myShoolInfo; //个人所属学校信息
    public List<ClientConnIM.GroupInfo> groupInfoList; //通讯录群组（群聊）列表
    public boolean backstage = false; //是否在后台
    private int MsgType; //主要用于 kMsgTypeSetSchoolAdmin 后台添加移除管理员

    public static MarsControl getSingleton() {
        if (null == instance) {
            instance = new MarsControl();
        }
        return instance;
    }
    public static String SERVICEURL = "http://www.91cb.net/_client_config/test/client_config.php";
    // 初始化(只在刚开始调一次，此后手动退出再登录也不应该调此函数，只调 NetLogin 即可)
    public void netInit(String account, String password, Context context) {
        android.util.Log.e(TAG, "you make me down" );
        this.account = account;
        this.passWord = password;
        JSONObject json = new JSONObject();
        String brand = android.os.Build.BRAND;
        String model = Build.MODEL; //型号
        android.util.Log.e("yang", "netInit password:" + password + " account:" + account + "  isACTIVITY "  + "--getVerName--" );
        try {
            json.put("brand", brand);
            json.put("model", model);
            android.util.Log.e(TAG, "BRAND:" + brand + " model:" + model + " account:" + account + " password:" + password);
            String local_config_file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fenboo/local_config.txt";
            //初始化并登陆
            System.loadLibrary("ClientConn");
//            ClientConnImp.getSingleton().NetInit(OverallSituation.SERVICEURL, "", "*", CommonUtil.getInstance().getVerName(OverallSituation.isACTIVITY), json.toString(), "1","");
            ClientConnImp.getSingleton().NetInit(SERVICEURL,local_config_file, "*", "2.1.3", json.toString(), "1", "teacher"); // student teacher
        //测试
//            MarsWrapple.marsInit(context.getApplicationContext());
            if (context == null){
                android.util.Log.e(TAG, "context == null:" );
            }
            MarsWrapple.marsInit(context);
        } catch (Exception e) {
            android.util.Log.e(TAG, "tell me the error:" + e.getLocalizedMessage());
        }
    }

    //登录事件
    public void loginEvent(String account, String password) {
        this.account = account;
        this.passWord = password;
        //写本地日志记录登录事件
        String msg = "正在调用 loginEvent NetLogin 事件";
        ClientConnImp.getSingleton().NetLogin(account, password); //userid , password     teacher
    }


    public boolean downloadApk = false;
    public String newVersion;

    public void OnGetVersionInfo(String versions, String download_url, int filesize, String update_list_url) {
        newVersion = versions;
        //apk版本检查
        android.util.Log.e("yang", "apk版本检查 versions:" + versions + " download_url:" + download_url + " filesize:" + filesize + " update_list_url:" + update_list_url);

        if (!downloadApk) {
            //新版apk
//           String[] newVersion = new String(versions).split("\\.");
//           //用户当前已有apk
//           String[] myVersion = CommonUtil.getInstance().getVerName(OverallSituation.isACTIVITY)
//                   .split("\\.");

//         if (Integer.parseInt(newVersion[0]) > Integer.parseInt(myVersion[0])|| Integer.parseInt(newVersion[1]) > Integer.parseInt(myVersion[1])
//                   || Integer.parseInt(newVersion[2]) > Integer.parseInt(myVersion[2])) { //版本更新
//               downloadApk = true;
//               if (new File(OverallSituation.PhotoPATH + "caibao" + newVersion + ".apk")
//                       .exists()) {
//                   CommonUtil.getInstance().installProgram("caibao" + newVersion + ".apk");
//               } else {
//                   BoutiquePromptDialog.testDlg(LoadingActivity.activity, versions, "");
//               }
//           } else {//正常登陆
//               downloadApk = false;
//               if (Control.getSingleton().autoLogin == 1) {
//                   loginEvent(account,passWord);
//               } else {
//                   LoginActivity.loginActivity.login();
//               }
//           }
//           Log.e("dahui", "-1***=="+compareVersion("1.1.52","2.0.0"));
//           Log.e("dahui", "1***=="+compareVersion("2.0.0","1.1.52"));
//           Log.e("dahui", "1***=="+compareVersion("2.0.1","2.0.0"));
//           Log.e("dahui", "0***=="+compareVersion("2.0.0","2.0.0"));
        }
    }


    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");
        Log.e("dahui", "version1Array==" + version1Array.length);
        Log.e("dahui", "version2Array==" + version2Array.length);
        int index = 0;
        // 获取最小长度值
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;
        // 循环判断每位的大小
        Log.e("dahui", "verTag2=2222=" + version1Array[index]);
        while (index < minLen
                && (diff = Integer.parseInt(version1Array[index])
                - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            // 如果位数不一致，比较多余位数
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }


    //任务开始
    public void OnDownLoadFileBegin(int taskid, String url, String save_to_file, long filesize, String user_data) {

    }

    public void OnDownLoadFileFinish(int taskid, String url, String save_to_file, String user_data) {
        if (downloadApk) {
            downloadApk = false;
        }

    }

    public void OnDownLoadFileFail(int taskid, String url, String save_to_file, String errmsg, String user_data) {
        if (downloadApk) {
            downloadApk = false;
        }
    }

    //下载过程中
    public void OnDownLoadFileProcess(int taskid, String url, String save_to_file, long filesize, long finish_size, String user_data) {
        long size = (long) finish_size / (long) filesize;
        if (downloadApk) {
        }
    }

    //成功返回
    public void OnTaskResponse(int taskid, int moudle_id, int cmd_id, byte[] resp_data, String user_data) {

    }

    public void OnTaskFail(int taskid, int moudle_id, int cmd_id, int err_type, int err_code, String user_data) {
        if (moudle_id == ClientConnImp.MOUDLE_ID_MARS) {
        }
    }

    //服务器推送
    public void OnServerPush(int moudle_id, int cmd_id, byte[] data) {

    }

    // 获取个人登录信息
    public void getMyInfo() {
        android.util.Log.i(TAG, "TestGetMyInfo...");
        ClientConnIM.GetMyInfoRequest.Builder request = ClientConnIM.GetMyInfoRequest.newBuilder();
        if (TextUtils.isDigitsOnly(account)) {
            request.setUserid(Integer.parseInt(account));
            byte[] data = request.build().toByteArray();
            int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_IM, ClientConnIM.CMD_ID.kCmdGetMyInfo_VALUE, data, data.length, "TestGetMyInfo");
            ClientConnImp.getSingleton().isManual = true;
            android.util.Log.e(TAG, "TestGetMyInfo end ,return: " + value);
        }
    }

    // 获取用户基本信息（可以获取自己的和好友的）
    public void getUserbaseInfo(long queryUserid) {
        android.util.Log.e(TAG, "getUserbaseInfo...");
        ClientConnIM.GetUserBaseInfoRequest.Builder request = ClientConnIM.GetUserBaseInfoRequest.newBuilder();
        request.setUserid(Integer.parseInt(account)); //发起请求者id
        request.addQueryUserids(queryUserid); //要请求的用户id,可重复
        byte[] data = request.build().toByteArray();
        int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_IM, ClientConnIM.CMD_ID.kCmdGetUserBaseInfo_VALUE, data, data.length, "queryUserid");
        android.util.Log.e(TAG, "getUserbaseInfo end ,return: " + value);
    }

    // 获取任教学校信息(老师调用),或学校信息（学生调用）
    public void getMyTeacherOrStudySchoolInfo() {
        android.util.Log.e(TAG, "getMyTeacherSchoolInfo...");
        ClientConnSchool.GetMyTeachSchoolInfoRequest.Builder request = ClientConnSchool.GetMyTeachSchoolInfoRequest.newBuilder();
        request.setUserid(LoginFenboo.getUserinfo().getUserid()); //发起请求者id
        byte[] data = request.build().toByteArray();
//        int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_SCHOOL, LoginFenboo.getUserinfo().getIsTeacher() ? ClientConnSchool.SCHOOL_CMD_ID.kCmdGetMyTeachSchoolInfo_VALUE : ClientConnSchool.SCHOOL_CMD_ID.kCmdGetMyStudySchoolInfo_VALUE,
        int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_SCHOOL, LoginFenboo.getUserinfo().getIsStudent() ? ClientConnSchool.SCHOOL_CMD_ID.kCmdGetMyStudySchoolInfo_VALUE : ClientConnSchool.SCHOOL_CMD_ID.kCmdGetMyTeachSchoolInfo_VALUE,
                data, data.length, "getMyTeacherSchoolInfo");
        android.util.Log.e(TAG, "getMyTeacherSchoolInfo end ,return: " + value);
    }

    // 获取特定学校信息
    public void getSomeSchoolInfo() {
        android.util.Log.e(TAG, "getSomeSchoolInfo...");
        ClientConnSchool.GetSchoolInfoRequest.Builder request = ClientConnSchool.GetSchoolInfoRequest.newBuilder();
        try {
            request.setUserid(LoginFenboo.getUserinfo().getUserid()); //发起请求者id
            if (LoginFenboo.getUserinfo().getIsTeacher())
                request.setSchoolid(teachSchoolInfoResponse.getSchoolid()); //所请求的学校id
            else {}

            byte[] data = request.build().toByteArray();
            int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_SCHOOL, ClientConnSchool.SCHOOL_CMD_ID.kCmdGetSchoolInfo_VALUE, data, data.length, "getSomeSchoolInfo");
            android.util.Log.e(TAG, "getSomeSchoolInfo end ,return: " + value);
        } catch (Exception e) {
            android.util.Log.e(TAG, "getSomeSchoolInfo end ,Exception: " + e.getLocalizedMessage());
        }
    }

    //获取学校班级信息,（可组合成年级列表,只有学生这样用）
    public void searchSchoolClassRequest(long schoolId) {
        if (schoolId > 0) {
            android.util.Log.e(TAG, "SearchSchoolClassRequest...");
            ClientConnSchool.SearchSchoolClassRequest.Builder request = ClientConnSchool.SearchSchoolClassRequest.newBuilder();
            request.setUserid(LoginFenboo.getUserinfo().getUserid()); //发起请求者id
            request.setSchoolid(schoolId);
//        request.setGrade(1);
            byte[] data = request.build().toByteArray();
            int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_SCHOOL, ClientConnSchool.SCHOOL_CMD_ID.kCmdSearchSchoolClass_VALUE, data, data.length, "SearchSchoolClassRequest");
            android.util.Log.e(TAG, "SearchSchoolClassRequest end ,return: " + value);
        } else {
            android.util.Log.e(TAG, "SearchSchoolClassRequest...schoolId:" + schoolId);
        }
    }

    //获取资源文件信息(头像图片)
    public void getResFileInfoRequest(String resId) {
        android.util.Log.e("yang", "getResFileInfoRequest...");
        ClientConnIM.GetResFileInfoRequest.Builder request = ClientConnIM.GetResFileInfoRequest.newBuilder();
        request.setUserid(MarsControl.getSingleton().LoginFenboo.getUserinfo().getUserid());
        request.addResFileid(resId);
        byte[] data = request.build().toByteArray();
        int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_IM, ClientConnIM.CMD_ID.kCmdGetResFileInfo_VALUE, data, data.length, "getResFileInfoRequest");
    }

    /*获取资源文件信息(头像图片), PS: 同一张图片服务器只保存一张，所以同一张图片的resFileInfo.getFileName永远是第一次的图片名，如有需要，需自己更改文件名
          文件服务器有去重的功能，如果两次都上传同一个文件，即使文件名不一样，也会被认为是相同的文件，第二次上传的那个就不会被保存，而是直接返回第一次上传的文件的resid、fileName
     */
    public void getResFileInfoResponse(ClientConnIM.GetResFileInfoResponse resFileInfo, String user_data) {

    }

    //获取群聊（群组）列表
    public void getMyGroups(String user_data) {
        android.util.Log.e("yang", "getMyGroups...");
        ClientConnIM.GetMyGroupsRequest.Builder request = ClientConnIM.GetMyGroupsRequest.newBuilder();
        request.setUserid(MarsControl.getSingleton().LoginFenboo.getUserinfo().getUserid()); //发起请求者id
        byte[] data = request.build().toByteArray();
        int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_IM, ClientConnIM.CMD_ID.kCmdGetMyGroups_VALUE, data, data.length, user_data);
        android.util.Log.e("yang", "getMyGroups end ,return: " + value);
    }

    public boolean isAddFriendApply; //有好友申请消息过来
    public List<Long> unreadClassActivityIds; //未读班级活动的classid

    //type  1 代表在线  0代表离线
    public void setCommonMessage(ClientConnIM.SeverPushCommonMessage pushCommonMessage, ClientConnMessage.CommonMessage message, int type) {
        android.util.Log.e("dahui", "setCommonMessage:------- " + message.getMsgType());
        switch (message.getMsgType()) {
            case kMsgTypeNone:
                break;
            case kMsgTypeSystemBroadcast:
                break;
            case kMsgTypeOrgBroadcast:
                break;
            case kMsgTypeFriendChatSync: //好友对聊同步(自己在PC端发)，发送给自己的其他设备
                android.util.Log.e("dahui", "收到自己对聊 PC:------- " + message.getMsgType());

                break;
            case kMsgTypeFriendChat:  //收到好友对聊
                android.util.Log.e("dahui", "收到好友对聊:------- " + message.getMsgType());

                break;
            case kMsgTypeGroupChat: //收到群聊天
                android.util.Log.e("dahui", "收到群聊: " + message.getMsgType());

                break;
            case kMsgTypeGroupInform: //群组通知
                android.util.Log.e("dahui", "setCommonMessage:-------群组通知 " + message.getContent());
                android.util.Log.e("dahui", "setCommonMessage:-------getFromUserid " + message.getFromUserid());
                break;
            case kMsgTypeAddFriendApply:  //新的好友
            case kMsgTypeAddFriendReply: //回复好友申请
                break;
            case kMsgTypeJoinGroupApply: //申请入群
                break;
            case kMsgTypeJoinGroupReply://回复申请入群
                break;
            case kMsgTypeCommonInform:
                break;
            case kMsgTypeTradeInform:
                break;
            case kMsgTypCooperativeInvite:
                break;
            case kMsgTypCooperativeApply:
                break;
            case kMsgTypCooperativeReply:
                break;
            case kMsgTypCooperativeNews:
                break;
            case kMsgTypeSchoolInform:
                break;
            case kMsgTypeSchoolTeacherInform:
                break;
            case kMsgTypeGradeTeacherInform:
                break;
            case kMsgTypeTeacherGroupInform:
                break;
            case kMsgTypeSchoolClassInform:
                break;
            case kMsgTypeSchoolResearch:
                break;
            case kMsgTypeSchoolPingKe:
                android.util.Log.e(TAG, "根据 message.to++:+" + message.toString());
                if (!userNameMap.containsKey(message.getFromUserid())) {
                    userNameMap.put(message.getFromUserid(), "");
                }

                break;
            case kMsgTypeStudentLeaveClass://学生离开了班级，本班老师会收到这个通知
                if (!classNameMap.containsKey(message.getSendTo())) {
                    classNameMap.put(message.getSendTo(), "");
                }
                if (!userNameMap.containsKey(message.getFromUserid())) {
                    userNameMap.put(message.getFromUserid(), "");
                }
                break;
            case kMsgTypeStudentJoinClassApply: //收到学生发出的入班申请71
                android.util.Log.e(TAG, "根据 userID++:+" + userID + " getFromUserid:" + message.getFromUserid() + " getSendTo:" + message.getSendTo());
                if (userID == 0) {
                    if (!classNameMap.containsKey(message.getSendTo())) {
                        classNameMap.put(message.getSendTo(), "");
                    }
                    if (!userNameMap.containsKey(message.getFromUserid())) {
                        userNameMap.put(message.getFromUserid(), "");
                    }
                    userID = message.getFromUserid();
                } else {
                    if (userID != message.getFromUserid()) {
                        if (!classNameMap.containsKey(message.getSendTo())) {
                            classNameMap.put(message.getSendTo(), "");
                        }
                        if (!userNameMap.containsKey(message.getFromUserid())) {
                            userNameMap.put(message.getFromUserid(), "");
                        }
                    }
                }
                break;
            case kMsgTypeStudentJoinClassReply://老师对学生入班申请做出处理72(本班老师也可以收到消息)
                break;
            case kMsgTypeTeacherJoinSchoolApply://学校管理员收到老师发来的入校申请73
                break;
            case kMsgTypeTeacherJoinSchoolReply:  //学校管理员对老师入校申请做出了处理74     PS : 老师被管理员批准的有通知。直接进去的没通知
                break;
            case kMsgTypeSchoolClassActivity:  //新增班级活动，通知给班级成员。 由 web 调用 PostMessage微服务发出 content内容：json格式{"classid":}   sendto=classid(班级id)
                android.util.Log.e("yang", "新增班级活动 getSendTo：" + message.getSendTo());
                break;
            case kMsgTypeRemoveStudentFromClass:  //老师将学生移除班级，本班老师和相应学生会收到这个通知 from_userid:执行操作的老师id   send_to:classid content:被移除的studentid
                Log.e("yang", "老师将学生移除班级 getFromUserid:" + message.getFromUserid() + " getSendTo:" + message.getSendTo() + " getContent:" + message.getContent());
                if (!TextUtils.isEmpty(message.getContent()) && Long.parseLong(message.getContent()) == LoginFenboo.getUserinfo().getUserid()) {
                    getMyInfo();
                    //被移除的人是自己,重新获取群组
                    getMyGroups("TopActivity");
                    //重新获取学校信息
                    getMyTeacherOrStudySchoolInfo();
                }
                break;
            case kMsgTypeRemoveTeacherFromClass: //班主任老师将移除班级，本班其他老师和相应的老师 会收到这个通知 from_userid:执行操作的老师id   send_to:classid content:被移除的teacherid
                Log.e("yang", "班主任老师将移除班级 getFromUserid:" + message.getFromUserid() + " getSendTo:" + message.getSendTo() + " getContent:" + message.getContent());
                break;
            case kMsgTypeRemoveTeacherFromSchool: //班主任老师将老师移除学校 from_userid:执行操作的老师id   send_to:teacherid content:学校id
                Log.e("yang", "班主任将老师移除学校 getFromUserid:" + message.getFromUserid() + " getSendTo:" + message.getSendTo() + " getContent:" + message.getContent());
                break;
            case kMsgTypeSchoolFeedBack:  //收到学生发来的学校反馈：学校管理员收到此消息 from_userid=学生id  send_to=schoolid
                android.util.Log.e("yang", "收到学校反馈:-------  getCustomFace" + message.getCustomFace() + " getDefaultFace:" + message.getDefaultFace());

                break;
            case kMsgTypeSchoolFeedBackReply:   //学生的学校反馈已被回复：其他管理员（或设备）收到此消息； 发送学校反馈的学生也会收到此消息  from_userid=进行回复的老师  send_to=发出反馈的学生id
                android.util.Log.e("yang", "收到学校反馈 其他老师回复:------- getCustomFace" + message.getCustomFace() + " getDefaultFace:" + message.getDefaultFace());
                break;
            case kMsgTypeSetSchoolAdmin:    //将某老师设为学校管理员，相应的老师会收到该消息。  from_userid=进行操作的userid  send_to=被设置的老师id ;content="" 表示被移除了权限，不为空表示重设了权限
                break;
            case kMsgTypeSchoolShiShengZhanPing:  //师生展评通知
                android.util.Log.e(TAG, "根据 师生展评通知.to++:+" + message.toString());
                if (!userNameMap.containsKey(message.getFromUserid())) {
                    userNameMap.put(message.getFromUserid(), "");
                }
                break;
            case kMsgTypeComment:  // 评论通知：某人评论了你发表的 视频、文章，或对你的评论进行了回复

                break;
            case kMsgTypeZan:  // 点赞通知：某人点赞了你发表的 视频、文章，或对你的评论进行了点赞

                break;
            case UNRECOGNIZED:
                break;
        }
    }

    private long userID = 0;


    public HashMap<Long, String> classNameMap = new HashMap<>();
    public HashMap<Long, String> userNameMap = new HashMap<>();

    public void getGroupUserInfo() {
        if (classNameMap.size() > 0) {
            //根据classid获取班级的信息
            ClientConnSchool.GetSchoolClassInfoRequest.Builder request = ClientConnSchool.GetSchoolClassInfoRequest.newBuilder();
            request.setUserid(MarsControl.getSingleton().LoginFenboo.getUserinfo().getUserid());
            for (Long classid : classNameMap.keySet()) {
                //根据classid获取班级的信息
                android.util.Log.e(TAG, "根据 classid获取特定班级信息classid++:+" + classid);
                request.addClassid(classid);
            }
            byte[] data = request.build().toByteArray();

            MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_SCHOOL, ClientConnSchool.SCHOOL_CMD_ID.kCmdGetSchoolClassInfo_VALUE, data, data.length, "GetSchoolClassInfo");
        } else {

        }
    }


    private void setUserInfo(ClientConnIM.GetUserBaseInfoResponse userInfo) {
    }

    public void clearGroup() {
        userID = 0;
        userNameMap.clear();
        classNameMap.clear();
    }

    // 获取好友列表
    public void getMyFriends() {
        android.util.Log.i("yang", "getMyFriends...");
        ClientConnIM.GetMyFriendsRequest.Builder request = ClientConnIM.GetMyFriendsRequest.newBuilder();
        request.setUserid(MarsControl.getSingleton().LoginFenboo.getUserinfo().getUserid());
        byte[] data = request.build().toByteArray();
        int value = MarsWrapple.marsSend(ClientConnImp.MOUDLE_ID_IM, ClientConnIM.CMD_ID.kCmdGetMyFriends_VALUE, data, data.length, "getMyFriends");
        android.util.Log.e("yang", "TestGetMyInfo end ,return: " + value);
    }

    //退出清除数据
    public void clearData() {
        LoginFenboo = null;
        userBaseInfo = null;
        teachSchoolInfoResponse = null;
        myShoolInfo = null;
        groupInfoList = null;
        isAddFriendApply = false;
        isReconnect = false;
        if (unreadClassActivityIds != null) {
            unreadClassActivityIds.clear();
        }
        backstage = false;
        clearGroup();
    }

    //一般在线推送
    public void onlineMessage(ClientConnMessage.OnlineMessage message) {
        android.util.Log.e("dahui", "OnlineMessage:------- " + message.getMsgType());
        switch (message.getMsgTypeValue()) {
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeMyInfoChange_VALUE:   //自己信息变化
                getMyInfo();//重新获取个人信息
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeMyMoneyChange_VALUE:  //余额发生变动
                getMyInfo();//重新获取个人信息
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeMyScoreChange_VALUE: //积分发生变动
                getMyInfo();//重新获取个人信息
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeHasAddFriend_VALUE: //其他设备上增加了好友。
                getMyFriends();
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeHasRemoveFriend_VALUE: //其他设备上删除了好友。。
                android.util.Log.e("yang", "其他设备上删除了好友" + message.getContent());
                getMyFriends();
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeHasDisposeFriendApply_VALUE: //其他设备上处理了好友申请。。。
                //查询数据库,表示“新的好友”消息
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeHasAddUserToBlackList_VALUE: //其他设备上将好友拉黑。。。
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeFriendOnlineStatusChange_VALUE: //好友上下线。。。。
                android.util.Log.e("yang", "好友上下线" + message.getContent());
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeGroupAddMember_VALUE: //群成员增加。。。。
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeGroupMemberLeave_VALUE: //群成员减少。。。。
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeFriendInfoItemChange_VALUE: //好友某项信息变更。 本方调用 im::kCmdSetMySomeInfo 时，给好友发送该消息。。。。
                android.util.Log.e("yang", "好友某项信息变更" + message.getContent());
//                MarsWrapple.getSingleton().promptMsg("好友某项信息变更:"+message.getContent(),-1);
                break;
            case ClientConnMessage.OnlineMessage.MESSAGE_TYPE.kMsgTypeFriendInfoChange_VALUE: //好友信息变更。 本方调用 im::kCmdSetMyBaseInfo时，给好友发送该消息。
                android.util.Log.e("yang", "好友信息变更" + message.getContent());
//                MarsWrapple.getSingleton().promptMsg("好友信息变更:"+message.getContent(),-1);
                break;
        }
    }

}
