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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import org.inversebit.proto01.Constants;
import org.inversebit.proto01.data.ElectroData;
import org.inversebit.proto01.main.Diagnoser;
import org.inversebit.proto01.main.DiagnoserFragment;
import org.inversebit.proto01.parser.strategy.CompareStartegy;
import org.inversebit.proto01.parser.strategy.NegPWCompare;
import org.inversebit.proto01.parser.strategy.PosPWCompare;

import android.os.AsyncTask;
import android.os.Environment;


public class ParseDataTask extends AsyncTask<Void, Void, Void>
{
	private static final double RIGHT_SLOPE_TOL = -7.0;
	private static final double LEFT_SLOPE_TOL = 4.0;
	private static final float R_PEAK_TOLERANCE = 0.22f;
	private static final int DUPLICATED_POSITION_DIFF_THRESHOLD = 5;
	private static final int VECTOR_TIME_DISPLACEMENT = 30;
	private static final int T_MS_LENGTH = 200;

	private static float RR_LENGTH_TOLERANCE;
	
	private final int samplingRate;
	private final float valOffset;
	private final float valRel;
	private Diagnoser diag;
	private ElectroData ed;
	
	private int[] amplitudeVals;
	private ArrayList<Beat> beats; 
	private ArrayList<Integer> RRLengths;
	
	public ParseDataTask(int[] pData, int psamplingRate, float pvalOffset, float pvalRel, Diagnoser pDiag)
	{
		amplitudeVals = pData;
		samplingRate = psamplingRate;
		valOffset = pvalOffset;
		valRel = pvalRel;
		
		diag = pDiag;
		ed = diag.getElectroData();
	}
	
	protected Void doInBackground(Void... params)
	{		
		ArrayList<Integer> RPeaks = computeSortedRPeaks();
								
		//+-+-+-+DATA+-+-+-+  
		initBeats(RPeaks);
		
		//+-+ COMPUTATION +-+
		computeBPMAndRRL();
		computeQRSLength();
		checkIfRegularRythm();
		
		//+-+-+-+DATA+-+-+-+ 
		identifyAndAddPWaves();
		parsePWaves();
				
		//+-+ COMPUTATION +-+
		computeMeanPAmplitude();
		computeMeanPLength();
		computePRInterval();
		
		//+-+-+-+DATA+-+-+-+ 
		identifyAndAddTWaves();
		
		//+-+ COMPUTATION +-+
		computeQTInterval();
		
		//+-+-+-+DATA+-+-+-+ 
		onPostExecute();
		
		return null;
	}

	private void identifyAndAddTWaves() {
			
		Iterator<Beat> itr = beats.iterator();
		int cont = 0;
		
		while(itr.hasNext())
		{
			Beat beat = itr.next();
			int nextBeatPStart = (cont<beats.size()-1)?beats.get(cont+1).getPStartPos():amplitudeVals.length;
			int SEndPos = beat.getSEndPos();
			int TPeak = SEndPos + (nextBeatPStart - SEndPos)/3;
					
			for(int i = TPeak; i > SEndPos; i--)
			{
				if(amplitudeVals[i] > amplitudeVals[TPeak])
				{
					TPeak = i;
				}
			}
					
			int TPrevEnd = TPeak + millisToUnits(25);
			int TEndPos = TPrevEnd + millisToUnits(20);
			boolean exit = false;
			while(!exit &&
					(TEndPos < TPeak + unitsToMillis(T_MS_LENGTH)) &&
					(TEndPos < nextBeatPStart))
			{
				TPrevEnd++;
				TEndPos++;
				
				if(amplitudeVals[TPrevEnd] < amplitudeVals[TEndPos]) //Condicion de salida para exit
				{
					if(amplitudeVals[TPrevEnd] < amplitudeVals[TEndPos+1]) //Comprobar que no es un valor tonto fastidiado pro el ruido
					{
						exit = true;
					}
				}
			}
			
			beat.setTPeakPos(TPeak);
			beat.setTEndPos(TEndPos);
			cont++;
		}
	}

