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
package org.inversebit.proto01.parser.strategy;

public class NegPWCompare implements CompareStartegy {

	@Override
	public boolean PStartAmpliCompare(int a, int b) {
		return a > b;
	}

	@Override
	public boolean PInitialVectorsPurge(int a, int b) {
		return a <= b;
	}

	@Override
	public boolean PFinalVectorsPurge(int a, int b) {
		return a >= b;
	}

	@Override
	public boolean PFinalVectorsExtraConditionPurge(int a, int b) {
		return a > b;
	}

	@Override
	public boolean VectorAmpliAcc(Integer a, int b) {
		return a < b;
	}

	@Override
	public boolean PStartVectorsPurge(Integer a, int b) {
		return a > b;
	}

}
