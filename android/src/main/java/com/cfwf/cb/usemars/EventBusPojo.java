package com.cfwf.cb.usemars;

import com.cfwf.cb.business_proto.ClientConnCommon;
import com.cfwf.cb.business_proto.ClientConnIM;
import com.cfwf.cb.business_proto.ClientConnSchool;

import java.util.List;

/**
 * Created by Administrator on 2018/5/31.
 */

public class EventBusPojo {
    public Object obj; //共同类
    public List<ClientConnCommon.UserInfo> list; //共同列表类
    public List<ClientConnIM.GroupInfo> groupList; //共同列表类
    public type mType; //识别码
    public int cmd; //指令码
    public Class cls; //区分当前界面
    public String userData; //回传信息


    public EventBusPojo(Class activity,int cmd, Object obj,String userData){
        this.obj = obj;
        this.cmd = cmd;
        this.cls = activity;
        this.userData = userData;
    }
    public EventBusPojo(Class activity, int cmd, List<ClientConnCommon.UserInfo> list, String userData){
        this.list = list;
        this.cmd = cmd;
        this.cls = activity;
        this.userData = userData;
    }
    public EventBusPojo(Class activity, int cmd, List<ClientConnIM.GroupInfo> groupList){
        this.groupList = groupList;
        this.cmd = cmd;
        this.cls = activity;
        this.userData = userData;
    }
    public EventBusPojo(Class activity, int cmd,Object obj,EventBusPojo.type type,String userData){
        this.obj = obj;
        this.mType = type;
        this.cmd = cmd;
        this.cls = activity;
        this.userData = userData;
    }

    public enum type{
        success,failure
    }
}