	private void identifyAndAddPWaves() {
		Iterator<Beat> itr = beats.iterator();
		while(itr.hasNext())
		{
			Beat beat = itr.next();
			int QStartPos = beat.getQStartPos();
			PInfo pinfo = getPInfo(QStartPos-(int)(ed.getMeanRRLength()/4), QStartPos);
			beat.setPStart(pinfo.getPStart());
			beat.setPForm(pinfo.isPPositive());
		}
	}

	/**
	 * Adds basic info to beats list
	 * @param RPeaks
	 */
	private void initBeats(ArrayList<Integer> RPeaks) {		
		addQRSInfo(RPeaks);
	}

	/**
	 * Adds info on QPeaks, SPeaks, QStartPos and SEndPos
	 * @param RPeaks
	 */
	private void addQRSInfo(ArrayList<Integer> RPeaks) {
		this.beats = new ArrayList<Beat>(RPeaks.size()-2);
		
		Iterator<Integer> itr = RPeaks.iterator();
		itr.next(); //Ignore first RPeak
		int anRPeak = itr.next();
		while(itr.hasNext())
		{
			int QPeak = findQPeak(anRPeak);
			int SPeak = findSPeak(anRPeak);
			int QStartPos = findQStart(QPeak);
			int SEndPos = findSEnd(SPeak);
			
			this.beats.add(new Beat(anRPeak, QPeak, SPeak, QStartPos, SEndPos));
						
			anRPeak = itr.next();
		}
	}

	/**
	 * Get the R peaks forom the ECG
	 * @return
	 */
	private ArrayList<Integer> computeSortedRPeaks()
	{
		//1. Get maxima from the wave
		ArrayList<Integer> maxima = getMaxima(amplitudeVals);
		
		//2. Sort the maxima
		sortPerValue(maxima);
		Collections.reverse(maxima);
				
		//3. Select the maxima corresponding to R peaks
		ArrayList<Integer> RPeaks = getRPeaks(maxima);
				
		//4. Order the R peaks
		Collections.sort(RPeaks);
		
		purgeDuplicated(RPeaks);
		
		return RPeaks;
	}

	private void sortPerValue(ArrayList<Integer> maxima) 
	{
		Collections.sort(maxima, new Comparator<Integer>(){
			
			@Override
			public int compare(Integer lhs, Integer rhs)
			{
				if(amplitudeVals[lhs] > amplitudeVals[rhs])
				{
					return 1;
				}
				else
				{
					if(amplitudeVals[lhs] < amplitudeVals[rhs]){
						return -1;
					}
				}
				return 0;
			}	
		});		
	}

	/**
	 * Get maxima from electro
	 * @param pData
	 * @return
	 */
	private ArrayList<Integer> getMaxima(int[] pData)
	{
		ArrayList<Integer> result = new ArrayList<Integer>(pData.length/2 + (int)(pData.length*0.33));
		
		int initialPos = millisToUnits(30); //A partir de los 30ms
		int finalPos = pData.length - millisToUnits(30); //30ms antes del final
		
		for(int previousPos = initialPos, presentPos = initialPos+1, futurePos = initialPos+2;
				futurePos < finalPos;
				previousPos++, presentPos++, futurePos++)
		{
			if((pData[presentPos] > pData[previousPos])&&(pData[presentPos] >= pData[futurePos]))
			{
				result.add(presentPos);
			}
		}
		
		return result;
	}

