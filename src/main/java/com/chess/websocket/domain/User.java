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

import com.alibaba.fastjson.JSONObject;

/**
 * @description ：
 * @author :Daniel Pine
 * @email :danielpine@sina.com
 * @date :2019年5月16日-上午11:14:09
 */
public class User {
  /**
   * 连接号
   */
  private String connid;
  /**
   * 棋子颜色
   */
  private Integer color;
  /**
   * 昵称
   */
  private String name;
  /**
   * 下棋状态 0 落子阶段 1 移动阶段 2 飞行阶段
   */
  private Integer status = 0;
  /**
   * 落子累计
   */
  private Integer pieceRecord = 0;
  /**
   * 棋子计数
   */
  private Integer pieceCount = 0;

  public String getConnid() {
    return connid;
  }

  public void setConnid(String connid) {
    this.connid = connid;
  }

  public Integer getColor() {
    return color;
  }

  public void setColor(Integer color) {
    this.color = color;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getPieceRecord() {
    return pieceRecord;
  }

  public Integer addPieceRecord() {
    return ++pieceRecord;
  }

  public void setPieceRecord(Integer pieceRecord) {
    this.pieceRecord = pieceRecord;
  }

  public Integer getPieceCount() {
    return pieceCount;
  }

  public Integer addPieceCount() {
    return ++pieceCount;
  }

  public Integer reducePieceCount() {
    return --pieceCount;
  }

  public void setPieceCount(Integer pieceCount) {
    this.pieceCount = pieceCount;
  }

  @Override
  public String toString() {
    return JSONObject.toJSONString(this, true);
  }


}
