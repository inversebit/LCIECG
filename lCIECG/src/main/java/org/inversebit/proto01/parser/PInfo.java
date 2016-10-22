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

public class PInfo {
	
	private int PStart;
	private boolean PPositive;
	
	public PInfo(int pPStart, boolean pPositive)
	{
		PStart = pPStart;
		PPositive = pPositive;
	}

	public int getPStart() {
		return PStart;
	}

	public boolean isPPositive() {
		return PPositive;
	}
	
	
}
