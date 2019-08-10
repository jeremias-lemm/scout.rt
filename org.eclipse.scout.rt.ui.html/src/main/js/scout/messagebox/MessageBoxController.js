/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/**
 * Controller with functionality to register and render message boxes.
 *
 * The message boxes are put into the list 'messageBoxes' contained in 'displayParent'.
 */
scout.MessageBoxController = function(displayParent, session) {
  this.displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given message box to this controller and renders it.
 */
scout.MessageBoxController.prototype.registerAndRender = function(messageBox) {
  scout.assertProperty(messageBox, 'displayParent');
  this.displayParent.messageBoxes.push(messageBox);
  this._render(messageBox);
};

/**
 * Removes the given message box from this controller and DOM. However, the message box's adapter is not destroyed. That only happens once the message box is closed.
 */
scout.MessageBoxController.prototype.unregisterAndRemove = function(messageBox) {
  if (messageBox) {
    scout.arrays.remove(this.displayParent.messageBoxes, messageBox);
    this._remove(messageBox);
  }
};

/**
 * Removes all message boxes registered with this controller from DOM.
 */
scout.MessageBoxController.prototype.remove = function() {
  this.displayParent.messageBoxes.forEach(this._remove.bind(this));
};

/**
 * Renders all message boxes registered with this controller.
 */
scout.MessageBoxController.prototype.render = function() {
  this.displayParent.messageBoxes.forEach(function(msgBox) {
    msgBox.setDisplayParent(this.displayParent);
    this._render(msgBox);
  }.bind(this));
};

scout.MessageBoxController.prototype._render = function(messageBox) {
  // Use parent's function or (if not implemented) our own.
  if (this.displayParent.acceptView) {
    if (!this.displayParent.acceptView(messageBox)) {
      return;
    }
  } else if (!this.acceptView(messageBox)) {
    return;
  }

  // Prevent "Already rendered" errors --> TODO [7.0] bsh: Remove this hack! Fix it on model if possible. See #162954.
  if (messageBox.rendered) {
    return;
  }
  // Open all message boxes in the center of the desktop, except message-boxes that belong to a popup-window
  // Since the message box doesn't have a DOM element as parent when render is called, we must find the
  // entryPoint by using the model.
  var $mbParent;
  if (this.displayParent instanceof scout.Form && this.displayParent.isPopupWindow()) {
    $mbParent = this.displayParent.popupWindow.$container;
  } else {
    $mbParent = this.session.desktop.$container;
  }
  // start focus tracking if not already started.
  messageBox.setTrackFocus(true);
  messageBox.render($mbParent);

  // Only display the message box if its 'displayParent' is visible to the user.
  if (!this.displayParent.inFront()) {
    messageBox.detach();
  }
};

scout.MessageBoxController.prototype._remove = function(messageBox) {
  messageBox.remove();
};

/**
 * Attaches all message boxes to their original DOM parents.
 * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already attached.
 */
scout.MessageBoxController.prototype.attach = function() {
  this.displayParent.messageBoxes.forEach(function(messageBox) {
    messageBox.attach();
  }, this);
};

/**
 * Detaches all message boxes from their DOM parents. Thereby, modality glassPanes are not detached.
 * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already detached.
 */
scout.MessageBoxController.prototype.detach = function() {
  this.displayParent.messageBoxes.forEach(function(messageBox) {
    messageBox.detach();
  }, this);
};

scout.MessageBoxController.prototype.acceptView = function(view) {
  return this.displayParent.rendered;
};
