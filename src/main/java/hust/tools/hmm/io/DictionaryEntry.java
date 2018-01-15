package hust.tools.hmm.io;

import java.io.Serializable;

/**
 *<ul>
 *<li>Description: 字典条目 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class DictionaryEntry implements Serializable {

	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = 2381440933843325537L;

	private Object object;
	
	private int index;
	
	public DictionaryEntry(Object object, int index) {
		this.object = object;
		this.index = index;
	}

	public Object getObject() {
		return object;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return object + "\t" + index;
	}
}
