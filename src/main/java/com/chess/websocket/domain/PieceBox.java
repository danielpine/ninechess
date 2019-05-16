package com.chess.websocket.domain;

public class PieceBox {
	private Integer piece;
	private Integer x;
	private Integer y;

	public Integer getPiece() {
		return piece;
	}

	public void setPiece(Integer piece) {
		this.piece = piece;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "PieceBox [piece=" + piece + ", x=" + x + ", y=" + y + "]";
	}

}
