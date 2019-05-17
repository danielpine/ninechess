/**
 * ===========================================
 *
 * Copyright (C) 2019 Codehelper
 *
 * All rights reserved
 *
 * 项 目 名： codehelper-be
 *
 * 文 件 名： WebSocket.java
 *
 * 版本信息： V1.0.0
 *
 * 作 者： Daniel Pine
 *
 * 日 期： 2019年5月16日-上午9:21:57
 * 
 * ============================================
 */

package com.chess.websocket.contoller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.chess.websocket.domain.NineChess;
import com.chess.websocket.domain.PieceBox;
import com.chess.websocket.domain.Result;
import com.chess.websocket.domain.Room;
import com.chess.websocket.domain.User;

/**
 * @description ：
 * @author :Daniel Pine
 * @email :danielpine@sina.com
 * @date :2019年5月16日-上午9:21:57
 */
// 该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint(value = "/ninesocket")
@Component
public class WebSocket {

	private static Logger log = LoggerFactory.getLogger(WebSocket.class);

	@PostConstruct
	public void init() {
		log.info("nine chess websocket 加载");
	}

	// concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	private static HashMap<String, WebSocket> webSocketMap = new HashMap<String, WebSocket>();
	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private static LinkedBlockingQueue<String> waitqueue = new LinkedBlockingQueue<String>();
	private static HashMap<String, Room> roomMap = new HashMap<String, Room>();
	private Session session;
	// 连上来的页面序号，用来配对对战，1与2一组，3与4一组，依次类推，奇数为黑先走，偶数为白，后走
	private static int index = 0;
	// 同上，用来从hashMap中获取websocket
	private String connid;
	// room id 记录 两个玩家信息
	private String roomid;
	// 所在房间
	private Room room;
	// 当前用户
	private User user;
	private int needRemovePieces = 0;
	// 对手socket连接
	private WebSocket oppo;

	/**
	 * 连接建立成功调用的方法
	 * 
	 * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 * @throws IOException
	 */
	@OnOpen
	public void onOpen(Session session) throws IOException {
		this.session = session;
		this.connid = UUID.randomUUID().toString();
		index++;
		try {
			webSocketMap.put(connid, this); // 加入map中
			Result result = new Result();
			result.setConnid(this.connid);
			result.setType(0);
			// log.info("{}", waitqueue);
			// 检查是否有玩家等待匹配
			if (waitqueue.isEmpty()) {
				result.setMessage("系统：等待玩家匹配！");
				waitqueue.put(connid);
			} else {
				// **匹配操作**////////////////////////////////////
				String oppoUserConnId = waitqueue.poll();
				// log.info("对手ID：" + oppoUserConnId);
				WebSocket oppoWebSocket = webSocketMap.get(oppoUserConnId);
				if (oppoWebSocket != null) {
					// 互设对手
					this.oppo = oppoWebSocket;
					oppoWebSocket.oppo = this;

					// TODO 预设空余房间
					String makeRoomId = UUID.randomUUID().toString();
					this.roomid = makeRoomId;
					oppo.roomid = makeRoomId;
					Room room = new Room();
					// 初始化棋盘
					room.initMap();
					// 设置先手
					room.setTermUserId(this.connid);
					// 设置Room
					this.room = room;
					oppo.room = room;

					// 此用户
					User thisuser = new User();
					// 对手
					User oppouser = new User();

					this.setUser(thisuser);
					oppo.setUser(oppouser);

					Map<String, User> userMap = room.getUserMap();
					userMap.put(this.connid, thisuser);
					userMap.put(oppo.connid, oppouser);
					roomMap.put(makeRoomId, room);
					result.setType(111);// 111 ：游戏初始化成功
										// 222 ：游戏操作信息
					result.setGoal(1);// 0 等待 1 落子 2 移动 3 吃子 4 飞行
					result.setBout(true);
					result.setMessage("系统：游戏开始，您是黑棋，请您先落子！");
					result.setColor(1);
					thisuser.setColor(1);
					this.sendMessage(result.toString());
					// 对先落子的对象发送数据结束
					result.setMessage("系统：游戏开始，您是白棋，请等待对手落子！");
					result.setGoal(0);// 0 等待 1 落子 2 移动 3 吃子
					result.setBout(false);
					result.setColor(2);
					oppouser.setColor(2);
					oppo.sendMessage(result.toString());
				} else {
					log.info("NULL: 对手ID > " + oppoUserConnId);
				}
			}
			log.info("{}", webSocketMap.size());
		} catch (Exception e) {
			e.printStackTrace();
			Result result = new Result();
			result.setMessage("系统：出错了！");
			this.sendMessage(result.toString());
		}
	}

