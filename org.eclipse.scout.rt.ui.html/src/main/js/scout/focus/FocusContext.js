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
 * A focus context is associated with a $container, and controls how to focus elements within that $container.
 */
scout.FocusContext = function($container, focusManager) {
  this.$container = $container;
  this.focusManager = focusManager;

  this.lastValidFocusedElement = null; // variable to store the last valid focus position; used to restore focus once being re-activated.
  this.focusedElement = null;
  this.locked = false;

  // Notice: every listener is installed on $container and not on $field level, except 'remove' listener because it does not bubble.
  this._keyDownListener = this._onKeyDown.bind(this);
  this._focusInListener = this._onFocusIn.bind(this);
  this._focusOutListener = this._onFocusOut.bind(this);
  this._unfocusableListener = this._onUnfocusable.bind(this);
  this._removeListener = this._onRemove.bind(this);

  this.$container
    .on('keydown', this._keyDownListener)
    .on('focusin', this._focusInListener)
    .on('focusout', this._focusOutListener)
    .on('hide disable', this._unfocusableListener);
};

scout.FocusContext.prototype.dispose = function() {
  this.$container
    .off('keydown', this._keyDownListener)
    .off('focusin', this._focusInListener)
    .off('focusout', this._focusOutListener)
    .off('hide disable', this._unfocusableListener);
  $(this.focusedElement).off('remove', this._removeListener);
};

/**
 * Method invoked once a 'keydown' event is fired to control proper tab cycle.
 */
scout.FocusContext.prototype._onKeyDown = function(event) {
  if (event.which === scout.keys.TAB) {
    var activeElement = this.$container.activeElement(true),
      $focusableElements = this.$container.find(':tabbable:visible'),
      firstFocusableElement = $focusableElements.first()[0],
      lastFocusableElement = $focusableElements.last()[0],
      activeElementIndex = $focusableElements.index(activeElement),
      focusedElement;

    // Forward Tab
    if (!event.shiftKey) {
      // If the last focusable element is focused, or the focus is on the container, set the focus to the first focusable element
      if (firstFocusableElement && (activeElement === lastFocusableElement || activeElement === this.$container[0])) {
        $.suppressEvent(event);
        this.validateAndSetFocus(firstFocusableElement);
        focusedElement = firstFocusableElement;
      } else if (activeElementIndex < $focusableElements.length - 1) {
        focusedElement = $focusableElements.get(activeElementIndex + 1);
        // Note: event is _not_ suppressed here --> will be handled by browser
      }
    }
    // Backward Tab (Shift+TAB)
    else {
      // If the first focusable element is focused, or the focus is on the container, set the focus to the last focusable element
      if (lastFocusableElement && (activeElement === firstFocusableElement || activeElement === this.$container[0])) {
        $.suppressEvent(event);
        this.validateAndSetFocus(lastFocusableElement);
        focusedElement = lastFocusableElement;
      } else if (activeElementIndex > 0) {
        focusedElement = $focusableElements.get(activeElementIndex - 1);
        // Note: event is _not_ suppressed here --> will be handled by browser
      }
    }
    if (!focusedElement) {
      return;
    }

    // Check if new focused element is currently visible, otherwise scroll the container
    var $focusableElement = $(focusedElement),
      containerBounds = scout.graphics.offsetBounds($focusableElement),
      $scrollable = $focusableElement.scrollParent();
    if (!scout.scrollbars.isLocationInView(new scout.Point(containerBounds.x, containerBounds.y), $scrollable)) {
      scout.scrollbars.scrollTo($scrollable, $focusableElement);
    }
  }
};

/**
 * Method invoked once a 'focusin' event is fired by this context's $container or one of its child controls.
 */
scout.FocusContext.prototype._onFocusIn = function(event) {
  var $target = $(event.target);
  $target.on('remove', this._removeListener);
  this.focusedElement = event.target;

  // Do not update current focus context nor validate focus if target is $entryPoint.
  // That is because focusing the $entryPoint is done whenever no control is currently focusable, e.g. due to glasspanes.
  if (event.target === this.$container.entryPoint(true)) {
    return;
  }

  // Make this context the active context (nothing done if already active) and validate the focus event.
  this.focusManager._pushIfAbsendElseMoveTop(this);
  this.validateAndSetFocus(event.target);
  event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
};

/**
 * Method invoked once a 'focusout' event is fired by this context's $container or one of its child controls.
 */
scout.FocusContext.prototype._onFocusOut = function(event) {
  $(event.target).off('remove', this._removeListener);
  this.focusedElement = null;
  event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event. Otherwise, that 'parent context' would be activated as well.
};

/**
 * Method invoked once a child element of this context's $container is removed.
 */