	private ArrayList<Integer> getRPeaks(ArrayList<Integer> maxima)
	{
		ArrayList<Integer> Rpeaks = new ArrayList<Integer>((amplitudeVals.length/samplingRate)*2); //Initial capacity: 2 times the seconds (up to 120BPM)
		int max = amplitudeVals[maxima.get(0)];
		boolean finished = false;
		Integer aux;
			
		//1. Get biggest peaks
		Iterator<Integer> itr = maxima.iterator();
		while(itr.hasNext() && !finished)
		{
			aux = itr.next();
			if(amplitudeVals[aux] < max-(max*R_PEAK_TOLERANCE ))
			{//1.1 Until a peak is less then the tolerance
				finished = true;
				break;
			}
			
			Rpeaks.add(aux);			
		}
		
		//2. Check slope of ECG to either side of peak
		LinkedList<Integer> toBePurged = new LinkedList<Integer>();
		itr = Rpeaks.iterator();
		while(itr.hasNext())
		{
			int posRPeak = itr.next();
			int ampliRPeak = amplitudeVals[posRPeak];
			
			//2.1 Init positions
			int posIzq = posRPeak-millisToUnits(VECTOR_TIME_DISPLACEMENT); //30ms a la izqda
				//if(posIzq < 0)posIzq = 0;
			int ampliIzq = amplitudeVals[posIzq];

			//2.2 Init vector
			float[] vectIzq = new float[2]; //0 -> comp x; 1 -> comp y
			vectIzq[0] = unitsToMillis(posIzq - posRPeak);
			vectIzq[1] = ampliIzq - ampliRPeak;
						
			int posDer = posRPeak+millisToUnits(VECTOR_TIME_DISPLACEMENT); //30 ms a la dcha
				//if(posDer > amplitudeVals.length)posDer = amplitudeVals.length-1;
			int ampliDer = amplitudeVals[posDer];
			
			float[] vectDer = new float[2]; //0 -> comp x; 1 -> comp y
			vectDer[0] = unitsToMillis(posDer - posRPeak);
			vectDer[1] = ampliDer - ampliRPeak;
			
			//2.3 Get slope
			float vectIzqSlope = (float)vectIzq[1]/vectIzq[0];
			float vectDerSlope = (float)vectDer[1]/vectDer[0];
					
			//2.4 Check if slope if big enough, if not: Discard peak
			if((vectIzqSlope < LEFT_SLOPE_TOL)&&(vectDerSlope > RIGHT_SLOPE_TOL))
			{
				toBePurged.add(posRPeak);
			}
		}
		
		//3. Discard non-R peaks
		Rpeaks.removeAll(toBePurged);
		
		return Rpeaks;
	}

	private void purgeDuplicated(ArrayList<Integer> values) {
		LinkedList<Integer> toBePurged = new LinkedList<Integer>();
		Iterator<Integer> itr = values.iterator();
		Integer previousVal = itr.next();
		Integer actualVal;
		
		//1. Traverse R-Peaks
		while(itr.hasNext())
		{
			actualVal = itr.next();
			//1.1 If peaks are to close together
			if(previousVal > actualVal-DUPLICATED_POSITION_DIFF_THRESHOLD )
			{
				//1.2 Prepare for removal
				toBePurged.add(previousVal);
			}
			
			previousVal = actualVal;
		}
		
		//2. Remove duplicated peaks (only one of the two mini-peaks on the R-Peak)
		values.removeAll(toBePurged);		
	}

	private int findSPeak(int RPeakPos) 
	{
		int posActual = RPeakPos + millisToUnits(10);
		int valActual = amplitudeVals[posActual];		
		int valAnterior = amplitudeVals[RPeakPos];
		
		while(valAnterior >= valActual)
		{
			posActual++;
			valAnterior = valActual;
			valActual = amplitudeVals[posActual];			
		}
		
		return posActual-1;		
	}
	
	private int findQPeak(int RPeakPos) 
	{
		int posActual = RPeakPos - 1;
		int valActual = amplitudeVals[posActual];
		int valAnterior = amplitudeVals[RPeakPos];
		
		while(valAnterior > valActual)
		{
			posActual--;
			valAnterior = valActual;
			valActual = amplitudeVals[posActual];			
		}
		
		return posActual+1;		
	}

	private int findQStart(int QPeakPos) 
	{	
		int QStartPos = QPeakPos-1;
		
		int actualVal = amplitudeVals[QStartPos];
		int prevVal = amplitudeVals[QStartPos+1];
		
		while(actualVal >= prevVal)
		{
			prevVal = actualVal;
			actualVal = amplitudeVals[QStartPos];
			QStartPos--;
		}
		
		return QStartPos;		
	}

	private int findSEnd(int SpeakPos) 
	{		
		int SEndPos = SpeakPos+1;
		int actualVal = amplitudeVals[SEndPos];
		int prevVal = amplitudeVals[SEndPos-1];
		
		while(actualVal >= prevVal)
		{
			prevVal = actualVal;
			actualVal = amplitudeVals[SEndPos];
			SEndPos++;
		}
		
		return SEndPos;				
	}

