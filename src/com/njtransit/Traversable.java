package com.njtransit;

/** Generic interface for an object that 
 * permits a function Fn to be applied over
 * its traversed set of elements.
 * 
 * @author doug
 */
public interface Traversable<T> {
	/**
	 * Some function that takes as its 
	 * argument an element T
	 * @param <T>
	 */
	public static interface Fn<T> {
		void apply(T element);
	}
	
	/** Applies Fn f to every element in 
	 * the Traversable's set */
	public void foreach(Fn<T> f);
}