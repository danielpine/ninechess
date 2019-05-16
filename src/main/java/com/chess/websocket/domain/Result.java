/**
 * ===========================================
 *
 * Copyright (C) 2019 Codehelper
 *
 * All rights reserved
 *
 * 项 目 名： codehelper-be
 *
 * 文 件 名： Result.java
 *
 * 版本信息： V1.0.0
 *
 * 作 者： Daniel Pine
 *
 * 日 期： 2019年5月16日-上午9:24:13
 * 
 * ============================================
 */


package com.chess.websocket.domain;

import com.alibaba.fastjson.JSONObject;

/**
 * @description ：
 * @author :Daniel Pine
 * @email :danielpine@sina.com
 * @date :2019年5月16日-上午9:24:13
 */
public class Result {
  /**
   * 连接号
   */
  private String connid;
  /**
   * 房间号
   */
  private String roomid;
  /**
   * 消息类型
   */
  private Integer type;
  /**
   * 消息目标
   */
  private Integer goal;
  /**
   * 落子坐标
   */
  private String xy;
  /**
   * 发送消息
   */
  private String message;
  /**
   * 是否允许落子
   */
  private boolean bout;
  /**
   * 落子颜色
   */
  private Integer color;

  public String getXy() {
    return xy;
  }

  public void setXy(String xy) {
    this.xy = xy;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isBout() {
    return bout;
  }

  public void setBout(boolean bout) {
    this.bout = bout;
  }

  public Integer getColor() {
    return color;
  }

  public void setColor(Integer color) {
    this.color = color;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public Integer getGoal() {
    return goal;
  }

  public void setGoal(Integer goal) {
    this.goal = goal;
  }

  public String getRoomid() {
    return roomid;
  }

  public void setRoomid(String roomid) {
    this.roomid = roomid;
  }

  public String getConnid() {
    return connid;
  }

  public void setConnid(String connid) {
    this.connid = connid;
  }

  @Override
  public String toString() {
    return JSONObject.toJSONString(this, true);
  }

}
