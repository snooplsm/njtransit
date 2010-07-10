package com.njtransit.domain;

public class Service implements Comparable<Service> {

	private int id;
	private int flag;
	
	public Service(int id, boolean... startWithMonday) {
		this.id = id;
		addFlag(startWithMonday[0],0);
		addFlag(startWithMonday[1],1);
		addFlag(startWithMonday[2],2);
		addFlag(startWithMonday[3],3);
		addFlag(startWithMonday[4],4);
		addFlag(startWithMonday[5],5);
		addFlag(startWithMonday[6],6);
	}
	
	private void addFlag(boolean add, int pos) {
		if(add) {
			flag = (flag | (int) Math.pow(2, pos));
		} else {
			flag = (flag & ~(int)Math.pow(2, pos));
		}
	}
	
	private boolean hasFlag(int pos) {
		int powered = (int)Math.pow(2,pos);
		return (powered & flag) == powered;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public boolean isFriday() {
		return hasFlag(4);
	}

	public boolean isMonday() {
		return hasFlag(0);
	}

	public boolean isSaturday() {
		return hasFlag(5);
	}

	public boolean isSunday() {
		return hasFlag(6);
	}

	public boolean isThursday() {
		return hasFlag(3);
	}

	public boolean isTuesday() {
		return hasFlag(1);
	}

	public boolean isWednesday() {
		return hasFlag(2);
	}

	@Override
	public int compareTo(Service another) {
		return Integer.valueOf(id).compareTo(another.id);
	}
	
}
