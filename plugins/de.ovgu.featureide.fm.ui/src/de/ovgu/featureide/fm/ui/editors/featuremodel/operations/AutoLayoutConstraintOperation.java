/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2016  FeatureIDE team, University of Magdeburg, Germany
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
package de.ovgu.featureide.fm.ui.editors.featuremodel.operations;

import static de.ovgu.featureide.fm.core.localization.StringTable.AUTO_LAYOUT_CONSTRAINTS;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;

import de.ovgu.featureide.fm.core.base.event.FeatureIDEEvent;
import de.ovgu.featureide.fm.core.base.event.FeatureIDEEvent.EventType;
import de.ovgu.featureide.fm.core.functional.Functional;
import de.ovgu.featureide.fm.ui.editors.IGraphicalConstraint;
import de.ovgu.featureide.fm.ui.editors.IGraphicalFeature;
import de.ovgu.featureide.fm.ui.editors.IGraphicalFeatureModel;
import de.ovgu.featureide.fm.ui.properties.FMPropertyManager;

/**
 * Operation to switch auto-layout for constraints on/off.
 * 
 * @author David Halm
 * @author Patrick Sulkowski
 * @author Marcus Pinnecke
 */
public class AutoLayoutConstraintOperation extends AbstractGraphicalFeatureModelOperation {

	private int counter;
	private LinkedList<LinkedList<Point>> oldPos = new LinkedList<LinkedList<Point>>();

	public AutoLayoutConstraintOperation(IGraphicalFeatureModel featureModel, LinkedList<LinkedList<Point>> oldPos, int counter) {
		super(featureModel, AUTO_LAYOUT_CONSTRAINTS);
		this.counter = counter;
		if (!(oldPos == null) && !oldPos.isEmpty())
			this.oldPos.addAll(oldPos);
	}

	@Override
	protected FeatureIDEEvent operation() {
		List<IGraphicalConstraint> constraintList = graphicalFeatureModel.getConstraints();
		int minX = Integer.MAX_VALUE;
		int maxX = 0;
		if (!constraintList.isEmpty()) {
			Point newPos = new Point();
			int y = 0;

			LinkedList<IGraphicalFeature> featureList = new LinkedList<>();
			featureList.addAll(Functional.toList(graphicalFeatureModel.getVisibleFeatures()));

			for (int i = 0; i < featureList.size(); i++) {
				if (y < featureList.get(i).getLocation().y) {
					y = featureList.get(i).getLocation().y;
				}
				if (minX > featureList.get(i).getLocation().x) {
					minX = featureList.get(i).getLocation().x;
				}
				if (maxX < featureList.get(i).getLocation().x) {
					maxX = featureList.get(i).getLocation().x + featureList.get(i).getSize().width;
				}
			}
			final IGraphicalConstraint constraint = constraintList.get(0);
			newPos.x = (minX + maxX) / 2 - constraint.getSize().width / 2;
			newPos.y = y + FMPropertyManager.getConstraintSpace();
			constraint.setLocation(newPos);
		}
		for (int i = 1; i < constraintList.size(); i++) {
			Point newPos = new Point();
			newPos.x = (minX + maxX) / 2 - constraintList.get(i).getSize().width / 2;
			newPos.y = constraintList.get(i - 1).getLocation().y + FMPropertyManager.getConstraintSpace();
			constraintList.get(i).setLocation(newPos);
		}
		return FeatureIDEEvent.getDefault(EventType.MODEL_LAYOUT_CHANGED);
	}

	@Override
	protected FeatureIDEEvent inverseOperation() {
		List<IGraphicalConstraint> constraintList = graphicalFeatureModel.getConstraints();
		if (!constraintList.isEmpty() && (!(oldPos == null) && !oldPos.isEmpty())) {
			constraintList.get(0).setLocation(oldPos.get(counter).get(0));
		}
		for (int i = 1; i < constraintList.size(); i++) {
			graphicalFeatureModel.getConstraints().get(i).setLocation(oldPos.get(counter).get(i));
		}
		return FeatureIDEEvent.getDefault(EventType.MODEL_LAYOUT_CHANGED);
	}

}
