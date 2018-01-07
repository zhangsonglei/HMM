package hust.tools.hmm.model;

import java.io.Serializable;

/**
 *<ul>
 *<li>Description: 元素概率与回退权重
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年8月20日
 *</ul>
 */
public class ProbBowEntry implements Serializable{
	
	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -4043018472570913337L;

	private double prob;

	private double bow;

	public double getProb() {
		return prob;
	}

	public void setBow(double bow) {
		this.bow = bow;
	}
	
	public double getBow() {
		return bow;
	}
	
	public ProbBowEntry(double prob, double bow) {
		this.prob = prob;
		this.bow = bow;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bow);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(prob);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProbBowEntry other = (ProbBowEntry) obj;
		if (Double.doubleToLongBits(bow) != Double.doubleToLongBits(other.bow))
			return false;
		if (Double.doubleToLongBits(prob) != Double.doubleToLongBits(other.prob))
			return false;
		return true;
	}
}
