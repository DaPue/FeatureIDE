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
package de.ovgu.featureide.fm.ui.editors.featuremodel.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.ui.PlatformUI;

import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import de.ovgu.featureide.fm.core.base.event.FeatureIDEEvent.EventType;
import de.ovgu.featureide.fm.core.base.impl.ExtendedFeature;
import de.ovgu.featureide.fm.core.explanations.Explanation.Reason;
import de.ovgu.featureide.fm.ui.FMUIPlugin;
import de.ovgu.featureide.fm.ui.editors.FeatureConnection;
import de.ovgu.featureide.fm.ui.editors.FeatureDiagramExtension;
import de.ovgu.featureide.fm.ui.editors.FeatureUIHelper;
import de.ovgu.featureide.fm.ui.editors.IGraphicalFeature;
import de.ovgu.featureide.fm.ui.editors.featuremodel.GUIDefaults;
import de.ovgu.featureide.fm.ui.editors.featuremodel.figures.CircleDecoration;
import de.ovgu.featureide.fm.ui.editors.featuremodel.figures.ConnectionFigure;
import de.ovgu.featureide.fm.ui.editors.featuremodel.figures.RelationDecoration;
import de.ovgu.featureide.fm.ui.editors.featuremodel.operations.ChangeFeatureGroupTypeOperation;
import de.ovgu.featureide.fm.ui.editors.featuremodel.operations.SetFeatureToMandatoryOperation;
import de.ovgu.featureide.fm.ui.properties.FMPropertyManager;

/**
 * An editpart for connections between features and their parents. Creates the
 * source decoration dependent on the mandatory property.
 * 
 * @author Thomas Thuem
 * @author Marcus Pinnecke
 */
public class ConnectionEditPart extends AbstractConnectionEditPart implements GUIDefaults, PropertyChangeListener {

	private static final DirectEditPolicy ROLE_DIRECT_EDIT_POLICY = new DirectEditPolicy() {
		@Override
		protected void showCurrentEditValue(DirectEditRequest request) {
		}

		@Override
		protected Command getDirectEditCommand(DirectEditRequest request) {
			return null;
		}
	};
	
	/** the currently active reason */
	private Reason activeReason;

	private Figure toolTipContent = new Figure();

	ConnectionEditPart(FeatureConnection connection) {
		setModel(connection);
	}

	@Override
	public FeatureConnection getModel() {
		return (FeatureConnection) super.getModel();
	}
	
	@Override
	public FeatureEditPart getSource() {
		return (FeatureEditPart) super.getSource();
	}
	
	@Override
	public FeatureEditPart getTarget() {
		return (FeatureEditPart) super.getTarget();
	}
	
	@Override
	public ConnectionFigure getFigure() {
		return (ConnectionFigure) super.getFigure();
	}

	@Override
	protected ConnectionFigure createFigure() {
		return new ConnectionFigure(connectsExternFeatures());
	}

