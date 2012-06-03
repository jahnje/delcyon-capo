package com.delcyon.capo;

public interface InterruptibleRunnable extends Runnable
{
	/** adds the ability to interrupt a to a runnable class **/
	public void interrupt();
}
