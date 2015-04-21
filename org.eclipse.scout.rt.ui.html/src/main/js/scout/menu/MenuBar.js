scout.MenuBar = function($parent, position, orderFunc) {
  this.position = position;
  this.orderFunc = orderFunc;
  this.menuItems = [];
  this.$parent = $parent;

  // Create a menubar container and add it to the parent, but don't show it yet. It will
  // be shown automatically when items are added to the menubar, see updateVisibility().
  this.$container = $.makeDiv('menubar').hide();
  if (this.position === 'top') {
    this.$parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    this.$parent.append(this.$container);
  }
};

scout.MenuBar.prototype.remove = function() {
  this.menuItems.forEach(function(item) {
    item.remove();
  });
  if (this.$container) {
    this.$container.remove();
  }
};

scout.MenuBar.prototype.updateItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);

  // stop if menus are the same as before
  // remove separators before comparison, because orderFunc may add new separators (arrays.equals compares by reference (===))
  if (scout.arrays.equals(this.menuItems.filter(notIsSeparator), menuItems.filter(notIsSeparator))) {
    return;
  }

  // remove existing menu items
  this.menuItems.forEach(function(item) {
    item.remove();
  });

  // The orderFunc may add separators to the list of items, that's why we
  // store the return value of orderFunc in this.menuItems and not the
  // menuItems passed to the updateItems method. We must do this because
  // otherwise we could not remove the added separator later.
  var orderedMenuItems = this.orderFunc(menuItems);
  this.menuItems = orderedMenuItems.left.concat(orderedMenuItems.right);
  this._renderMenuItems(orderedMenuItems.left, false);
  this._renderMenuItems(orderedMenuItems.right, true);

  // Fix for Firefox issue with float:right. In Firefox elements with float:right must
  // come first in the HTML order of elements. Otherwise a strange layout bug occurs.
  this.$container.children('.right-aligned').detach().prependTo(this.$container);

  // The _first_ right-aligned item must have the 'last' class (reverse order because of float:right)
  this.$container.children('.right-aligned').first().addClass('last');

  this.updateVisibility();

  // --- Helper functions ---

  function notIsSeparator(menu) {
    return !menu.separator;
  }
};

scout.MenuBar.prototype.updateVisibility = function() {
  var wasVisible = this.$container.isVisible(),
    visible = !this.hiddenByUi && this.menuItems.length > 0;

  this.$container.setVisible(visible);
  if (this.$container.isVisible() !== wasVisible) {
    var htmlComp = scout.HtmlComponent.optGet(this.$parent);
    if (htmlComp) {
      htmlComp.invalidateTree();
    }
  }
};

scout.MenuBar.prototype._renderMenuItems = function(menuItems, right) {
  // Reverse the list if alignment is right to preserve the visible order specified by the
  // Scout model (in HTML, elements with 'float: right' are displayed in reverse order)
  if (right) {
    menuItems.reverse();
  }
  var tooltipPosition = (this.position === 'top' ? 'bottom' : 'top');
  menuItems.forEach(function(item) {
    item.tooltipPosition = tooltipPosition;
    item.render(this.$container);
    item.$container.removeClass('form-field');
    if (right) {
      item.$container.addClass('right-aligned');
    }
  }.bind(this));
};
