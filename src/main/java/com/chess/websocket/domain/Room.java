/**
 * ===========================================
 *
 * Copyright (C) 2019 Codehelper
 *
 * All rights reserved
 *
 * 项 目 名： codehelper-be
 *
 * 文 件 名： Room.java
 *
 * 版本信息： V1.0.0
 *
 * 作 者： Daniel Pine
 *
 * 日 期： 2019年5月16日-上午11:14:09
 * 
 * ============================================
 */


package com.chess.websocket.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @description ：
 * @author :Daniel Pine
 * @email :danielpine@sina.com
 * @date :2019年5月16日-上午11:14:09
 */
public class Room {
  /**
   * 玩家列表
   */
  private Map<String, User> userMap = new HashMap<String, User>();
  /**
   * 房间号
   */
  private String roomid;
  /**
   * 当前回合玩家
   */
  private String termUserId;
  /**
   * 步数记录
   */
  private List<String> steps = new ArrayList<String>();
  /**
   * 棋谱
   */
  private JSONObject map;

  public String getRoomid() {
    return roomid;
  }

  public void setRoomid(String roomid) {
    this.roomid = roomid;
  }


  public Map<String, User> getUserMap() {
    return userMap;
  }

  public void setUserMap(Map<String, User> userMap) {
    this.userMap = userMap;
  }

  public List<String> getSteps() {
    return steps;
  }

  public void setSteps(List<String> steps) {
    this.steps = steps;
  }

  public JSONObject getMap() {
    return map;
  }

  public void setMap(JSONObject map) {
    this.map = map;
  }

  public String getTermUserId() {
    return termUserId;
  }

  public void setTermUserId(String termUserId) {
    this.termUserId = termUserId;
  }

  public void initMap() {
    this.map = JSON.parseObject(
        "{\"11\":{\"x\":1,\"y\":1,\"piece\":0},\"12\":{\"x\":1,\"y\":2,\"piece\":0},\"13\":{\"x\":1,\"y\":3,\"piece\":0},\"14\":{\"x\":1,\"y\":4,\"piece\":0},\"15\":{\"x\":1,\"y\":5,\"piece\":0},\"16\":{\"x\":1,\"y\":6,\"piece\":0},\"17\":{\"x\":1,\"y\":7,\"piece\":0},\"18\":{\"x\":1,\"y\":8,\"piece\":0},\"21\":{\"x\":2,\"y\":1,\"piece\":0},\"22\":{\"x\":2,\"y\":2,\"piece\":0},\"23\":{\"x\":2,\"y\":3,\"piece\":0},\"24\":{\"x\":2,\"y\":4,\"piece\":0},\"25\":{\"x\":2,\"y\":5,\"piece\":0},\"26\":{\"x\":2,\"y\":6,\"piece\":0},\"27\":{\"x\":2,\"y\":7,\"piece\":0},\"28\":{\"x\":2,\"y\":8,\"piece\":0},\"31\":{\"x\":3,\"y\":1,\"piece\":0},\"32\":{\"x\":3,\"y\":2,\"piece\":0},\"33\":{\"x\":3,\"y\":3,\"piece\":0},\"34\":{\"x\":3,\"y\":4,\"piece\":0},\"35\":{\"x\":3,\"y\":5,\"piece\":0},\"36\":{\"x\":3,\"y\":6,\"piece\":0},\"37\":{\"x\":3,\"y\":7,\"piece\":0},\"38\":{\"x\":3,\"y\":8,\"piece\":0}}");
  }

  public void updateMap() {}

  @Override
  public String toString() {
    return JSONObject.toJSONString(this, true);
  }


}
