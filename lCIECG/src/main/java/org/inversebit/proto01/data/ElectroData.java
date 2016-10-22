/*
Copyright (C) 2014 Alexander Mariel

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/
package org.inversebit.proto01.data;


public class ElectroData
{
	private int BPM;
	private float QRSLength;
	private float QRSAmplitude;
	private float meanRRLength;
	private int irregularBeatNum;
	private int weirdPWaves;
	private int negativePWaves;
	private int totalPAmplitude;
	private float PRLength;
	private float meanQTLength;
	private float meanQTcLength;
	private float meanPAmplitude;
	private float meanPLength;
	
	public ElectroData()
	{
		this.irregularBeatNum = 0;
		this.weirdPWaves = 0;
		this.negativePWaves = 0;
		this.totalPAmplitude = 0;
		this.PRLength = 0;
	}

	public int getBPM() {
		return BPM;
	}

	public void setBPM(int bPM) {
		BPM = bPM;
	}

	public float getQRSLength() {
		return QRSLength;
	}

	public void setQRSLength(float qRSLength) {
		QRSLength = qRSLength;
	}

	public float getQRSAmplitude() {
		return QRSAmplitude;
	}

	public void setQRSAmplitude(float qRSAmplitude) {
		QRSAmplitude = qRSAmplitude;
	}

	public float getMeanRRLength() {
		return meanRRLength;
	}

	public void setMeanRRLength(float pMeanRRLength) {
		this.meanRRLength = pMeanRRLength;
	}

	public int getIrregularBeatNum() {
		return irregularBeatNum;
	}

	public void increaseIrregularBeatNum() {
		this.irregularBeatNum++;
	}

	public int getWeirdPWaves() {
		return weirdPWaves;
	}

	public void increaseWeirdPWaves() {
		this.weirdPWaves++;
	}

	public int getNegativePWaves() {
		return negativePWaves;
	}

	public void increaseNegativePWaves() {
		this.negativePWaves++;
	}

	public int getTotalPAmplitude() {
		return totalPAmplitude;
	}

	public void addToTotalPAmplitude(int pExtraAmplitude) {
		this.totalPAmplitude += pExtraAmplitude;
	}

	public float getPRLength() {
		return PRLength;
	}

	public void setPRLength(float pRLength) {
		PRLength = pRLength;
	}

	public float getMeanQTLength() {
		return meanQTLength;
	}

	public void setMeanQTLength(float meanQTLength) {
		this.meanQTLength = meanQTLength;
	}

	public float getMeanQTcLength() {
		return meanQTcLength;
	}

	public void setMeanQTcLength(float meanQTcLength) {
		this.meanQTcLength = meanQTcLength;
	}

	public float getMeanPAmplitude() {
		return meanPAmplitude;
	}

	public void setMeanPAmplitude(float meanPAmplitude) {
		this.meanPAmplitude = meanPAmplitude;
	}

	public float getMeanPLength() {
		return meanPLength;
	}

	public void setMeanPLength(float meanPLength) {
		this.meanPLength = meanPLength;
	}
	
}
