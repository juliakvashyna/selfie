package com.bigdropinc.selfieking.adapters;

public class MenuItem {
	private int id;

	

	private int titleres;
	private int imageres;
	private String path;

	public MenuItem(int id, String path) {
		super();
		this.setId(id);
		this.setPath(path);
	}
	public MenuItem(int id, int imageres) {
		super();
		this.setId(id);
		this.imageres = imageres;
	}
	
	public MenuItem(int id,int textres, int imageres) {
		super();
		this.setId(id);
		this.setTitleres(textres);
		this.imageres = imageres;
	}

	public MenuItem(int imageres) {
		this.imageres = imageres;
	}
	public int getImageres() {
		return imageres;
	}

	public void setImageres(int imageres) {
		this.imageres = imageres;
	}

	public int getTitleres() {
		return titleres;
	}

	public void setTitleres(int titleres) {
		this.titleres = titleres;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
