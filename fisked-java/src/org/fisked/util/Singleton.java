package org.fisked.util;

import java.util.concurrent.ConcurrentHashMap;

public class Singleton<T> {
	private Class<T> _class = null;
	private T _instance = null;
	
	private static ConcurrentHashMap<Class<?>, Singleton<?>> _map = new ConcurrentHashMap<>();
	
	public Singleton(Class<T> klass) {
		_class = klass;
	}
	
	private T get() {
		if (_instance != null) return _instance;
		synchronized (this) {
			if (_instance != null) return _instance;
			try {
				_instance = _class.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return _instance;
	}

	@SuppressWarnings("unchecked")
	public static <T> Singleton<T> getSingleton(Class<T> klass) {
		Singleton<T> instance = (Singleton<T>)_map.get(klass);
		
		if (instance == null) {
			instance = new Singleton<T>(klass);
			Singleton<T> prev = (Singleton<T>)_map.putIfAbsent(klass, instance);
			if (prev != null) return prev;
		}
		
		return instance;
	}
	
	public static <T> T getInstance(Class<T> klass) {
		return getSingleton(klass).get();
	}
}