scout.FocusContext.prototype._onRemove = function(event) {
  // This listener is installed on the focused element only.
  this.validateAndSetFocus(null, scout.filters.notSameFilter(event.target));
  event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
};

/**
 * Function invoked once a child element of this context's $container is hidden or disabled
 * and it cannot have the focus anymore. In that case we need to look for a new focusable
 * element.
 */
scout.FocusContext.prototype._onUnfocusable = function(event) {
  if ($(event.target).isOrHas(this.lastValidFocusedElement)) {
    this.validateAndSetFocus(null, scout.filters.notSameFilter(event.target));
    event.stopPropagation(); // Prevent a possible 'parent' focus context to consume this event.
  }
};

/**
 * Focuses the given element if being a child of this context's container and matches the given filter (if provided).
 *
 * @param element
 *        the element to gain focus, or null to focus the context's first focusable element matching the given filter.
 * @param filter
 *        filter to control which element to gain focus, or null to accept all focusable candidates.
 */
scout.FocusContext.prototype.validateAndSetFocus = function(element, filter) {
  // Ensure the element to be a child element, or set it to null otherwise.
  if (element && !$.contains(this.$container[0], element)) {
    element = null;
  }

  var elementToFocus = null;
  if (!element) {
    elementToFocus = this.focusManager.findFirstFocusableElement(this.$container, filter);
  } else if (!filter || filter.call(element)) {
    elementToFocus = element;
  } else {
    elementToFocus = this.focusManager.findFirstFocusableElement(this.$container, filter);
  }

  // Store the element to be focused, and regardless of whether currently covert by a glass pane or the focus manager is not active. That is for later focus restore.
  this.lastValidFocusedElement = elementToFocus;

  // Focus the element.
  this._focus(elementToFocus);
};

/**
 * Calls {@link #validateAndSetFocus} with {@link #lastValidFocusedElement}.
 */
scout.FocusContext.prototype.validateFocus = function(filter) {
  this.validateAndSetFocus(this.lastValidFocusedElement, filter);
};

/**
 * Restores the focus on the last valid focused element. Does nothing, if there is no last valid focused element.
 */
scout.FocusContext.prototype.restoreFocus = function() {
  if (this.lastValidFocusedElement) {
    this._focus(this.lastValidFocusedElement);
  }
};

/**
 * Controls, whether focus requests are allowed to be executed or not. The requests are not executed while the property is set to true.
 * But: the element of the focused request will be stored as usual in lastValidFocusedElement. So as soon as the lock is removed, a call to restoreFocus would focus that element.
 *
 * This is useful to temporarily disable focus requests without losing the element which should be focused. Typical usage would look like this:
 * <pre>
 * context.setLocked(true);
 * // do some stuff
 * context.setLocked(false);
 * context.restoreFocus();
 * </pre>
 *
 * @param {boolean} locked true, to block focus requests, false to allow them again.
 */
scout.FocusContext.prototype.setLocked = function(locked) {
  this.locked = locked;
};

/**
 * Focuses the requested element.
 */
scout.FocusContext.prototype._focus = function(elementToFocus) {
  // Only focus element if focus manager is active
  if (!this.focusManager.active) {
    return;
  }
  if (this.locked) {
    return;
  }

  // Check whether the element is covert by a glasspane
  if (this.focusManager.isElementCovertByGlassPane(elementToFocus)) {
    var activeElement = this.$container.activeElement(true);
    if (elementToFocus && (!activeElement || !this.focusManager.isElementCovertByGlassPane(activeElement))) {
      // If focus should be removed (blur), don't break here and try to focus the root element
      // Otherwise, if desired element cannot be focused then break and leave the focus where it is, unless the currently focused element is covered by a glass pane
      return false;
    }
    elementToFocus = null;
  }

  // Focus $entryPoint if current focus is to be blured.
  // Otherwise, the HTML body would be focused which makes global keystrokes (like backspace) not to work anymore.
  elementToFocus = elementToFocus || this.$container.entryPoint(true);

  // If element may not be focused (example SVG element in IE) -> use the entryPoint as fallback
  // $elementToFocus.focus() would trigger a focus event even the element won't be focused -> loop
  // In that case the focus function does not exist on the svg element
  if (!elementToFocus.focus) {
    elementToFocus = this.$container.entryPoint(true);
  }

  // Only focus element if different to current focused element
  if (scout.focusUtils.isActiveElement(elementToFocus)) {
    return;
  }

  var $elementToFocus = $(elementToFocus);

  // Focus the requested element
  $elementToFocus.focus();

  $.log.isDebugEnabled() && $.log.debug('Focus set to ' + scout.graphics.debugOutput(elementToFocus));
};