	/**
	 * 连接关闭调用的方法
	 * 
	 * @throws IOException
	 */
	@OnClose
	public void onClose() {
		webSocketMap.remove(connid); // 从set中删除
		waitqueue.remove(connid);
		try {
			if (oppo != null) {
				Result result = new Result();
				result.setMessage("系统：你的对手已离开！");
				if (oppo.session.isOpen()) {
					oppo.sendMessage(result.toString());
				}
				// TODO 初始化房间信息
				index--;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * type: 111 ：游戏初始化成功 113:吃子操作信息 222 :游戏操作信息 202：服务器返回移动错误 444:移除操作 201:客户端移动操作
	 * 
	 * goal: 0 等待 1 落子 2 移动 3 吃子 4 飞行
	 * 
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 */
	@OnMessage
	public void onMessage(String message) {
		Result result = JSONObject.parseObject(message).toJavaObject(Result.class);
		log.info("{}", result);
		try {
			if (oppo != null && oppo.session.isOpen()) {
				// 有坐标表示为落子，反之则为发送信息
				if (result.getXy() != null && !"".equals(result.getXy())) {
					Integer goal = result.getGoal();
					Integer type = result.getType();
					log.info("===> type {}", type);
					log.info("===> goal {}", goal);
					switch (type) {
					case 113:
						if (goal == 3) {
							processRemove(message, result);
						}
						break;
					case 201:
						if (goal == 2) {
							processMove(message, result);
						}
						break;
					case 301:
						if (goal == 3) {
							processFly(message, result);
						}
						break;

					default:
						processPlace(message, result);
						break;
					}
				} else {// 没有坐标表示为单纯的聊天
					Result newResult = new Result();
					newResult.setMessage("自己：" + result.getMessage());
					this.sendMessage(newResult.toString());
					newResult.setMessage("对方：" + result.getMessage());
					oppo.sendMessage(newResult.toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processRemove(String message, Result result) throws IOException {
		log.info("===> processRemove");
		String thisxy = result.getXy();
		JSONObject map = room.getMap();
		PieceBox box = map.getJSONObject(thisxy).toJavaObject(PieceBox.class);
		if (box.getPiece() == 0 || box.getPiece() == this.getUser().getColor()) {
			result.setType(333);
			result.setMessage("系统：请选择正确的棋子");
			this.sendMessage(result.toString());
		} else {
			// 厂判断 TODO allMill()
			int millCount = NineChess.mill(thisxy, oppo.getUser().getColor(), room.getMap());
			log.info("mill check get count {}", millCount);
			boolean checkMill = NineChess.checkMill(thisxy, oppo.getUser().getColor(), room.getMap());
			if (checkMill) {
				result.setType(333);
				result.setMessage("系统：请选择正确的棋子");
				this.sendMessage(result.toString());
			} else {
				// 服务端棋谱移除
				room.getMap().getJSONObject(thisxy).put("piece", 0);
				room.getSteps().add("-" + result.getXy());
				this.needRemovePieces--;
				// 减少对手棋子数量（关键代码）
				oppo.getUser().reducePieceCount();
				// 检查剩余待移除数
				if (this.needRemovePieces > 0) {
					result.setType(555);
					result.setBout(true);
					result.setColor(oppo.getUser().getColor());
					result.setGoal(3);// 0 等待 1 落子 2 移动 3飞行4吃子
					result.setMessage("系统：移除【" + thisxy + "】 还需移除" + needRemovePieces + "颗棋子！");
					this.sendMessage(result.toString());
					// 对手设置
					result.setType(555);
					result.setBout(false);
					result.setGoal(0);// 0 等待 1 落子 2 移动 3飞行4吃子
					result.setMessage("系统：移除我方【" + thisxy + "】 还需等待对方移除" + needRemovePieces + "颗棋子！");
					oppo.sendMessage(result.toString());
					NineChess.win(result, oppo, this, room.getMap());
				} else {
					result.setType(444);
					result.setColor(oppo.getUser().getColor());
					result.setBout(false);
					result.setMessage("系统：移除【" + thisxy + "】");
					this.sendMessage(result.toString());
					// oppo
					result.setBout(true);
					// 对手的bout改为true，表示接下来可以落子
					// 判断落子还是移动
					Integer pieceRecord = oppo.getUser().getPieceRecord();
					log.info("pieceRecord  :  " + pieceRecord);
					if (pieceRecord >= 9) {
						Integer pieceCount = oppo.getUser().getPieceCount();
						NineChess.win(result, oppo, this, room.getMap());
						// 判断飞行
						if (pieceCount <= 3) {
							result.setGoal(3);// 0 等待 1 落子 2 移动 3飞行4吃子
							result.setMessage("系统：您的【" + thisxy + "】被移除,正在等待您飞行！");
						} else {
							result.setGoal(2);// 0 等待 1 落子 2 移动 3 吃子
							result.setMessage("系统：您的【" + thisxy + "】被移除,正在等待您移动！");
						}
					} else {
						// 成三判断 输赢判断
						result.setGoal(1);// 0 等待 1 落子 2 移动 3 吃子
						result.setMessage("系统：您的【" + thisxy + "】被移除,正在等待您落子！");
					}
					oppo.sendMessage(result.toString());
				}
			}
		}

	}

	public void processPlace(String message, Result result) throws IOException {
		result.setMessage("系统：" + result.getXy());
		String thisxy = result.getXy();
		room.getSteps().add(result.getXy());
		Integer addPieceCount = this.getUser().addPieceCount();
		Integer addPieceRecord = this.getUser().addPieceRecord();
		// addPieceRecord ==9 状态设为移动
		log.info("addPieceRecord  :  " + addPieceRecord);
		log.info("addPieceCount  :  " + addPieceCount);
		for (String step : room.getSteps()) {
			log.info("step  :  {}", step);
		}
		// log.info("map : {}", JSONObject.toJSONString(room.getMap(), true));
		// TODO update map
		int updateMapRes = NineChess.updateMap(thisxy, getUser().getColor(), room.getMap());

		if (updateMapRes != NineChess.UPDATE_DONE) {
			// TODO 回退前台操作
			log.info("update Map Error by  :  {}", updateMapRes);
		}
		// TODO win

		// mill check

		int millCount = NineChess.mill(thisxy, getUser().getColor(), room.getMap());

		log.info("millCount  :  {}", millCount);

		if (millCount > 0) {
			this.needRemovePieces = millCount;
			result.setType(113);
			result.setBout(true);
			result.setGoal(4);// 0 等待 1 落子 2 移动 3飞行4吃子
			result.setMessage("系统：恭喜成三，移除对方" + millCount + "颗棋子！");
			this.sendMessage(result.toString());
			// 对手设置
			result.setType(112);
			result.setBout(false);
			result.setGoal(0);// 0 等待 1 落子 2 移动 3飞行4吃子
			result.setMessage("系统：对方成三，等待对方移除我方" + millCount + "颗棋子！");
			oppo.sendMessage(result.toString());
		} else {
			this.sendMessage(result.toString());
			result.setType(112);
			result.setBout(true);
			// 对手的bout改为true，表示接下来可以落子
			// 判断落子还是移动
			Integer pieceRecord = oppo.getUser().getPieceRecord();
			log.info("pieceRecord  :  " + pieceRecord);
			if (pieceRecord >= 9) {
				Integer pieceCount = oppo.getUser().getPieceCount();
				boolean win = NineChess.win(result, oppo, this, room.getMap());
				result.setWin(win);
				// 判断飞行
				if (pieceCount <= 3) {
					result.setGoal(3);// 0 等待 1 落子 2 移动 3飞行4吃子
					result.setMessage("系统：对方已落子，正在等待您飞行！");
				} else {
					result.setGoal(2);// 0 等待 1 落子 2 移动 3 吃子
					result.setMessage("系统：对方已落子，正在等待您移动！");
				}
			} else {
				// 成三判断
				result.setGoal(1);// 0 等待 1 落子 2 移动 3 吃子
				result.setMessage("系统：对方已落子，正在等待您落子！");
			}
			log.info("ROOM  :  " + roomid);
			oppo.sendMessage(result.toString());
		}
	}

	/**
	 * 
	 * @description：操作移动 @author： Daniel Pine @date： 2019年5月17日-下午12:11:00
	 * 
	 * @return
	 */
	public void processMove(String message, Result result) throws IOException {
		result.setMessage("系统：" + result.getXy());
		result.setMessage("系统：" + result.getXy());
		String thisxy = result.getXy();
		boolean canMove = NineChess.canMove(thisxy, getUser().getColor(), room.getMap());
		log.info("canMove  :  " + canMove);

		if (canMove) {
			// 互换updateMap
			result.setType(203);
			result.setMessage("系统：移动成功");
			// 记步数
			room.getSteps().add(result.getXy());
			for (String step : room.getSteps()) {
				log.info("step  :  {}", step);
			}
			// log.info("map : {}", JSONObject.toJSONString(room.getMap(), true));
			// TODO update map
			int updateMapRes = NineChess.updateMap(thisxy, getUser().getColor(), room.getMap());

			if (updateMapRes != NineChess.UPDATE_DONE) {
				// TODO 回退前台操作
				log.info("update Map Error by  :  {}", updateMapRes);
			}
			this.sendMessage(result.toString());
			oppo.sendMessage(result.toString());
			// win
			NineChess.win(result, oppo, this, room.getMap());
			// mill check

			int millCount = NineChess.mill(thisxy.split(",")[1], getUser().getColor(), room.getMap());
			// 检查成三
			log.info("mill after move  :  {}", millCount);

			if (millCount > 0) {
				this.needRemovePieces = millCount;
				result.setType(204);
				result.setBout(true);
				result.setGoal(4);// 0 等待 1 落子 2 移动 3飞行4吃子
				result.setMessage("系统：恭喜成三，移除对方" + millCount + "颗棋子！");
				this.sendMessage(result.toString());
				// 对手设置
				result.setType(204);
				result.setBout(false);
				result.setGoal(0);// 0 等待 1 落子 2 移动 3飞行4吃子
				result.setMessage("系统：对方成三，等待对方移除我方" + millCount + "颗棋子！");
				oppo.sendMessage(result.toString());
			} else {
				result.setType(204);
				result.setMessage("系统：移动成功！");
				result.setBout(false);
				this.sendMessage(result.toString());
				result.setBout(true);
				// 对手的bout改为true，表示接下来可以落子
				// 判断移动还是飞行
				Integer pieceRecord = oppo.getUser().getPieceRecord();
				log.info("pieceRecord  :  " + pieceRecord);
				Integer pieceCount = oppo.getUser().getPieceCount();
				log.info("pieceRecord  :  " + pieceRecord);
				// 判断飞行
				if (pieceCount == 3) {
					result.setGoal(3);// 0 等待 1 落子 2 移动 3飞行4吃子
					result.setMessage("系统：对方已移动，正在等待您飞行！");
				} else {
					result.setGoal(2);// 0 等待 1 落子 2 移动 3 吃子
					result.setMessage("系统：对方已移动，正在等待您移动！");
				}
				log.info("ROOM  :  " + roomid);
				oppo.sendMessage(result.toString());
			}

			// 检查输赢

		} else {
			// 返回错误信息 202
			result.setType(202);
			result.setMessage("系统：非法移动，请重新移动");
			this.sendMessage(result.toString());
		}
	}

	/**
	 * 处理飞行
	 * 
	 * @param message
	 * @param result
	 * @throws IOException
	 */
	public void processFly(String message, Result result) throws IOException {
		result.setMessage("系统：" + result.getXy());
		result.setMessage("系统：" + result.getXy());
		String thisxy = result.getXy();
		boolean canMove = NineChess.canFly(thisxy, getUser().getColor(), room.getMap());
		log.info("canMove  :  " + canMove);

		if (canMove) {
			// 互换updateMap
			result.setType(203);
			result.setMessage("系统：飞行成功");
			// 记步数
			room.getSteps().add(result.getXy());
			for (String step : room.getSteps()) {
				log.info("step  :  {}", step);
			}
			// log.info("map : {}", JSONObject.toJSONString(room.getMap(), true));
			// TODO update map
			int updateMapRes = NineChess.updateMap(thisxy, getUser().getColor(), room.getMap());

			if (updateMapRes != NineChess.UPDATE_DONE) {
				// TODO 回退前台操作
				log.info("update Map Error by  :  {}", updateMapRes);
			}
			this.sendMessage(result.toString());
			oppo.sendMessage(result.toString());
			// TODO win
			NineChess.win(result, oppo, this, room.getMap());
			// mill check

			int millCount = NineChess.mill(thisxy.split(",")[1], getUser().getColor(), room.getMap());
			// 检查成三
			log.info("mill after fly  :  {}", millCount);

			if (millCount > 0) {
				this.needRemovePieces = millCount;
				result.setType(204);
				result.setBout(true);
				result.setGoal(4);// 0 等待 1 落子 2 移动 3飞行4吃子
				result.setMessage("系统：恭喜成三，移除对方" + millCount + "颗棋子！");
				this.sendMessage(result.toString());
				// 对手设置
				result.setType(204);
				result.setBout(false);
				result.setGoal(0);// 0 等待 1 落子 2 移动 3飞行4吃子
				result.setMessage("系统：对方成三，等待对方移除我方" + millCount + "颗棋子！");
				oppo.sendMessage(result.toString());
			} else {
				result.setType(204);
				result.setMessage("系统：飞行成功！");

				// TODO 判断是否无子可走

				result.setBout(false);
				this.sendMessage(result.toString());
				result.setBout(true);
				// 对手的bout改为true，表示接下来可以落子
				// 判断移动还是飞行
				Integer pieceRecord = oppo.getUser().getPieceRecord();
				log.info("pieceRecord  :  " + pieceRecord);
				Integer pieceCount = oppo.getUser().getPieceCount();
				log.info("pieceRecord  :  " + pieceRecord);
				// 判断飞行
				if (pieceCount < 3) {
					result.setGoal(5);// 0 等待 1 落子 2 移动 3飞行4吃子
					result.setMessage("系统：很遗憾您输了！");
				} else if (pieceCount == 3) {
					result.setGoal(3);// 0 等待 1 落子 2 移动 3飞行4吃子
					result.setMessage("系统：对方已移动，正在等待您飞行！");
				} else {
					result.setGoal(2);// 0 等待 1 落子 2 移动 3 吃子
					result.setMessage("系统：对方已移动，正在等待您移动！");
				}
				log.info("ROOM  :  " + roomid);
				oppo.sendMessage(result.toString());
			}

			// 检查输赢

		} else {
			// 返回错误信息 202
			result.setType(202);
			result.setMessage("系统：非法移动，请重新移动");
			this.sendMessage(result.toString());
		}
	}

	/**
	 * 发生错误时调用
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.info("连接断开");
		// error.printStackTrace();
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
		// this.session.getAsyncRemote().sendText(message);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
