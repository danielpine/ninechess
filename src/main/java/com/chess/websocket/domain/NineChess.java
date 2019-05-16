package com.chess.websocket.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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

		log.info("{}", NineChess.millList);
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
		if (mills > 0) {
			String millstr = list.toString();
			Collection<Object> values = map.values();
			List<PieceBox> coloredList = values.stream()
					.map(x -> JSONObject.toJavaObject(JSON.parseObject(JSON.toJSONString(x)), PieceBox.class))
					.filter(x -> {
						log.info(x.getPiece() + "");
						log.info(millstr);
						log.info(x.getX() + "" + x.getY());
						return x.getPiece() == color && !millstr.contains(x.getX() + "" + x.getY());
					}).collect(Collectors.toList());
			int size = coloredList.size();
			log.info("{}", coloredList);
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

	public static boolean win() {
		return false;
	}

	public static int updateMap(String currentXy, Integer color, JSONObject map) {
		log.info("currentXy => {}", currentXy);
		log.info("color	=> {}", color);
//		log.info("map	=> {}", map);
		JSONObject pieceBox = map.getJSONObject(currentXy);
		log.info("currentNode	=> {}", pieceBox);
		Integer piece = pieceBox.getInteger("piece");
		if (piece == 0) {
			// 空格子
			log.info("updatemap	done {}", currentXy);
			pieceBox.put("piece", color);
//			map.put(currentXy, pieceBox);
//			log.info("nowmap {}", JSONObject.toJSONString(map, true));
			return UPDATE_DONE;
		} else {
			// 有子
			log.info("###has piece {} - {}", currentXy, pieceBox);
			return UPDATE_ERROR_HAVE_PIECE;
		}
	}
}