	/**
	 * Gets RRLengths between Rs and computes the mean
	 * Also computes BPM
	 */
	private void computeBPMAndRRL()
	{
		RRLengths = new ArrayList<Integer>(beats.size());
		Iterator<Beat> itr = beats.iterator();
		int previousPosition = itr.next().getRPeakPos();
		int totalDisplacement = 0;
		int aux, RRLength;
		while(itr.hasNext())
		{
			aux = itr.next().getRPeakPos();
			RRLength = (aux - previousPosition);
			RRLengths.add(RRLength);
			totalDisplacement += RRLength;
			previousPosition = aux;
		}
				
		ed.setMeanRRLength(unitsToMillis(totalDisplacement/beats.size()));
		ed.setBPM((int)((60 * 1000) / ed.getMeanRRLength()));
	}

	private void computeQRSLength() {
		int cumulativeQRSLengths = 0;
		int cumulativeQRSAmplitude = 0;
		
		Iterator<Beat> itr = beats.iterator();
		
		while(itr.hasNext())
		{
			Beat beat = itr.next();
			int RPeak = beat.getRPeakPos();
			int QPeak = beat.getQPeakPos();
			int SPeak = beat.getSPeakPos();
			int QStartPos = beat.getQStartPos();
			int SEndPos = beat.getSEndPos();
			
			cumulativeQRSAmplitude += computeQRSAmplitude(amplitudeVals[RPeak], amplitudeVals[QPeak], amplitudeVals[SPeak]);
			cumulativeQRSLengths += (SEndPos - QStartPos);
		}
		
		ed.setQRSAmplitude((cumulativeQRSAmplitude/beats.size())/valRel);
		ed.setQRSLength(unitsToMillis(cumulativeQRSLengths/beats.size()));
	}

	private int computeQRSAmplitude(int RPVal, int QPVal, int SPVal) {
		int minimum;
		
		if(QPVal < SPVal){
			minimum = QPVal;
		}else{
			minimum = SPVal;
		}
		
		return RPVal - minimum;
	}

	/**
	 * Check if distance between Rs is inside mean*(1+tolerance) where tolerance is
	 * the value computed by getRRLengthTolerance
	 */
	private void checkIfRegularRythm() {
		Iterator<Integer> itr = RRLengths.iterator();
		
		getRRLengthTolerance();
		
		int RRL;
				
		while(itr.hasNext()){
			RRL = itr.next();
			if((RRL > ed.getMeanRRLength() + RR_LENGTH_TOLERANCE) || (RRL < ed.getMeanRRLength() - RR_LENGTH_TOLERANCE))
			{
				ed.increaseIrregularBeatNum();
			}
		}
	}

	/**
	 * Gets RRLengthTolerance by computing the standard deviation
	 */
	private void getRRLengthTolerance() 
	{
		double accumulator = 0.0;
		Iterator<Integer> itr = RRLengths.iterator();
		while(itr.hasNext())
		{
			accumulator += Math.pow(unitsToMillis(itr.next()-ed.getMeanRRLength()), 2);
		}
		
		RR_LENGTH_TOLERANCE = (float) Math.sqrt(accumulator/RRLengths.size());
	}

	/**
	 * Parses the PWave, first it vecotrizes it, then the vectorization
	 * is refined and finally useful data is extracted from the vectors
	 */
	private void parsePWaves() 
	{
		CompareStartegy compstrat;
		Iterator<Beat> beatsItr = beats.iterator();
		while(beatsItr.hasNext())
		{
			Beat beat = beatsItr.next();
			Integer QStartPos = beat.getQStartPos();
			Integer PStart = beat.getPStartPos();
			if(beat.isPPositive())compstrat= new PosPWCompare();
			else compstrat = new NegPWCompare();
			
			ArrayList<Integer> vectors = vectorize(PStart, QStartPos);			
			int forwardPos = invRefinePWaveLocalization(vectors, compstrat);
			beat.setNewPStartPos(beat.getPStartPos()+10*forwardPos);
			
			int PEnd = beat.getPStartPos() + 10*vectors.size();
			if(PEnd >= QStartPos)PEnd = QStartPos - 1;
			
			beat.setPEndPos(PEnd);
			
			parsePWave(vectors, compstrat);		
		}
	}

