package hust.tools.hmm.utils;

import java.util.Iterator;
import java.util.List;

/**
 * <ul>
 *<li>Description: 观测序列，状态序列接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月27日
 *</ul>
 * @param <T>序列元素的类型
 */
public interface Sequence<T> {
    
    public void add(T token);
    
    public void add(T[] tokens);
    
    public void update(T token, int index);
    
    public void remove(int index);
    
    public T get(int index);
    
    public List<T> get();
    
    public Iterator<T> iterator();
    
    public int size();
    
    @Override
	public int hashCode();

	@Override
	public boolean equals(Object object);
	
	@Override
    public String toString();
}
