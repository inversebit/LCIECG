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

import java.io.IOException;

import org.inversebit.proto01.main.Diagnoser;
import org.inversebit.proto01.main.DiagnoserFragment;


public class ReceiveAndStoreTask extends ReceiveDataTask
{
	private Diagnoser diag;
	
	public ReceiveAndStoreTask(DataReceiver pDr) throws IOException
	{
		super(pDr);
		diag = (Diagnoser) pDr;
	}

	@Override
	protected Void doInBackground(Integer... params)
	{
		super.doInBackground(params);
		
		int maxRounds = params[0];
		int actualRounds = 0;
		Integer result;
		
		try
		{
			while((actualRounds < maxRounds)&&!isCancelled()){
				result = super.readAndParseData();
				publishProgress(result);
				actualRounds++;
			}
		}
		catch(IOException e1){
			e1.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		super.onPostExecute(result);
		DiagnoserFragment.updateProgressBar(1);
		diag.parseCollectedData();
	}
}
