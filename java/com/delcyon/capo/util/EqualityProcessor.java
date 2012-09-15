package com.delcyon.capo.util;




public abstract class EqualityProcessor {

	
	public static boolean areSame(Object object1, Object object2) {
		if (object1 == null ^ object2 == null){
			return false;
		}
		else if (object1 == null & object2 == null){
			return true;
		}
		else return object1.equals(object2);
	}
	

}
