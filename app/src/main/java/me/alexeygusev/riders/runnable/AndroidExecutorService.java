package me.alexeygusev.riders.runnable;

/**
 * Created by Alex Gusev on 30/04/2014.
 * Project: Fidel.
 *
 * Copyright (c) 2014. All rights reserved.
 */

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AndroidExecutorService implements ExecutorService {
	private static class NotImplementedException extends Error {
		private static final long serialVersionUID = -7038309951849359513L;}
	
	private int mMaxThreads;
	
	private LooperThread[] mThreads;
	private int mNextCreateThread = 0, mNextUseThread = 0;
	private int mThreadCount = 0;
	
	private static int sThreadId = 0;
	
	@SuppressLint("HandlerLeak")
	private class LooperThread extends Thread {
		private static final int MSG_QUIT = 0xdead;
		
		private Object mHandlerLock = new Object();
		private Handler mHandler;
		
		public LooperThread() {
			super();
			setName("Android-Thread-"+sThreadId++);
		}
		
		@Override
		public void run() {
			Looper.prepare();
			final Looper ownLooper = Looper.myLooper();
			
			synchronized(mHandlerLock) {
				mHandler = new Handler(ownLooper) {
					@Override
					public void handleMessage(Message msg) {
						switch(msg.what) {
							case MSG_QUIT: ownLooper.quit();
						}
					}
				};
				
				mHandlerLock.notifyAll();
			}
			
			RunOn.mainThread(new Runnable() {
				@Override
				public void run() {
					mThreadCount++;
				}
			});
			
			Looper.loop();
			
			RunOn.mainThread(new Runnable() {
				@Override
				public void run() {
					mThreadCount--;
				}
			});
		}
		
		public Handler getHandler() {
			synchronized(mHandlerLock) {
				while(mHandler == null)
					try {
						mHandlerLock.wait();
					} catch (InterruptedException e) {
					}
			}
			
			return mHandler;
		}
	}
	
	public AndroidExecutorService() {
		this(10);
	};

	public AndroidExecutorService(int maxThreadPool) {
		mMaxThreads = maxThreadPool;
		mThreads = new LooperThread[mMaxThreads];
	}

	@Override
	public void execute(Runnable command) {
		LooperThread thread = null;
		
		if(mThreadCount < mMaxThreads && 
				mNextCreateThread < mMaxThreads) {
			thread = new LooperThread();
			thread.start();
			
			mThreads[mNextCreateThread++] = thread;
		} else {
			// Check if we can respawn a thread
			int nextUseThread = 0;
			for(; nextUseThread < mMaxThreads; nextUseThread++) {
				thread = mThreads[nextUseThread];
				
				if(!thread.isAlive()) {
					thread = new LooperThread();
					mThreads[nextUseThread] = thread;
					thread.start();
					break;
				}
			}
			
			if(nextUseThread == mMaxThreads) {
				// reached the end, so just take the next thread in a 
				// roundrobin fashion
				thread = mThreads[mNextUseThread++];
				mNextUseThread = mNextCreateThread % mMaxThreads;
			}
		}	
		
		Handler handler = thread.getHandler();
		
		handler.removeMessages(LooperThread.MSG_QUIT);
		handler.post(command);
		handler.sendEmptyMessageDelayed(LooperThread.MSG_QUIT, 10000);
	}

	@Override
	public void shutdown() {
		for(LooperThread thread: mThreads) {
			if(thread != null && thread.isAlive()) {
				thread.getHandler().sendEmptyMessage(LooperThread.MSG_QUIT);
			}
		}
	}

	@Override
	public List<Runnable> shutdownNow() {
		shutdown();
		return null;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return false;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		throw new NotImplementedException();
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		throw new NotImplementedException();
	}

	@Override
	public Future<?> submit(Runnable task) {
		throw new NotImplementedException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		throw new NotImplementedException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new NotImplementedException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		throw new NotImplementedException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
		throw new NotImplementedException();
	}

}
