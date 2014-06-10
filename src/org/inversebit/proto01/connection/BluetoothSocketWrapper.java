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
package org.inversebit.proto01.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.inversebit.proto01.Constants;

import android.bluetooth.BluetoothSocket;


public class BluetoothSocketWrapper
{	
	private int i;
	
	private BluetoothSocket bts;
	
	public BluetoothSocketWrapper(BluetoothSocket pBts){
		bts = pBts;
		i = 0;
	}
	
	public InputStream getInputStream() throws IOException{
		if(bts == null){
			return new InputStream() {
				int x = 3, max = 3;
				String aux;
				
				@Override
				public int read() throws IOException
				{
					long t1 = System.currentTimeMillis();
					long t2 = t1;
					//while(t2-t1 < 1) t2 = System.currentTimeMillis();
					
					if(x == max){
						x = 0;
						if(i >= Constants.data.length){
							i = 0;
						}
						aux = Integer.toString(Constants.data[i]);
						max = aux.length();
						i++;
						return 88;
					}
					else
					{
						x++;
						return (int)aux.charAt(x-1);						
					}
				}
			};
		}
		else
		{
			return bts.getInputStream();
		}
	}
	
	public OutputStream getoutputStream() throws IOException{
		if(bts == null){
			return new OutputStream() {
				@Override
				public void write(int oneByte) throws IOException
				{			
				}
			};
		}
		else{
			return bts.getOutputStream();
		}
	}

	public void close() throws IOException
	{
		if(bts == null){
			
		}
		else{
			bts.close();
		}		
	}
}