	/**
	 * Computes if the PWave is positive or negative
	 * Then it computes where the PWave begins
	 * @param start
	 * @param end
	 * @return PInfo instance which contains pstartpos and if PWave is
	 * positive or negative
	 */
	private PInfo getPInfo(int start, int end) {
		CompareStartegy compstrat;
		int extremePos;
		boolean PPositive = true;
		
		//1. Pre-check if convex or concave P wave
		int sum = 0;
		for(int i = start; i < end; i++)
		{
			sum += this.amplitudeVals[i] - amplitudeVals[end]; 
		}
		
		//2. Run getting PStart algorithm
		if(sum > 0)
		{//Convex Pwave (positive)
			compstrat = new PosPWCompare();	
		}
		else
		{//Concave Pwave (negative)
			//if del bucle sera >
			compstrat = new NegPWCompare();
		}
		
		int midpoint = (start+end)/2;
		extremePos = getPStart(start, midpoint, compstrat);
		
		//3. Check result sense
		if((extremePos < midpoint+millisToUnits(20))&&(extremePos > midpoint-millisToUnits(20))){
			System.err.println("Nonsense P start at: " + extremePos + " Sum: " + sum);
			
			if(compstrat instanceof PosPWCompare){
				compstrat = new NegPWCompare();
			}else{
				compstrat = new PosPWCompare();
			}
			
			extremePos = getPStart(start, midpoint, compstrat);
		}	
		
		if(compstrat instanceof NegPWCompare)
		{
			System.err.println("NP: " + extremePos + " start: " + start + " end: " + end + " sum: " + sum);
			ed.increaseNegativePWaves();
			PPositive = false;
		}
		
		return new PInfo(extremePos, PPositive);
	}

	private int getPStart(int segmentStart, final int segmentMidpoint, CompareStartegy compstrat) {
		int currentStart = segmentMidpoint;
		for(int i = segmentMidpoint; i > segmentStart; i--)
		{
			if(compstrat.PStartAmpliCompare(amplitudeVals[i], amplitudeVals[currentStart]))
			{
				currentStart = i;
			}
		}
		return currentStart;
	}

	private ArrayList<Integer> vectorize(int start, int end) 
	{
		ArrayList<Integer> result = new ArrayList<Integer>((int)ed.getMeanRRLength()/10 + 1);
		
		//get vectors
		for(int i = start; i < end; i+=10)
		{
			result.add(Integer.valueOf(amplitudeVals[i]-amplitudeVals[i-10]));
		}
		
		return result;
	}

	/**
	 * Removes vectors that are probably wrong due to bad PStartPos
	 * or PEndPos
	 * @param vectors
	 * @param compstrat 
	 */
	private int refinePWaveLocalization(ArrayList<Integer> vectors, CompareStartegy compstrat) 
	{
		System.err.println("vects: " + vectors);
		
		int forwardPos = 0;
		int index = 0;
		while(compstrat.PStartVectorsPurge(vectors.get(0), 0))//pos: a < b; neg a > b
		{
			vectors.remove(0);
			forwardPos++;
		}
		
		while((index < vectors.size())&&
			 (compstrat.PInitialVectorsPurge(vectors.get(index), 0)))//pos: a >= b; neg: a <= b
					index++;
		
		index++;
		while((index < vectors.size())&&
			 (compstrat.PFinalVectorsPurge(vectors.get(index), 0)))//pos:a <= b; neg: a >= b
				if(compstrat.PFinalVectorsExtraConditionPurge(vectors.get(index), -5))//pos: a < b; neg: a > b
					index++; 
				else break;
		
		while(vectors.size() > index) vectors.remove(index);
				
		return forwardPos;
	}
	
