package com.chess.websocket.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chess.websocket.contoller.WebSocket;

@Component
public class NineChess {

	private static Logger log = LoggerFactory.getLogger(NineChess.class);
	/**
	 * 工程列表
	 */
	public static List<String> millList = new ArrayList<>();
	public static int UPDATE_DONE = 1;
	public static int UPDATE_ERROR_HAVE_PIECE = 2;

	static {
		log.info("初始化工厂列表");
		// 内圈
		Stream<String> stream1 = Stream.of("18,11,12", "12,13,14", "14,15,16", "16,17,18");
		// 中圈
		Stream<String> stream2 = Stream.of("28,21,22", "22,23,24", "24,25,26", "26,27,28");
		// 外圈
		Stream<String> stream3 = Stream.of("38,31,32", "32,33,34", "34,35,36", "36,37,38");
		// 横线
		Stream<String> stream4 = Stream.of("31,21,11", "13,23,33", "15,25,35", "17,27,37");

		NineChess.millList.addAll(stream1.collect(Collectors.toList()));
		NineChess.millList.addAll(stream2.collect(Collectors.toList()));
		NineChess.millList.addAll(stream3.collect(Collectors.toList()));
		NineChess.millList.addAll(stream4.collect(Collectors.toList()));
	}

	public static int mill(String currentXy, Integer color, JSONObject map) {
		// 选出包含当前落子点的成三表
		List<String> containCurrentXyList = NineChess.millList.stream().filter(x -> x.contains(currentXy))
				.collect(Collectors.toList());
		log.info("currentXy => {}", currentXy);
		log.info("color	=> {}", color);
		log.info("containCurrentXyList	=> {}", containCurrentXyList);
		int mills = containCurrentXyList.stream().map(x -> new ArrayList<String>(Arrays.asList(x.split(","))))
				.filter(arr -> {
					return isMill(arr, currentXy, color, map);
				}).collect(Collectors.toList()).size();
		return mills;
	}

