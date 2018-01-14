package hust.tools.hmm.io;

import java.io.IOException;

/**
 *<ul>
 *<li>Description: 模型读入接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public interface DataReader {
	
	/**
	 * 读入长整型数据 
	 * @return 长整型数据 
	 * @throws IOException
	 */
	
	public long readCount() throws IOException;
	
	/**
	 * 读入字典数据
	 * @return	字典条目
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public DictionaryEntry readDict() throws IOException, ClassNotFoundException;
	
	/**
	 * 读入初始转移向量
	 * @return	初始转移向量的条目
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public PiEntry readPi() throws IOException, ClassNotFoundException;
	
	/**
	 * 读入转移矩阵
	 * @return	转移矩阵的条目
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public TransitionEntry readTransitionMatrix() throws IOException, ClassNotFoundException;
	
	/**
	 * 读入发射矩阵
	 * @return 发射矩阵的条目
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public EmissionEntry readEmissionMatrix() throws IOException, ClassNotFoundException;

	/**
	 * 关闭流  
	 * @throws IOException
	 */
	
	public void close() throws IOException;
}
