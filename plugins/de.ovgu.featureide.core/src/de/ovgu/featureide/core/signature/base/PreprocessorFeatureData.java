/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2015  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.core.signature.base;

import java.util.Collection;
import java.util.HashSet;

/**
 * TODO description
 * 
 * @author Sebastian Krieter
 */
public class PreprocessorFeatureData extends AFeatureData {

	private final HashSet<Integer> involvedFeatures = new HashSet<>();
	
	PreprocessorFeatureData(int startLineNumber, int endLineNumber) {
		super(-1, startLineNumber, endLineNumber);
	}

	public Collection<Integer> getInvolvedFeature() {
		return involvedFeatures;
	}
	
	public void addFeature(Integer featureID) {
		involvedFeatures.add(featureID);
	}
	
}