	@Override
	protected void createEditPolicies() {
		if (connectsExternFeatures()) {
			return;
		}

		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, ROLE_DIRECT_EDIT_POLICY);
	}

	@Override
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_OPEN) {
			final boolean success = changeMandatory(request);
			if (success) {
				return;
			}
			changeConnectionType();
		}
	}

	/**
	 * Change the mandatory type is the circle decoration was selected.
	 */
	private boolean changeMandatory(Request request) {
		final IFeature feature = getModel().getSource().getObject();
		if (feature.getStructure().getParent().isAnd()) {
			final List<?> decorators = getFigure().getChildren();
			if (!decorators.isEmpty()) {
				Object child = decorators.get(0);
				if (child instanceof CircleDecoration) {
					final Rectangle decoratorBounds = new Rectangle(((CircleDecoration) child).getBounds());
					if (request instanceof SelectionRequest) {
						final Point requestLocation = ((SelectionRequest) request).getLocation();
						if (decoratorBounds.contains(requestLocation)) {
							final IFeatureModel featureModel = feature.getFeatureModel();
							final SetFeatureToMandatoryOperation op = new SetFeatureToMandatoryOperation(feature, featureModel);
							try {
								PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, null, null);
							} catch (ExecutionException e) {
								FMUIPlugin.getDefault().logError(e);
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private void changeConnectionType() {
		if (connectsExternFeatures()) {
			return;
		}

		IFeature feature = getModel().getTarget().getObject();
		IFeatureModel featureModel = feature.getFeatureModel();

		int groupType;

		if (feature.getStructure().isAlternative()) {
			groupType = ChangeFeatureGroupTypeOperation.AND;
		} else if (feature.getStructure().isAnd()) {
			groupType = ChangeFeatureGroupTypeOperation.OR;
		} else {
			groupType = ChangeFeatureGroupTypeOperation.ALTERNATIVE;
		}

		ChangeFeatureGroupTypeOperation op = new ChangeFeatureGroupTypeOperation(groupType, feature, featureModel);

		try {
			PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, null, null);
		} catch (ExecutionException e) {
			FMUIPlugin.getDefault().logError(e);
		}
	}

	@Override
	protected void refreshVisuals() {
		refreshParent();
		refreshSourceDecoration();
		refreshTargetDecoration();
		refreshToolTip();
	}

	public void refreshParent() {
		IGraphicalFeature newModel = getModel().getTarget();
		FeatureEditPart newEditPart = (FeatureEditPart) getViewer().getEditPartRegistry().get(newModel);
		setTarget(newEditPart);
		getFigure().setVisible(getTarget() != null);
		
		if (activeReason != null) {
			getFigure().setForegroundColor(FMPropertyManager.getReasonColor(activeReason));
			getFigure().setLineWidth(FMPropertyManager.getReasonLineWidth(activeReason));
		} else {
			getFigure().setForegroundColor(FMPropertyManager.getConnectionForegroundColor());
			getFigure().setLineWidth(1);
		}
	}

	public void refreshSourceDecoration() {
		IFeature source = getModel().getSource().getObject();
		IFeature sourceParent = getModel().getSource().getObject();
		final IGraphicalFeature graphicalTarget = getModel().getTarget();
		if (graphicalTarget == null) {
			return;
		}
		IFeature target = graphicalTarget.getObject();

		boolean parentHidden = false;

		CircleDecoration sourceDecoration = null;
		while (!sourceParent.getStructure().isRoot()) {
			sourceParent = sourceParent.getStructure().getParent().getFeature();
			if (sourceParent.getStructure().isHidden())
				parentHidden = true;

		}
		if ((target.getStructure().isAnd()) && !(source.getStructure().isHidden() && !FeatureUIHelper.showHiddenFeatures(graphicalTarget.getGraphicalModel()))) {
			if (!(parentHidden && !FeatureUIHelper.showHiddenFeatures(graphicalTarget.getGraphicalModel()))) {
				sourceDecoration = new CircleDecoration(source.getStructure().isMandatory());
			}
		}
		getFigure().setSourceDecoration(sourceDecoration);
		
		if (sourceDecoration != null && getActiveReason() != null) {
			sourceDecoration.setActiveReason(getActiveReason());
		}
	}

	public void refreshTargetDecoration() {
		IGraphicalFeature target = getModel().getTarget();
		if (target == null) {
			return;
		}
		
		RelationDecoration targetDecoration = null;
		final IFeatureStructure structure = target.getObject().getStructure();
		if (structure.getChildrenCount() > 1
				&& !structure.isAnd()
				&& structure.isFirstChild(getModel().getSource().getObject().getStructure())) {
			final List<IGraphicalFeature> graphicalChildren = FeatureUIHelper.getGraphicalChildren(target);
			final IGraphicalFeature lastChild = FeatureUIHelper.hasVerticalLayout(target.getGraphicalModel())
					? graphicalChildren.get(0)
					: graphicalChildren.get(graphicalChildren.size() - 1);
			targetDecoration = new RelationDecoration(structure.isMultiple(), lastChild);
		}
		getFigure().setTargetDecoration(targetDecoration);
		
		if (getActiveReason() != null
				&& getFigure().getTargetDecoration() != null
				&& (getFigure().getTargetDecoration().getActiveReason() == null
				|| getFigure().getTargetDecoration().getActiveReason().getConfidence() < getActiveReason().getConfidence())) { //maximum confidence of all siblings
			getFigure().getTargetDecoration().setActiveReason(getActiveReason());
		}
	}

	public void refreshToolTip() {
		final IGraphicalFeature graphicalTarget = getModel().getTarget();
		if (graphicalTarget == null) {
			return;
		}
		IFeature target = graphicalTarget.getObject();
		toolTipContent.removeAll();
		toolTipContent.setLayoutManager(new GridLayout());
		toolTipContent.add(new Label(" Connection type: \n" + (target.getStructure().isAnd() ? " And" : (target.getStructure().isMultiple() ? " Or" : " Alternative"))));

		// call of the FeatureDiagramExtensions
		for (FeatureDiagramExtension extension : FeatureDiagramExtension.getExtensions()) {
			toolTipContent = extension.extendConnectionToolTip(toolTipContent, this);
		}

		getFigure().setToolTip(toolTipContent);
	}

	@Override
	public void activate() {
		getFigure().setVisible(getTarget() != null);
		super.activate();
	}

	@Override
	public void deactivate() {
		super.deactivate();
		getFigure().setVisible(false);
	}

	public void propertyChange(PropertyChangeEvent event) {
		String prop = event.getPropertyName();
		if (EventType.PARENT_CHANGED.toString().equals(prop)) {
			refreshParent();
		} else if (EventType.MANDATORY_CHANGED.toString().equals(prop)) {
			refreshSourceDecoration();
		}
	}
	
	/**
	 * Returns the currently active reason.
	 * @return the currently active reason
	 */
	public Reason getActiveReason() {
		return activeReason;
	}
	
	/**
	 * Sets the currently active reason.
	 * @param activeReason new active reason
	 */
	public void setActiveReason(Reason activeReason) {
		this.activeReason = activeReason;
	}
	
	/**
	 * Checks if the target and source features are from an external feature
	 * model.
	 * 
	 * @return true if both features are from an external feature model
	 */
	private boolean connectsExternFeatures() {
		FeatureConnection featureConnection = getModel();
		final IFeature source = featureConnection.getSource().getObject();
		final IGraphicalFeature graphicalTarget = featureConnection.getTarget();
		if (graphicalTarget == null) {
			return false;
		}
		final IFeature target = graphicalTarget.getObject();
		return (source instanceof ExtendedFeature && ((ExtendedFeature) source).isFromExtern()
				&& target instanceof ExtendedFeature && ((ExtendedFeature) target).isFromExtern());
	}
}
