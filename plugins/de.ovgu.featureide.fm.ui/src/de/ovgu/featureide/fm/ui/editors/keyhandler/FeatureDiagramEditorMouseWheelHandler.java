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
package de.ovgu.featureide.fm.ui.editors.keyhandler;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;

/**
 * <<<<<<< HEAD:plugins/de.ovgu.featureide.fm.ui/src/de/ovgu/featureide/fm/ui/editors/keyhandler/FeatureDiagramEditorMouseWheelHandler.java
 * The mouse wheel listener performs two actions depending on mouse wheel input
 * preferred with state mask (optional)
 * =======
 * The mouse listener performs actions depending on mouse input including scrolling and zooming
 * >>>>>>> Added mouse scrolling while holding shift and using middle mouse
 * button:plugins/de.ovgu.featureide.fm.ui/src/de/ovgu/featureide/fm/ui/editors/keyhandler/FeaetureDiagramEditorMouseHandler.java
 * 
 * The default state mask is 0x0
 * 
 * @author Enis Belli
 * @author Joshua Sprey
 */
public class FeatureDiagramEditorMouseWheelHandler implements MouseWheelListener, MouseListener {
	private Action mouseWheelUpAction;
	private Action mouseWheelDownAction;
	private FigureCanvas figureCanvas;
	private int stateMask;

	private MouseMoveListener mouseMoveListener;
	private Point positionAtClick;

	public FeatureDiagramEditorMouseWheelHandler(Action mouseWheelUpAction, Action mouseWheelDownAtion) {
		this.mouseWheelDownAction = mouseWheelDownAtion;
		this.mouseWheelUpAction = mouseWheelUpAction;
		stateMask = 0x0;
	}

	public FeatureDiagramEditorMouseWheelHandler(Action mouseWheelUpAction, Action mouseWheelDownAtion, int stateMask) {
		this.mouseWheelDownAction = mouseWheelDownAtion;
		this.mouseWheelUpAction = mouseWheelUpAction;
		this.stateMask = stateMask;
	}

	public FeatureDiagramEditorMouseWheelHandler(int stateMask, FigureCanvas figureCanvas) {
		this.stateMask = stateMask;
		this.figureCanvas = figureCanvas;
	}

	public FeatureDiagramEditorMouseWheelHandler(final FigureCanvas figureCanvas) {
		this.figureCanvas = figureCanvas;
		mouseMoveListener = new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				Point currentMousePosition = new Point(e.x, e.y);
				Dimension difference = currentMousePosition.getDifference(positionAtClick);
				int xPosition = figureCanvas.getViewport().getViewLocation().x - difference.width;
				int yPosition = figureCanvas.getViewport().getViewLocation().y - difference.height;
				figureCanvas.scrollTo(xPosition, yPosition);
				positionAtClick = new Point(e.x, e.y);
			}
		};
	}

	@Override
	public void mouseScrolled(MouseEvent e) {
		if (mouseWheelUpAction != null || mouseWheelDownAction != null) {
			if (e.stateMask == stateMask && e.count > 0) {
				mouseWheelUpAction.run();
			} else if (e.stateMask == stateMask && e.count < 0) {
				mouseWheelDownAction.run();
			}

		} else if (mouseWheelDownAction == null && mouseWheelUpAction == null) {
			if (e.stateMask == stateMask && e.count > 0) {
				figureCanvas.scrollTo(figureCanvas.getViewport().getViewLocation().x + 200, figureCanvas.getViewport().getViewLocation().y);
			} else if (e.stateMask == stateMask && e.count < 0) {
				figureCanvas.scrollTo(figureCanvas.getViewport().getViewLocation().x - 200, figureCanvas.getViewport().getViewLocation().y);
			}
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button == 2) {
			figureCanvas.addMouseMoveListener(mouseMoveListener);
			positionAtClick = new Point(e.x, e.y);
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		if (e.button == 2) {
			figureCanvas.removeMouseMoveListener(mouseMoveListener);
		}

	}
}
