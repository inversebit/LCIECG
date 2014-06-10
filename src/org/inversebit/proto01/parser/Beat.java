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
package org.inversebit.proto01.parser;


public class Beat {
	
	private int RPeakPos;
	private int QPeakPos;
	private int SPeakPos;
	private int QStartPos;
	private int SEndPos;
	private int PStartPos;
	private int PEndPos;
	private boolean PPositive;
	private int TPeakPos;
	private int TEndPos;

	
	public Beat(int pRPP, int pQPP, int pSPP, int pQSP, int pSEP)
	{
		RPeakPos = pRPP;
		QPeakPos = pQPP;
		SPeakPos = pSPP;
		QStartPos = pQSP;
		SEndPos = pSEP;
	}
	
	@Override
	public String toString() {
		return "PStart: " + PStartPos + "\t PEnd: " +PEndPos + "\t QStart: " + QStartPos + " QPeak: " + QPeakPos +
				" RPeak: " + RPeakPos + " SPeak: " + SPeakPos + " SEnd: "+ SEndPos + 
				"\t TPeak: " + TPeakPos + " TEnd: " + TEndPos + "\n";
	}

	public int getRPeakPos() {
		return RPeakPos;
	}

	public int getQPeakPos() {
		return QPeakPos;
	}

	public int getSPeakPos() {
		return SPeakPos;
	}

	public int getQStartPos() {
		return QStartPos;
	}

	public int getSEndPos() {
		return SEndPos;
	}
	
	public int getPStartPos() {
		return PStartPos;
	}

	public void setPStart(int pStart) 
	{
		PStartPos = pStart;
	}

	public void setPForm(boolean pPositive) {
		PPositive = pPositive;
	}

	public boolean isPPositive() {
		return PPositive;
	}
	
	public int getTPeakPos() {
		return TPeakPos;
	}

	public void setTPeakPos(int tPeakPos) {
		TPeakPos = tPeakPos;
	}

	public int getTEndPos() {
		return TEndPos;
	}

	public void setTEndPos(int tEndPos) {
		TEndPos = tEndPos;
	}

	public void setNewPStartPos(int pPNewStartPos) {
		this.PStartPos = pPNewStartPos;		
	}

	public void setPEndPos(int pPEndPos) {
		this.PEndPos = pPEndPos;		
	}

	public int getPEndPos() {
		return this.PEndPos;
	}
}
