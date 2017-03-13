package jp.co.shekeenlab.PrefixDial;

import android.util.Log;

public class DebugHelper{
	
	private static boolean ENABLED = false;
	private static final String TAG = "shekeen";
	
	public static void print(Object... args){
		if(!ENABLED){
			return;
		}
		StringBuffer buffer = new StringBuffer();
		StackTraceElement stack = new Throwable().getStackTrace()[1];
		buffer.append("[");
		buffer.append(stack.getClassName());
		buffer.append("::");
		buffer.append(stack.getMethodName());
		buffer.append("] ");
		for(int i = 0; i < args.length; i++){
			if(i > 0){
				buffer.append(" ");
			}
			buffer.append(args[i]);
		}
		Log.d(TAG, buffer.toString());
	}
	
	public static void print(String text, byte[] data){
		if(!ENABLED){
			return;
		}
		if(data == null){
			return;
		}
		StringBuffer buffer = new StringBuffer(text);
		buffer.append(" 0x");
		for(byte bytedata : data){
			buffer.append(String.format("%02X", bytedata));
		}
		DebugHelper.print(buffer);
	}
	
	public static void printStackTrace(Exception e){
		if(!ENABLED){
			return;
		}
		e.printStackTrace();
	}
}
