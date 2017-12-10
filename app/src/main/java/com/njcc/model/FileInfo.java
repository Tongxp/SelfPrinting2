package com.njcc.model;

import java.io.Serializable;

public class FileInfo implements Serializable {
	private String name = "";
	private int pages = 0;
	private double cost = 0.0d;
	private String kind = "";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPages() {
		return pages;
	}
	public void setPages(int pages) {
		this.pages = pages;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	@Override
	public String toString() {
		return "FileInfo [name=" + name + ", pages=" + pages + ", cost=" + cost + ", kind=" + kind + "]";
	}

}
