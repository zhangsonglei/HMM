package hust.tools.hmm.io;

import java.io.IOException;

/**
 *<ul>
 *<li>Description: 模型数据写出接口 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public interface HMMWriter {
	
	/**
	 * 写出状态索引
	 * @param entry	状态索引条目
	 * @throws IOException
	 */
	public void writeIndex(DictionaryEntry entry) throws IOException;
	
	/**
	 * 写出初始转移向量
	 * @param entry	初始转移向量的条目
	 * @throws IOException
	 */
	public void writePi(PiEntry entry) throws IOException;
	
	/**
	 * 写出转移矩阵
	 * @param entry	转移矩阵的条目
	 * @throws IOException
	 */
	public void writeTransitionMatrix(TransitionEntry entry) throws IOException;
	
	/**
	 * 写出发射矩阵
	 * @param entry	发射矩阵的条目
	 * @throws IOException
	 */
	public void writeEmissionMatrix(EmissionEntry entry) throws IOException;
	
	
	/**
	 * 写出长整型数据 
	 * @param value	待写出的整型数据 
	 * @throws IOException
	 */
	
	public void writeCount(long value) throws IOException;
	
	/**
	 * 关闭写入流  
	 * @throws IOException
	 */
	
	public void close() throws IOException;

	/**
	 * 保存模型 ，执行此方法后将自动关闭写入流
	 * @throws IOException
	 */
	public void persist() throws IOException;
}
