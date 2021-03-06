package com.njtransit.domain;

import java.util.Calendar;

public class Service implements IService, Comparable<Service> {

	private int id;
	private int flag;
	
	public Service(int id, boolean... startWithMonday) {
		this.id = id;
		// SUNDAY IS 1 aka 0
		addFlag(startWithMonday[6],0);
		addFlag(startWithMonday[0],1);
		addFlag(startWithMonday[1],2);
		addFlag(startWithMonday[2],3);
		addFlag(startWithMonday[3],4);
		addFlag(startWithMonday[4],5);
		addFlag(startWithMonday[5],6);		
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
		int poweredAndFlagged = powered & flag;
		return poweredAndFlagged == powered;
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
		return hasFlag(5);
	}

	public boolean isMonday() {
		return hasFlag(1);
	}

	public boolean isSaturday() {
		return hasFlag(6);
	}

	public boolean isSunday() {
		return hasFlag(0);
	}

	public boolean isThursday() {
		return hasFlag(4);
	}

	public boolean isToday() {
		int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		boolean flag = hasFlag(today-1);
		return flag;
	}
	
	public boolean isTomorrow() {
		int tomorrow = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) + 1;
		return hasFlag(tomorrow);
	}
	
	public boolean isTuesday() {
		return hasFlag(2);
	}

	public boolean isWednesday() {
		return hasFlag(3);
	}

	@Override
	public int compareTo(Service another) {
		return Integer.valueOf(id).compareTo(another.id);
	}

	@Override
	public boolean isDate(Calendar cal) {
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if(Calendar.MONDAY==dayOfWeek) {
			return isMonday();
		}
		if(Calendar.TUESDAY==dayOfWeek) {
			return isTuesday();
		}
		if(Calendar.WEDNESDAY==dayOfWeek) {
			return isWednesday();
		}
		if(Calendar.THURSDAY==dayOfWeek) {
			return isThursday();
		}
		if(Calendar.FRIDAY==dayOfWeek) {
			return isFriday();
		}
		if(Calendar.SATURDAY==dayOfWeek) {
			return isSaturday();
		}
		if(Calendar.SUNDAY==dayOfWeek) {
			return isSunday();
		}
		return false;		
	}
	
}
