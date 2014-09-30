package me.alexeygusev.riders.runnable;

/**
 * Created by Alex Gusev on 30/04/2014.
 * Project: Fidel.
 *
 * Copyright (c) 2014. All rights reserved.
 */

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class RunOn {
	public static interface TaskListener{
		public void onPostExecute();
	}
	
	public static interface TaskResultListener<T>{
		public void onPostExecute(T result);
		public void onError();
	}
	
	private static final ExecutorService sTaskPool = new AndroidExecutorService(5);
	private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
	
	private static Runnable captureCallingLooper(final Runnable run, final TaskListener listener) {
		final Looper currentLooper = Looper.myLooper();
		
		if(currentLooper == null && listener != null)
			throw new IllegalArgumentException("Can't use listeners from threads without Loopers!");
		
		final Handler currentHandler = new Handler(currentLooper);

		final Runnable decoratedRunnable = new Runnable() {
			@Override
			public void run() {
				run.run();
				
				currentHandler.post(new Runnable() {
					@Override
					public void run() {
						listener.onPostExecute();
					}
				});
			}
		};
		
		return decoratedRunnable;
	}
	
	private static <V> Runnable captureCallingLooper(final Callable<V> call, final TaskResultListener<V> listener) {
		final Looper currentLooper = Looper.myLooper();
		
		if(currentLooper == null && listener != null)
			throw new IllegalArgumentException("Can't use listeners from threads without Loopers!");
		
		final Handler currentHandler = new Handler(currentLooper);

		final Runnable decoratedRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					final V result = call.call();
					
					currentHandler.post(new Runnable() {
						@Override
						public void run() {
							listener.onPostExecute(result);
						}
					});
					
				} catch (Exception e) {
					currentHandler.post(new Runnable() {
						@Override
						public void run() {
							listener.onError();
						}
					});
				}
				
				
			}
		};
		
		return decoratedRunnable;
	}
	
	public static void taskThread(Runnable run) {
		sTaskPool.execute(run);
	}
	
	public static void taskThread(final Runnable run, final TaskListener listener) {
		if(listener == null)
			sTaskPool.execute(run);
		else
			sTaskPool.execute(captureCallingLooper(run, listener));
	}

	public static void mainThread(Runnable run) {
		sMainHandler.post(run);
	}

    public static void mainThreadDelayed(Runnable run, long delay) {
        sMainHandler.postDelayed(run, delay);
    }
	
	public static void mainThread(final Callable<?> call) {
		sMainHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					call.call();
				} catch (Exception e) {
				}
			}
		});
	}
	
	
	public static void mainThread(Runnable run, TaskListener listener) {
		if(listener == null)
			sMainHandler.post(run);
		else
			sMainHandler.post(captureCallingLooper(run, listener));
	}

    public static void mainThreadDelayed(Runnable run, TaskListener listener, long delay) {
        if(listener == null)
            sMainHandler.postDelayed(run, delay);
        else
            sMainHandler.postDelayed(captureCallingLooper(run, listener), delay);
    }
	
	public static <V> void mainThread(final Callable<V> call, TaskResultListener<V> listener) {
		if(listener == null)
			mainThread(call);
		else
			sMainHandler.post(captureCallingLooper(call, listener));
	}
}
