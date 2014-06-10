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

public class CircularArray {

	private int data[];
	private int capacity;
	private int head;
	private int rear;
	
	public CircularArray(int pCapacity)
	{
		capacity = pCapacity;
		data = new int[capacity];
		head = 0;
		rear = 0;
	}
	
	public void insertValue(int pVal)
	{
		head = (head+1)%capacity;
		
		if(head == rear)
		{
			rear = (rear+1)%capacity;;
		}
		
		data[head] = pVal;
	}
	
	public int getHead()
	{
		return data[head];
	}
	
	public int getRear()
	{
		return data[rear];
	}	
}