	/**
	 * @description： @author： Daniel Pine @date： 2019年5月17日-下午12:17:46
	 * 
	 * @return
	 */
	public static boolean canMove(String thisxy, Integer color, JSONObject map) {
		// 检查能否移动
		String[] split = thisxy.split(",");
		String select = split[0];
		String target = split[1];
		// 1.检查棋子合规性
		// 检查select是否为用户颜色
		PieceBox selectPieceBox = map.getJSONObject(select).toJavaObject(PieceBox.class);
		if (selectPieceBox.getPiece() != color) {
			return false;
		}
		PieceBox targetPieceBox = map.getJSONObject(target).toJavaObject(PieceBox.class);
		// 检查select是否为0(空)
		if (targetPieceBox.getPiece() != 0) {
			return false;
		}
		// 检查select target是否相邻
		// 1.x相同那么y相差1,y为8相差7
		if (selectPieceBox.getX() == targetPieceBox.getX()) {
			int delta = Math.abs(selectPieceBox.getY() - targetPieceBox.getY());
			if (delta == 1 || delta == 7) {
				return true;
			}
		}
		// 2.y相同并且在【1357中】那么x相差1
		if (selectPieceBox.getY() == targetPieceBox.getY() && "1357".contains(targetPieceBox.getY() + "")) {
			int delta = Math.abs(selectPieceBox.getX() - targetPieceBox.getX());
			if (delta == 1) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		JSONObject map = JSON.parseObject(
				"{\"11\":{\"x\":1,\"y\":1,\"piece\":0},\"12\":{\"x\":1,\"y\":2,\"piece\":0},\"13\":{\"x\":1,\"y\":3,\"piece\":0},\"14\":{\"x\":1,\"y\":4,\"piece\":0},\"15\":{\"x\":1,\"y\":5,\"piece\":0},\"16\":{\"x\":1,\"y\":6,\"piece\":0},\"17\":{\"x\":1,\"y\":7,\"piece\":0},\"18\":{\"x\":1,\"y\":8,\"piece\":0},\"21\":{\"x\":2,\"y\":1,\"piece\":0},\"22\":{\"x\":2,\"y\":2,\"piece\":0},\"23\":{\"x\":2,\"y\":3,\"piece\":0},\"24\":{\"x\":2,\"y\":4,\"piece\":0},\"25\":{\"x\":2,\"y\":5,\"piece\":0},\"26\":{\"x\":2,\"y\":6,\"piece\":0},\"27\":{\"x\":2,\"y\":7,\"piece\":0},\"28\":{\"x\":2,\"y\":8,\"piece\":0},\"31\":{\"x\":3,\"y\":1,\"piece\":0},\"32\":{\"x\":3,\"y\":2,\"piece\":0},\"33\":{\"x\":3,\"y\":3,\"piece\":0},\"34\":{\"x\":3,\"y\":4,\"piece\":0},\"35\":{\"x\":3,\"y\":5,\"piece\":0},\"36\":{\"x\":3,\"y\":6,\"piece\":0},\"37\":{\"x\":3,\"y\":7,\"piece\":0},\"38\":{\"x\":3,\"y\":8,\"piece\":0}}");
		System.out.println(map.keySet());

		Set<String> keySet = map.keySet();

		keySet.forEach(e -> {
			String[] split = e.split("");
			int x = Integer.parseInt(split[0]);
			int y = Integer.parseInt(split[1]);
			getNeiber(x, y).forEach(item -> {
				System.out.println(e + "->" + item);
			});
		});

	}

	// 获取相邻点
	public static Stream<String> getNeiber(int x, int y) {
		String line;
		if (y % 2 == 0) {
			line = x + "" + (y + 1 == 9 ? 1 : y + 1) + "," + x + "" + (y - 1);
		} else {
			line = x + "" + (y + 1) + "," + (x + "" + (y - 1 == 0 ? 8 : y - 1)) + ","
					+ ((x + 1) == 4 ? "" : (x + 1) + "" + y) + "," + ((x - 1) == 0 ? "" : (x - 1) + "" + y);
		}
		return Stream.of(line.split(",+"));
	}

	public static boolean hasMove(PieceBox box, Integer color, JSONObject map) {
		// thisxy 找到所以相邻点然后遍历 检查canMove
		String thisxy = box.getX() + "" + box.getY();
		List<String> canMoveList = getNeiber(box.getX(), box.getY()).filter(target -> {
			return canMove(thisxy + "," + target, color, map);
		}).collect(Collectors.toList());
		log.info("canMoveList {}", canMoveList);
		return !canMoveList.isEmpty();
	}

	public static boolean canFly(String thisxy, Integer color, JSONObject map) {
		// 检查能否移动
		String[] split = thisxy.split(",");
		String select = split[0];
		String target = split[1];
		// 1.检查棋子合规性
		// 检查select是否为用户颜色
		PieceBox selectPieceBox = map.getJSONObject(select).toJavaObject(PieceBox.class);
		if (selectPieceBox.getPiece() != color) {
			return false;
		}
		PieceBox targetPieceBox = map.getJSONObject(target).toJavaObject(PieceBox.class);
		// 检查select是否为0(空)
		if (targetPieceBox.getPiece() == 0) {
			return true;
		}
		return false;
	}

	public static boolean checkMill(String currentXy, Integer color, JSONObject map) {
		// 选出包含当前落子点的成三表
		List<String> containCurrentXyList = NineChess.millList.stream().filter(x -> x.contains(currentXy))
				.collect(Collectors.toList());
		log.info("currentXy => {}", currentXy);
		log.info("color	=> {}", color);
		log.info("containCurrentXyList	=> {}", containCurrentXyList);
		List<ArrayList<String>> list = containCurrentXyList.stream()
				.map(x -> new ArrayList<String>(Arrays.asList(x.split(",")))).filter(arr -> {
					return isMill(arr, currentXy, color, map);
				}).collect(Collectors.toList());
		int mills = list.size();
		// 当前点为场
		// TODO 判断有无散点
		if (mills > 0) {
			String millstr = list.toString();
			Collection<Object> values = map.values();

			// 获取除了当前点之外的棋子
			List<PieceBox> allList = values.stream()
					.map(x -> JSONObject.toJavaObject(JSON.parseObject(JSON.toJSONString(x)), PieceBox.class))
					.filter(x -> x.getPiece() == color && !millstr.contains(x.getX() + "" + x.getY()))
					.collect(Collectors.toList());
			List<PieceBox> collect = allList.stream().filter(x -> {
				return mill(x.getX() + "" + x.getY(), color, map) == 0;
			}).collect(Collectors.toList());
			int size = collect.size();
			log.info("{}", allList);
			if (size == 0) {
				// 全为厂
				log.info("全为厂");
				return false;
			} else {
				log.info("不全为厂");
				return true;
			}
		} else {
			// 没有厂
			return false;
		}
	}

	public static boolean isMill(List<String> list, String currentXy, Integer color, JSONObject map) {
		int n = 0;
		ArrayList<String> uselist = new ArrayList<>();
		uselist.addAll(list);
		uselist.remove(currentXy);
		for (String xy : uselist) {
			log.info("arr	=> {}", xy);
			log.info("arr	=> {}", map.getJSONObject(xy));
			PieceBox thisPieceBox = map.getJSONObject(xy).toJavaObject(PieceBox.class);
			if (thisPieceBox.getPiece() == color) {
				log.info("arr => {} {}", xy, true);
				n++;
			}
		}
		log.info("==========================================");
		return n == 2;
	}

	public static boolean win(Result result, WebSocket oppo, WebSocket user, JSONObject map) throws IOException {

		Result res = new Result();

		Integer pieceCount = oppo.getUser().getPieceCount();
		Integer pieceRecord = oppo.getUser().getPieceRecord();
		log.info("pieceRecord  :  " + pieceRecord);
		if (pieceRecord >= 9) {
			// 判断输赢
			log.info("===判断输赢===");
			if (pieceCount < 3) {
				res.setType(888); // 结局
				res.setGoal(5);// 0 等待 1 落子 2 移动 3飞行4吃子
				res.setMessage("系统：很遗憾您输了！");
				oppo.sendMessage(res.toString());
				res.setMessage("系统：恭喜您赢了！");
				user.sendMessage(res.toString());
				result.setWin(true);
				return true;
			} else if (pieceCount == 3) {
				log.info("pieceCount == 3  :  随便飞");
				Integer userCount = user.getUser().getPieceCount();
				if (userCount == 3) {
					res.setType(888); // 结局
					res.setGoal(5);// 0 等待 1 落子 2 移动 3飞行4吃子
					res.setMessage("系统：和棋！");
					oppo.sendMessage(res.toString());
					res.setMessage("系统：和棋！");
					user.sendMessage(res.toString());
					result.setWin(true);
					return true;
				}
			} else {
				// 是否每个棋子都不能移动
				// 1.拿到对手的每个点
				Collection<Object> values = map.values();
				List<PieceBox> allOppoPieceList = values.stream()
						.map(x -> JSONObject.toJavaObject(JSON.parseObject(JSON.toJSONString(x)), PieceBox.class))
						.filter(x -> x.getPiece() == oppo.getUser().getColor()).collect(Collectors.toList());
				log.info("allOppoPieceList {}", allOppoPieceList);
				// 判断是否有可移动的点
				List<PieceBox> canMovePieceList = allOppoPieceList.stream().filter(x -> {
					return hasMove(x, oppo.getUser().getColor(), map);
				}).collect(Collectors.toList());
				log.info("canMovePieceList {}", canMovePieceList);
				if (canMovePieceList.size() == 0) {
					res.setType(888); // 结局
					res.setGoal(5);// 0 等待 1 落子 2 移动 3飞行4吃子
					res.setMessage("系统：很遗憾您输了！");
					oppo.sendMessage(res.toString());
					res.setMessage("系统：恭喜您赢了！");
					user.sendMessage(res.toString());
					result.setWin(true);
					return true;
				}
			}
		}
		return false;
	}

	public static int updateMap(String currentXy, Integer color, JSONObject map) {
		log.info("currentXy => {}", currentXy);
		log.info("color	=> {}", color);
		// log.info("map => {}", map);
		if (currentXy.contains(",")) {// 包含逗号是移动操作
			String[] split = currentXy.split(",");
			String select = split[0];
			String target = split[1];
			JSONObject selectObject = map.getJSONObject(select);
			selectObject.put("piece", 0);
			JSONObject targetObject = map.getJSONObject(target);
			targetObject.put("piece", color);
			return UPDATE_DONE;
		} else {
			JSONObject pieceBox = map.getJSONObject(currentXy);
			log.info("currentNode	=> {}", pieceBox);
			Integer piece = pieceBox.getInteger("piece");
			if (piece == 0) {
				// 空格子
				log.info("updatemap	done {}", currentXy);
				pieceBox.put("piece", color);
				// map.put(currentXy, pieceBox);
				// log.info("nowmap {}", JSONObject.toJSONString(map, true));
				return UPDATE_DONE;
			} else {
				// 有子
				log.info("###has piece {} - {}", currentXy, pieceBox);
				return UPDATE_ERROR_HAVE_PIECE;
			}
		}
	}
}