	/**
	 * Removes vectors that are probably wrong due to bad PStartPos
	 * or PEndPos
	 * @param vectors
	 * @param compstrat 
	 */
	private int invRefinePWaveLocalization(ArrayList<Integer> vectors, CompareStartegy compstrat) 
	{
		//System.err.println("vects: " + vectors);
		int index = vectors.size() - 1;
		
		//Si el de mas a la derecha es positivo 
		if(vectors.get(index) > 0 && vectors.get(index-1) < 0){
			vectors.remove(index);
			index--;
		}
		
		//Desde atras avanzar hasta el primer positivo
		while(index > 0 && vectors.get(index) < 5)
		{
			//vectors.remove(index);
			index--;
		}
		
		index--;
		//Subir y bajar por los positivos hasta que veo un negativo + Asegurar que no es un negativo chorra
		while(index > 0 && (vectors.get(index) > -5 && vectors.get(index-1) > 0)) index--;
		
		for(int i = 0; i < index; i++)
			vectors.remove(0);
		
		//Localizar vectores de bajada
		//while(vectors.get(index) > 0) index++;
		//System.err.println(vectors);
		int sum = 0;
		for(int i = 0; i < vectors.size(); i++)
			sum += vectors.get(i);
		//System.err.println("Sum: " + sum);
		
		int forwardPos = index;
		
		return forwardPos;
	}

	/**
	 * Extracts info from the vectors which represent the PWave
	 * @param vectors
	 * @param compstrat 
	 */
	private void parsePWave(ArrayList<Integer> vectors, CompareStartegy compstrat) 
	{		
		int elem = vectors.get(0);
		int sum = elem;
		int index = 1;
		
		while((index < vectors.size())&&(compstrat.VectorAmpliAcc(vectors.get(index), 0)))
		{
			sum += vectors.get(index);
			index++;
		}
		
		int amplitude = sum;
		
		if(!superficialWeirdPCheck(compstrat, amplitude))
		{
			ed.addToTotalPAmplitude(amplitude);
		}		
	}

	private boolean superficialWeirdPCheck(CompareStartegy compstrat, int amplitude) {
		if(compstrat instanceof PosPWCompare)
		{
			if(amplitude <= 40){
				ed.increaseWeirdPWaves();
				return true;
			}
		}
		else
		{
			if(amplitude >= -40){
				ed.increaseWeirdPWaves();
				return true;
			}
		}
		
		return false;
	}

	private void computeQTInterval() {
		Iterator<Beat> itr = this.beats.iterator();
		int accumulatedQTLength = 0;
		
		while(itr.hasNext())
		{
			Beat beat = itr.next();
			accumulatedQTLength += (beat.getTEndPos() - beat.getQStartPos());
		}
		
		ed.setMeanQTLength(unitsToMillis((float)accumulatedQTLength/beats.size()));
		ed.setMeanQTcLength(ed.getMeanQTLength()/(float)Math.sqrt(ed.getMeanRRLength()/1000));
	}

	private void computeMeanPAmplitude() {
		ed.setMeanPAmplitude(((float)ed.getTotalPAmplitude()/this.beats.size())/Constants.VAL_REL);
	}

	private void computeMeanPLength() {
		Iterator<Beat> itr = beats.iterator();
		int accPLength = 0;
		while(itr.hasNext())
		{
			Beat beat = itr.next();
			accPLength += beat.getPEndPos() - beat.getPStartPos();			
		}
		ed.setMeanPLength(unitsToMillis((float)accPLength/beats.size()));  
	}
	
	private void computePRInterval()
	{
		Iterator<Beat> itr = this.beats.iterator();
		itr.next();
		int accPRLength = 0;
		while(itr.hasNext())
		{
			Beat beat = itr.next();
			int PStart = beat.getPStartPos();
			int QStart = beat.getQStartPos();
			accPRLength += QStart-PStart;
		}
		
		ed.setPRLength(unitsToMillis((float)accPRLength/this.beats.size()));
	}
	
	private float unitsToMillis(float pUnits)
	{
		return pUnits*(1000/(float)samplingRate);
	}
	
	private int millisToUnits(int pMillis)
	{
		return (pMillis/(1000/samplingRate));
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		printToFile();
		
		DiagnoserFragment.updateProgressBar(2);
		diag.publishData();
		diag.diagnosePatient();
	}
	
