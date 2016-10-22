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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.inversebit.proto01.Constants;
import org.inversebit.proto01.connection.SocketHolder;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


public class ReceiveDataTask extends AsyncTask<Integer, Integer, Void>{
	
	private static final int CA_CAPACITY = Constants.MEASUREMENTS_PER_SECOND/50; 
	
	private ArrayList<Integer> rawVals;
	private ArrayList<Float> filtVals;
	private ArrayList<Integer> roundVals;
	
	protected InputStream input;
	private DataReceiver dr;
	private CircularArray ca;
	private float avg;
	
	public ReceiveDataTask(DataReceiver pDr) throws IOException{
		input = SocketHolder.getMySH().getBluetoothSocket().getInputStream();
		dr = pDr;
		ca = new CircularArray(CA_CAPACITY);
		initFiltering();
		
		rawVals = new ArrayList<Integer>(10000);
		filtVals = new ArrayList<Float>(10000);
		roundVals = new ArrayList<Integer>(10000);
	}
	
	@Override
	protected Void doInBackground(Integer... params)
	{
		discardInvalidData();
		try {
			initFiltering();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void discardInvalidData()
	{
		try{
			while((input.read()) != 88);
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		dr.setNextVal(progress[0]);
	}

	protected Integer readAndParseData() throws IOException,UnsupportedEncodingException
	{		
		Integer result;
		byte[] buffer;
		int bl;
		int readByte;
		buffer = new byte[4];
		bl = 0;
		
		while((readByte = input.read()) != 88) //88=X
		{
			buffer[bl] = (byte)readByte;
			bl++;
		}
		
		result = Integer.parseInt((new String(buffer, 0, bl, "UTF-8")));
		
		rawVals.add(result);
		
		int rear = ca.getRear();
		avg = avg * CA_CAPACITY;
		avg = avg - rear;
		avg = avg + result;
		avg = avg / CA_CAPACITY;
		ca.insertValue(result);
				
		filtVals.add(avg);
		roundVals.add(Math.round(avg));
		
		return Math.round(avg);
	}
    
    @Override
    protected void onCancelled(Void result){
    	super.onCancelled(result);
    	Log.d(Constants.TAG, "RDT: onCancelled " + this.toString());
    	
    	//logVals();    	
    }

	private void logVals() {
		File sdCard = Environment.getExternalStorageDirectory();		
		File dir = new File (sdCard.getAbsolutePath() + "/proto1");
		dir.mkdirs();
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(dir, "debug_vals.txt"));

			for(int i = 0; i < rawVals.size(); i++)
			{
				fw.append(rawVals.get(i) + ";" + filtVals.get(i) + ";" + roundVals.get(i));
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
	}
    
    protected void initFiltering() throws IOException
    {
    	avg = 0;
    	
    	for(int i = 0; i < CA_CAPACITY; i++)
    	{
    		int readByte, bl = 0;
    		byte[] buffer = new byte[4];
			while((readByte = input.read()) != 88) //88=X
    		{    			
				buffer [bl] = (byte)readByte;
    			bl++;
    		}
			
			try
			{
				int num = Integer.parseInt((new String(buffer, 0, bl, "UTF-8")));
				ca.insertValue(num);
				avg += num;
			}
			catch(NumberFormatException e)
			{
				i--;
			}
    	}
    	
    	avg = avg/CA_CAPACITY;
    }
}
