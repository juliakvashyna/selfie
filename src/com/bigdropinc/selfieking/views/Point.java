package com.bigdropinc.selfieking.views;

import java.io.Serializable;

public class Point implements Serializable {

	private static final long serialVersionUID = 4595441011960584209L;

	public float dy;

	public float dx;
	public float x;

	public float y;

	@Override
	public String toString() {
		return x + ", " + y;
	}
}