	protected void onPostExecute()
	{
		/*
		System.out.println("BPM:\t\t" + this.BPM);
		System.out.println("RPeaks:\t\t" + this.beats.size());
		System.out.printf("QRSAMpli:\t%.2fmV\n", this.QRSAmplitude );
		System.out.printf("QRSLength:\t%.2fms\n", this.QRSLength);
		System.out.println("Mean RR Length:\t" + this.meanRRLength + "ms");
		System.out.printf("Mean P amplit:\t%.2fmV\n", this.meanPAmplitude);
		System.out.printf("Mean P length:\t%.2fms\n", this.meanPLength);
		System.out.printf("Mean PR length:\t%.2fms\n", this.PRLength);
		System.out.println("Abnormal P-Ws:\t" + this.weirdPWaves);
		System.out.println("Negative P-Ws:\t" + this.negativePWaves);
		System.out.printf("Mean QT len:\t%.2fms\n", this.meanQTLength);
		System.out.printf("Mean QTc len.:\t%.2fms\n", this.meanQTcLength);
		System.out.println("Irreg beats:\t" + this.irregularBeatNum);
		
		System.err.println(beats);
		
		printToFile();	
		*/	
	}

	private void printToFile() {
		File sdCard = Environment.getExternalStorageDirectory();		
		File dir = new File (sdCard.getAbsolutePath() + "/proto1");
		dir.mkdirs();
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(dir, "tmp.txt"));
			int lineNum = 0;
			
			Iterator<Beat> itr = beats.iterator();
			while(itr.hasNext())
			{
				Beat beat = itr.next();
				
				while(lineNum < beat.getPStartPos())
				{
					fw.append("\n");
					lineNum++;
				}
				fw.append(""+amplitudeVals[beat.getPStartPos()]);
				
				while(lineNum < beat.getPEndPos())
				{
					fw.append("\n");
					lineNum++;
				}
				fw.append(""+amplitudeVals[beat.getPEndPos()]);
				
				while(lineNum < beat.getQStartPos())
				{
					fw.append("\n");
					lineNum++;
				}				
				fw.append(""+amplitudeVals[beat.getQStartPos()]);
				
				while(lineNum < beat.getQPeakPos())
				{
					fw.append("\n");
					lineNum++;
				}				
				fw.append(""+amplitudeVals[beat.getQPeakPos()]);
				
				while(lineNum < beat.getRPeakPos())
				{
					fw.append("\n");
					lineNum++;
				}			
				fw.append(""+amplitudeVals[beat.getRPeakPos()]);
				
				while(lineNum < beat.getSPeakPos())
				{
					fw.append("\n");
					lineNum++;
				}				
				fw.append(""+amplitudeVals[beat.getSPeakPos()]);
				
				while(lineNum < beat.getSEndPos())
				{
					fw.append("\n");
					lineNum++;
				}				
				fw.append(""+amplitudeVals[beat.getSEndPos()]);
				
				while(lineNum < beat.getTPeakPos())
				{
					fw.append("\n");
					lineNum++;
				}				
				fw.append(""+amplitudeVals[beat.getTPeakPos()]);
				
				while(lineNum < beat.getTEndPos())
				{
					fw.append("\n");
					lineNum++;
				}				
				fw.append(""+amplitudeVals[beat.getTEndPos()]);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Scanner scan = null;
		try
		{
			scan = new Scanner(new File(dir, "tmp.txt"));
			fw = new FileWriter(new File(dir, "result.txt"));
			
			while(scan.hasNext())
			{
				try
				{
					int num = Integer.parseInt(scan.nextLine());
					if(num > 10000)
					{
						int num2 = num%1000;
						fw.append(num2 + "\n");
					}
					else
					{
						fw.append(num + "\n");
					}
				}
				catch(NumberFormatException e)
				{
					fw.append("\n");
				}				
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			scan.close();
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			fw = new FileWriter(new File(dir, "data.txt"));
			for(int i = 0; i < amplitudeVals.length; i++)
				fw.append(amplitudeVals[i] + "\n");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
}
